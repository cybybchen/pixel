package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBattletowerBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipPokedeBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.EquipProto.Synthetise;
import com.trans.pixel.protoc.RechargeProto.VipLibao;
import com.trans.pixel.protoc.RechargeProto.VipLibaoList;
import com.trans.pixel.protoc.ShopProto.Commodity;
import com.trans.pixel.protoc.ShopProto.CommodityList;
import com.trans.pixel.protoc.ShopProto.ContractReward;
import com.trans.pixel.protoc.ShopProto.ContractRewardList;
import com.trans.pixel.protoc.ShopProto.ContractWeight;
import com.trans.pixel.protoc.ShopProto.ContractWeightList;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.ShopProto.LibaoList;
import com.trans.pixel.protoc.ShopProto.PurchaseCoinCostList;
import com.trans.pixel.protoc.ShopProto.PurchaseCoinReward;
import com.trans.pixel.protoc.ShopProto.PurchaseCoinRewardList;
import com.trans.pixel.protoc.ShopProto.ShopList;
import com.trans.pixel.protoc.ShopProto.ShopWill;
import com.trans.pixel.protoc.ShopProto.ShopWillList;
import com.trans.pixel.protoc.ShopProto.Will;
import com.trans.pixel.service.UserEquipPokedeService;
import com.trans.pixel.service.UserTalentService;
import com.trans.pixel.service.cache.CacheService;

@Repository
public class ShopRedisService extends RedisService{
	Logger logger = Logger.getLogger(ShopRedisService.class);
	private final static String USERDATA = RedisKey.PREFIX+RedisKey.USERDATA_PREFIX;
	@Resource
	private PropRedisService propRedisService;
	@Resource
	private UserEquipPokedeService userEquipPokedeService;
	@Resource
	private UserTalentService userTalentService;
	
	public ShopRedisService() {
		// buildExpeditionShopConfig();
		// readExpeditionShopComms();
		// buildBattletowerShopConfig();
		// readBattletowerShopComms();
		// buildPurchaseCoinCostList();
		// readUnionShopConfig();
		// readUnionShopCommsConfig();
		buildDailyShopConfig();
		readDailyShopComms();
		buildBlackShopConfig();
		buildPVPShopConfig();
		buildLadderShopConfig();
		buildLibaoShop();
		buildVipLibao();
		// buildPurchaseCoinReward();
//		readRaidShopConfig();
		// buildContractRewardList();
		// buildContractConfig();
	}
	
	//普通商店
	public ShopList getDailyShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "DAILYSHOP", user.getId());
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
			this.hput(USERDATA+user.getId(), "DAILYSHOP", formatJson(shoplist), user.getId());
	}
	
	public ShopList.Builder buildComms(UserBean user, ShopWill shopwill, Map<Integer, CommodityList> commsmap){
		ShopList.Builder builder = ShopList.newBuilder();
		if(shopwill!=null)
		for(Will will : shopwill.getWillList()){
			CommodityList.Builder commsbuilder = CommodityList.newBuilder(commsmap.get(will.getWill()));
			if(will.getWill() >= 100){
				for(int i = commsbuilder.getDataCount() - 1; i >= 0; i--) {
					int itemid = commsbuilder.getData(i).getItemid();
					if(itemid/1000*1000 == RewardConst.SYNTHETISE) {
						Synthetise synthetise = propRedisService.getSynthetise(itemid);
						itemid = synthetise.getTargetid();
					}
					if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
						UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
						if(bean != null)
							commsbuilder.removeData(i);
					}
				}
				if(commsbuilder.getDataCount() == 0)
					commsbuilder = CommodityList.newBuilder(commsmap.get(will.getWill()-100));
			}
			int index = nextInt(commsbuilder.getDataCount());
			builder.addItems(commsbuilder.getDataBuilder(index));
		}
		return builder;
	}
	
