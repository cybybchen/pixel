package com.trans.pixel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.RequestAreaCommand;
import com.trans.pixel.protoc.Commands.RequestAttackBossCommand;
import com.trans.pixel.protoc.Commands.RequestAttackMonsterCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceCommand;
import com.trans.pixel.protoc.Commands.RequestAttackResourceMineCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.ResponseAreaCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class AreaTest extends BaseTest {
	private static Logger logger = Logger.getLogger(AreaTest.class);

	@Test
	public void testArea() {
		login();
		testGetArea();
		testAreaBoss();
		testAreaMonster();
		testAreaResource();
		testAreaResourceMine();
	}
	
	ResponseAreaCommand area = null;
	private void testGetArea() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAreaCommand.Builder builder = RequestAreaCommand.newBuilder();
		requestBuilder.setAreaCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testArea Not yet implemented");
        area = response.getAreaCommand();
        logger.info(response.getAllFields());
	}
	
	private void testAreaBoss() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackBossCommand.Builder builder = RequestAttackBossCommand.newBuilder();
		AreaBoss boss = area.getAreas(4).getBosses(0);
		builder.setId(boss.getId());
		builder.setScore(boss.getHp());
		requestBuilder.setAttackBossCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaBoss Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaMonster() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackMonsterCommand.Builder builder = RequestAttackMonsterCommand.newBuilder();
		AreaMonster monster = area.getAreas(0).getMonsters(0);
		builder.setId(monster.getId());
		requestBuilder.setAttackMonsterCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaMonster Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaResource() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackResourceCommand.Builder builder = RequestAttackResourceCommand.newBuilder();
		AreaResource resource = area.getAreas(0).getResources(0);
		builder.setId(resource.getId());
		requestBuilder.setAttackResourceCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaResource Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testAreaResourceMine() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestAttackResourceMineCommand.Builder builder = RequestAttackResourceMineCommand.newBuilder();
		AreaResource resource = area.getAreas(0).getResources(0);
		builder.setId(resource.getMines(0).getId());
		builder.setTeamid(1);
		requestBuilder.setAttackResourceMineCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasAreaCommand())
        	fail("testAreaResourceMine Not yet implemented");
        logger.info(response.getAllFields());
	}

}
