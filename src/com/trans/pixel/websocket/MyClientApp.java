package com.trans.pixel.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.log4j.Logger;

import com.trans.pixel.protoc.UserInfoProto.HeadInfo;

public class MyClientApp {

	private static Logger log = Logger.getLogger(MyClientApp.class);
	
	public Session session;

	protected boolean start() {

		WebSocketContainer container = ContainerProvider
				.getWebSocketContainer();

//		String uri = "ws://localhost:8080/pixel/websocket/anti/12345";
		String uri = "ws://123.207.88.112:8082/Lol450/websocket/anti/12345";
//		log.debug("Connecting to" + uri);
		try {
			session = container
					.connectToServer(MyClient.class, URI.create(uri));
			
		} catch (DeploymentException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static void main(String args[]) {
		for (int i = 1; i< 50000; i++) {
			MyClientApp wSocketTest = new MyClientApp();
			if (!wSocketTest.start()) {
//				System.out.println("测试结束！");
				break;
			}
		}

//		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//		String input = "";
//		try {
//			do {
//				input = br.readLine();
//				if (!input.equals("exit")) {
////					client.session.getBasicRemote().sendText(input);
//					RequestCommand.Builder builder = RequestCommand.newBuilder();
//					builder.setHead(head());
//					RequestAchieveRewardCommand.Builder b = RequestAchieveRewardCommand.newBuilder();
//					b.setId(101);
//					builder.setAchieveRewardCommand(b.build());
//					RequestCommand reqcmd = builder.build();
//					byte[] reqData = reqcmd.toByteArray();
//					
//				   client.session.getBasicRemote().sendBinary(ByteBuffer.wrap(reqData));
//				
//				}
//
//			} while (!input.equals("exit"));
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	protected static HeadInfo head() {
    	return head("cyb", 1);
    }

    protected static HeadInfo head(String account, int serverId) {
        HeadInfo.Builder builder = HeadInfo.newBuilder();
        builder.setGameVersion(1);
        builder.setAccount(account);
        builder.setServerId(serverId);
        builder.setUserId(111);
        builder.setVersion(1);
        builder.setSession("cyb");
        builder.setDatetime(new Date().getTime());
        return builder.build();
    }
}
