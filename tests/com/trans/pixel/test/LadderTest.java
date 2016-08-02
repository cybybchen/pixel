package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestGetLadderUserInfoCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.RequestReadyAttackLadderCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserLadderRankListCommand;

public class LadderTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LadderTest.class);

	@Test
	public void testLadder() {
		login();
		getUserLadder();
		attackLadder();
//		getLadderUserInfo();
	}
	private void attackLadder() {
		int teamid = 1;
		long rank = ranks.getUserRankList().get(0).getRank();
		long enemyid = ranks.getUserRankList().get(0).getUserId();
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackLadderModeCommand.Builder builder = RequestAttackLadderModeCommand.newBuilder();
		builder.setRank(rank);
		builder.setRet(true);
		builder.setTeamId(teamid);
		builder.setAttackUserId(enemyid);
		requestBuilder.setAttackLadderModeCommand(builder.build());
		RequestReadyAttackLadderCommand.Builder readybuilder = RequestReadyAttackLadderCommand.newBuilder();
		requestBuilder.setReadyAttackLadderCommand(readybuilder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
	private ResponseGetUserLadderRankListCommand ranks = null;
	private void getUserLadder() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestGetUserLadderRankListCommand.Builder builder = RequestGetUserLadderRankListCommand.newBuilder();
		requestBuilder.setGetUserLadderRankListCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        ranks = response.getGetUserLadderRankListCommand();
        logger.info(response.getAllFields());
	}
	public void getLadderUserInfo(){
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestGetLadderUserInfoCommand.Builder builder = RequestGetLadderUserInfoCommand.newBuilder();
		builder.setRank(9);
		requestBuilder.setLadderUserInfoCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
}
