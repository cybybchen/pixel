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
	
	public void levelResutlTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestLevelResultCommand.Builder b = RequestLevelResultCommand.newBuilder();
		UserLevel userLevel = loginResponse.getUserLevelCommand().getUserLevel();
		b.setLevelId(userLevel.getPutongLevel() + 1);
		b.setFightInfo("");
		b.setTeamInfo("");
		builder.setLevelResultCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
		InputStream input = new ByteArrayInputStream(reqData);
		ResponseCommand response = http.post(url, input);
		Assert.assertNotNull(response);
	}
	
	public void levelPrepareTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestLevelPrepareCommand.Builder b = RequestLevelPrepareCommand.newBuilder();
		b.setLevelId(1001);
		builder.setLevelPrepareCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
	public UserLevel levelPauseTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestLevelPauseCommand.Builder b = RequestLevelPauseCommand.newBuilder();
		builder.setLevelPauseCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        
        return response.getUserLevelCommand().getUserLevel();
	}
	
	public void levelStartTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestLevelStartCommand.Builder b = RequestLevelStartCommand.newBuilder();
		b.setLevelId(1001);
		builder.setLevelStartCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
		InputStream input = new ByteArrayInputStream(reqData);
		ResponseCommand response = http.post(url, input);
		Assert.assertNotNull(response);
	}
	
	public void levellootStartTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestLevelLootStartCommand.Builder b = RequestLevelLootStartCommand.newBuilder();
		b.setLevelId(1001);
		builder.setLevelLootStartCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
	
	public void levellootResultTest(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestLevelLootResultCommand.Builder b = RequestLevelLootResultCommand.newBuilder();
		builder.setLevelLootResultCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
	}
}
