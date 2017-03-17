package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.mapper.UserLevelMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Commands.Area;
import com.trans.pixel.protoc.Commands.AreaEvent;
import com.trans.pixel.protoc.Commands.AreaEventList;
import com.trans.pixel.protoc.Commands.AreaList;
import com.trans.pixel.protoc.Commands.Daguan;
import com.trans.pixel.protoc.Commands.DaguanList;
import com.trans.pixel.protoc.Commands.Event;
import com.trans.pixel.protoc.Commands.Loot;
import com.trans.pixel.protoc.Commands.LootList;

@Repository
public class LevelRedisService extends RedisService {
//	private static Logger logger = Logger.getLogger(LevelRedisService.class);
	@Resource
	private UserLevelMapper mapper;
	// private static final String LOOTTIME_FILE_NAME = "lol_loottime.xml";
	// @Resource
	// private RedisTemplate<String, String> redisTemplate;
	
	// public XiaoguanBean getXiaoguanByLevelId(final int levelId) {
	// 	return redisTemplate.execute(new RedisCallback<XiaoguanBean>() {
	// 		@Override
	// 		public XiaoguanBean doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
				
	// 			return XiaoguanBean.fromJson(bhOps.get("" + levelId));
	// 		}
	// 	});
	// }
	
	// public void setXiaoguanList(final List<XiaoguanBean> xiaoguanList) {
	// 	redisTemplate.execute(new RedisCallback<Object>() {
	// 		@Override
	// 		public Object doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_KEY);
				
	// 			for (XiaoguanBean xiaoguan : xiaoguanList) {
	// 				bhOps.put("" + xiaoguan.getId(), xiaoguan.toJson());
	// 			}
				
	// 			return null;
	// 		}
	// 	});
	// }
	
	// public void setXiaoguanListByDiff(final List<XiaoguanBean> xiaoguanList, final int diff) {
	// 	redisTemplate.execute(new RedisCallback<Object>() {
	// 		@Override
	// 		public Object doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + diff);
				
	// 			for (XiaoguanBean xiaoguan : xiaoguanList) {
	// 				bhOps.put("" + xiaoguan.getId(), xiaoguan.toJson());
	// 			}
				
	// 			return null;
	// 		}
	// 	});
	// }
	
	// public List<XiaoguanBean> getXiaoguanListByDiff(final int diff) {
	// 	return redisTemplate.execute(new RedisCallback<List<XiaoguanBean>>() {
	// 		@Override
	// 		public List<XiaoguanBean> doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.LEVEL_DIFF_PREDIX + diff);
				
	// 			List<XiaoguanBean> xgList = new ArrayList<XiaoguanBean>();
	// 			Iterator<Entry<String, String>> itr = bhOps.entries().entrySet().iterator();
	// 			while (itr.hasNext()) {
	// 				Entry<String, String> entry = itr.next();
	// 				XiaoguanBean xg = XiaoguanBean.fromJson(entry.getValue());
	// 				xgList.add(xg);
	// 			}
	// 			return xgList;
	// 		}
	// 	});
	// }
	
	// public DaguanBean getDaguanById(final int id) {
	// 	return redisTemplate.execute(new RedisCallback<DaguanBean>() {
	// 		@Override
	// 		public DaguanBean doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
				
	// 			return DaguanBean.fromJson(bhOps.get("" + id));
	// 		}
	// 	});
	// }
	
	// public void setDaguanList(final List<DaguanBean> daguanList) {
	// 	redisTemplate.execute(new RedisCallback<Object>() {
	// 		@Override
	// 		public Object doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
				
	// 			for (DaguanBean daguan : daguanList) {
	// 				bhOps.put("" + daguan.getId(), daguan.toJson());
	// 			}
				
	// 			return null;
	// 		}
	// 	});
	// }
	
	// public List<DaguanBean> getDaguanList() {
	// 	return redisTemplate.execute(new RedisCallback<List<DaguanBean>>() {
	// 		@Override
	// 		public List<DaguanBean> doInRedis(RedisConnection arg0)
	// 				throws DataAccessException {
	// 			BoundHashOperations<String, String, String> bhOps = redisTemplate
	// 					.boundHashOps(RedisKey.PREFIX + RedisKey.DAGUAN_KEY);
				