//	//征战世界
//	public ShopList.Builder buildPVPComms(ShopWillList.Builder shopwillsbuilder, Map<Integer, CommodityList.Builder> commsmap, UserBean user){
//		ShopList.Builder builder = ShopList.newBuilder();
//		for(ShopWill shopwill : shopwillsbuilder.getShopList()){
//			if (shopwill.getJudge1() == user.getPvpUnlock()) {
//				for(Will will : shopwill.getLootList()){
//					CommodityList.Builder commsbuilder = commsmap.get(will.getWill());
//					int index = nextInt(commsbuilder.getItemCount());
//					Commodity.Builder comm = commsbuilder.getItemBuilder(index);
//	//				if(shopwill.hasJudge1())
//	//					comm.setJudge(shopwill.getJudge1());
//					builder.addItems(comm);
//				}
//				break;
//			}
//		}
//		return builder;
//	}
//	
//	//镜像大陆
//	public ShopList.Builder buildAreaComms(ShopWillList.Builder shopwillsbuilder, Map<Integer, CommodityList.Builder> commsmap, UserBean user){
//		ShopList.Builder builder = ShopList.newBuilder();
//		for(ShopWill shopwill : shopwillsbuilder.getShopList()){
//			if (shopwill.getJudge1() == user.getAreaUnlock()) {
//				for(Will will : shopwill.getLootList()){
//					CommodityList.Builder commsbuilder = commsmap.get(will.getWill());
//					int index = nextInt(commsbuilder.getItemCount());
//					Commodity.Builder comm = commsbuilder.getItemBuilder(index);
//	//				if(shopwill.hasJudge1())
//	//					comm.setJudge(shopwill.getJudge1());
//					builder.addItems(comm);
//				}
//				break;
//			}
//		}
//		return builder;
//	}
//	
//	public ShopList.Builder buildLadderComms(ShopWillList.Builder shopwillsbuilder, Map<Integer, CommodityList.Builder> commsmap, UserBean user){
//		ShopList.Builder builder = ShopList.newBuilder();
//		ShopWill.Builder shop = shopwillsbuilder.getShopBuilder(0);
//		for(ShopWill shopwill : shopwillsbuilder.getShopList()){
//			if (shop.getJudge1() > shopwill.getJudge1() && shopwill.getJudge1() > user.getLadderModeHistoryTop())
//				shop = ShopWill.newBuilder(shopwill);	
//		}
//		
//		for(Will will : shop.getLootList()){
//			CommodityList.Builder commsbuilder = commsmap.get(will.getWill());
//			int index = nextInt(commsbuilder.getItemCount());
//			Commodity.Builder comm = commsbuilder.getItemBuilder(index);
////			if(shop.hasJudge1())
////				comm.setJudge(shop.getJudge1());
//			builder.addItems(comm);
//		}
//		return builder;
//	}
//	
//	public ShopList.Builder buildBattletowerComms(ShopWillList.Builder shopwillsbuilder, Map<Integer, CommodityList.Builder> commsmap, UserBattletowerBean ubt){
//		ShopList.Builder builder = ShopList.newBuilder();
//		ShopWill.Builder shop = shopwillsbuilder.getShopBuilder(0);
//		for(ShopWill shopwill : shopwillsbuilder.getShopList()){
//			if (shop.getJudge1() > shopwill.getJudge1() && shopwill.getJudge1() <= ubt.getToptower())
//				shop = ShopWill.newBuilder(shopwill);	
//		}
//		
//		for(Will will : shop.getLootList()){
//			CommodityList.Builder commsbuilder = commsmap.get(will.getWill());
//			int index = nextInt(commsbuilder.getItemCount());
//			Commodity.Builder comm = commsbuilder.getItemBuilder(index);
////			if(shop.hasJudge1())
////				comm.setJudge(shop.getJudge1());
//			builder.addItems(comm);
//		}
//		return builder;
//	}
	
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
	
	public ShopWillList.Builder getDailyShopConfig() {
		ShopWillList wills = CacheService.getcache(RedisKey.DAILYSHOP_CONFIG+"Type");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder(wills);
		return willsbuilder;
	}
	
	public ShopWillList.Builder buildDailyShopConfig() {
		String xml = ReadConfig("ld_shopputong2.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		CacheService.setcache(RedisKey.DAILYSHOP_CONFIG+"Type", willsbuilder.build());
		
		return willsbuilder;
	}
	
	public ShopList buildDailyShop(UserBean user){
		ShopWillList.Builder willsbuilder = getDailyShopConfig();
		ShopWill shopwill = willsbuilder.getData(0);
		for(ShopWill will : willsbuilder.getDataList()){
			if(will.getMerlevel() <= user.getMerlevel())
				shopwill = will;
		}
		ShopList.Builder builder = buildComms(user, shopwill, getDailyShopComms());
		
//		if(user.getVip() >= 5){
//			List<Integer> list = new ArrayList<Integer>();
//			int index = nextInt(builder.getItemsCount());
//			list.add(index);
//			Commodity.Builder comm = builder.getItemsBuilderList().get(index);
//			comm.setDiscount(90);
//			comm.setDiscost(comm.getCost()*90/100);
//			while(list.contains(index))
//				index = nextInt(builder.getItemsCount());
//			list.add(index);
//			comm = builder.getItemsBuilderList().get(index);
//			comm.setDiscount(90);
//			comm.setDiscost(comm.getCost()*90/100);
//			if(user.getVip() >= 14){
//				while(list.contains(index))
//					index = nextInt(builder.getItemsCount());
//				list.add(index);
//				comm = builder.getItemsBuilderList().get(index);
//				comm.setDiscount(70);
//				comm.setDiscost(comm.getCost()*70/100);
//				while(list.contains(index))
//					index = nextInt(builder.getItemsCount());
//				list.add(index);
//				comm = builder.getItemsBuilderList().get(index);
//				comm.setDiscount(70);
//				comm.setDiscost(comm.getCost()*70/100);
//			}
//		}
		builder.setEndTime(getDailyShopEndTime());
		return builder.build();
	}

	private Map<Integer, CommodityList> readDailyShopComms(){
		String xml = ReadConfig("ld_shopputong.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList> map = new HashMap<Integer, CommodityList>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList list = map.get(comm.getWill());
			CommodityList.Builder comms;
			if(list == null){
				comms = CommodityList.newBuilder();
			}else{
				comms = CommodityList.newBuilder(list);
			}
			comm.clearName();
			comms.addData(comm);
			map.put(comm.getWill(), comms.build());
		}
		CacheService.hputcacheAll(RedisKey.DAILYSHOP_CONFIG, map);
		
		return map;
	}
	
	public Map<Integer, CommodityList> getDailyShopComms(){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.DAILYSHOP_CONFIG);
		return map;
	}
	
	public CommodityList getDailyShopComms(int will){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.DAILYSHOP_CONFIG);
		return map.get(will);
	}

	//黑市
	public ShopList.Builder buildBlackShopConfig() {
		String xml = ReadConfig("ld_shopshenmi.xml");
		ShopList.Builder builder = ShopList.newBuilder();
		CommodityList.Builder commodities = CommodityList.newBuilder();
		parseXml(xml, commodities);
		builder.addAllItems(commodities.getDataList());
		for(int index = builder.getItemsCount()-1; index >= 0;index--) {
			Commodity.Builder commbuilder = builder.getItemsBuilder(index);
			if(commbuilder.getLimit() > 0) {
				commbuilder.setMaxlimit(commbuilder.getLimit());
				commbuilder.setLimit(0);
			}
		}
		CacheService.setcache(RedisKey.SHENMISHOP_CONFIG, builder.build());
		
		return builder;
	}
	
	public ShopList.Builder getBlackShop(UserBean user) {
		ShopList list = CacheService.getcache(RedisKey.SHENMISHOP_CONFIG);
		ShopList.Builder builder = ShopList.newBuilder(list);
		
		String value = this.hget(USERDATA+user.getId(), "BLACKSHOP", user.getId());
		ShopList.Builder mybuilder = ShopList.newBuilder();
		if(value != null) {
			parseJson(value, mybuilder);
			builder.setEndTime(mybuilder.getEndTime());
		}
		for(int index = builder.getItemsCount()-1; index >= 0;index--) {
			Commodity.Builder comm = builder.getItemsBuilder(index);
			if(comm.getWeekbuy() == 1) {
				for(Commodity mycomm : mybuilder.getItemsList()) {
					if(mycomm.getPosition() == comm.getPosition()) {
						comm.setLimit(mycomm.getLimit());
					}
				}
			}else if(comm.getPosition() == 3 && user.getFriendVip() == 1){
				builder.removeItems(index);
			}
			comm.setIsOut(comm.getLimit() == comm.getMaxlimit());
		}
		if(!builder.hasEndTime())
			builder.setEndTime(nextWeek(0));
		return builder;
	}
