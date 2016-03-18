package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Commodity;
import com.trans.pixel.protoc.Commands.CommodityList;
import com.trans.pixel.protoc.Commands.PurchaseCoinCostList;
import com.trans.pixel.protoc.Commands.PurchaseCoinReward;
import com.trans.pixel.protoc.Commands.PurchaseCoinRewardList;
import com.trans.pixel.protoc.Commands.ShopList;
import com.trans.pixel.protoc.Commands.ShopWill;
import com.trans.pixel.protoc.Commands.ShopWillList;
import com.trans.pixel.protoc.Commands.Will;

@Repository
public class ShopRedisService extends RedisService{
	Logger logger = Logger.getLogger(ShopRedisService.class);
	private final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
	private final static String DAILYSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"DailyShop";
	private final static String SHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"Shop";
	private final static String BLACKSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"BlackShop";
	private final static String UNIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"UnionShop";
	private final static String PVPSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"PVPShop";
	private final static String EXPEDITIONSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"ExpeditionShop";
	private final static String LADDERSHOP_CONFIG = RedisKey.PREFIX+RedisKey.CONFIG_PREFIX+"LadderShop";
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	
	//普通商店
	public ShopList getDailyShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "DAILYSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildDailyShop();
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
				if(weight <= 0)
					willnum = will.getWill();
			}
			CommodityList.Builder commsbuilder = commsmap.get(willnum);
			int index = (int)(commsbuilder.getItemCount()*Math.random());
			builder.addItems(commsbuilder.getItem(index));
		}
		return builder;
	}
	
	public long getDailyShopEndTime(){
		long time[] = {today(9), today(12), today(18), today(21)};
		long now = System.currentTimeMillis()/1000;
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
	
	public ShopList buildDailyShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(DAILYSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shop1shop1.xml");
			parseXml(xml, willsbuilder);
			set(DAILYSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getDailyShopComms());
		builder.setEndTime(getDailyShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readDailyShopComms(){
		String xml = ReadConfig("lol_shop1.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity comm : commsbuilder.getItemList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(DAILYSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getDailyShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(DAILYSHOP_CONFIG);
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
		String value = this.hget(DAILYSHOP_CONFIG, will+"");
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
			ShopList shoplist = buildBlackShop();
			saveBlackShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveBlackShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "BLACKSHOP", formatJson(shoplist));
	}
	
	public long getBlackShopEndTime(){
		long time = today(21);
		if(System.currentTimeMillis()/1000 < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildBlackShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(BLACKSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopblackshopblack.xml");
			parseXml(xml, willsbuilder);
			set(BLACKSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = buildComms(willsbuilder, getBlackShopComms());
		builder.setEndTime(getBlackShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readBlackShopComms(){
		String xml = ReadConfig("lol_shopblack.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity comm : commsbuilder.getItemList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(BLACKSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getBlackShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(BLACKSHOP_CONFIG);
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
		String value = this.hget(BLACKSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readBlackShopComms();
			return map.get(will).build();
		}
	}

	//工会商店
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
		if(System.currentTimeMillis()/1000 < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildUnionShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(UNIONSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopgonghuishopgonghui.xml");
			parseXml(xml, willsbuilder);
			set(UNIONSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
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
		for(Commodity comm : commsbuilder.getItemList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(UNIONSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getUnionShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(UNIONSHOP_CONFIG);
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
		String value = this.hget(UNIONSHOP_CONFIG, will+"");
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
		if(System.currentTimeMillis()/1000 < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildPVPShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(PVPSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopmojingshopmojing.xml");
			parseXml(xml, willsbuilder);
			set(PVPSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
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
		for(Commodity comm : commsbuilder.getItemList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(PVPSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getPVPShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(PVPSHOP_CONFIG);
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
		String value = this.hget(PVPSHOP_CONFIG, will+"");
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
		if(System.currentTimeMillis()/1000 < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildExpeditionShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(EXPEDITIONSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopyuanzhengshopyuanzheng.xml");
			parseXml(xml, willsbuilder);
			set(EXPEDITIONSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
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
		for(Commodity comm : commsbuilder.getItemList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(EXPEDITIONSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getExpeditionShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(EXPEDITIONSHOP_CONFIG);
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
		String value = this.hget(EXPEDITIONSHOP_CONFIG, will+"");
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
		if(System.currentTimeMillis()/1000 < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildLadderShop(){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(LADDERSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shoptiantishoptianti.xml");
			parseXml(xml, willsbuilder);
			set(LADDERSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
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
		for(Commodity comm : commsbuilder.getItemList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comms.addItem(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(LADDERSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getLadderShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(LADDERSHOP_CONFIG);
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
		String value = this.hget(LADDERSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readLadderShopComms();
			return map.get(will).build();
		}
	}

	//商城
	public Commodity getShop(int id) {
		String value = this.hget(SHOP_CONFIG, id+"");
		Commodity.Builder builder = Commodity.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
	}

	public ShopList getShop() {
		Map<String, String> keyvalue = this.hget(SHOP_CONFIG);
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
			this.hputAll(SHOP_CONFIG, map);
		}
	}
	
	public ShopList buildShop(){
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		String xml = ReadConfig("lol_shop2.xml");
		parseXml(xml, commsbuilder);
		ShopList.Builder shoplist = ShopList.newBuilder();
		shoplist.addAllItems(commsbuilder.getItemList());
		saveShop(shoplist.build());
		return shoplist.build();
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
}
