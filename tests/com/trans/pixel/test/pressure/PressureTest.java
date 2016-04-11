package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import org.apache.log4j.Logger;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.protoc.Commands.HeadInfo;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLoginCommand;
import com.trans.pixel.protoc.Commands.RequestRegisterCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserLevel;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.test.BaseTest;

public class PressureTest extends BaseTest {
	public static final String RANDOM_CODE = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
	
	public static void main(String[] args) {
//		while (true) {
//			sleep(1000);
			new Thread() {
				public void run() {
					PressureTest test = new PressureTest();
					test.randomCommand();
				}
			}.start();
//		}
	}
	
	private static void sleep(int sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void randomCommand() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		String account = randomAccount();
		builder.setHead(buildHead(account, 0));
		ResponseCommand response = login(builder);//登录
		builder.setHead(response.getHead());
				
		levelTest(builder, response);//挂机
		
		lotteryTest(builder, response);//抽奖
		
		long teamId = teamTest(builder, response);//更新队伍
		
		ladderTest(builder, response, teamId);//天梯
		
		messageBoardTest(builder, response);//留言板
		
		response = login(builder);
		
		heroTest(builder, response);//英雄
		
		packageTest(builder, response);//道具
		
		equipTest(builder, response);//装备
	}
	
	private void levelTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		LevelTest levelTest = new LevelTest();
		
		levelTest.levelPauseTest(builder, loginResponse);
		
		UserLevel userLevel = levelTest.levelPauseTest(builder, loginResponse);
		sleep(userLevel.getPrepareTime() * 1000);
		
		levelTest.levelStartTest(builder, loginResponse);
		
		levelTest.levelResutlTest(builder, loginResponse);
		
		levelTest.levellootStartTest(builder, loginResponse);
		
		levelTest.levellootResultTest(builder, loginResponse);
	}
	
	private void lotteryTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		LotteryTest lottery = new LotteryTest();
		lottery.lotteryTest(builder, loginResponse, RewardConst.COIN, 1);
		lottery.lotteryTest(builder, loginResponse, RewardConst.COIN, 10);
		lottery.lotteryTest(builder, loginResponse, RewardConst.JEWEL, 1);
		lottery.lotteryTest(builder, loginResponse, RewardConst.JEWEL, 1);
	}
	
	private long teamTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		TeamTest teamTest = new TeamTest();
		long teamId = teamTest.teamAddTest(builder, loginResponse);
		
		teamTest.teamUpdateTest(builder, loginResponse, teamId);
		
		return teamId;
	}
	
	private void ladderTest(RequestCommand.Builder builder, ResponseCommand loginResponse, long teamId) {
		LadderTest ladderTest = new LadderTest();
		UserRank userRank = ladderTest.getUserLadder(builder, loginResponse);
		
		TeamTest teamTest = new TeamTest();
		teamTest.gettTeamCacheTest(builder, loginResponse, userRank.getUserId());
		
		ladderTest.attackLadder(builder, loginResponse, userRank.getRank(), teamId);
	}
	
	private void messageBoardTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		MessageTest messageTest = new MessageTest();
		messageTest.testGetMessageList(builder, loginResponse);
		
		messageTest.testCreateMessage(builder, loginResponse);
	}
	
	private void heroTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		HeroTest heroTest = new HeroTest();
		heroTest.testHeroLevelUpTest(builder, loginResponse, 1);
		
		heroTest.testHeroLevelUpTest(builder, loginResponse, 3);
		
		heroTest.testFenjieHeroTest(builder, loginResponse);
	}
	
	private void packageTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		PackageTest packageTest = new PackageTest();
		packageTest.testPackage(builder, loginResponse);
	}
	
	private void equipTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		EquipTest equipTest = new EquipTest();
		equipTest.testFenjieEquip(builder, loginResponse);
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
	
	private HeadInfo buildHead(String account, long userId) {
		if (url == null) {
			url = headurl();
			System.out.println("test server:" + url);
		}
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
	
	private ResponseCommand login(RequestCommand.Builder req) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestLoginCommand.Builder b = RequestLoginCommand.newBuilder();
		builder.setLoginCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        if(response.hasErrorCommand() && response.getErrorCommand().getCode().equals("1000")){
        	System.out.println(response.getErrorCommand().getMessage()+"，重新注册");
    		RequestRegisterCommand.Builder registerbuilder = RequestRegisterCommand.newBuilder();
    		registerbuilder.setUserName(builder.getHead().getAccount());
    		builder.clearLoginCommand().setRegisterCommand(registerbuilder.build());
    		
    		reqcmd = builder.build();
    		reqData = reqcmd.toByteArray();
            input = new ByteArrayInputStream(reqData);
            response = http.post(url, input);
        }
        
        return response;
	}
}
