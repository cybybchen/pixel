package com.trans.pixel.test;

import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Request.RequestCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestBlackShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestDailyShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestExpeditionShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestLadderShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestPVPShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.RequestPurchaseContractCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopPurchaseCommand;
import com.trans.pixel.protoc.ShopProto.RequestUnionShopRefreshCommand;
import com.trans.pixel.protoc.ShopProto.ResponseBlackShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseDailyLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseDailyShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseExpeditionShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseLadderShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseLibaoShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponsePVPShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseShopCommand;
import com.trans.pixel.protoc.ShopProto.ResponseUnionShopCommand;

public class ShopTest extends BaseTest {

	@Test
	public void testShop() {
		login();
		testShop(getRequestCommand());
	}
	
	public void testShop(RequestCommand request) {
		getDailyLibaoShop(request);
//		getLibaoShop(request);
//		getDailyShop(request);
//		DailyShopRefresh(request);
//		DailyShopPurchase(request, 0);
//		getBlackShop(request);
//		BlackShopRefresh(request);
//		BlackShopPurchase(request, 0);
////		getUnionShop(request);
////		UnionShopRefresh(request);
////		UnionShopPurchase(request, 0);
//		getPVPShop(request);
//		PVPShopRefresh(request);
//		PVPShopPurchase(request, 0);
////		getExpeditionShop(request);
////		ExpeditionShopRefresh(request);
////		ExpeditionShopPurchase(request, 0);
//		getLadderShop(request);
//		LadderShopRefresh(request);
//		LadderShopPurchase(request, 0);
//		getShop(request);
//		ShopPurchase(request);
//		ContractPurchase(request);
	}

	private ResponseLibaoShopCommand libaoshop = null;
	private void getLibaoShop(RequestCommand request) {
		RequestLibaoShopCommand.Builder builder = RequestLibaoShopCommand.newBuilder();
        ResponseCommand response = request("libaoShopCommand", builder.build(), request);
        
        if(response != null && response.hasLibaoShopCommand())
        	libaoshop = response.getLibaoShopCommand();
	}
	
	private ResponseDailyLibaoShopCommand dailylibaoshop = null;
	private void getDailyLibaoShop(RequestCommand request) {
		RequestDailyLibaoShopCommand.Builder builder = RequestDailyLibaoShopCommand.newBuilder();
        ResponseCommand response = request("dailyLibaoShopCommand", builder.build(), request);
        
        if(response != null && response.hasDailyLibaoShopCommand())
        	dailylibaoshop = response.getDailyLibaoShopCommand();
	}
	
	private ResponseDailyShopCommand dailyshop = null;
	private void getDailyShop(RequestCommand request) {
		RequestDailyShopCommand.Builder builder = RequestDailyShopCommand.newBuilder();
        ResponseCommand response = request("dailyShopCommand", builder.build(), request);
        
        if(response != null && response.hasDailyShopCommand())
        	dailyshop = response.getDailyShopCommand();
	}
	
	private void DailyShopPurchase(RequestCommand request, int index) {
		if(dailyshop == null)
			return;
		RequestDailyShopPurchaseCommand.Builder builder = RequestDailyShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(dailyshop.getItems(index).getId());
        ResponseCommand response = request("dailyShopPurchaseCommand", builder.build(), request);

        if(response != null && response.hasDailyShopCommand())
	        dailyshop = response.getDailyShopCommand();
	}
	
	private void DailyShopRefresh(RequestCommand request) {
		RequestDailyShopRefreshCommand.Builder builder = RequestDailyShopRefreshCommand.newBuilder();
        ResponseCommand response = request("dailyShopRefreshCommand", builder.build(), request);

        if(response != null && response.hasDailyShopCommand())
	        dailyshop = response.getDailyShopCommand();
	}


	private ResponseBlackShopCommand blackshop = null;
	private void getBlackShop(RequestCommand request) {
		RequestBlackShopCommand.Builder builder = RequestBlackShopCommand.newBuilder();
        ResponseCommand response = request("blackShopCommand", builder.build(), request);
        
        if(response != null && response.hasBlackShopCommand())
            blackshop = response.getBlackShopCommand();
	}
	
	private void BlackShopPurchase(RequestCommand request, int index) {
		if(blackshop == null)
			return;
		RequestBlackShopPurchaseCommand.Builder builder = RequestBlackShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(blackshop.getItems(index).getId());
        ResponseCommand response = request("blackShopPurchaseCommand", builder.build(), request);

        if(response != null && response.hasBlackShopCommand())
        	blackshop = response.getBlackShopCommand();
	}
	
	private void BlackShopRefresh(RequestCommand request) {
		if(blackshop == null)
			return;
		RequestBlackShopRefreshCommand.Builder builder = RequestBlackShopRefreshCommand.newBuilder();
        ResponseCommand response = request("blackShopRefreshCommand", builder.build(), request);

        if(response != null && response.hasBlackShopCommand())
        	blackshop = response.getBlackShopCommand();
	}


	private ResponseUnionShopCommand unionshop = null;
	private void getUnionShop(RequestCommand request) {
		RequestUnionShopCommand.Builder builder = RequestUnionShopCommand.newBuilder();
        ResponseCommand response = request("unionShopCommand", builder.build(), request);

        if(response != null && response.hasUnionShopCommand())
        	unionshop = response.getUnionShopCommand();
	}
	
