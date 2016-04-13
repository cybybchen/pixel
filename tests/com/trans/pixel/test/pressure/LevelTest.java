package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;

import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelLootStartCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPauseCommand;
import com.trans.pixel.protoc.Commands.RequestLevelPrepareCommand;
import com.trans.pixel.protoc.Commands.RequestLevelResultCommand;
import com.trans.pixel.protoc.Commands.RequestLevelStartCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.UserLevel;
import com.trans.pixel.test.BaseTest;

public class LevelTest extends BaseTest {
	
	public void levelResutlTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLevelResultCommand.Builder b = RequestLevelResultCommand.newBuilder();
		b.setLevelId(1001);
		b.setFightInfo("");
		b.setTeamInfo("");
		builder.setLevelResultCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
		InputStream input = new ByteArrayInputStream(reqData);
		ResponseCommand response = http.post(url, input);
		Assert.assertNotNull(response);
		
		builder.clearLevelResultCommand();
	}
	
	public void levelPrepareTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLevelPrepareCommand.Builder b = RequestLevelPrepareCommand.newBuilder();
		b.setLevelId(1001);
		builder.setLevelPrepareCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        
        builder.clearLevelPrepareCommand();
	}
	
	public UserLevel levelPauseTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLevelPauseCommand.Builder b = RequestLevelPauseCommand.newBuilder();
		builder.setLevelPauseCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        
        return response.getUserLevelCommand().getUserLevel();
	}
	
	public void levelStartTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLevelStartCommand.Builder b = RequestLevelStartCommand.newBuilder();
		b.setLevelId(1001);
		builder.setLevelStartCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
		InputStream input = new ByteArrayInputStream(reqData);
		ResponseCommand response = http.post(url, input);
		Assert.assertNotNull(response);
	}
	
	public void levellootStartTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLevelLootStartCommand.Builder b = RequestLevelLootStartCommand.newBuilder();
		b.setLevelId(1001);
		builder.setLevelLootStartCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
	public void levellootResultTest(RequestCommand req, ResponseCommand loginResponse) {
		RequestCommand.Builder builder = RequestCommand.newBuilder(req);
		RequestLevelLootResultCommand.Builder b = RequestLevelLootResultCommand.newBuilder();
		builder.setLevelLootResultCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
