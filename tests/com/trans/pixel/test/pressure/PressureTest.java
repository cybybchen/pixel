package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.test.BaseTest;

public class PressureTest extends BaseTest {
	private static Logger logger = Logger.getLogger(PressureTest.class);
	
	public static final String RANDOM_CODE = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
	
	public static void main(String[] args) {
		while (true) {
//			sleep(1000);
			new Thread() {
				public void run() {
					PressureTest test = new PressureTest();
					test.randomCommand();
				}
			}.start();
		}
	}
	
	private static void sleep(int sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void randomCommand() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		String account = randomAccount();
		builder.setHead(buildHead(account, 0));
		ResponseCommand response = login(builder);
		int randomNum = RandomUtils.nextInt(10);
		switch (randomNum) {
			case 0:
				levelTest(builder, response);
				break;
				
			default:
				break;
		}
	}
	
	private void levelTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		LevelTest levelTest = new LevelTest();
		levelTest.levelPauseTest(builder, loginResponse);
		levelTest.levellootStartTest(builder, loginResponse);
		levelTest.levelPauseTest(builder, loginResponse);
		levelTest.levelStartTest(builder, loginResponse);
		levelTest.levelResutlTest(builder, loginResponse);
		levelTest.levellootResultTest(builder, loginResponse);
	}
	
	private void LotteryTest() {
		
	}
	
	private String randomAccount() {
		int ckdCodeLength = RANDOM_CODE.length();
		String str = "";
		Random rand = new Random();
		while (str.length() < 8) {
			int randNum = rand.nextInt(ckdCodeLength);
			str += RANDOM_CODE.charAt(randNum);
		}
		
		return str;
	}
	
	private HeadInfo buildHead(String account, int userId) {
        HeadInfo.Builder head = HeadInfo.newBuilder();
        head.setGameVersion(GAME_VERSION);
        head.setAccount(account);
        head.setServerId(SERVER_ID);
        head.setUserId(userId);
        head.setVersion(VERSION);
        head.setSession(SESSION);
        head.setDatetime((new Date()).getTime());
        return head.build();
    }
	
	private ResponseCommand login(RequestCommand.Builder builder) {
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
    		builder.clearLoginCommand().setRegisterCommand(registerbuilder.build());
    		
    		reqcmd = builder.build();
    		reqData = reqcmd.toByteArray();
            input = new ByteArrayInputStream(reqData);
            response = http.post(url, input);
        }
        if(response.hasUserInfoCommand()){
        	user = response.getUserInfoCommand().getUser();
        }
        
        return response;
	}
}