//	public ShopList getBlackShop(UserBean user) {
//		String value = this.hget(USERDATA+user.getId(), "BLACKSHOP");
//		ShopList.Builder builder = ShopList.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder.build();
//		}else{
//			ShopList shoplist = buildBlackShop(user);
//			saveBlackShop(shoplist, user);
//			return shoplist;
//		}
//	}

//	public int getBlackShopRefreshCost(int time){
////		String value = get(RedisKey.BLACKSHOPCOST_CONFIG);
////		ShopRefreshList.Builder builder = ShopRefreshList.newBuilder();
////		if(value != null && parseJson(value, builder)){
////		}else{
////			String xml = ReadConfig("ld_shopshuaxin.xml");
////			parseXml(xml, builder);
////			set(RedisKey.BLACKSHOPCOST_CONFIG, formatJson(builder.build()));
////		}
////		for(ShopRefresh refresh : builder.getIdList()){
////			if(time >= refresh.getCount()-1 && (time < refresh.getCount1() || refresh.getCount1()<0))
////				return refresh.getCost();
////		}
//		return 500;
//	}
	
	public void saveBlackShop(ShopList shoplist, UserBean user) {
		ShopList.Builder builder = ShopList.newBuilder(shoplist);
		for(int index = builder.getItemsCount()-1; index >= 0; index--) {
			if(builder.getItems(index).getWeekbuy() == 0)
				builder.removeItems(index);
		}
		if(builder.getItemsCount() > 0){
			this.hput(USERDATA+user.getId(), "BLACKSHOP", formatJson(builder.build()), user.getId());
		}
	}
	
	public void deleteBlackShop(UserBean user) {
		hdelete(USERDATA+user.getId(), "BLACKSHOP", user.getId());
	}
	
	public long getBlackShopEndTime(){
//		long times[] = {today(0), today(3), today(6), today(9), today(12), today(15), today(18), today(21)};
		return nextWeek(0);
//		long time = today(21);
//		if(now() < time)
//			return time;
//		else//第二天21点
//			return time+24*3600;
	}
	
//	public ShopList buildBlackShop(UserBean user){
//		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
//		String value = get(RedisKey.BLACKSHOP_CONFIG+"Type");
//		if(value == null || !parseJson(value, willsbuilder)){
//			String xml = ReadConfig("ld_shopshenmi2.xml");
//			parseXml(xml, willsbuilder);
//			set(RedisKey.BLACKSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
//		}
//		ShopWill shopwill = willsbuilder.getData(0);
//		for(ShopWill will : willsbuilder.getDataList()){
//			if(will.getMerlevel() <= user.getMerlevel())
//				shopwill = will;
//		}
//		ShopList.Builder builder = buildComms(user, shopwill, getBlackShopComms());
////		if(user.getVip() >= 12){
////			List<Integer> list = new ArrayList<Integer>();
////			int index = nextInt(builder.getItemsCount());
////			list.add(index);
////			Commodity.Builder comm = builder.getItemsBuilderList().get(index);
////			comm.setDiscount(90);
////			comm.setDiscost(comm.getCost()*90/100);
////			while(list.contains(index))
////				index = nextInt(builder.getItemsCount());
////			list.add(index);
////			comm = builder.getItemsBuilderList().get(index);
////			comm.setDiscount(90);
////			comm.setDiscost(comm.getCost()*90/100);
////		}
//		builder.setEndTime(getBlackShopEndTime());
//		return builder.build();
//	}

//	public Map<Integer, CommodityList.Builder> readBlackShopComms(){
//		String xml = ReadConfig("ld_shopshenmi.xml");
//		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
//		parseXml(xml, commsbuilder);
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
//			CommodityList.Builder comms = map.get(comm.getWill());
//			if(comms == null){
//				comms = CommodityList.newBuilder();
//			}
//			comm.clearName();
//			comms.addData(comm);
//			map.put(comm.getWill(), comms);
//		}
//		Map<String, String> resultmap = new HashMap<String, String>();
//		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
//			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
//		}
//		this.hputAll(RedisKey.BLACKSHOP_CONFIG, resultmap);
//		return map;
//	}
	
