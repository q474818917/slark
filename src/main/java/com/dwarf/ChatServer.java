package com.dwarf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;

public class ChatServer {
	private static Logger logger = LoggerFactory.getLogger(ChatServer.class);
	
	private final static Map<String, String> userMap = new HashMap<>();
	private static SocketIOServer server;
	
	private static Properties prop = new Properties();
	{
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("server.properties");
		try {
			prop.load(in);
			logger.info("server.properties loading succeed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ChatServer(){
		Configuration config = new Configuration();
		String hostName = prop.getProperty("socketio.server");
		String port = prop.getProperty("socketio.port");
		config.setHostname(hostName);
		config.setPort(Integer.parseInt(port));
		logger.info("ChatServer ip is {}, port is {}", hostName, port);
		server = new SocketIOServer(config);
	}
	
	public void start(){
		server.start();
	}
	
	public void stop(){
		server.stop();
	}
	
	public <T> void addEventListener(String eventName, Class<T> eventClass, DataListener<T> listener){
		server.addEventListener(eventName, eventClass, listener);
	}
	
	public void addConnectionListener(){
		
	}
	
	public void addDisConnectionListener(){
		
	}
	
	public static void main(String args[]){
		ChatServer server = new ChatServer();
		server.start();
	}
	
}
