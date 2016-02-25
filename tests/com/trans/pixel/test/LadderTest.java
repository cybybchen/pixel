package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class LadderTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LadderTest.class);

	@Test
	public void testLadder() {
		login();
		attackLadder();
	}
	private void attackLadder() {
		int teamid = 6;
		int rank = 1;
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
}