//	public Map<Integer, CommodityList.Builder> getBlackShopComms(){
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		Map<String, String> keyvalue = this.hget(RedisKey.BLACKSHOP_CONFIG);
//		if(keyvalue.isEmpty()){
//			return readBlackShopComms();
//		}else{
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				CommodityList.Builder builder = CommodityList.newBuilder();
//				parseJson(entry.getValue(), builder);
//				map.put(Integer.parseInt(entry.getKey()), builder);
//			}
//			return map;
//		}
//	}
	
//	public CommodityList getBlackShopComms(int will){
//		String value = this.hget(RedisKey.BLACKSHOP_CONFIG, will+"");
//		CommodityList.Builder builder = CommodityList.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder.build();
//		}else{
//			Map<Integer, CommodityList.Builder> map = readBlackShopComms();
//			return map.get(will).build();
//		}
//	}

	//公会商店
	public ShopList getUnionShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "UNIONSHOP", user.getId());
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildUnionShop(user);
			saveUnionShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveUnionShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "UNIONSHOP", formatJson(shoplist), user.getId());
	}
	
	public long getUnionShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildUnionShop(UserBean user){
		ShopWillList wills = CacheService.getcache(RedisKey.UNIONSHOP_CONFIG+"Type");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder(wills);
		
		ShopList.Builder builder = ShopList.newBuilder();//buildAreaComms(willsbuilder, getUnionShopComms(), user);
		builder.setEndTime(getUnionShopEndTime());
		return builder.build();
	}
	
	private ShopWillList readUnionShopConfig() {
		String xml = ReadConfig("lol_shopgonghuishopgonghui.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		CacheService.setcache(RedisKey.UNIONSHOP_CONFIG+"Type", willsbuilder.build());
		return willsbuilder.build();
	}

	private Map<Integer, CommodityList> readUnionShopCommsConfig(){
		String xml = ReadConfig("lol_shopgonghui.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList> map = new HashMap<Integer, CommodityList>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList list = map.get(comm.getWill());
			CommodityList.Builder comms;
			if(list == null){
				comms = CommodityList.newBuilder();
			}else {
				comms = CommodityList.newBuilder(list);
			}
			comm.clearName();
			comms.addData(comm);
			map.put(comm.getWill(), comms.build());
		}
		CacheService.hputcacheAll(RedisKey.UNIONSHOP_CONFIG, map);
		
		return map;
	}
	
	public Map<Integer, CommodityList> getUnionShopComms(){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.UNIONSHOP_CONFIG);
		return map;
	}
	
	public CommodityList getUnionShopComms(int will){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.UNIONSHOP_CONFIG);
		return map.get(will);
	}

	//挂机副本商店
	public ShopList getRaidShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "RAIDSHOP", user.getId());
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildRaidShop(user);
			saveRaidShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveRaidShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "RAIDSHOP", formatJson(shoplist), user.getId());
	}
	
	public long getRaidShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildRaidShop(UserBean user){
		CommodityList comms = CacheService.getcache(RedisKey.RAIDSHOP_CONFIG);
		CommodityList.Builder commsbuilder = CommodityList.newBuilder(comms);
		ShopList.Builder builder = ShopList.newBuilder();
		builder.addAllItems(commsbuilder.getDataList());
		builder.setEndTime(getRaidShopEndTime());
		return builder.build();
	}
	
	private CommodityList readRaidShopConfig() {
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		String xml = ReadConfig("ld_shopraid3.xml");
		parseXml(xml, commsbuilder);
		CacheService.setcache(RedisKey.RAIDSHOP_CONFIG, commsbuilder.build());
		
		return commsbuilder.build();
	}