	private void UnionShopPurchase(RequestCommand request, int index) {
		if(unionshop == null)
			return;
		RequestUnionShopPurchaseCommand.Builder builder = RequestUnionShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(unionshop.getItems(index).getId());
        ResponseCommand response = request("unionShopPurchaseCommand", builder.build(), request);

        if(response != null && response.hasUnionShopCommand())
        	unionshop = response.getUnionShopCommand();
	}
	
	private void UnionShopRefresh(RequestCommand request) {
		if(unionshop == null)
			return;
		RequestUnionShopRefreshCommand.Builder builder = RequestUnionShopRefreshCommand.newBuilder();
        ResponseCommand response = request("unionShopRefreshCommand", builder.build(), request);

        if(response != null && response.hasUnionShopCommand())
        	unionshop = response.getUnionShopCommand();
	}


	private ResponsePVPShopCommand pvpshop = null;
	private void getPVPShop(RequestCommand request) {
		RequestPVPShopCommand.Builder builder = RequestPVPShopCommand.newBuilder();
        ResponseCommand response = request("PVPShopCommand", builder.build(), request);

        if(response != null && response.hasPVPShopCommand())
        	pvpshop = response.getPVPShopCommand();
	}
	
	private void PVPShopPurchase(RequestCommand request, int index) {
		RequestPVPShopPurchaseCommand.Builder builder = RequestPVPShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(pvpshop.getItems(index).getId());
        ResponseCommand response = request("PVPShopPurchaseCommand", builder.build(), request);

        if(response != null && response.hasPVPShopCommand())
        	pvpshop = response.getPVPShopCommand();
	}
	
	private void PVPShopRefresh(RequestCommand request) {
		if(pvpshop == null)
			return;
		RequestPVPShopRefreshCommand.Builder builder = RequestPVPShopRefreshCommand.newBuilder();
        ResponseCommand response = request("PVPShopRefreshCommand", builder.build(), request);

        if(response != null && response.hasPVPShopCommand())
        	pvpshop = response.getPVPShopCommand();
	}


	private ResponseExpeditionShopCommand expeditionshop = null;
	private void getExpeditionShop(RequestCommand request) {
		RequestExpeditionShopCommand.Builder builder = RequestExpeditionShopCommand.newBuilder();
        ResponseCommand response = request("expeditionShopCommand", builder.build(), request);

        if(response != null && response.hasExpeditionShopCommand())
        	expeditionshop = response.getExpeditionShopCommand();
	}
	
	private void ExpeditionShopPurchase(RequestCommand request, int index) {
		RequestExpeditionShopPurchaseCommand.Builder builder = RequestExpeditionShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(expeditionshop.getItems(index).getId());
        ResponseCommand response = request("expeditionShopPurchaseCommand", builder.build(), request);

        if(response != null && response.hasExpeditionShopCommand())
        	expeditionshop = response.getExpeditionShopCommand();
	}
	
	private void ExpeditionShopRefresh(RequestCommand request) {
		if(expeditionshop == null)
			return;
		RequestExpeditionShopRefreshCommand.Builder builder = RequestExpeditionShopRefreshCommand.newBuilder();
        ResponseCommand response = request("expeditionShopRefreshCommand", builder.build(), request);

        if(response != null && response.hasExpeditionShopCommand())
        	expeditionshop = response.getExpeditionShopCommand();
	}


	private ResponseLadderShopCommand laddershop = null;
	private void getLadderShop(RequestCommand request) {
		RequestLadderShopCommand.Builder builder = RequestLadderShopCommand.newBuilder();
        ResponseCommand response = request("ladderShopCommand", builder.build(), request);

        if(response != null && response.hasLadderShopCommand())
        	laddershop = response.getLadderShopCommand();
	}
	
	private void LadderShopPurchase(RequestCommand request, int index) {
		RequestLadderShopPurchaseCommand.Builder builder = RequestLadderShopPurchaseCommand.newBuilder();
		builder.setIndex(index);
		builder.setId(laddershop.getItems(index).getId());
        ResponseCommand response = request("ladderShopPurchaseCommand", builder.build(), request);

        if(response != null && response.hasLadderShopCommand())
        	laddershop = response.getLadderShopCommand();
	}
	
	private void LadderShopRefresh(RequestCommand request) {
		if(laddershop == null)
			return;
		RequestLadderShopRefreshCommand.Builder builder = RequestLadderShopRefreshCommand.newBuilder();
        ResponseCommand response = request("ladderShopRefreshCommand", builder.build(), request);

        if(response != null && response.hasLadderShopCommand())
        	laddershop = response.getLadderShopCommand();
	}


	private ResponseShopCommand shop = null;
	private void getShop(RequestCommand request) {
		RequestShopCommand.Builder builder = RequestShopCommand.newBuilder();
        ResponseCommand response = request("shopCommand", builder.build(), request);

        if(response != null && response.hasShopCommand())
        	shop = response.getShopCommand();
	}
	
	private void ShopPurchase(RequestCommand request) {
		RequestShopPurchaseCommand.Builder builder = RequestShopPurchaseCommand.newBuilder();
		builder.setId(shop.getItems(0).getId());
        ResponseCommand response = request("shopPurchaseCommand", builder.build(), request);

	}
	
	private void ContractPurchase(RequestCommand request) {
		RequestPurchaseContractCommand.Builder builder = RequestPurchaseContractCommand.newBuilder();
		builder.setHeroid(42);
        ResponseCommand response = request("purchaseContractCommand", builder.build(), request);

	}

}
