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
import com.trans.pixel.protoc.Commands.ShopList;
import com.trans.pixel.protoc.Commands.ShopWill;
import com.trans.pixel.protoc.Commands.ShopWillList;
import com.trans.pixel.protoc.Commands.Will;

@Repository
public class ShopRedisService extends RedisService{
	Logger logger = Logger.getLogger(ShopRedisService.class);
	public final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
	public final static String USERDAILYDATA = RedisKey.PREFIX+RedisKey.USERDAILYDATA_PREFIX;
	public final static String DAILYSHOP = RedisKey.PREFIX+"DailyShop";
	public final static String SHOP = RedisKey.PREFIX+"Shop";
	public final static String BLACKSHOP = RedisKey.PREFIX+"BlackShop";
	public final static String UNIONSHOP = RedisKey.PREFIX+"UnionShop";
	public final static String PVPSHOP = RedisKey.PREFIX+"PVPShop";
	public final static String EXPEDITIONSHOP = RedisKey.PREFIX+"ExpeditionShop";
	public final static String LADDERSHOP = RedisKey.PREFIX+"LadderShop";
	@Resource
	private RedisTemplate<String, String> redisTemplate;
	private UserBean user = null;
	
	//普通商店
	public ShopList getDailyShop() {
		String value = this.hget(USERDATA+user.getId(), DAILYSHOP);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildDailyShop();
			saveDailyShop(shoplist);
			return shoplist;
		}
	}
	
	public void saveDailyShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), DAILYSHOP, formatJson(shoplist));
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
		long time = System.currentTimeMillis()/1000;
		long day = time/24/3600;
		long hour = (time/3600)%24;
		if(hour < 9)
			hour = 9;
		else if(hour <12)
			hour = 12;
		else if(hour < 18)
			hour = 18;
		else if(hour < 21)
			hour = 21;
		else//第二天9点
			hour = 33;
		return day*24*3600+hour*3600;
	}
	
	public ShopList buildDailyShop(){
		String xml = ReadConfig("lol_shop1shop1.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		
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
		this.hputAll(DAILYSHOP, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getDailyShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(DAILYSHOP);
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
		String value = this.hget(DAILYSHOP, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readDailyShopComms();
			return map.get(will).build();
		}
	}

	public int getDailyShopRefreshTime(){
		String value = this.hget(USERDAILYDATA+user.getId(), "DailyShopRefreshTime");
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void saveDailyShopRefreshTime(int time){
		this.setExpireDate(nextDay());
		hput(USERDAILYDATA+user.getId(), "DailyShopRefreshTime", time+"");
	}

	//黑市
	public ShopList getBlackShop() {
		String value = this.hget(USERDATA+user.getId(), BLACKSHOP);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildBlackShop();
			saveBlackShop(shoplist);
			return shoplist;
		}
	}
	
	public void saveBlackShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), BLACKSHOP, formatJson(shoplist));
	}
	
	public long getBlackShopEndTime(){
		long time = System.currentTimeMillis()/1000;
		long day = time/24/3600;
		long hour = (time/3600)%24;
		if(hour < 21)
			hour = 21;
		else//第二天21点
			hour = 45;
		return day*24*3600+hour*3600;
	}
	
	public ShopList buildBlackShop(){
		String xml = ReadConfig("lol_shopblackblack.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		
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
		this.hputAll(BLACKSHOP, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getBlackShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(BLACKSHOP);
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
		String value = this.hget(BLACKSHOP, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readBlackShopComms();
			return map.get(will).build();
		}
	}

	public int getBlackShopRefreshTime(){
		String value = this.hget(USERDAILYDATA+user.getId(), "BlackShopRefreshTime");
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void saveBlackShopRefreshTime(int time){
		this.setExpireDate(nextDay());
		hput(USERDAILYDATA+user.getId(), "BlackShopRefreshTime", time+"");
	}

	//工会商店
	public ShopList getUnionShop() {
		String value = this.hget(USERDATA+user.getId(), UNIONSHOP);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildUnionShop();
			saveUnionShop(shoplist);
			return shoplist;
		}
	}
	
	public void saveUnionShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), UNIONSHOP, formatJson(shoplist));
	}
	
	public long getUnionShopEndTime(){
		long time = System.currentTimeMillis()/1000;
		long day = time/24/3600;
		long hour = (time/3600)%24;
		if(hour < 21)
			hour = 21;
		else//第二天21点
			hour = 45;
		return day*24*3600+hour*3600;
	}
	
	public ShopList buildUnionShop(){
		String xml = ReadConfig("lol_shopgonghuigonghui.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		
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
		this.hputAll(UNIONSHOP, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getUnionShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(UNIONSHOP);
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
		String value = this.hget(UNIONSHOP, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readUnionShopComms();
			return map.get(will).build();
		}
	}

	public int getUnionShopRefreshTime(){
		String value = this.hget(USERDAILYDATA+user.getId(), "UnionShopRefreshTime");
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void saveUnionShopRefreshTime(int time){
		this.setExpireDate(nextDay());
		hput(USERDAILYDATA+user.getId(), "UnionShopRefreshTime", time+"");
	}

	//魔化商店
	public ShopList getPVPShop() {
		String value = this.hget(USERDATA+user.getId(), PVPSHOP);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildPVPShop();
			savePVPShop(shoplist);
			return shoplist;
		}
	}
	
	public void savePVPShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), PVPSHOP, formatJson(shoplist));
	}
	
	public long getPVPShopEndTime(){
		long time = System.currentTimeMillis()/1000;
		long day = time/24/3600;
		long hour = (time/3600)%24;
		if(hour < 21)
			hour = 21;
		else//第二天21点
			hour = 45;
		return day*24*3600+hour*3600;
	}
	
	public ShopList buildPVPShop(){
		String xml = ReadConfig("lol_shopmohuamohua.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		
		ShopList.Builder builder = buildComms(willsbuilder, getPVPShopComms());
		builder.setEndTime(getPVPShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readPVPShopComms(){
		String xml = ReadConfig("lol_shopmohua.xml");
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
		this.hputAll(PVPSHOP, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getPVPShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(PVPSHOP);
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
		String value = this.hget(PVPSHOP, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readPVPShopComms();
			return map.get(will).build();
		}
	}

	public int getPVPShopRefreshTime(){
		String value = this.hget(USERDAILYDATA+user.getId(), "PVPShopRefreshTime");
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void savePVPShopRefreshTime(int time){
		this.setExpireDate(nextDay());
		hput(USERDAILYDATA+user.getId(), "PVPShopRefreshTime", time+"");
	}

	//魔化商店
	public ShopList getExpeditionShop() {
		String value = this.hget(USERDATA+user.getId(), EXPEDITIONSHOP);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildExpeditionShop();
			saveExpeditionShop(shoplist);
			return shoplist;
		}
	}
	
	public void saveExpeditionShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), EXPEDITIONSHOP, formatJson(shoplist));
	}
	
	public long getExpeditionShopEndTime(){
		long time = System.currentTimeMillis()/1000;
		long day = time/24/3600;
		long hour = (time/3600)%24;
		if(hour < 21)
			hour = 21;
		else//第二天21点
			hour = 45;
		return day*24*3600+hour*3600;
	}
	
	public ShopList buildExpeditionShop(){
		String xml = ReadConfig("lol_shopmohuamohua.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		
		ShopList.Builder builder = buildComms(willsbuilder, getExpeditionShopComms());
		builder.setEndTime(getExpeditionShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readExpeditionShopComms(){
		String xml = ReadConfig("lol_shopmohua.xml");
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
		this.hputAll(EXPEDITIONSHOP, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getExpeditionShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(EXPEDITIONSHOP);
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
		String value = this.hget(EXPEDITIONSHOP, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readExpeditionShopComms();
			return map.get(will).build();
		}
	}

	public int getExpeditionShopRefreshTime(){
		String value = this.hget(USERDAILYDATA+user.getId(), "ExpeditionShopRefreshTime");
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void saveExpeditionShopRefreshTime(int time){
		this.setExpireDate(nextDay());
		hput(USERDAILYDATA+user.getId(), "ExpeditionShopRefreshTime", time+"");
	}

	//天梯商店
	public ShopList getLadderShop() {
		String value = this.hget(USERDATA+user.getId(), LADDERSHOP);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildLadderShop();
			saveLadderShop(shoplist);
			return shoplist;
		}
	}
	
	public void saveLadderShop(ShopList shoplist) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), LADDERSHOP, formatJson(shoplist));
	}
	
	public long getLadderShopEndTime(){
		long time = System.currentTimeMillis()/1000;
		long day = time/24/3600;
		long hour = (time/3600)%24;
		if(hour < 21)
			hour = 21;
		else//第二天21点
			hour = 45;
		return day*24*3600+hour*3600;
	}
	
	public ShopList buildLadderShop(){
		String xml = ReadConfig("lol_shoptiantitianti.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		
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
		this.hputAll(LADDERSHOP, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getLadderShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(LADDERSHOP);
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
		String value = this.hget(LADDERSHOP, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readLadderShopComms();
			return map.get(will).build();
		}
	}

	public int getLadderShopRefreshTime(){
		String value = this.hget(USERDAILYDATA+user.getId(), "LadderShopRefreshTime");
		if(value != null)
			return Integer.parseInt(value);
		else
			return 0;
	}

	public void saveLadderShopRefreshTime(int time){
		this.setExpireDate(nextDay());
		hput(USERDAILYDATA+user.getId(), "LadderShopRefreshTime", time+"");
	}

	//商城
	public Commodity getShop(int id) {
		String value = this.hget(SHOP, id+"");
		Commodity.Builder builder = Commodity.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		return null;
	}

	public ShopList getShop() {
		Map<String, String> keyvalue = this.hget(SHOP);
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
			this.hputAll(SHOP, map);
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

	public void setUser(UserBean user) {
		this.user = user;
	}
}