//	public Map<Integer, CommodityList.Builder> readRaidShopComms(){
//		String xml = ReadConfig("ld_shopraid3.xml");
//		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
//		parseXml(xml, commsbuilder);
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		for(Commodity.Builder comm : commsbuilder.getIdBuilderList()){
//			CommodityList.Builder comms = map.get(comm.getWill());
//			if(comms == null){
//				comms = CommodityList.newBuilder();
//			}
//			comm.clearName();
//			comms.addId(comm);
//			map.put(comm.getWill(), comms);
//		}
//		Map<String, String> resultmap = new HashMap<String, String>();
//		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
//			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
//		}
//		this.hputAll(RedisKey.RAIDSHOP_CONFIG, resultmap);
//		return map;
//	}
//	
//	public Map<Integer, CommodityList.Builder> getRaidShopComms(){
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		Map<String, String> keyvalue = this.hget(RedisKey.RAIDSHOP_CONFIG);
//		if(keyvalue.isEmpty()){
//			return readRaidShopComms();
//		}else{
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				CommodityList.Builder builder = CommodityList.newBuilder();
//				parseJson(entry.getValue(), builder);
//				map.put(Integer.parseInt(entry.getKey()), builder);
//			}
//			return map;
//		}
//	}
//	
//	public CommodityList getRaidShopComms(int will){
//		String value = this.hget(RedisKey.RAIDSHOP_CONFIG, will+"");
//		CommodityList.Builder builder = CommodityList.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder.build();
//		}else{
//			Map<Integer, CommodityList.Builder> map = readRaidShopComms();
//			return map.get(will).build();
//		}
//	}

	//挂机PVP商店
	public ShopList getPVPShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "PVPSHOP", user.getId());
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildPVPShop(user);
			savePVPShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void savePVPShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "PVPSHOP", formatJson(shoplist), user.getId());
	}
	
	public long getPVPShopEndTime(){
		return nextWeek(0);
//		long time[] = {today(0), today(12), today(18), today(21)};
//		long now = now();
//		if(now < time[0])
//			return time[0];
//		else if(now < time[1])
//			return time[1];
//		else if(now < time[2])
//			return time[2];
//		else if(now < time[3])
//			return time[3];
//		else//第二天9点
//			return time[0]+24*3600;
	}

	public CommodityList.Builder getPVPShopConfig() {
		CommodityList list = CacheService.getcache(RedisKey.PVPSHOP_CONFIG);
		CommodityList.Builder builder = CommodityList.newBuilder(list);
		return builder;
	}
	
	private CommodityList.Builder buildPVPShopConfig() {
		String xml = ReadConfig("ld_shopmojing.xml");
		CommodityList.Builder builder = CommodityList.newBuilder();
		parseXml(xml, builder);
		CacheService.setcache(RedisKey.PVPSHOP_CONFIG, builder.build());
		
		return builder;
	}
	
	public ShopList buildPVPShop(UserBean user){
		CommodityList.Builder list = getPVPShopConfig();
		ShopList.Builder builder = ShopList.newBuilder();
		builder.addAllItems(list.getDataList());
		for(int i = builder.getItemsCount()-1; i >= 0; i--) {
			Commodity.Builder commbuilder = builder.getItemsBuilder(i);
			commbuilder.setMaxlimit(commbuilder.getLimit());
			commbuilder.setLimit(0);
			if(commbuilder.getCount() == 1) {
				int itemid = commbuilder.getItemid();
				if(itemid/1000*1000 == RewardConst.SYNTHETISE) {
					Synthetise synthetise = propRedisService.getSynthetise(itemid);
					itemid = synthetise.getTargetid();
				}
				if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
					UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
					if(bean != null) {
						builder.removeItems(i);
//						commbuilder.setIsOut(true);
					}
				}else if(itemid > 0 && itemid < 100) {
					if(userTalentService.getUserTalent(user, itemid) != null){
						builder.removeItems(i);
//						commbuilder.setIsOut(true);
					}
				}
			}
//			if(commbuilder.getPosition() == 100) {
//				commbuilder.setMaxlimit(commbuilder.getMaxlimit()+user.getShopchipboxTime());
//				if(commbuilder.getMaxlimit() == 0)
//					builder.removeItems(i);
//			}
		}
		builder.setEndTime(getPVPShopEndTime());
		return builder.build();
	}
	
