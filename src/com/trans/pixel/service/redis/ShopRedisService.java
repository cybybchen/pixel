package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public ShopList.Builder buildComms(UserBean user, ShopWill shopwill, Map<Integer, CommodityList.Builder> commsmap){
		ShopList.Builder builder = ShopList.newBuilder();
		if(shopwill!=null)
		for(Will will : shopwill.getWillList()){
			CommodityList.Builder commsbuilder = commsmap.get(will.getWill());
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
					commsbuilder = commsmap.get(will.getWill()-100);
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
	
	public ShopList buildDailyShop(UserBean user){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.DAILYSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("ld_shopputong2.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.DAILYSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
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

	public Map<Integer, CommodityList.Builder> readDailyShopComms(){
		String xml = ReadConfig("ld_shopputong.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addData(comm);
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
	public ShopList.Builder getBlackShop(UserBean user) {
		String value = get(RedisKey.SHENMISHOP_CONFIG);
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			if(user.getFriendVip() == 1)
				for(int index = 0; index < builder.getItemsCount();index++) {
					Commodity.Builder comm = builder.getItemsBuilder(index);
					if(comm.getPosition() == 3){
						builder.removeItems(index);
						break;
					}
				}
			return builder;
		}else{
			CommodityList.Builder commodities = CommodityList.newBuilder();
			String xml = ReadConfig("ld_shopshenmi.xml");
			parseXml(xml, commodities);
			builder.addAllItems(commodities.getDataList());
			set(RedisKey.SHENMISHOP_CONFIG, formatJson(builder.build()));
			if(user.getFriendVip() == 1)
				for(int index = 0; index < builder.getItemsCount();index++) {
					Commodity.Builder comm = builder.getItemsBuilder(index);
					if(comm.getPosition() == 3){
						builder.removeItems(index);
						break;
					}
				}
			return builder;
		}
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
	
//	public void saveBlackShop(ShopList shoplist, UserBean user) {
//		if(shoplist.getItemsCount() > 0)
//			this.hput(USERDATA+user.getId(), "BLACKSHOP", formatJson(shoplist));
//	}
	
//	public long getBlackShopEndTime(){
////		long times[] = {today(0), today(3), today(6), today(9), today(12), today(15), today(18), today(21)};
//		long now = now();
//		for(int time = 0; time < 24; time+=2){
//			if(now < today(time))
//				return today(time);
//		}
//		return today(24);
////		long time = today(21);
////		if(now() < time)
////			return time;
////		else//第二天21点
////			return time+24*3600;
//	}
	
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
		String value = this.hget(USERDATA+user.getId(), "UNIONSHOP");
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
			this.hput(USERDATA+user.getId(), "UNIONSHOP", formatJson(shoplist));
	}
	
	public long getUnionShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildUnionShop(UserBean user){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.UNIONSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shopgonghuishopgonghui.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.UNIONSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = ShopList.newBuilder();//buildAreaComms(willsbuilder, getUnionShopComms(), user);
		builder.setEndTime(getUnionShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readUnionShopComms(){
		String xml = ReadConfig("lol_shopgonghui.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addData(comm);
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

	//挂机副本商店
	public ShopList getRaidShop(UserBean user) {
		String value = this.hget(USERDATA+user.getId(), "RAIDSHOP");
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
			this.hput(USERDATA+user.getId(), "RAIDSHOP", formatJson(shoplist));
	}
	
	public long getRaidShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	
	public ShopList buildRaidShop(UserBean user){
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		String value = get(RedisKey.RAIDSHOP_CONFIG);
		if(value == null || !parseJson(value, commsbuilder)){
			String xml = ReadConfig("ld_shopraid3.xml");
			parseXml(xml, commsbuilder);
			set(RedisKey.RAIDSHOP_CONFIG, formatJson(commsbuilder.build()));
		}
		ShopList.Builder builder = ShopList.newBuilder();
		builder.addAllItems(commsbuilder.getDataList());
		builder.setEndTime(getRaidShopEndTime());
		return builder.build();
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
		String value = this.hget(USERDATA+user.getId(), "PVPSHOP");
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
			this.hput(USERDATA+user.getId(), "PVPSHOP", formatJson(shoplist));
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

	public ShopList buildPVPShop(UserBean user){
		CommodityList.Builder list = CommodityList.newBuilder();
		String value = get(RedisKey.PVPSHOP_CONFIG);
		if(value == null || !parseJson(value, list)){
			String xml = ReadConfig("ld_shopmojing.xml");
			parseXml(xml, list);
			set(RedisKey.PVPSHOP_CONFIG, formatJson(list.build()));
		}
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
			if(commbuilder.getPosition() == 100) {
				commbuilder.setMaxlimit(commbuilder.getMaxlimit()+user.getShopchipboxTime());
				if(commbuilder.getMaxlimit() == 0)
					builder.removeItems(i);
			}
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
		
		ShopList.Builder builder = ShopList.newBuilder();//buildComms(willsbuilder, getExpeditionShopComms());
		builder.setEndTime(getExpeditionShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readExpeditionShopComms(){
		String xml = ReadConfig("lol_shopyuanzheng.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addData(comm);
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
			ShopList shoplist = buildLadderShop(user);
			saveLadderShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveLadderShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "LADDERSHOP", formatJson(shoplist));
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

	public ShopList buildLadderShop(UserBean user){
		CommodityList.Builder list = CommodityList.newBuilder();
		String value = get(RedisKey.LADDERSHOP_CONFIG);
		if(value == null || !parseJson(value, list)){
			String xml = ReadConfig("ld_shoptianti.xml");
			parseXml(xml, list);
			set(RedisKey.LADDERSHOP_CONFIG, formatJson(list.build()));
		}
		ShopList.Builder builder = ShopList.newBuilder();
		builder.addAllItems(list.getDataList());
		builder.setEndTime(getPVPShopEndTime());
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
				if(commbuilder.getPosition() == 100) {
					commbuilder.setMaxlimit(commbuilder.getMaxlimit()+user.getShopbaohuTime());
					if(commbuilder.getMaxlimit() == 0)
						builder.removeItems(i);
				}
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
		String value = this.hget(USERDATA+user.getId(), "BATTLETOWERSHOP");
		ShopList.Builder builder = ShopList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			ShopList shoplist = buildBattletowerShop(ubt);
			saveBattletowerShop(shoplist, user);
			return shoplist;
		}
	}
	
	public void saveBattletowerShop(ShopList shoplist, UserBean user) {
		if(shoplist.getItemsCount() > 0)
			this.hput(USERDATA+user.getId(), "BATTLETOWERSHOP", formatJson(shoplist));
	}
	
	public long getBattletowerShopEndTime(){
		long time = today(21);
		if(now() < time)
			return time;
		else//第二天21点
			return time+24*3600;
	}
	 
	public ShopList buildBattletowerShop(UserBattletowerBean ubt){
		ShopWillList.Builder willsbuilder = ShopWillList.newBuilder();
		String value = get(RedisKey.BATTLETOWERSHOP_CONFIG+"Type");
		if(value == null || !parseJson(value, willsbuilder)){
			String xml = ReadConfig("lol_shoptowershoptower.xml");
			parseXml(xml, willsbuilder);
			set(RedisKey.BATTLETOWERSHOP_CONFIG+"Type", formatJson(willsbuilder.build()));
		}
		
		ShopList.Builder builder = ShopList.newBuilder();//buildBattletowerComms(willsbuilder, getBattletowerShopComms(), ubt);
		builder.setEndTime(getBattletowerShopEndTime());
		return builder.build();
	}

	public Map<Integer, CommodityList.Builder> readBattletowerShopComms(){
		String xml = ReadConfig("lol_shoptower.xml");
		CommodityList.Builder commsbuilder = CommodityList.newBuilder();
		parseXml(xml, commsbuilder);
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		for(Commodity.Builder comm : commsbuilder.getDataBuilderList()){
			CommodityList.Builder comms = map.get(comm.getWill());
			if(comms == null){
				comms = CommodityList.newBuilder();
			}
			comm.clearName();
			comms.addData(comm);
			map.put(comm.getWill(), comms);
		}
		Map<String, String> resultmap = new HashMap<String, String>();
		for(Entry<Integer, CommodityList.Builder> entry : map.entrySet()){
			resultmap.put(entry.getKey()+"", formatJson(entry.getValue().build()));
		}
		this.hputAll(RedisKey.BATTLETOWERSHOP_CONFIG, resultmap);
		return map;
	}
	
	public Map<Integer, CommodityList.Builder> getBattletowerShopComms(){
		Map<Integer, CommodityList.Builder> map = new HashMap<Integer, CommodityList.Builder>();
		Map<String, String> keyvalue = this.hget(RedisKey.BATTLETOWERSHOP_CONFIG);
		if(keyvalue.isEmpty()){
			return readBattletowerShopComms();
		}else{
			for(Entry<String, String> entry : keyvalue.entrySet()){
				CommodityList.Builder builder = CommodityList.newBuilder();
				parseJson(entry.getValue(), builder);
				map.put(Integer.parseInt(entry.getKey()), builder);
			}
			return map;
		}
	}
	
	public CommodityList getBattletowerShopComms(int will){
		String value = this.hget(RedisKey.BATTLETOWERSHOP_CONFIG, will+"");
		CommodityList.Builder builder = CommodityList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			Map<Integer, CommodityList.Builder> map = readBattletowerShopComms();
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
		String xml = ReadConfig("ld_shoplibao.xml");
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
		String value = hget(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG, "" + quality);
		if (value == null) {
			Map<String, ContractWeight> config = getContractConfig();
			return config.get("" + quality);
		} else {
			ContractWeight.Builder builder = ContractWeight.newBuilder();
			if(parseJson(value, builder))
				return builder.build();
		}
		
		return null;
	}
	
	public Map<String, ContractWeight> getContractConfig() {
		Map<String, String> keyvalue = hget(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, ContractWeight> map = buildContractConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, ContractWeight> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.PURCHASECONTRACTWEIGHT_CONFIG, redismap);
			return map;
		}else{
			Map<String, ContractWeight> map = new HashMap<String, ContractWeight>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				ContractWeight.Builder builder = ContractWeight.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	private Map<String, ContractWeight> buildContractConfig(){
		String xml = ReadConfig("lol_soulcontract.xml");
		ContractWeightList.Builder builder = ContractWeightList.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build " + "lol_soulcontract.xml");
			return null;
		}
		
		Map<String, ContractWeight> map = new HashMap<String, ContractWeight>();
		for(ContractWeight.Builder weight : builder.getQualityBuilderList()){
			int weightall = 0;
			for (RewardInfo reward : weight.getCountList()) {
				weightall += reward.getWeight();
			}
			weight.setWeightall(weightall);
			map.put("" + weight.getQuality(), weight.build());
		}
		return map;
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
		String xml = ReadConfig("ld_libao.xml");
		VipLibaoList.Builder list = VipLibaoList.newBuilder();
		Map<String, String> keyvalue = new HashMap<String, String>();
		parseXml(xml, list);
		for(VipLibao libao : list.getDataList()){
			keyvalue.put(libao.getItemid()+"", formatJson(libao));
			if(libao.getItemid() == id)
				builder = VipLibao.newBuilder(libao);
		}
		hputAll(RedisKey.VIPLIBAO_CONFIG, keyvalue);
		return builder.build();
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
