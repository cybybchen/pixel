package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.HeroProto.RequestUpdateTeamCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class TeamTest extends BaseTest {
//private static Logger logger = Logger.getLogger(TeamTest.class);
	
	@Test
	public void test() {
		login();
		teamTest();
	}
	
	public void teamTest() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestUpdateTeamCommand.Builder b = RequestUpdateTeamCommand.newBuilder();
		b.setId(1);
		b.setTeamInfo("42,1|");
		builder.setUpdateTeamCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        System.out.println(response.getAllFields());
	}
}
