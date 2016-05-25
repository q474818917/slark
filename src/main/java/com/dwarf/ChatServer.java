package com.dwarf;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.dwarf.common.mongo.MongoStore;
import com.dwarf.utils.SerializationUtils;
import com.dwarf.zk.ZkClient;
import com.google.common.collect.Maps;

/**
 * 
 * namespace、user在connect时，对于同一个sessionID不知道zk如何处理
 * znode保存用户的UUID，数据就是用户的ID
 * @author jiyu
 *
 */
public class ChatServer implements SocketIOServerOperations {
	
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);
	
	private final static String PREFIX = "/slark";
	private final SocketIOServer server;
	private final ZkClient zkclient;
	private final MongoStore mongoStore;
	
	private static Map<String, UUID> userMap = Maps.newConcurrentMap();
	
	public ChatServer(){
		Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        server = new SocketIOServer(config);
        
        zkclient = new ZkClient("120.25.163.237:2181");
        mongoStore = new MongoStore("chat");
	}
	
	public void init(){
		this.handleNamespaces();
		this.handleUserchat();
		this.handleZkListener();
	}
	
	/**
	 * 群聊事件：message
	 */
	public void handleNamespaces(){
		List<Document> docList = mongoStore.findAll("chat_room");
		for(Document document : docList){
			String name = "/" + document.get("id");
			SocketIONamespace namespace = server.addNamespace(name);
			
			namespace.addDisconnectListener(new DisconnectListener(){

				@Override
				public void onDisconnect(SocketIOClient client) {
					client.disconnect();
					zkclient.close();
				}
				
			});
			
			namespace.addEventListener("message", ChatObject.class, new DataListener<ChatObject>(){

				@Override
				public void onData(SocketIOClient client, ChatObject data, AckRequest ackSender) throws Exception {
					namespace.getBroadcastOperations().sendEvent("broadcast", data);
				}
				
			});
			
			namespace.addEventListener("online", UserObject.class, new DataListener<UserObject>(){

				@Override
				public void onData(SocketIOClient client, UserObject data, AckRequest ackSender) throws Exception {
					data.setUuid(client.getSessionId());
					zkclient.createEphemeral(PREFIX + "/" + data.getId(), SerializationUtils.serialize(data));
				}
				
			});
		}
	}
	
	public void start(){
		server.start();
	}
	
	@Override
	public void handleUserchat() {
		server.addEventListener("message", ChatObject.class, new DataListener<ChatObject>(){

			@Override
			public void onData(SocketIOClient client, ChatObject data, AckRequest ackSender) throws Exception {
				
			}
			
		});
		
		server.addDisconnectListener(new DisconnectListener(){

			@Override
			public void onDisconnect(SocketIOClient client) {
				client.disconnect();
				zkclient.close();
			}
			
		});
		
		server.addEventListener("online", UserObject.class, new DataListener<UserObject>(){

			@Override
			public void onData(SocketIOClient client, UserObject data, AckRequest ackSender) throws Exception {
				data.setUuid(client.getSessionId());
				zkclient.createEphemeral(PREFIX + "/" + data.getId(), SerializationUtils.serialize(data));
			}
			
		});
	}
	
	public void handleZkListener(){
		PathChildrenCacheListener userlistener = new PathChildrenCacheListener(){

			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				if(event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED){
					System.out.println("add event");
				}
				if(event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
					System.out.println("add event");
				}
			}
			
		};
		zkclient.addListener(PREFIX, userlistener);
		try {
			Thread.sleep(Integer.MAX_VALUE);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		zkclient.close();
	}
	
	public static void main(String args[]){
		ChatServer server = new ChatServer();
		server.init();
		server.start();
	}
	
}
