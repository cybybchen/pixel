package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.RechargeProto.RequestBindAccountCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.UserInfoProto.HeadInfo;
import com.trans.pixel.protoc.UserInfoProto.RequestRegisterCommand;

public class RegisterTest extends BaseTest {
	private static Logger logger = Logger.getLogger(RegisterTest.class);
	
    protected HeadInfo head() {
		if (url == null) {
			url = headurl();
			System.out.println("test server:" + url);
		}
//		ACCOUNT = "RegisterTest"+System.currentTimeMillis();
		USER_NAME = ACCOUNT;
        HeadInfo.Builder head = HeadInfo.newBuilder();
        head.setGameVersion(GAME_VERSION);
        head.setAccount(ACCOUNT);
        head.setServerId(SERVER_ID);
        head.setUserId(USER_ID);
        head.setVersion(VERSION);
        head.setSession(SESSION);
        head.setDatetime((new Date()).getTime());
        return head.build();
    }
    

	@Test
	public void test() {
		login();
		bindAccountTest();
	}
    
//	@Test
	public void registerTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestRegisterCommand.Builder b = RequestRegisterCommand.newBuilder();
		b.setUserName(USER_NAME);
		builder.setRegisterCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
//	@Test
	public void bindAccountTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestBindAccountCommand.Builder b = RequestBindAccountCommand.newBuilder();
		b.setNewAccount("haha");
		b.setOldAccount("test");
		builder.setBindAccountCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