//	public ShopList buildPVPShop(UserBean user){
//		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
//		String value = get(RedisKey.PVPSHOP_CONFIG+"Type");
//		if(value == null || !parseJson(value, willsbuilder)){
//			String xml = ReadConfig("ld_shopmojing2.xml");
//			parseXml(xml, willsbuilder);
//			set(RedisKey.PVPSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
//		}
//		ShopWill shopwill = willsbuilder.getId(0);
//		for(ShopWill will : willsbuilder.getIdList()){
//			if(will.getMerlevel() <= user.getMerlevel())
//				shopwill = will;
//		}
//		ShopList.Builder builder = buildComms(user, shopwill, getPVPShopComms());
//		builder.setEndTime(getPVPShopEndTime());
//		return builder.build();
//	}
//
//	public Map<Integer, CommodityList.Builder> readPVPShopComms(){
//		String xml = ReadConfig("ld_shopmojing.xml");
//		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
//		parseXml(xml, commsbuilder);
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		for(Commodity.Builder comm : commsbuilder.getIdBuilderList()){
//			CommodityList.Builder comms = map.get(comm.getWill());
//			if(comms == null){
//				comms = CommodityList.newBuilder();
//			}
//			comm.clearName();
//			comms.addId(comm);
//			map.put(comm.getWill(), comms);
//		}
//		Map<String, String> resultmap = new HashMap<String, String>();
//		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
//			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
//		}
//		this.hputAll(RedisKey.PVPSHOP_CONFIG, resultmap);
//		return map;
//	}
//	
//	public Map<Integer, CommodityList.Builder> getPVPShopComms(){
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		Map<String, String> keyvalue = this.hget(RedisKey.PVPSHOP_CONFIG);
//		if(keyvalue.isEmpty()){
//			return readPVPShopComms();
//		}else{
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				CommodityList.Builder builder = CommodityList.newBuilder();
//				parseJson(entry.getValue(), builder);
//				map.put(Integer.parseInt(entry.getKey()), builder);
//			}
//			return map;
//		}
//	}
//	
//	public CommodityList getPVPShopComms(int will){
//		String value = this.hget(RedisKey.PVPSHOP_CONFIG, will+"");
//		CommodityList.Builder builder = CommodityList.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder.build();
//		}else{
//			Map<Integer, CommodityList.Builder> map = readPVPShopComms();
//			return map.get(will).build();
//		}
//	}

	//远征商店
	public ShopList getExpeditionShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "EXPEDITIONSHOP", user.getId());
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = getExpeditionShop();
			saveExpeditionShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveExpeditionShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "EXPEDITIONSHOP", formatJson(shoplist), user.getId());
	}
	
	public long getExpeditionShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList getExpeditionShop(){
		ShopWillList wills = CacheService.getcache(RedisKey.EXPEDITIONSHOP_CONFIG+"Type");
		ShopList.Builder builder = ShopList.newBuilder();//buildComms(willsbuilder, getExpeditionShopComms());
		builder.setEndTime(getExpeditionShopEndTime());
		return builder.build();
	}

	private ShopWillList buildExpeditionShopConfig(){
		String xml = ReadConfig("lol_shopyuanzhengshopyuanzheng.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		CacheService.setcache(RedisKey.EXPEDITIONSHOP_CONFIG+"Type", willsbuilder.build());
		
		return willsbuilder.build();
	}

	private Map<Integer, CommodityList> readExpeditionShopComms(){
		String xml = ReadConfig("lol_shopyuanzheng.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList> map = new HashMap<Integer, CommodityList>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList list = map.get(comm.getWill());
			CommodityList.Builder comms;
			if(list == null){
				comms = CommodityList.newBuilder();
			}else {
				comms = CommodityList.newBuilder(list);
			}
			comm.clearName();
			comms.addData(comm);
			map.put(comm.getWill(), comms.build());
		}
		CacheService.hputcacheAll(RedisKey.EXPEDITIONSHOP_CONFIG, map);
		
		return map;
	}
	
	public Map<Integer, CommodityList> getExpeditionShopComms(){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.EXPEDITIONSHOP_CONFIG);
		return map;
	}
	
	public CommodityList getExpeditionShopComms(int will){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.EXPEDITIONSHOP_CONFIG);
		return map.get(will);
	}

	//天梯商店
	public ShopList getLadderShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "LADDERSHOP", user.getId());
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildLadderShop(user);
			saveLadderShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveLadderShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "LADDERSHOP", formatJson(shoplist), user.getId());
	}
	
	public long getLadderShopEndTime(){
		return nextWeek(0);
//		long time[] = {today(0), today(12), today(18), today(21)};
//		long now = now();
//		if(now < time[0])
//			return time[0];
//		else if(now < time[1])
//			return time[1];
//		else if(now < time[2])
//			return time[2];
//		else if(now < time[3])
//			return time[3];
//		else//第二天9点
//			return time[0]+24*3600;
	}

	public CommodityList.Builder getLadderShopConfig() {
		CommodityList list = CacheService.getcache(RedisKey.LADDERSHOP_CONFIG);
		CommodityList.Builder builder = CommodityList.newBuilder(list);
		return builder;
	}

	private CommodityList.Builder buildLadderShopConfig() {
		String xml = ReadConfig("ld_shoptianti.xml");
		CommodityList.Builder builder = CommodityList.newBuilder();
		parseXml(xml, builder);
		CacheService.setcache(RedisKey.LADDERSHOP_CONFIG, builder.build());
		
		return builder;
	}
	
	public ShopList buildLadderShop(UserBean user){
		CommodityList.Builder list = getLadderShopConfig();
		ShopList.Builder builder = ShopList.newBuilder();
		builder.addAllItems(list.getDataList());
		builder.setEndTime(getLadderShopEndTime());
		for(int i = builder.getItemsCount()-1; i >= 0; i--) {
			Commodity.Builder commbuilder = builder.getItemsBuilder(i);
			commbuilder.setMaxlimit(commbuilder.getLimit());
			commbuilder.setLimit(0);
			if(commbuilder.getCount() == 1) {
				int itemid = commbuilder.getItemid();
				if(itemid/1000*1000 == RewardConst.SYNTHETISE) {
					Synthetise synthetise = propRedisService.getSynthetise(itemid);
					itemid = synthetise.getTargetid();
				}
				if(itemid/10000*10000 == RewardConst.EQUIPMENT) {
					UserEquipPokedeBean bean = userEquipPokedeService.selectUserEquipPokede(user, itemid);
					if(bean != null) {
						builder.removeItems(i);
//						commbuilder.setIsOut(true);
					}
				}else if(itemid > 0 && itemid < 100) {
					if(userTalentService.getUserTalent(user, itemid) != null){
						builder.removeItems(i);
//						commbuilder.setIsOut(true);
					}
				}
//				if(commbuilder.getPosition() == 100) {
//					commbuilder.setMaxlimit(commbuilder.getMaxlimit()+user.getShopbaohuTime());
//					if(commbuilder.getMaxlimit() == 0)
//						builder.removeItems(i);
//				}
			}
		}
		return builder.build();
	}
	 
