package com.trans.pixel.test.pressure;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestCreateMessageBoardCommand;
import com.trans.pixel.protoc.Commands.RequestMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.test.BaseTest;

public class MessageTest extends BaseTest {
	private static Logger logger = Logger.getLogger(MessageTest.class);
	
	public void testCreateMessage(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestCreateMessageBoardCommand.Builder b = RequestCreateMessageBoardCommand.newBuilder();
		b.setMessage("hahaha");
		b.setType(MessageConst.TYPE_MESSAGE_NORMAL);
		builder.setCreateMessageBoardCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
	
	public void testGetMessageList(RequestCommand.Builder builder, ResponseCommand loginResponse) {
		RequestMessageBoardListCommand.Builder b = RequestMessageBoardListCommand.newBuilder();
		b.setType(MessageConst.TYPE_MESSAGE_NORMAL);
		builder.setMessageBoardListCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}
}
