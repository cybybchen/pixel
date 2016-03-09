package com.trans.pixel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestApplyUnionCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestCreateUnionCommand;
import com.trans.pixel.protoc.Commands.RequestUnionInfoCommand;
import com.trans.pixel.protoc.Commands.RequestUnionListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class UnionTest extends BaseTest {
	private static Logger logger = Logger.getLogger(UnionTest.class);

	@Test
	public void testUnion() {
//		login();
//		testCreateUnion();
		testGetUnion();
//		testUnionApply();
//		testGetUnionList();
	}
	
	private void testCreateUnion() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestCreateUnionCommand.Builder builder = RequestCreateUnionCommand.newBuilder();
		builder.setIcon(1);
		builder.setName("工会"+System.currentTimeMillis()/1000);
		requestBuilder.setCreateUnionCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasUnionInfoCommand())
        	fail("testUnion Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testGetUnion() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestUnionInfoCommand.Builder builder = RequestUnionInfoCommand.newBuilder();
		requestBuilder.setUnionInfoCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasUnionInfoCommand())
        	fail("testUnionBoss Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void testGetUnionList() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestUnionListCommand.Builder builder = RequestUnionListCommand.newBuilder();
		requestBuilder.setUnionListCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
	
	private void testUnionApply() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestApplyUnionCommand.Builder builder = RequestApplyUnionCommand.newBuilder();
		builder.setUnionId(17);
		requestBuilder.setApplyUnionCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
	
	private void testUnionReply() {
	}
	
	private void testUnionQuit() {
	}

}
