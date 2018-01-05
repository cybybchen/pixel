package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestLoginCommand;
import com.trans.pixel.protoc.UserInfoProto.RequestRegisterCommand;

public class LoginTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LoginTest.class);
	
	@Test
	public void testLogin() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
//		builder.getHeadBuilder().setAccount(randomAccount());
		RequestLoginCommand.Builder b = RequestLoginCommand.newBuilder();
		builder.setLoginCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        if(response.hasErrorCommand() && response.getErrorCommand().getCode().equals("1000")){
        	logger.warn(response.getErrorCommand().getMessage()+"，重新注册");
    		RequestRegisterCommand.Builder registerbuilder = RequestRegisterCommand.newBuilder();
    		registerbuilder.setUserName(head().getAccount());
    		registerbuilder.setHeroId(42);
    		builder.clearLoginCommand().setRegisterCommand(registerbuilder.build());
    		try {
    			Thread.sleep(5000);
    		} catch (InterruptedException e) {
    		}
    		reqcmd = builder.build();
    		reqData = reqcmd.toByteArray();
            input = new ByteArrayInputStream(reqData);
            response = http.post(url, input);
        }
        if(response.hasUserInfoCommand()){
        	user = response.getUserInfoCommand().getUser();
        	logger.warn(response.getUserInfoCommand().getUser().getName()+"登陆成功");
        }else if(response.hasErrorCommand()){
        	logger.error(response.getErrorCommand().getMessage());
        }else{
        	logger.error("登陆错误");
        }
	}
}