	// 			List<DaguanBean> dgList = new ArrayList<DaguanBean>();
	// 			Iterator<Entry<String, String>> itr = bhOps.entries().entrySet().iterator();
	// 			while (itr.hasNext()) {
	// 				Entry<String, String> entry = itr.next();
	// 				DaguanBean dg = DaguanBean.fromJson(entry.getValue());
	// 				dgList.add(dg);
	// 			}
	// 			return dgList;
	// 		}
	// 	});
	// }
	
	// public LootTime getLootTime(int xiaoguan) {
	// 	String value = hget(RedisKey.LOOTTIME_CONFIG, "" + xiaoguan);
	// 	if (value == null) {
	// 		Map<String, LootTime> chipConfig = getLootTimeConfig();
	// 		return chipConfig.get("" + xiaoguan);
	// 	} else {
	// 		LootTime.Builder builder = LootTime.newBuilder();
	// 		if(parseJson(value, builder))
	// 			return builder.build();
	// 	}
		
	// 	return null;
	// }
	
	// public Map<String, LootTime> getLootTimeConfig() {
	// 	Map<String, String> keyvalue = hget(RedisKey.LOOTTIME_CONFIG);
	// 	if(keyvalue.isEmpty()){
	// 		Map<String, LootTime> map = buildLootTimeConfig();
	// 		Map<String, String> redismap = new HashMap<String, String>();
	// 		for(Entry<String, LootTime> entry : map.entrySet()){
	// 			redismap.put(entry.getKey(), formatJson(entry.getValue()));
	// 		}
	// 		hputAll(RedisKey.LOOTTIME_CONFIG, redismap);
	// 		return map;
	// 	}else{
	// 		Map<String, LootTime> map = new HashMap<String, LootTime>();
	// 		for(Entry<String, String> entry : keyvalue.entrySet()){
	// 			LootTime.Builder builder = LootTime.newBuilder();
	// 			if(parseJson(entry.getValue(), builder))
	// 				map.put(entry.getKey(), builder.build());
	// 		}
	// 		return map;
	// 	}
	// }
	
	// private Map<String, LootTime> buildLootTimeConfig(){
	// 	String xml = ReadConfig(LOOTTIME_FILE_NAME);
	// 	LootTimeList.Builder builder = LootTimeList.newBuilder();
	// 	if(!parseXml(xml, builder)){
	// 		logger.warn("cannot build " + LOOTTIME_FILE_NAME);
	// 		return null;
	// 	}
		
	// 	Map<String, LootTime> map = new HashMap<String, LootTime>();
	// 	for(LootTime.Builder lootTime : builder.getXiaoguanBuilderList()){
	// 		map.put("" + lootTime.getXiaoguan(), lootTime.build());
	// 	}
	// 	return map;
	// }
		
	// public UserLevelBean selectUserLevelRecord(final long userId) {
	// 	String value = hget(RedisKey.USERDATA + userId, "LevelRecord");
	// 	if(value != null){
	// 		JSONObject object = JSONObject.fromObject(value);
	// 		return (UserLevelBean) JSONObject.toBean(object, UserLevelBean.class);
	// 	}else
	// 		return null;
	// }
	
	// public void updateUserLevelRecord(final UserLevelBean userLevelRecordBean) {
	// 	JSONObject object = JSONObject.fromObject(userLevelRecordBean);
	// 	hput(RedisKey.USERDATA + userLevelRecordBean.getUserId(), "LevelRecord", object.toString());

	// 	sadd(RedisKey.PUSH_MYSQL_KEY+"LevelRecord", userLevelRecordBean.getUserId()+"");
	// }
	
	// public String popLevelRecordDBKey(){
	// 	return spop(RedisKey.PUSH_MYSQL_KEY+"LevelRecord");
	// }

	// public UserLevelLootBean selectUserLevelLootRecord(final long userId) {
	// 	String value = hget(RedisKey.USERDATA + userId, "LootLevel");
	// 	return UserLevelLootBean.fromJson(value);
	// }
	
	// public void updateUserLevelLootRecord(final UserLevelLootBean userLevelLootRecordBean) {
	// 	hput(RedisKey.USERDATA + userLevelLootRecordBean.getUserId(), "LootLevel", userLevelLootRecordBean.toJson());
		
	// 	sadd(RedisKey.PUSH_MYSQL_KEY+"LootLevel", userLevelLootRecordBean.getUserId()+"");
	// }
	
