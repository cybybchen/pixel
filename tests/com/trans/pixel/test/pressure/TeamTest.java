package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.HeroProto.RequestGetTeamCommand;
import com.trans.pixel.protoc.HeroProto.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.test.BaseTest;

public class TeamTest extends BaseTest {
	
	public long teamAddTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestUpdateTeamCommand.Builder b = RequestUpdateTeamCommand.newBuilder();
		b.setId(1);
		b.setTeamInfo("10,1|2,3|");
		builder.setUpdateTeamCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        
        return response.getUserTeamListCommand().getUserTeam(0).getId();
	}
	
	public void teamUpdateTest(RequestCommand req, ResponseCommand loginResponse, long teamId) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
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
	
	public void gettTeamCacheTest(RequestCommand req, ResponseCommand loginResponse, long userId) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
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
