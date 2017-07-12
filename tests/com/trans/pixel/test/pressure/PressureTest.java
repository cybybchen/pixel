package com.trans.pixel.test.pressure;

import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.EquipProto.RequestSubmitZhanliCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.test.BaseTest;
import com.trans.pixel.test.PvpMapTest;
import com.trans.pixel.test.ShopTest;

public class PressureTest extends BaseTest {
	public static final String RANDOM_CODE = "QWERTYUIOPASDFGHJKLZXCVBNM1234567890";
	public static final int THREAD_COUNT = 10000;//最大并发数量
	public static final int TEST_TIME = 10000;//脚本执行秒数
	
	public static void main(String[] args) {
		extraLog = false;
		Queue<Thread> queue = new ConcurrentLinkedQueue<Thread>();
		long time = System.currentTimeMillis();
		int count = 0;
		while (true) {
			sleep(200);
			Thread thread = new Thread() {
				public void run() {
					PressureTest test = new PressureTest();
					test.randomCommand();
				}
			};
			thread.start();
			queue.add(thread);
			if(queue.size() >= THREAD_COUNT){
				waitThread(queue);
			}
			for(Entry<String, TimeBean> entry : timemap.entrySet()){
				count += entry.getValue().getTime();
			}
			System.out.println("总次数：" + count);
			System.out.println("执行时间为:" + (System.currentTimeMillis() - time) / 1000);
			count = 0;
			if((System.currentTimeMillis() - time)/1000 > TEST_TIME){
				while(!queue.isEmpty()){
					waitThread(queue);
				}
				System.out.println("成功次数\t总次数\t平均时间\t发送请求");
				
				for(Entry<String, TimeBean> entry : timemap.entrySet()){
					count += entry.getValue().getTime();
					if(entry.getValue().getSuccess() == 0)
						System.out.println(entry.getValue().getSuccess() + "\t"
								+ entry.getValue().getTime() + "\t0\t"
								+entry.getKey());
					else
						System.out.println(entry.getValue().getSuccess() + "\t"
								+ entry.getValue().getTime() + "\t"
								+entry.getValue().getMsec()/entry.getValue().getSuccess()+"\t"
								+entry.getKey());
				}
				
				break;
			}
		}
		System.out.println("总次数：" + count);
		System.out.println("执行时间为:" + (System.currentTimeMillis() - time) / 1000);
	}
	
	private static void waitThread(Queue<Thread> queue){
		Thread th = queue.remove();
		try {
			th.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
		
		while (true) {
			sleep(200);
		submitZhanliTest(request);
		
		levelTest(request, response);//挂机
		
		lotteryTest(request, response);//抽奖
		
		long teamId = teamTest(request, response);//更新队伍
		
		ladderTest(request, response, teamId);//天梯
		
		messageBoardTest(request, response);//留言板
		
		response = login(request);
		request = getRequestCommand(head);
		
		heroTest(request, response);//英雄
		
//		packageTest(request, response);//道具
		
//		equipTest(request, response);//装备
		
		new PvpMapTest().testPvp(request);
		
		//area
		//union
		
		new ShopTest().testShop(request);
		}
	}

	private void submitZhanliTest(RequestCommand request){
		RequestSubmitZhanliCommand.Builder builder = RequestSubmitZhanliCommand.newBuilder();
		builder.setZhanli(50000);
//		builder.setZhanli(20000+(int)(System.currentTimeMillis()%30000));

		ResponseCommand response = request("submitZhanliCommand", builder.build(), request);
	}
	
	private void levelTest(RequestCommand request, ResponseCommand loginResponse) {
		LevelTest levelTest = new LevelTest();
		
//		levelTest.levelPauseTest(request, loginResponse);
		
//		UserLevel userLevel = levelTest.levelPauseTest(request, loginResponse);
//		sleep(userLevel.getPrepareTime() * 1000);
//		
//		levelTest.levelStartTest(request, loginResponse);
//		
//		levelTest.levelResutlTest(request, loginResponse);
//		
//		levelTest.levellootStartTest(request, loginResponse);
		
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
		teamTest.gettTeamCacheTest(request, loginResponse, userRank.getTeam().getUser().getId());
		
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
