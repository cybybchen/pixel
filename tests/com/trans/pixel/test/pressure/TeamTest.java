package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.protoc.Commands.RequestAddTeamCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestGetTeamCommand;
import com.trans.pixel.protoc.Commands.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.test.BaseTest;

public class TeamTest extends BaseTest {
	private static Logger logger = Logger.getLogger(TeamTest.class);
	
	public long teamAddTest(RequestCommand.Builder req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestAddTeamCommand.Builder b = RequestAddTeamCommand.newBuilder();
		b.setTeamInfo("10,1|2,3|");
		builder.setAddTeamCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        
        return response.getUserTeamListCommand().getUserTeam(0).getId();
	}
	
	public void teamUpdateTest(RequestCommand.Builder req, ResponseCommand loginResponse, long teamId) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestUpdateTeamCommand.Builder b = RequestUpdateTeamCommand.newBuilder();
		b.setTeamInfo("10,1|2,3|4,1|");
		b.setId(teamId);
		builder.setUpdateTeamCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
	public void gettTeamCacheTest(RequestCommand.Builder req, ResponseCommand loginResponse, long userId) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req.build());
		RequestGetTeamCommand.Builder b = RequestGetTeamCommand.newBuilder();
		b.setUserId(userId);
		builder.setTeamCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
