package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Commodity;
import com.trans.pixel.protoc.Commands.CommodityList;
import com.trans.pixel.protoc.Commands.ContractReward;
import com.trans.pixel.protoc.Commands.ContractRewardList;
import com.trans.pixel.protoc.Commands.ContractWeight;
import com.trans.pixel.protoc.Commands.ContractWeightList;
import com.trans.pixel.protoc.Commands.LibaoList;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.PurchaseCoinCostList;
import com.trans.pixel.protoc.Commands.PurchaseCoinReward;
import com.trans.pixel.protoc.Commands.PurchaseCoinRewardList;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.ShopList;
import com.trans.pixel.protoc.Commands.ShopRefresh;
import com.trans.pixel.protoc.Commands.ShopRefreshList;
import com.trans.pixel.protoc.Commands.ShopWill;
import com.trans.pixel.protoc.Commands.ShopWillList;
import com.trans.pixel.protoc.Commands.VipLibao;
import com.trans.pixel.protoc.Commands.VipLibaoList;
import com.trans.pixel.protoc.Commands.Will;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.protoc.Commands.YueKaList;

@Repository
public class ShopRedisService extends RedisService{
	Logger logger = Logger.getLogger(ShopRedisService.class);
	private final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
	
	//普通商店
	public ShopList getDailyShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "DAILYSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildDailyShop(user);
			saveDailyShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveDailyShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "DAILYSHOP", formatJson(shoplist));
	}
	
	public ShopList.Builder buildComms(ShopWillList.Builder shopwillsbuilder, Map<Integer, CommodityList.Builder> commsmap){
		ShopList.Builder builder = ShopList.newBuilder();
		for(ShopWill shopwill : shopwillsbuilder.getShopList()){
			int willnum = 0;
			double weight = 0;
			for(Will will : shopwill.getLootList()){
				weight += will.getWeight();
			}
			weight = weight*Math.random();
			for(Will will : shopwill.getLootList()){
				weight -= will.getWeight();
				if(weight <= 0){
					willnum = will.getWill();
					break;
				}
			}
			CommodityList.Builder commsbuilder = commsmap.get(willnum);
			int index = nextInt(commsbuilder.getItemCount());
			Commodity.Builder comm = commsbuilder.getItemBuilder(index);
			if(shopwill.hasJudge1())
				comm.setJudge(shopwill.getJudge1());
			builder.addItems(comm);
		}
		return builder;
	}
	
	public long getDailyShopEndTime(){
		long time[] = {today(0), today(12), today(18), today(21)};
		long now = now();
		if(now < time[0])
			return time[0];
		else if(now < time[1])
			return time[1];
		else if(now < time[2])
			return time[2];
		else if(now < time[3])
			return time[3];
		else//第二天9点
			return time[0]+24*3600;
	}
	
	public ShopList buildDailyShop(UserBean user){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.DAILYSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shop1shop1.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.DAILYSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getDailyShopComms());
		if(user.getVip() >= 5){
			List<Integer> list = new ArrayList<Integer>();
			int index = nextInt(builder.getItemsCount());
			list.add(index);
			Commodity.Builder comm = builder.getItemsBuilderList().get(index);
			comm.setDiscount(90);
			comm.setDiscost(comm.getCost()*90/100);
			while(list.contains(index))
				index = nextInt(builder.getItemsCount());
			list.add(index);
			comm = builder.getItemsBuilderList().get(index);
			comm.setDiscount(90);
			comm.setDiscost(comm.getCost()*90/100);
			if(user.getVip() >= 14){
				while(list.contains(index))
					index = nextInt(builder.getItemsCount());
				list.add(index);
				comm = builder.getItemsBuilderList().get(index);
				comm.setDiscount(70);
				comm.setDiscost(comm.getCost()*70/100);
				while(list.contains(index))
					index = nextInt(builder.getItemsCount());
				list.add(index);
				comm = builder.getItemsBuilderList().get(index);
				comm.setDiscount(70);
				comm.setDiscost(comm.getCost()*70/100);
			}
		}
		builder.setEndTime(getDailyShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readDailyShopComms(){
		String xml = ReadConfig("lol_shop1.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.DAILYSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getDailyShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.DAILYSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readDailyShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getDailyShopComms(int will){
		String value = this.hget(RedisKey.DAILYSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readDailyShopComms();
			return map.get(will).build();
		}
	}

	//黑市
	public ShopList getBlackShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "BLACKSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildBlackShop(user);
			saveBlackShop(shoplist, user);
			return shoplist;
		}
	}

	public int getBlackShopRefreshCost(int time){
		String value = get(RedisKey.BLACKSHOPCOST_CONFIG);
		ShopRefreshList.Builder builder = ShopRefreshList.newBuilder();
		if(value != null && parseJson(value, builder)){
		}else{
			String xml = ReadConfig("lol_shopblackshuaxin.xml");
			parseXml(xml, builder);
			set(RedisKey.BLACKSHOPCOST_CONFIG, formatJson(builder.build()));
		}
		for(ShopRefresh refresh : builder.getCountList()){
			if(time >= refresh.getCount()-1 && (time < refresh.getCount1() || refresh.getCount1()<0))
				return refresh.getCost();
		}
		return 500;
	}
	
	public void saveBlackShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "BLACKSHOP", formatJson(shoplist));
	}
	
	public long getBlackShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildBlackShop(UserBean user){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.BLACKSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopblackshopblack.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.BLACKSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getBlackShopComms());
		if(user.getVip() >= 12){
			List<Integer> list = new ArrayList<Integer>();
			int index = nextInt(builder.getItemsCount());
			list.add(index);
			Commodity.Builder comm = builder.getItemsBuilderList().get(index);
			comm.setDiscount(90);
			comm.setDiscost(comm.getCost()*90/100);
			while(list.contains(index))
				index = nextInt(builder.getItemsCount());
			list.add(index);
			comm = builder.getItemsBuilderList().get(index);
			comm.setDiscount(90);
			comm.setDiscost(comm.getCost()*90/100);
		}
		builder.setEndTime(getBlackShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readBlackShopComms(){
		String xml = ReadConfig("lol_shopblack.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.BLACKSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getBlackShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.BLACKSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readBlackShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getBlackShopComms(int will){
		String value = this.hget(RedisKey.BLACKSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readBlackShopComms();
			return map.get(will).build();
		}
	}

	//公会商店
	public ShopList getUnionShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "UNIONSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildUnionShop();
			saveUnionShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveUnionShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "UNIONSHOP", formatJson(shoplist));
	}
	
	public long getUnionShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildUnionShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.UNIONSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopgonghuishopgonghui.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.UNIONSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getUnionShopComms());
		builder.setEndTime(getUnionShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readUnionShopComms(){
		String xml = ReadConfig("lol_shopgonghui.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.UNIONSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getUnionShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.UNIONSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readUnionShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getUnionShopComms(int will){
		String value = this.hget(RedisKey.UNIONSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readUnionShopComms();
			return map.get(will).build();
		}
	}

	//挂机PVP商店
	public ShopList getPVPShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "PVPSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildPVPShop();
			savePVPShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void savePVPShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "PVPSHOP", formatJson(shoplist));
	}
	
	public long getPVPShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildPVPShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.PVPSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopmojingshopmojing.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.PVPSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getPVPShopComms());
		builder.setEndTime(getPVPShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readPVPShopComms(){
		String xml = ReadConfig("lol_shopmojing.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.PVPSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getPVPShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.PVPSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readPVPShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getPVPShopComms(int will){
		String value = this.hget(RedisKey.PVPSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readPVPShopComms();
			return map.get(will).build();
		}
	}

	//远征商店
	public ShopList getExpeditionShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "EXPEDITIONSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildExpeditionShop();
			saveExpeditionShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveExpeditionShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "EXPEDITIONSHOP", formatJson(shoplist));
	}
	
	public long getExpeditionShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildExpeditionShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.EXPEDITIONSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopyuanzhengshopyuanzheng.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.EXPEDITIONSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getExpeditionShopComms());
		builder.setEndTime(getExpeditionShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readExpeditionShopComms(){
		String xml = ReadConfig("lol_shopyuanzheng.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.EXPEDITIONSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getExpeditionShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.EXPEDITIONSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readExpeditionShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getExpeditionShopComms(int will){
		String value = this.hget(RedisKey.EXPEDITIONSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readExpeditionShopComms();
			return map.get(will).build();
		}
	}

	//天梯商店
	public ShopList getLadderShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "LADDERSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildLadderShop();
			saveLadderShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveLadderShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "LADDERSHOP", formatJson(shoplist));
	}
	
	public long getLadderShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
