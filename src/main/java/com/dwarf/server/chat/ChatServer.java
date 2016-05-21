package com.dwarf.server.chat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

/**
 * 单聊、群聊
 * 上线、下线
 * 
 * @author jiyu
 *
 */
public class ChatServer {
	// 此处存储ClientID和用户信息映射管理，用于显示相关用户信息（例如：头像、用户名称等）
	private final Map<UUID, ChatUser> userInfoMap = new ConcurrentHashMap<UUID, ChatUser>();
	private final Map<String, UUID> openIdMap = new ConcurrentHashMap<String, UUID>();
	private final UserList userList = new UserList();

	SocketIOServer server;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(AckChatLauncher.class);

	public ChatServer() {
		userList.setUserList(Collections
				.synchronizedList(new ArrayList<ChatUser>()));
	}

	private void initialServer() {
		Configuration config = new Configuration();
		// TODO: 这里可以启用properties 配置文件，不要写死
		config.setHostname("localhost");
		config.setPort(9092);
		config.setUpgradeTimeout(1024 * 1024);
		config.setPingTimeout(1024 * 1024);
		config.setMaxFramePayloadLength(1024 * 1024);
		server = new SocketIOServer(config);
	}

	public void startServer() throws InterruptedException {
		initialServer();
		addEventListener();
		server.start();
	}

	private void addEventListener() {
		connectEvent();

		registerEvent();

		disconnectEvent();
		// 聊天
		sayEvent();

	}

	private void sayEvent() {
		server.addEventListener("say", ChatObject.class,
				new DataListener<ChatObject>() {
					@Override
					public void onData(final SocketIOClient client,
							ChatObject data, final AckRequest ackRequest) {
						if (data.getTargetUser() == null
								|| data.getTargetUser().length() == 0) { // for//
																			// all
							server.getBroadcastOperations().sendEvent("say",
									userInfoMap.get(client.getSessionId()),
									data);
							
						} else {
							server.getClient(
									UUID.fromString(data.getTargetUser()))
									.sendEvent(
											"say",
											userInfoMap.get(client
													.getSessionId()), data);

							client.sendEvent("say",
									userInfoMap.get(client.getSessionId()),
									data);
						}
					}
				});
	}

	private void disconnectEvent() {
		// 下线
		server.addDisconnectListener(new DisconnectListener() {
			@Override
			public void onDisconnect(SocketIOClient client) {
				LOGGER.info("User offline..." + client.getSessionId());
				
				ChatUser user =userInfoMap.get(client.getSessionId());
				userInfoMap.remove(client.getSessionId());
				userList.getUserList().remove(user);
				openIdMap.remove(user.getOpenId());
				
				
				server.getBroadcastOperations().sendEvent("offline",
						userInfoMap.get(client.getSessionId()));
				server.getBroadcastOperations().sendEvent("list",
						userList);
			}
		});
	}

	private void registerEvent() {
		// 上线
		server.addEventListener("register", ChatUser.class,
				new DataListener<ChatUser>() {
					@Override
					public void onData(final SocketIOClient client,
							ChatUser data, final AckRequest ackRequest) {

						LOGGER.info("User login..." + data.getUserName());
						data.setUuid(client.getSessionId().toString());
						//
						if (!openIdMap.containsKey(data.getOpenId())) {
							openIdMap.put(data.getOpenId(),
									client.getSessionId());
							userList.getUserList().add(data);
							userInfoMap.put(client.getSessionId(), data);
							server.getBroadcastOperations().sendEvent("online",
									data);
						} else {
							UUID uuid = openIdMap.get(data.getOpenId());
							userInfoMap.remove(uuid);
							userInfoMap.put(client.getSessionId(), data);
						}
						server.getBroadcastOperations().sendEvent("list",
								userList);
					}
				});
	}

	private void connectEvent() {
		server.addConnectListener(new ConnectListener() {
			@Override
			public void onConnect(SocketIOClient client) {
				LOGGER.info("New user login..." + client.getSessionId());
				// 推送到所有客户端，更新在线列表
			}
		});
	}
}
