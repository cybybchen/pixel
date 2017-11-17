package com.trans.pixel.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.RechargeProto.RequestCanRechargeCommand;
import com.trans.pixel.protoc.RechargeProto.RequestQueryRechargeCommand;
import com.trans.pixel.protoc.Request.RequestCommand;

public class RechargeTest extends BaseTest {
//private static Logger logger = Logger.getLogger(TeamTest.class);
	
	@Test
	public void test() {
		login();
		int itemid = 6;
		canrecharge(itemid);
//		get_rechargeRecall(itemid);
//		queryRecharge();
	}
	
	public void canrecharge(int itemid) {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestCanRechargeCommand.Builder b = RequestCanRechargeCommand.newBuilder();
		b.setType(1);
		b.setRechargeid(itemid);
		b.setOrder(332);
		builder.setCanRechargeCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        System.out.println(response.getAllFields());
	}

//	public void post_rechargeRecall() {
//		Map<String, String> map = new HashMap<String, String>();
//		map.put("itemid", "4");
//		map.put("playerid", "4");
//		map.put("order_id", "100000035276236");
//		map.put("zone_id", "1");
//		map.put("action", "1");
//		map.put("company", "ios");
//		map.put("sn", "2b7d0366edeaca043d64b5fdf6195846");
//		map.put("player", "1");
//        String response = http.post("http://123.207.88.112:8082/Lol450/recharge", map);
//        System.out.println(response);
//	}

	public void get_rechargeRecall(int itemid) {
		long orderid = System.currentTimeMillis();
        http.get("http://123.207.88.112:8082/Lol450/recharge?itemid="+itemid+"&zone_id=1&action=1&company=ios&sn=2b7d0366edeaca043d64b5fdf6195846&order_id="+orderid+"&player=&playerid="+user.getId());
	}

	public void queryRecharge() {
		RequestCommand.Builder builder = RequestCommand.newBuilder();
		builder.setHead(head());
		RequestQueryRechargeCommand.Builder b = RequestQueryRechargeCommand.newBuilder();
		b.setOrderId("1");
		builder.setQueryRechargeCommand(b.build());
		
		RequestCommand reqcmd = builder.build();
		byte[] reqData = reqcmd.toByteArray();
        InputStream input = new ByteArrayInputStream(reqData);
        ResponseCommand response = http.post(url, input);
        Assert.assertNotNull(response);
        System.out.println(response.getAllFields());
	}
}