//	public ShopList buildLadderShop(UserBean user){
//		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
//		String value = get(RedisKey.LADDERSHOP_CONFIG+"Type");
//		if(value == null || !parseJson(value, willsbuilder)){
//			String xml = ReadConfig("ld_shoptianti2.xml");
//			parseXml(xml, willsbuilder);
//			set(RedisKey.LADDERSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
//		}
//		ShopWill shopwill = willsbuilder.getId(0);
//		for(ShopWill will : willsbuilder.getIdList()){
//			if(will.getMerlevel() <= user.getMerlevel())
//				shopwill = will;
//		}
//		ShopList.Builder builder = buildComms(user, shopwill, getLadderShopComms());
//		builder.setEndTime(getLadderShopEndTime());
//		return builder.build();
//	}
//
//	public Map<Integer, CommodityList.Builder> readLadderShopComms(){
//		String xml = ReadConfig("ld_shoptianti.xml");
//		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
//		parseXml(xml, commsbuilder);
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		for(Commodity.Builder comm : commsbuilder.getIdBuilderList()){
//			CommodityList.Builder comms = map.get(comm.getWill());
//			if(comms == null){
//				comms = CommodityList.newBuilder();
//			}
//			comm.clearName();
//			comms.addId(comm);
//			map.put(comm.getWill(), comms);
//		}
//		Map<String, String> resultmap = new HashMap<String, String>();
//		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
//			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
//		}
//		this.hputAll(RedisKey.LADDERSHOP_CONFIG, resultmap);
//		return map;
//	}
//	
//	public Map<Integer, CommodityList.Builder> getLadderShopComms(){
//		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
//		Map<String, String> keyvalue = this.hget(RedisKey.LADDERSHOP_CONFIG);
//		if(keyvalue.isEmpty()){
//			return readLadderShopComms();
//		}else{
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				CommodityList.Builder builder = CommodityList.newBuilder();
//				parseJson(entry.getValue(), builder);
//				map.put(Integer.parseInt(entry.getKey()), builder);
//			}
//			return map;
//		}
//	}
//	
//	public CommodityList getLadderShopComms(int will){
//		String value = this.hget(RedisKey.LADDERSHOP_CONFIG, will+"");
//		CommodityList.Builder builder = CommodityList.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder.build();
//		}else{
//			Map<Integer, CommodityList.Builder> map = readLadderShopComms();
//			return map.get(will).build();
//		}
//	}

	//战斗塔商店
	public ShopList getBattletowerShop(UserBean user, UserBattletowerBean ubt) {
		String value = this.hget(USERDATA+user.getId(), "BATTLETOWERSHOP", user.getId());
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = getBattletowerShopConfig(ubt);
			saveBattletowerShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveBattletowerShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "BATTLETOWERSHOP", formatJson(shoplist), user.getId());
	}
	
	public long getBattletowerShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	 
	public ShopList getBattletowerShopConfig(UserBattletowerBean ubt){
		ShopWillList wills = CacheService.getcache(RedisKey.BATTLETOWERSHOP_CONFIG+"Type");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder(wills);
		
		ShopList.Builder builder = ShopList.newBuilder();//buildBattletowerComms(willsbuilder, getBattletowerShopComms(), ubt);
		builder.setEndTime(getBattletowerShopEndTime());
		return builder.build();
	}
	
	private void buildBattletowerShopConfig(){
		String xml = ReadConfig("lol_shoptowershoptower.xml");
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		parseXml(xml, willsbuilder);
		CacheService.setcache(RedisKey.BATTLETOWERSHOP_CONFIG+"Type", willsbuilder.build());
	}

	public Map<Integer, CommodityList> readBattletowerShopComms(){
		String xml = ReadConfig("lol_shoptower.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList> map = new HashMap<Integer, CommodityList>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList list = map.get(comm.getWill());
			CommodityList.Builder comms;
			if(list == null){
				comms = CommodityList.newBuilder();
			}else{
				comms = CommodityList.newBuilder(list);
			}
			comm.clearName();
			comms.addData(comm);
			map.put(comm.getWill(), comms.build());
		}
		CacheService.hputcacheAll(RedisKey.BATTLETOWERSHOP_CONFIG, map);
		
		return map;
	}
	
	public Map<Integer, CommodityList> getBattletowerShopComms(){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.BATTLETOWERSHOP_CONFIG);
		return map;
	}
	
	public CommodityList getBattletowerShopComms(int will){
		Map<Integer, CommodityList> map = CacheService.hgetcache(RedisKey.BATTLETOWERSHOP_CONFIG);
		return map.get(will);
	}
	
	//libao
	public LibaoList.Builder getLibaoShop() {
		LibaoList list = CacheService.getcache(RedisKey.LIBAOSHOP_CONFIG);
		LibaoList.Builder shopbuilder = LibaoList.newBuilder(list);
		return shopbuilder;
	}
	
	private LibaoList buildLibaoShop(){
		LibaoList.Builder itemsbuilder = LibaoList.newBuilder();
		String xml = ReadConfig("ld_shoplibao.xml");
		parseXml(xml, itemsbuilder);
		for(Libao.Builder libao : itemsbuilder.getDataBuilderList()) {
			libao.setMaxlimit(libao.getPurchase());
			libao.setPurchase(0);
		}
		CacheService.setcache(RedisKey.LIBAOSHOP_CONFIG, itemsbuilder.build());
		
		return itemsbuilder.build();
	}

	//商城
	public Commodity getShop(int id) {
		Map<Integer, Commodity> map = CacheService.hgetcache(RedisKey.SHOP_CONFIG);
		return map.get(id);
	}

	public ShopList getShop() {
//		Map<String, String> keyvalue = this.hget(RedisKey.SHOP_CONFIG);
//		if(keyvalue.isEmpty()){
//			return buildShop();
//		}
		ShopList.Builder shopbuilder = ShopList.newBuilder();
//		for(String value : keyvalue.values()){
//			Commodity.Builder builder = Commodity.newBuilder();
//			if(parseJson(value, builder)){
//				shopbuilder.addItems(builder);
//			}
//		}
		return shopbuilder.build();
	}
	
//	private void saveShop(ShopList shoplist) {
//		if(shoplist.getItemsCount() > 0){
//			Map<String, String> map = new HashMap<String, String>();
//			for(Commodity comm : shoplist.getItemsList())
//				map.put(comm.getId()+"", formatJson(comm));
//			this.hputAll(RedisKey.SHOP_CONFIG, map);
//		}
//	}
	
