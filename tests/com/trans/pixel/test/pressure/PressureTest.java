package com.trans.pixel.test.pressure;

import java.util.Random;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserLevel;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.test.BaseTest;
import com.trans.pixel.test.ShopTest;

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
		head(randomAccount(), SERVER_ID);
		RequestCommand request = getRequestCommand(head);
		ResponseCommand response = login(request);//登录
		request = getRequestCommand(head);
		
		submitZhanliTest(request);
		
		levelTest(request, response);//挂机
		
		lotteryTest(request, response);//抽奖
		
		long teamId = teamTest(request, response);//更新队伍
		
		ladderTest(request, response, teamId);//天梯
		
		messageBoardTest(request, response);//留言板
		
		response = login(request);
		
		heroTest(request, response);//英雄
		
		packageTest(request, response);//道具
		
		equipTest(request, response);//装备
		
		new ShopTest().testShop(request);
	}

	private void submitZhanliTest(RequestCommand request){
		RequestSubmitZhanliCommand.Builder builder = RequestSubmitZhanliCommand.newBuilder();
		builder.setZhanli(50000);

		ResponseCommand response = request("submitZhanliCommand", builder.build(), request);
	}
	
	private void levelTest(RequestCommand request, ResponseCommand loginResponse) {
		LevelTest levelTest = new LevelTest();
		
		levelTest.levelPauseTest(request, loginResponse);
		
		UserLevel userLevel = levelTest.levelPauseTest(request, loginResponse);
		sleep(userLevel.getPrepareTime() * 1000);
		
		levelTest.levelStartTest(request, loginResponse);
		
		levelTest.levelResutlTest(request, loginResponse);
		
		levelTest.levellootStartTest(request, loginResponse);
		
		levelTest.levellootResultTest(request, loginResponse);
	}
	
	private void lotteryTest(RequestCommand request, ResponseCommand loginResponse) {
		LotteryTest lottery = new LotteryTest();
		lottery.lotteryTest(request, loginResponse, RewardConst.COIN, 1);
		lottery.lotteryTest(request, loginResponse, RewardConst.COIN, 10);
		lottery.lotteryTest(request, loginResponse, RewardConst.JEWEL, 1);
		lottery.lotteryTest(request, loginResponse, RewardConst.JEWEL, 1);
	}
	
	private long teamTest(RequestCommand request, ResponseCommand loginResponse) {
		TeamTest teamTest = new TeamTest();
		long teamId = teamTest.teamAddTest(request, loginResponse);
		
		teamTest.teamUpdateTest(request, loginResponse, teamId);
		
		return teamId;
	}
	
	private void ladderTest(RequestCommand request, ResponseCommand loginResponse, long teamId) {
		LadderTest ladderTest = new LadderTest();
		UserRank userRank = ladderTest.getUserLadder(request, loginResponse);
		
		TeamTest teamTest = new TeamTest();
		teamTest.gettTeamCacheTest(request, loginResponse, userRank.getUserId());
		
		ladderTest.attackLadder(request, loginResponse, userRank.getRank(), teamId);
	}
	
	private void messageBoardTest(RequestCommand request, ResponseCommand loginResponse) {
		MessageTest messageTest = new MessageTest();
		messageTest.testGetMessageList(request, loginResponse);
		
		messageTest.testCreateMessage(request, loginResponse);
	}
	
	private void heroTest(RequestCommand request, ResponseCommand loginResponse) {
		HeroTest heroTest = new HeroTest();
		heroTest.testHeroLevelUpTest(request, loginResponse, 1);
		
		heroTest.testHeroLevelUpTest(request, loginResponse, 3);
		
		heroTest.testFenjieHeroTest(request, loginResponse);
	}
	
	private void packageTest(RequestCommand request, ResponseCommand loginResponse) {
		PackageTest packageTest = new PackageTest();
		packageTest.testPackage(request, loginResponse);
	}
	
	private void equipTest(RequestCommand request, ResponseCommand loginResponse) {
		EquipTest equipTest = new EquipTest();
		equipTest.testFenjieEquip(request, loginResponse);
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
}
