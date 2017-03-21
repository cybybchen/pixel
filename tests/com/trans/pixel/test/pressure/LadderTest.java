package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.protoc.Base.UserRank;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.LadderProto.RequestAttackLadderModeCommand;
import com.trans.pixel.protoc.LadderProto.RequestGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.test.BaseTest;

public class LadderTest extends BaseTest {
	private static Logger logger = Logger.getLogger(LadderTest.class);

	public void attackLadder(RequestCommand req, ResponseCommand loginResponse, long rank, long teamId) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestAttackLadderModeCommand.Builder b = RequestAttackLadderModeCommand.newBuilder();
		b.setRank(rank);
		b.setRet(true);
		b.setTeamId(teamId);
		builder.setAttackLadderModeCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
	
	public UserRank getUserLadder(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestGetUserLadderRankListCommand.Builder b = RequestGetUserLadderRankListCommand.newBuilder();
		builder.setGetUserLadderRankListCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
        
        return response.getGetUserLadderRankListCommand().getUserRank(0);
	}
}
