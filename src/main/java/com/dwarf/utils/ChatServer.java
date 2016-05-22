package com.dwarf.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastAckCallback;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

public class ChatServer {
	
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);
	
	public static void main(String args[]){
		
		Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9092);
        
        SocketIOServer server = new SocketIOServer(config);
        server.addConnectListener(new ConnectListener(){

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
        
        server.start();
        
		
	}
	
}
