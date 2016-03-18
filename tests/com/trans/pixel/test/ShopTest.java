package com.trans.pixel.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.RequestBlackShopCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.Commands.RequestCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopCommand;
import com.trans.pixel.protoc.Commands.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopCommand;
import com.trans.pixel.protoc.Commands.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopCommand;
import com.trans.pixel.protoc.Commands.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestShopCommand;
import com.trans.pixel.protoc.Commands.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopCommand;
import com.trans.pixel.protoc.Commands.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.Commands.ResponseBlackShopCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseDailyShopCommand;
import com.trans.pixel.protoc.Commands.ResponseExpeditionShopCommand;
import com.trans.pixel.protoc.Commands.ResponseLadderShopCommand;
import com.trans.pixel.protoc.Commands.ResponsePVPShopCommand;
import com.trans.pixel.protoc.Commands.ResponseShopCommand;
import com.trans.pixel.protoc.Commands.ResponseUnionShopCommand;

public class ShopTest extends BaseTest {
	private static Logger logger = Logger.getLogger(ShopTest.class);

	@Test
	public void testShop() {
		login();
//		getDailyShop();
//		DailyShopPurchase(0);
		getBlackShop();
		BlackShopRefresh();
		getBlackShop();
		BlackShopPurchase(0);
		getUnionShop();
		UnionShopPurchase(0);
		getPVPShop();
		PVPShopPurchase(0);
		getExpeditionShop();
		ExpeditionShopPurchase(0);
		getLadderShop();
		LadderShopPurchase(0);
		getShop();
		ShopPurchase();
	}
	private ResponseDailyShopCommand dailyshop = null;
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
        dailyshop = response.getDailyShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void DailyShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestDailyShopPurchaseCommand.Builder builder = RequestDailyShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(dailyshop.getItems(index).getId());
		requestBuilder.setDailyShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasDailyShopCommand())
        	fail("testShop Not yet implemented");
        dailyshop = response.getDailyShopCommand();
        logger.info(response.getAllFields());
	}


	private ResponseBlackShopCommand blackshop = null;
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
        if(response.hasBlackShopCommand()){
            blackshop = response.getBlackShopCommand();
            logger.info(response.getAllFields());
        }else if(user.getVip() >= 8)
        	fail("testShop Not yet implemented");
	}
	
	private void BlackShopPurchase(int index) {
		if(blackshop == null)
			return;
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestBlackShopPurchaseCommand.Builder builder = RequestBlackShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(blackshop.getItems(index).getId());
		requestBuilder.setBlackShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasBlackShopCommand())
        	fail("testShop Not yet implemented");
        blackshop = response.getBlackShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void BlackShopRefresh() {
		if(blackshop == null)
			return;
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestBlackShopRefreshCommand.Builder builder = RequestBlackShopRefreshCommand.newBuilder();
		requestBuilder.setBlackShopRefreshCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasBlackShopCommand())
        	fail("testShop Not yet implemented");
        blackshop = response.getBlackShopCommand();
        logger.info(response.getAllFields());
	}


	private ResponsePVPShopCommand pvpshop = null;
	private void getPVPShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestPVPShopCommand.Builder builder = RequestPVPShopCommand.newBuilder();
		requestBuilder.setPVPShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasPVPShopCommand())
        	fail("testShop Not yet implemented");
        pvpshop = response.getPVPShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void PVPShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestPVPShopPurchaseCommand.Builder builder = RequestPVPShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(pvpshop.getItems(index).getId());
		requestBuilder.setPVPShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasPVPShopCommand())
        	fail("testShop Not yet implemented");
        pvpshop = response.getPVPShopCommand();
        logger.info(response.getAllFields());
	}


	private ResponseUnionShopCommand unionshop = null;
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
        if(!response.hasUnionShopCommand()){
        	if(user.getUnionId() != 0)
        		fail("testShop Not yet implemented");
        }else
        	unionshop = response.getUnionShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void UnionShopPurchase(int index) {
		if(unionshop == null)
			return;
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestUnionShopPurchaseCommand.Builder builder = RequestUnionShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(unionshop.getItems(index).getId());
		requestBuilder.setUnionShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasUnionShopCommand())
        	fail("testShop Not yet implemented");
        unionshop = response.getUnionShopCommand();
        logger.info(response.getAllFields());
	}


	private ResponseExpeditionShopCommand expeditionshop = null;
	private void getExpeditionShop() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestExpeditionShopCommand.Builder builder = RequestExpeditionShopCommand.newBuilder();
		requestBuilder.setExpeditionShopCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasExpeditionShopCommand())
        	fail("testShop Not yet implemented");
        expeditionshop = response.getExpeditionShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void ExpeditionShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestExpeditionShopPurchaseCommand.Builder builder = RequestExpeditionShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(expeditionshop.getItems(index).getId());
		requestBuilder.setExpeditionShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasExpeditionShopCommand())
        	fail("testShop Not yet implemented");
        expeditionshop = response.getExpeditionShopCommand();
        logger.info(response.getAllFields());
	}


	private ResponseLadderShopCommand laddershop = null;
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
        laddershop = response.getLadderShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void LadderShopPurchase(int index) {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestLadderShopPurchaseCommand.Builder builder = RequestLadderShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(laddershop.getItems(index).getId());
		requestBuilder.setLadderShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        if(!response.hasLadderShopCommand())
        	fail("testShop Not yet implemented");
        laddershop = response.getLadderShopCommand();
        logger.info(response.getAllFields());
	}


	private ResponseShopCommand shop = null;
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
        else
        	shop = response.getShopCommand();
        logger.info(response.getAllFields());
	}
	
	private void ShopPurchase() {
		RequestCommand.Builder requestBuilder = RequestCommand.newBuilder();
		requestBuilder.setHead(head());
		RequestShopPurchaseCommand.Builder builder = RequestShopPurchaseCommand.newBuilder();
		builder.setId(shop.getItems(0).getId());
		requestBuilder.setShopPurchaseCommand(builder.build());
		
		RequestCommand reqcmd = requestBuilder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        logger.info(response.getAllFields());
	}

}
