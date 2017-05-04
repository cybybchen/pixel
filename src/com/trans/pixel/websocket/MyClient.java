package com.trans.pixel.websocket;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.EndpointConfig;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

@ClientEndpoint
public class MyClient {
	
	private String deviceId;

	private Session session;

	public MyClient () {
	}

	public MyClient (String deviceId) {
		this.deviceId = deviceId;
	}
	
	protected boolean start() {
		WebSocketContainer Container = ContainerProvider
				.getWebSocketContainer();
		String uri = "ws://localhost:8080/pixel/websocket/anti/" + deviceId;
		// System.out.println("Connecting to " + uri);
		try {
			session = Container
					.connectToServer(MyClient.class, URI.create(uri));
			// System.out.println("count: " + deviceId);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		for (int i = 1; i< 50000; i++) {
			MyClient wSocketTest = new MyClient(String.valueOf(i));
			if (!wSocketTest.start()) {
//				System.out.println("测试结束！");
				break;
			}
		}
	}
	
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		System.out.println("Connected to endpoint:" + session.getBasicRemote());
		try {
			session.getBasicRemote().sendText("Hello");
		} catch (IOException ex) {
		}
	}

	@OnMessage
	public void onMessage(String message) {
		System.out.println(message);
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}
}