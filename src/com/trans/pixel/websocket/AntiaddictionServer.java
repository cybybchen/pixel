package com.trans.pixel.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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

import com.trans.pixel.controller.chain.PixelRequest;
import com.trans.pixel.protoc.Request.RequestCommand;

/**
 * WebSocket 消息推送服务类
 * 
 * @author XXX
 * 
 *         2016-7-26 00:00:00
 */
@ServerEndpoint(value = "/websocket/anti/{userId}")
// @ServerEndpoint(value = "/websocket/chat/{relationId}/{userCode}",
// configurator = GetHttpSessionConfigurator.class)
public class AntiaddictionServer {

	private static long onlineTime = 0;
	
	private static long loginTime = 0;
	
	private static long logoutTime = 0;

	private static final Logger log = LoggerFactory.getLogger(AntiaddictionServer.class);
	
	private static final CopyOnWriteArraySet<AntiaddictionServer> onlineUsers = new CopyOnWriteArraySet<AntiaddictionServer>();

	private Session session;

//	private static final UserService userService = (UserService) ContextLoader
//			.getCurrentWebApplicationContext().getBean("userService");

	@OnOpen
	public void start(@PathParam("userId") long userId, Session session) {
//		log.debug("" + userId);
		this.session = session;
//		UserBean user = userService.getUser(userId);
//		if (user != null) {
			
//		}
		loginTime = System.currentTimeMillis();
//		log.debug("start websocket");
		onlineUsers.add(this);
		log.debug("user count:" + onlineUsers.size());
	}

	@OnClose
	public void end(@PathParam("userId") long userId, Session session) {
		onlineUsers.remove(this);
		logoutTime = System.currentTimeMillis();
		onlineTime = logoutTime - loginTime;
	}

	@OnMessage
	public void incoming(@PathParam("userId") long userId, String message) {
		// Never trust the client
		log.debug("send websocket message");
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