//	public ShopList buildShop(){
//		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
//		String xml = ReadConfig("lol_shop2.xml");
//		parseXml(xml, commsbuilder);
//		ShopList.Builder shoplist = ShopList.newBuilder();
//		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
//			comm.clearName();
//			shoplist.addItems(comm);
//		}
//		saveShop(shoplist.build());
//		return shoplist.build();
//	}
	
	//contract activity
	public ContractWeight getContract(int quality) {
		Map<Integer, ContractWeight> map = CacheService.hgetcache(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG);
		return map.get(quality);
	}
	
	private void buildContractConfig(){
		String xml = ReadConfig("lol_soulcontract.xml");
		ContractWeightList.Builder builder = ContractWeightList.newBuilder();
		parseXml(xml, builder);
		Map<Integer, ContractWeight> map = new HashMap<Integer, ContractWeight>();
		for(ContractWeight.Builder weight : builder.getQualityBuilderList()){
			int weightall = 0;
			for (RewardInfo reward : weight.getCountList()) {
				weightall += reward.getWeight();
			}
			weight.setWeightall(weightall);
			map.put(weight.getQuality(), weight.build());
		}
		CacheService.hputcacheAll(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG, map);
	}
	
	public MultiReward.Builder getContractRewardList(){
		MultiReward rewards = CacheService.getcache(RedisKey.PURCHASECONTRACTREWARD_CONFIG);
		MultiReward.Builder builder = MultiReward.newBuilder(rewards);
		
		return builder;
	}
	

	private MultiReward.Builder buildContractRewardList(){
		String xml = ReadConfig("lol_soulreward.xml");
		ContractRewardList.Builder listbuilder = ContractRewardList.newBuilder();
		MultiReward.Builder builder = MultiReward.newBuilder();
		parseXml(xml, listbuilder);
		for(ContractReward reward : listbuilder.getRewardList()){
			RewardInfo.Builder rewardbuilder = RewardInfo.newBuilder();
			rewardbuilder.setItemid(reward.getRewardid());
			rewardbuilder.setCount(reward.getCount());
			builder.addLoot(rewardbuilder);
		}
		CacheService.setcache(RedisKey.PURCHASECONTRACTREWARD_CONFIG, formatJson(builder.build()));
		
		return builder;
	}
	
	public PurchaseCoinCostList getPurchaseCoinCostList(){
		PurchaseCoinCostList list = CacheService.getcache(RedisKey.PURCHASECOIN_CONFIG);
		PurchaseCoinCostList.Builder builder = PurchaseCoinCostList.newBuilder(list);
		
		return builder.build();
	}

	private PurchaseCoinCostList buildPurchaseCoinCostList(){
		String xml = ReadConfig("lol_goldcost.xml");
		PurchaseCoinCostList.Builder builder = PurchaseCoinCostList.newBuilder();
		parseXml(xml, builder);
		CacheService.setcache(RedisKey.PURCHASECOIN_CONFIG, builder.build());
		
		return builder.build();
	}
	
	public PurchaseCoinReward getPurchaseCoinReward(int daguan){
		Map<Integer, PurchaseCoinReward> map = CacheService.hgetcache(RedisKey.PURCHASECOINREWARD_CONFIG);
		return map.get(daguan);
	}
	
	private Map<Integer, PurchaseCoinReward> buildPurchaseCoinReward(){
		String xml = ReadConfig("lol_goldreward.xml");
		PurchaseCoinRewardList.Builder list = PurchaseCoinRewardList.newBuilder();
		Map<Integer, PurchaseCoinReward> map = new HashMap<Integer, PurchaseCoinReward>();
		parseXml(xml, list);
		for(PurchaseCoinReward reward : list.getGoldList()){
			map.put(reward.getDaguan(), reward);
		}
		CacheService.hputcacheAll(RedisKey.PURCHASECOINREWARD_CONFIG, map);
		
		return map;
	}
	
	public VipLibao getVipLibao(int id){
		Map<Integer, VipLibao> map = CacheService.hgetcache(RedisKey.VIPLIBAO_CONFIG);
		return map.get(id);
	}
	
	private Map<Integer, VipLibao> buildVipLibao(){
		String xml = ReadConfig("ld_libao.xml");
		VipLibaoList.Builder list = VipLibaoList.newBuilder();
		Map<Integer, VipLibao> map = new HashMap<Integer, VipLibao>();
		parseXml(xml, list);
		for(VipLibao libao : list.getDataList()){
			map.put(libao.getItemid(), libao);
		}
		CacheService.hputcacheAll(RedisKey.VIPLIBAO_CONFIG, map);
		
		return map;
	}
	
//	public Map<Integer, YueKa> getYueKas(){
//		Map<Integer, YueKa> map = new HashMap<Integer, YueKa>();
//		Map<String, String> keyvalue = this.hget(RedisKey.YUEKA_CONFIG);
//		if(!keyvalue.isEmpty()){
//			for(String value : keyvalue.values()){
//				YueKa.Builder builder = YueKa.newBuilder();
//				if(parseJson(value, builder)){
//					map.put(builder.getItemid(), builder.build());
//				}
//			}
//			return map;
//		}else{
//			return buildYueKa();
//		}
//	}

//	public YueKa getYueKa(int id){
//		String value = this.hget(RedisKey.YUEKA_CONFIG, ""+id);
//		YueKa.Builder builder = YueKa.newBuilder();
//		if(value != null && parseJson(value, builder)){
//			return builder.build();
//		}else{
//			Map<Integer, YueKa> map = buildYueKa();
//			YueKa yueka = map.get(id);
//			return yueka;
//		}
//	}

//	public Map<Integer, YueKa> buildYueKa(){
//		String xml = ReadConfig("ld_yueka.xml");
//		YueKaList.Builder listbuilder = YueKaList.newBuilder();
//		Map<String, String> keyvalue = new HashMap<String, String>();
//		Map<Integer, YueKa> map = new HashMap<Integer, YueKa>();
//		parseXml(xml, listbuilder);
//		for(YueKa libao : listbuilder.getItemList()){
//			map.put(libao.getItemid(), libao);
//			keyvalue.put(libao.getItemid()+"", formatJson(libao));
//		}
//		hputAll(RedisKey.YUEKA_CONFIG, keyvalue);
//		
//		return map;
//	}
}