//	class CommCreater {
//		public void available(int value) {
//		}
//	}
	 
	public ShopList buildLadderShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.LADDERSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shoptiantishoptianti.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.LADDERSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getLadderShopComms());
		builder.setEndTime(getLadderShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readLadderShopComms(){
		String xml = ReadConfig("lol_shoptianti.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.LADDERSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getLadderShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.LADDERSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readLadderShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getLadderShopComms(int will){
		String value = this.hget(RedisKey.LADDERSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readLadderShopComms();
			return map.get(will).build();
		}
	}

	//libao
	public LibaoList.Builder getLibaoShop() {
		String value = this.get(RedisKey.LIBAOSHOP_CONFIG);
		LibaoList.Builder shopbuilder = LibaoList.newBuilder();
		if(value == null){
			shopbuilder = LibaoList.newBuilder(buildLibaoShop());
		}else{
			parseJson(value, shopbuilder);
		}
		return shopbuilder;
	}
	
	private LibaoList buildLibaoShop(){
		LibaoList.Builder itemsbuilder = LibaoList.newBuilder();
		String xml = ReadConfig("lol_shoplibao.xml");
		parseXml(xml, itemsbuilder);
		set(RedisKey.LIBAOSHOP_CONFIG, formatJson(itemsbuilder.build()));
		return itemsbuilder.build();
	}

	//商城
	public Commodity getShop(int id) {
		String value = this.hget(RedisKey.SHOP_CONFIG, id+"");
		Commodity.Builder builder = Commodity.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
	}

	public ShopList getShop() {
		Map<String, String> keyvalue = this.hget(RedisKey.SHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return buildShop();
		}
		ShopList.Builder shopbuilder = ShopList.newBuilder();
		for(String value : keyvalue.values()){
			Commodity.Builder builder = Commodity.newBuilder();
			if(parseJson(value, builder)){
				shopbuilder.addItems(builder);
			}
		}
		return shopbuilder.build();
	}
	
	private void saveShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0){
			Map<String, String> map = new HashMap<String, String>();
			for(Commodity comm : shoplist.getItemsList())
				map.put(comm.getId()+"", formatJson(comm));
			this.hputAll(RedisKey.SHOP_CONFIG, map);
		}
	}
	
	public ShopList buildShop(){
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		String xml = ReadConfig("lol_shop2.xml");
		parseXml(xml, commsbuilder);
		ShopList.Builder shoplist = ShopList.newBuilder();
		for(Commodity.Builder comm : commsbuilder.getItemBuilderList()){
			comm.clearName();
			shoplist.addItems(comm);
		}
		saveShop(shoplist.build());
		return shoplist.build();
	}
	
	public ContractWeightList getContractWeightList(){
		ContractWeightList.Builder builder = ContractWeightList.newBuilder();
		String value = get(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG);
		if(value != null && parseJson(value, builder))
			return builder.build();
		String xml = ReadConfig("lol_soulcontract.xml");
		parseXml(xml, builder);
		int weightall = 0;
		for(ContractWeight weight : builder.getContractList())
			weightall += weight.getWeight();
		builder.setWeightall(weightall);
		set(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG, formatJson(builder.build()));
		return builder.build();
	}
	
	public MultiReward.Builder getContractRewardList(){
		MultiReward.Builder builder = MultiReward.newBuilder();
		String value = get(RedisKey.PURCHASECONTRACTREWARD_CONFIG);
		if(value != null && parseJson(value, builder))
			return builder;
		ContractRewardList.Builder listbuilder = ContractRewardList.newBuilder();
		String xml = ReadConfig("lol_soulreward.xml");
		parseXml(xml, listbuilder);
		for(ContractReward reward : listbuilder.getRewardList()){
			RewardInfo.Builder rewardbuilder = RewardInfo.newBuilder();
			rewardbuilder.setItemid(reward.getRewardid());
			rewardbuilder.setCount(reward.getCount());
			builder.addLoot(rewardbuilder);
		}
		set(RedisKey.PURCHASECONTRACTREWARD_CONFIG, formatJson(builder.build()));
		return builder;
	}
	
	public PurchaseCoinCostList getPurchaseCoinCostList(){
		PurchaseCoinCostList.Builder builder = PurchaseCoinCostList.newBuilder();
		String value = get(RedisKey.PURCHASECOIN_CONFIG);
		if(value != null && parseJson(value, builder))
			return builder.build();
		String xml = ReadConfig("lol_goldcost.xml");
		parseXml(xml, builder);
		set(RedisKey.PURCHASECOIN_CONFIG, formatJson(builder.build()));
		return builder.build();
	}
	
	public PurchaseCoinReward getPurchaseCoinReward(int daguan){
		PurchaseCoinReward.Builder builder = PurchaseCoinReward.newBuilder();
		String value = hget(RedisKey.PURCHASECOINREWARD_CONFIG, daguan+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		String xml = ReadConfig("lol_goldreward.xml");
		PurchaseCoinRewardList.Builder list = PurchaseCoinRewardList.newBuilder();
		Map<String, String> keyvalue = new HashMap<String, String>();
		parseXml(xml, list);
		for(PurchaseCoinReward reward : list.getGoldList()){
			keyvalue.put(reward.getDaguan()+"", formatJson(reward));
			if(reward.getDaguan() == daguan)
				builder = PurchaseCoinReward.newBuilder(reward);
		}
		hputAll(RedisKey.PURCHASECOINREWARD_CONFIG, keyvalue);
		return builder.build();
	}
	
	public VipLibao getVipLibao(int id){
		VipLibao.Builder builder = VipLibao.newBuilder();
		String value = hget(RedisKey.VIPLIBAO_CONFIG, id+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		String xml = ReadConfig("lol_libao.xml");
		VipLibaoList.Builder list = VipLibaoList.newBuilder();
		Map<String, String> keyvalue = new HashMap<String, String>();
		parseXml(xml, list);
		for(VipLibao libao : list.getLibaoList()){
			keyvalue.put(libao.getItemid()+"", formatJson(libao));
			if(libao.getItemid() == id)
				builder = VipLibao.newBuilder(libao);
		}
		hputAll(RedisKey.VIPLIBAO_CONFIG, keyvalue);
		return builder.build();
	}
	
	public Map<Integer, YueKa> getYueKas(){
		Map<Integer, YueKa> map = new HashMap<Integer, YueKa>();
		Map<String, String> keyvalue = this.hget(RedisKey.YUEKA_CONFIG);
		if(!keyvalue.isEmpty()){
			for(String value : keyvalue.values()){
				YueKa.Builder builder = YueKa.newBuilder();
				if(parseJson(value, builder)){
					map.put(builder.getItemid(), builder.build());
				}
			}
			return map;
		}else{
			return buildYueKa();
		}
	}

	public YueKa getYueKa(int id){
		String value = this.hget(RedisKey.YUEKA_CONFIG, ""+id);
		YueKa.Builder builder = YueKa.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, YueKa> map = buildYueKa();
			YueKa yueka = map.get(id);
			return yueka;
		}
	}

	public Map<Integer, YueKa> buildYueKa(){
		String xml = ReadConfig("lol_yueka.xml");
		YueKaList.Builder listbuilder = YueKaList.newBuilder();
		Map<String, String> keyvalue = new HashMap<String, String>();
		Map<Integer, YueKa> map = new HashMap<Integer, YueKa>();
		parseXml(xml, listbuilder);
		for(YueKa libao : listbuilder.getItemList()){
			map.put(libao.getItemid(), libao);
			keyvalue.put(libao.getItemid()+"", formatJson(libao));
		}
		hputAll(RedisKey.YUEKA_CONFIG, keyvalue);
		
		return map;
	}
}
