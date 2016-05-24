package com.dwarf;

import java.util.List;
import java.util.UUID;

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
import com.dwarf.zk.ZkClient;

public class ChatServer implements SocketIOServerOperations {
	
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);
	private final SocketIOServer server;
	private final ZkClient zkclient;
	private final MongoStore mongoStore;
	
	private UUID sessionID;
	
	public ChatServer(){
		Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        server = new SocketIOServer(config);
        
        zkclient = new ZkClient("120.25.163.237:2181");
        mongoStore = new MongoStore("chat");
	}
	
	/**
	 * 群聊事件：message
	 */
	public void handleNamespaces(){
		List<Document> docList = mongoStore.findAll("chat_room");
		for(Document document : docList){
			String prefix = "/" + document.get("id");
			SocketIONamespace namespace = server.addNamespace(prefix);
			
			namespace.addConnectListener(new ConnectListener(){

				@Override
				public void onConnect(SocketIOClient client) {
					sessionID = client.getSessionId();
					zkclient.createEphemeral(prefix + "/" + sessionID, null);
				}
				
			});
			
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
		}
	}
	
	public void start(){
		server.start();
	}
	
	public static void main(String args[]){
		
		
        /*server.addConnectListener(new ConnectListener(){

			@Override
			public void onConnect(SocketIOClient client) {
				logger.info("client UUID has connect successful !!!, and the uuid is {}", client.getSessionId() );
			}
        	
        });
        
        server.addDisconnectListener(new DisconnectListener(){

			@Override
			public void onDisconnect(SocketIOClient client) {
				logger.info("client UUID has disconnect successful !!! and the uuid is {}", client.getSessionId());
			}
        	
        });
        
        server.addEventListener("chatEvent", ChatObject.class, new DataListener<ChatObject>(){

			@Override
			public void onData(SocketIOClient client, ChatObject data,
					AckRequest ackSender) throws Exception {
				//判别client是否带有ackRequest
				if(ackSender.isAckRequested()){
					ackSender.sendAckData("message was delivered to server!!");
				}
				server.getBroadcastOperations().sendEvent("broadcast", data, new BroadcastAckCallback<String>(String.class));
				
			}
        	
        });
        
        server.start();*/
        
		
	}

	@Override
	public void handleUserchat() {
		
	}
	
}
