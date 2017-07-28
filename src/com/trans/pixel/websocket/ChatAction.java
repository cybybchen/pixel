package com.trans.pixel.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.ContextLoader;

import com.trans.pixel.controller.chain.PixelRequest;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.service.UserService;

/**
 * WebSocket 消息推送服务类
 * 
 */
@ServerEndpoint(value = "/websocket/chat/{userId}")
// @ServerEndpoint(value = "/websocket/chat/{relationId}/{userCode}",
// configurator = GetHttpSessionConfigurator.class)
public class ChatAction {

	private static final Map<Integer, StringBuffer> msgBuffer = new HashMap<Integer, StringBuffer>();

	// private static Logger log = Logger.getLogger(ChatAction.class);
	private static final Logger log = LoggerFactory.getLogger(ChatAction.class);

	// connections就是ClientSet
	private static final Map<String, Object> connections = new HashMap<String, Object>();

	private static final CopyOnWriteArraySet<ChatAction> onlineUsers = new CopyOnWriteArraySet<ChatAction>();
	private String nickname;
	private Session session;
//	private HttpSession httpSession;
	private int serverId;

	private static final UserService userService = (UserService) ContextLoader
			.getCurrentWebApplicationContext().getBean("userService");
//	@Resource(name = "redisTemplate")
	@SuppressWarnings("unchecked")
	private RedisTemplate<String, String> redis = (RedisTemplate<String, String>) ContextLoader
			.getCurrentWebApplicationContext().getBean("redisTemplate");
//	@Resource(name = "redisTemplate1")
	@SuppressWarnings("unchecked")
	private RedisTemplate<String, String> redis1 = (RedisTemplate<String, String>) ContextLoader
			.getCurrentWebApplicationContext().getBean("redisTemplate1");

	@OnOpen
	// public void start(@PathParam("relationId") String relationId,
	// @PathParam("userCode") int userCode, Session session, EndpointConfig
	// config) {
	public void start(@PathParam("userId") long userId, Session session) {
		log.debug("" + userId);
		
		this.session = session;
		UserBean user = userService.getUserOther(userId);
		if (user != null) {
			this.nickname = user.getUserName();
			this.serverId = user.getServerId();
		}

		log.debug("start websocket");
		// this.httpSession = (HttpSession) config.getUserProperties()
		// .get(HttpSession.class.getName());
		// this.nickname = (String) httpSession.getAttribute("loginOperatorId");
		onlineUsers.add(this);
		sendHistory(this, serverId);
		String message = String.format("%s %s", nickname,
				" :from websocket 上线了...");
		broadcast(message, serverId);
		
		long i = 0;
		while (true) {
			try {
				log.debug("i is:" + i);
				set("haha", "" + i, i);
				++i;
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void set(final String key, final String value, final long i) {
    	getRedis(i).execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection arg0)
					throws DataAccessException {
				BoundValueOperations<String, String> Ops = getRedis(i)
						.boundValueOps(key);
				
				Ops.set(value);
				return null;
			}
		});
    }
	
	private RedisTemplate<String, String> getRedis(long i) {
		if (i % 2 == 0)
			return redis;
		
		return redis1;
	}
	
	@OnClose
	public void end(@PathParam("userId") long userId, Session session) {
		onlineUsers.remove(this);
		String message = String.format("%s %s", nickname,
				" :from websocket 已经离开...");
		broadcast(message, serverId);
	}

	@OnMessage
	// public void incoming(@PathParam("relationId") String relationId,
	// @PathParam("userCode") int userCode, String message, EndpointConfig
	// config) {
	public void incoming(@PathParam("userId") long userId, String message) {
		// Never trust the client
		log.debug("send websocket message");
		String filteredMessage = String.format("%s: %s", nickname,
				HTMLFilter.filter(message.toString()));
		// 放入缓存
		StringBuffer sb = msgBuffer.get(serverId);
		if (sb == null) {
			sb = new StringBuffer();
		}
		sb.put(filteredMessage);
		msgBuffer.put(serverId, sb);
		
		broadcast(filteredMessage, serverId);
	}

	@OnMessage
	public void incoming(Session session, ByteBuffer msg) {
		byte[] reqData = msg.array();
		httpcmd(reqData);
		System.out
				.println("Binary message: "
						+ msg.toString()
						+ " ,compareTo(ByteBuffer.wrap(new byte[]{1, 9, 2, 0, 1, 5, 1, 6}))="
						+ msg.compareTo(ByteBuffer.wrap(new byte[] { 1, 9, 2,
								0, 1, 5, 1, 6 })));
	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		log.error("Chat Error: " + t.toString(), t);
	}

	private static void broadcast(String msg, int serverId) {
		for (ChatAction client : onlineUsers) {
			try {
				synchronized (client) {
					if (client.serverId == serverId)
						client.session.getBasicRemote().sendText(msg);
				}
			} catch (IOException e) {
				log.debug("错误: 消息发送失败!", e);
				onlineUsers.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {
					// Ignore
				}
				String message = String.format("* %s %s", client.nickname,
						" from websocket 已经离开...");
				broadcast(message, serverId);
			}
		}
	}

	public static void sendHistory(ChatAction newClient, int serverId) {
//		ChatAction newClient = null;
		try {
//			newClient = (ChatAction) connections.get(nick);
			synchronized (newClient) {
				// 发送消息
//				newClient.session.getBasicRemote().sendText(
//						"<---------history--------->");
				StringBuffer sb = msgBuffer.get(newClient.serverId);
				if (sb != null) {
					for (String s : sb.getBuffer()) {
						newClient.session.getBasicRemote().sendText(s);
					}
				}
//				newClient.session.getBasicRemote().sendText(
//						"<---------history--------->");
			}
		} catch (IOException e) {
			log.error("Chat Error: Failed to send message to client", e);
			onlineUsers.remove(newClient);
			try {
				newClient.session.close();
			} catch (IOException e1) {
				// Ignore
			}
			String message = String.format("* %s %s", newClient.nickname,
					"has been disconnected.");
			broadcast(message, serverId);
		}

	}
	
	@SuppressWarnings("finally")
	private PixelRequest httpcmd(byte[] bytes) {
		InputStream in = new ByteArrayInputStream(bytes);
		RequestCommand requestCommand;
		PixelRequest ret = new PixelRequest();
		try {
			requestCommand = RequestCommand.parseFrom(in);
			log.debug("PROCESSING request={}, bytes={}", requestCommand,
					requestCommand.getSerializedSize());

			ret.command = requestCommand;
			ret.user = null;
			return ret;
		} catch (IOException e) {

		} finally {
			try {
				in.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			return ret;
		}
	}
}