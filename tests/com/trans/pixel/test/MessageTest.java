package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.MessageBoardProto.RequestCreateMessageBoardCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class MessageTest extends BaseTest {
	private static Logger logger = Logger.getLogger(MessageTest.class);

	@Test
	public void test() {
		login();
		testCreateMessage();
	}
	
	private void testCreateMessage() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestCreateMessageBoardCommand.Builder builder = RequestCreateMessageBoardCommand.newBuilder();
		builder.setMessage("hahaha");
		builder.setType(MessageConst.TYPE_MESSAGE_NORMAL);
		requestBuilder.setCreateMessageBoardCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
}
