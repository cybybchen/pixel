package com.trans.pixel.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

import com.trans.pixel.controller.chain.PixelRequest;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.service.UserService;

/**
 * WebSocket 消息推送服务类
 * 
 * @author XXX
 * 
 *         2016-7-26 00:00:00
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
	private HttpSession httpSession;
	private int serverId;

	private static final UserService userService = (UserService) ContextLoader
			.getCurrentWebApplicationContext().getBean("userService");

	@OnOpen
	// public void start(@PathParam("relationId") String relationId,
	// @PathParam("userCode") int userCode, Session session, EndpointConfig
	// config) {
	public void start(@PathParam("userId") long userId, Session session) {
		log.debug("" + userId);
		this.session = session;
		UserBean user = userService.getOther(userId);
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return ret;
		}
	}
}