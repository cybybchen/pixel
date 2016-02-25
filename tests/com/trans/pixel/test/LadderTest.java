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
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class LadderTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LadderTest.class);

	@Test
	public void testLadder() {
		login();
//		attackLadder();
//		getUserLadder();
		getLadderUserInfo();
	}
	private void attackLadder() {
		int teamid = 6;
		int rank = 9;
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackLadderModeCommand.Builder builder = RequestAttackLadderModeCommand.newBuilder();
		builder.setRank(rank);
		builder.setRet(true);
		builder.setTeamId(teamid);
		requestBuilder.setAttackLadderModeCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
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