	public void updateToDB(long userId){
		UserLevelBean bean = getUserLevel(userId);
		if(bean != null)
			mapper.updateUserLevel(bean);
	}
	public String popDBKey(){
		return spop(RedisKey.PUSH_MYSQL_KEY+"UserLevel");
	}
	public UserLevelBean getUserLevel(long userId){
		String value = hget(RedisKey.USERDATA+userId, "UserLevel");
		return UserLevelBean.fromJson(value);
	}
	public UserLevelBean getUserLevel(UserBean user){
		UserLevelBean userLevel = getUserLevel(user.getId());
		if(userLevel != null)
			return userLevel;
		userLevel = mapper.getUserLevel(user.getId());
		if(userLevel == null){
			userLevel = new UserLevelBean();
			Daguan.Builder daguan = getDaguan(1);
			userLevel.setUserId(user.getId());
			userLevel.setLootTime((int)now());
			userLevel.setEventTime((int)now());
			userLevel.setUnlockDaguan(daguan.getId());
			userLevel.setLeftCount(daguan.getCount());
			userLevel.setLootDaguan(daguan.getId());
			userLevel.setCoin(daguan.getGold());
			userLevel.setExp(daguan.getExperience());
			AreaEvent.Builder events = getDaguanEvent(daguan.getId());
			for(Event.Builder event : events.getEventBuilderList()){
				if(daguan.getId() == event.getDaguan())
					saveEvent(user, event.build());
			}
		}else{
			Daguan.Builder daguan = getDaguan(userLevel.getLootDaguan());
			userLevel.setCoin(daguan.getGold());
			userLevel.setExp(daguan.getExperience());
		}
		saveUserLevel(userLevel);
		return userLevel;
	}
	public int getCoin(UserBean user){
		UserLevelBean userLevel = getUserLevel(user);
		return (int)(now()-userLevel.getLootTime())*userLevel.getCoin();
	}
	public int getExp(UserBean user){
		UserLevelBean userLevel = getUserLevel(user);
		return (int)(now()-userLevel.getLootTime())*userLevel.getExp();
	}
	public void saveUserLevel(UserLevelBean bean){
		hput(RedisKey.USERDATA+bean.getUserId(), "UserLevel", toJson(bean));
		sadd(RedisKey.PUSH_MYSQL_KEY+"UserLevel", bean.getUserId()+"");
	}
	public Map<Integer, Event.Builder> getEvents(UserBean user) {
		Map<String, String> keyvalue = hget(RedisKey.USEREVENT_PREFIX+user.getId());
		Map<Integer, Event.Builder> map = new TreeMap<Integer, Event.Builder>();
		for(String value : keyvalue.values()){
			Event.Builder event = Event.newBuilder();
			if(parseJson(value, event))
				map.put(event.getOrder(), event);
		}
		return map;
	}
	public boolean hasEvent(UserBean user){
		for(Event.Builder event : getEvents(user).values()){
			if(event.getOrder() < 100)
				return true;
		}
		return false;
	}
	public void productEvent(UserBean user, UserLevelBean userLevel){
		long time = now() - userLevel.getEventTime();
		if(time >= 60){
			AreaEvent.Builder events = getDaguanEvent(userLevel.getLootDaguan());
			if(events != null)
			for(int i = 0; i < time/60; i++){
				int weight = nextInt(events.getWeight());
				for(Event.Builder event : events.getEventBuilderList()){
					if(event.getWeight() >= weight){
						event.setOrder(currentIndex()+i);
						saveEvent(user, event.build());
						break;
					}else{
						weight -= event.getWeight();
					}
				}
			}
			userLevel.setEventTime(userLevel.getEventTime()+(int)time/60*60);
			saveUserLevel(userLevel);
		}
	}
	public Event getEvent(UserBean user, int order) {
		String value = hget(RedisKey.USEREVENT_PREFIX+user.getId(), order+"");
		Event.Builder builder = Event.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		return null;
	}
	public void saveEvent(UserBean user, Event event) {
		hput(RedisKey.USEREVENT_PREFIX+user.getId(), event.getOrder()+"", toJson(event));
	}
	public void delEvent(UserBean user, int order) {
		hdelete(RedisKey.USEREVENT_PREFIX+user.getId(), order+"");
	}
	public Event getEvent(int eventid){
		String value = hget(RedisKey.EVENT_CONFIG, eventid+"");
		Event.Builder builder = Event.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		buildEvent();
		value = hget(RedisKey.EVENT_CONFIG, eventid+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
	public AreaEvent.Builder getDaguanEvent(int daguanid){
		String value = hget(RedisKey.DAGUANEVENT_CONFIG, daguanid+"");
		AreaEvent.Builder builder = AreaEvent.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder;
		buildEvent();
		value = hget(RedisKey.DAGUANEVENT_CONFIG, daguanid+"");
		if(value != null && parseJson(value, builder))
			return builder;
		else
			return null;
	}
	private void buildEvent(){
		Map<String, String> keyvalue = new HashMap<String, String>();
		Map<String, String> keyvalue2 = new HashMap<String, String>();
		String xml = ReadConfig("ld_event.xml");
		AreaEventList.Builder list = AreaEventList.newBuilder();
		parseXml(xml, list);
		Map<Integer, AreaEvent.Builder> map = new HashMap<Integer, AreaEvent.Builder>();
		for(AreaEvent.Builder area : list.getIdBuilderList()){
			for(Event.Builder event : area.getEventBuilderList()){
				// event.clearName();
				event.clearDes();
				keyvalue2.put(event.getEventid()+"", formatJson(event.build()));
				event.clearReward();
				if(event.getDaguan() != 0){
					if(map.get(event.getDaguan())!=null){
						AreaEvent.Builder builder = AreaEvent.newBuilder();
						builder.setId(area.getId());
						map.put(builder.getId(), builder);
					}
					AreaEvent.Builder builder = map.get(event.getDaguan());
					builder.addEvent(event);
					builder.setWeight(builder.getWeight()+event.getWeight());
				}
			}
			for(Event.Builder event : area.getEventBuilderList()){
				if(event.getDaguan() == 0){
					for(AreaEvent.Builder daguan : map.values()){
						if(daguan.getId() == area.getId()){
							daguan.addEvent(event);
							daguan.setWeight(daguan.getWeight()+event.getWeight());
						}
					}
				}
			}
		}
		for(AreaEvent.Builder daguan : map.values())
			keyvalue.put(daguan.getId()+"", formatJson(daguan.build()));
		hputAll(RedisKey.DAGUANEVENT_CONFIG, keyvalue);
		hputAll(RedisKey.EVENT_CONFIG, keyvalue2);
	}

	public Daguan.Builder getDaguan(int id){
		String value = hget(RedisKey.DAGUAN_CONFIG, id+"");
		Daguan.Builder builder = Daguan.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder;
		Map<String, String> keyvalue = new HashMap<String, String>();
		String xml = ReadConfig("ld_daguan.xml");
		DaguanList.Builder list = DaguanList.newBuilder();
		parseXml(xml, list);
		Map<Integer, Integer> areaMap = getAreaConfig();
		Map<Integer, Loot> lootMap = getLootConfig();
		Map<Integer, Daguan.Builder> map = new HashMap<Integer, Daguan.Builder>();
		for(Daguan.Builder daguan : list.getIdBuilderList()){
			// daguan.clearName();
			daguan.clearDes();
			daguan.setAreaid(areaMap.get(daguan.getId()));
			daguan.addAllItem(lootMap.get(daguan.getId()).getItemList());
			keyvalue.put(daguan.getId()+"", formatJson(daguan.build()));
			map.put(daguan.getId(), daguan);
		}
		hputAll(RedisKey.DAGUAN_CONFIG, keyvalue);
		return map.get(id);
	}

	public Map<Integer, Loot> getLootConfig(){
		Map<Integer, Loot> map = new HashMap<Integer, Loot>();
		String xml = ReadConfig("ld_loot.xml");
		LootList.Builder list = LootList.newBuilder();
		parseXml(xml, list);
		for(Loot loot : list.getIdList()){
			map.put(loot.getId(), loot);
		}
		return map;
	}

	public Map<Integer, Integer> getAreaConfig(){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		String xml = ReadConfig("ld_area.xml");
		AreaList.Builder list = AreaList.newBuilder();
		parseXml(xml, list);
		for(Area.Builder area : list.getAreaBuilderList()){
			for(Daguan.Builder daguan : area.getIdBuilderList()){
				map.put(daguan.getId(), area.getAreaid());
			}
		}
		return map;
	}
}
