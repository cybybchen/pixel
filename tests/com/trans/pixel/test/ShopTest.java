package com.trans.pixel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestMagicShopCommand;
import com.trans.pixel.protoc.Commands.RequestMagicShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;

public class ShopTest extends BaseTest {
	private static Logger logger = Logger.getLogger(ShopTest.class);

	@Test
	public void testShop() {
		new LoginTest().testLogin();
		getDailyShop();
		DailyShopPurchase(0);
		getBlackShop();
		BlackShopPurchase(0);
		getUnionShop();
		UnionShopPurchase(0);
		getMagicShop();
		MagicShopPurchase(0);
		getLadderShop();
		LadderShopPurchase(0);
		getShop();
		ShopPurchase(1);
	}
	
	private void getDailyShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestDailyShopCommand.Builder builder = RequestDailyShopCommand.newBuilder();
		requestBuilder.setDailyShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasDailyShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void DailyShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestDailyShopPurchaseCommand.Builder builder = RequestDailyShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		requestBuilder.setDailyShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasDailyShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}

	
	private void getBlackShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestBlackShopCommand.Builder builder = RequestBlackShopCommand.newBuilder();
		requestBuilder.setBlackShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasBlackShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void BlackShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestBlackShopPurchaseCommand.Builder builder = RequestBlackShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		requestBuilder.setBlackShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasBlackShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}

	
	private void getMagicShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestMagicShopCommand.Builder builder = RequestMagicShopCommand.newBuilder();
		requestBuilder.setMagicShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasMagicShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void MagicShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestMagicShopPurchaseCommand.Builder builder = RequestMagicShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		requestBuilder.setMagicShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasMagicShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}

	
	private void getUnionShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestUnionShopCommand.Builder builder = RequestUnionShopCommand.newBuilder();
		requestBuilder.setUnionShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasUnionShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void UnionShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestUnionShopPurchaseCommand.Builder builder = RequestUnionShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		requestBuilder.setUnionShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasUnionShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}

	
	private void getLadderShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestLadderShopCommand.Builder builder = RequestLadderShopCommand.newBuilder();
		requestBuilder.setLadderShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasLadderShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void LadderShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestLadderShopPurchaseCommand.Builder builder = RequestLadderShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		requestBuilder.setLadderShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasLadderShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}

	
	private void getShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestShopCommand.Builder builder = RequestShopCommand.newBuilder();
		requestBuilder.setShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}
	
	private void ShopPurchase(int id) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestShopPurchaseCommand.Builder builder = RequestShopPurchaseCommand.newBuilder();
		builder.setId(id);
		requestBuilder.setShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasShopCommand())
        	fail("testShop Not yet implemented");
        logger.info(response.getAllFields());
	}

}
