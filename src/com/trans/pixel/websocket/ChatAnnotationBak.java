package com.trans.pixel.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.apache.log4j.Logger;

/**
 * WebSocket 消息推送服务类
 * 
 * @author XXX
 * 
 *         2016-7-26 00:00:00
 */
//@ServerEndpoint(value = "/websocket/chat")
public class ChatAnnotationBak {

	private static final StringBuffer msgBuffer = new StringBuffer();

	private static Logger log = Logger.getLogger(ChatAnnotationBak.class);

	private static final String GUEST_PREFIX = "Guest";
	private static final AtomicInteger connectionIds = new AtomicInteger(0);
	// connections就是ClientSet
	private static final Map<String, Object> connections = new HashMap<String, Object>();

	private final String nickname;
	private Session session;

	public ChatAnnotationBak() {
		nickname = GUEST_PREFIX + connectionIds.getAndIncrement();
	}

	//
	public String getNickname() {
		return nickname;
	}

	// 当客户端连接进来时自动激发该方法
	@OnOpen
	public void start(Session session) {
		log.debug("start");
		this.session = session;
		// 将WebSocket客户端会话添加到集合中
		connections.put(nickname, this);
		String message = String.format("* %s %s", nickname, "has joined.");

		sendHistory(nickname);
		// 发送消息
		broadcast(message);
	}

	@OnClose
	public void end() {
		connections.remove(this);
		String message = String
				.format("* %s %s", nickname, "has disconnected.");
		// 发送消息
		broadcast(message);
	}

	/**
	 * 消息发送触发方法
	 * 
	 * @param message
	 */
	@OnMessage
	public void incoming(String message) {
		// Never trust the client
		String filteredMessage = String.format("%s: %s", nickname,
				HTMLFilter.filter(message.toString()));
		// 放入缓存
		msgBuffer.put(filteredMessage);
		// 发送消息
		broadcast(filteredMessage);
	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		log.error("Chat Error: " + t.toString(), t);
	}

	/**
	 * 消息发送方法
	 * 
	 * @param msg
	 */
	private static void broadcast(String msg) {
		// msgBuffer.put(msg);
		if (msg.indexOf("Guest0") != -1) { // String.indexOf()返回第一次出现的指定子字符串在此字符串中的索引
			sendUser(msg);
		} else {
			sendAll(msg);
		}
	}

	/**
	 * 向所有用户发送
	 * 
	 * @param msg
	 */
	public static void sendAll(String msg) {
		for (String key : connections.keySet()) {
			ChatAnnotationBak client = null;
			try {
				client = (ChatAnnotationBak) connections.get(key);
				synchronized (client) {
					// 发送消息
					client.session.getBasicRemote().sendText(msg);
				}
			} catch (IOException e) {
				log.debug("Chat Error: Failed to send message to client", e);
				connections.remove(client);
				try {
					client.session.close();
				} catch (IOException e1) {
					// Ignore
				}
				String message = String.format("* %s %s", client.nickname,
						"has been disconnected.");
				broadcast(message);
			}
		}
	}

	/**
	 * 向指定用户发送消息
	 * 
	 * @param msg
	 */
	public static void sendUser(String msg) {
		ChatAnnotationBak c = (ChatAnnotationBak) connections.get("Guest0");
		try {
			c.session.getBasicRemote().sendText(msg);
			// for(String s : msgBuffer.getBuffer()) {
			// c.session.getBasicRemote().sendText(s);
			// }
		} catch (IOException e) {
			log.error("Chat Error: Failed to send message to client", e);
			connections.remove(c);
			try {
				c.session.close();
			} catch (IOException e1) {
				// Ignore
			}
			String message = String.format("* %s %s", c.nickname,
					"has been disconnected.");
			broadcast(message);
		}
	}

	/**
	 * 向新连进来的发送历史消息
	 * 
	 * @param msg
	 */
	public static void sendHistory(String nick) {
		ChatAnnotationBak newClient = null;
		try {
			newClient = (ChatAnnotationBak) connections.get(nick);
			synchronized (newClient) {
				// 发送消息
				newClient.session.getBasicRemote().sendText(
						"<---------history--------->");
				for (String s : msgBuffer.getBuffer()) {
					newClient.session.getBasicRemote().sendText(s);
				}
				newClient.session.getBasicRemote().sendText(
						"<---------history--------->");
			}
		} catch (IOException e) {
			log.error("Chat Error: Failed to send message to client", e);
			connections.remove(newClient);
			try {
				newClient.session.close();
			} catch (IOException e1) {
				// Ignore
			}
			String message = String.format("* %s %s", newClient.nickname,
					"has been disconnected.");
			broadcast(message);
		}

	}
}