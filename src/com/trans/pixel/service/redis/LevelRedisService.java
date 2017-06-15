package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.management.RuntimeErrorException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.mapper.UserLevelMapper;
import com.trans.pixel.model.userinfo.EventBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.Event;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.UserTalent;
import com.trans.pixel.protoc.HeroProto.HeroChoice;
import com.trans.pixel.protoc.UserInfoProto.Area;
import com.trans.pixel.protoc.UserInfoProto.AreaEvent;
import com.trans.pixel.protoc.UserInfoProto.AreaEventList;
import com.trans.pixel.protoc.UserInfoProto.AreaList;
import com.trans.pixel.protoc.UserInfoProto.Daguan;
import com.trans.pixel.protoc.UserInfoProto.DaguanList;
import com.trans.pixel.protoc.UserInfoProto.Enemy;
import com.trans.pixel.protoc.UserInfoProto.EnemyList;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.protoc.UserInfoProto.EventConfigList;
import com.trans.pixel.protoc.UserInfoProto.EventExp;
import com.trans.pixel.protoc.UserInfoProto.EventExpList;
import com.trans.pixel.protoc.UserInfoProto.EventLevel;
import com.trans.pixel.protoc.UserInfoProto.EventLevelList;
import com.trans.pixel.protoc.UserInfoProto.EventRandom;
import com.trans.pixel.protoc.UserInfoProto.EventRandoms;
import com.trans.pixel.protoc.UserInfoProto.EventRandomsList;
import com.trans.pixel.protoc.UserInfoProto.Loot;
import com.trans.pixel.protoc.UserInfoProto.LootList;
import com.trans.pixel.protoc.UserInfoProto.SavingBox;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.UserTalentService;

@Repository
public class LevelRedisService extends RedisService {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LevelRedisService.class);
	private static final int NEWPLAY_LEVEL_1 = 1012;
	private static final int NEWPLAY_LEVEL_2 = 1013;
	private static final int NEWPLAY_LEVEL_3 = 1014;
	public static final int EVENTTIME = 480;
	public static final int EVENTSIZE = 50;
	private static final long EVENT_BUY_COUNT = 5;
	@Resource
	private UserLevelMapper mapper;
	@Resource
	private HeroRedisService heroRedisService;
	@Resource
	private UserTalentService userTalentService;
	@Resource
	private LootRedisService lootRedisService;
	@Resource
	private CostService costService;
	@Resource
	private UserService userService;
	
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
		if(value != null)
			return UserLevelBean.fromJson(value);
		UserLevelBean userLevel = mapper.getUserLevel(userId);
		if(userLevel == null){
			userLevel = new UserLevelBean();
			Daguan.Builder daguan = getDaguan(1);
			userLevel.setUserId(userId);
			userLevel.setLootTime((int)now());
			userLevel.setLootTimeNormal(RedisService.now());
			userLevel.setEventTime((int)now());
			userLevel.setUnlockDaguan(daguan.getId());
			userLevel.setLeftCount(daguan.getCount());
			userLevel.setLootDaguan(daguan.getId());
			userLevel.setCoin(daguan.getGold());
			userLevel.setExp(daguan.getExp());
			AreaEvent.Builder events = getMainEvent(daguan.getId());
			Map<Integer, Event.Builder> eventmap = new HashMap<Integer, Event.Builder>();
			for(Event.Builder event : events.getEventBuilderList()){
//				if(daguan.getId() == event.getDaguan() && event.getWeight() == 0){
					event.setOrder(events.getId()*30+event.getOrder());
					eventmap.put(event.getOrder(), event);
//				}
			}
			productMainEvent(userLevel, eventmap);
		}else{
			Daguan.Builder daguan = getDaguan(userLevel.getLootDaguan());
			userLevel.setCoin(daguan.getGold());
			userLevel.setExp(daguan.getExp());
		}
		saveUserLevel(userLevel);
		return userLevel;
	}
	public UserLevelBean getUserLevel(UserBean user){
		return getUserLevel(user.getId());
	}
	
	public MultiReward.Builder getLootReward(UserBean user){
		MultiReward.Builder builder = MultiReward.newBuilder();
		String value = hget(RedisKey.USERDATA+user.getId(), "LootReward");
		if(value != null)
			parseJson(value, builder);
		return builder;
	}
	public void saveLootReward(MultiReward.Builder rewards, UserBean user){
		hput(RedisKey.USERDATA+user.getId(), "LootReward", formatJson(rewards.build()));
	}
//	public int getCoin(UserBean user){
//		UserLevelBean userLevel = getUserLevel(user);
//		return (int)(now()-userLevel.getLootTime())*userLevel.getCoin();
//	}
//	public long getExp(UserBean user){
//		UserLevelBean userLevel = getUserLevel(user);
//		return (now()-userLevel.getLootTime())*userLevel.getExp();
//	}
	public void saveUserLevel(UserLevelBean bean){
		hput(RedisKey.USERDATA+bean.getUserId(), "UserLevel", toJson(bean));
		sadd(RedisKey.PUSH_MYSQL_KEY+"UserLevel", bean.getUserId()+"");
	}
	public Map<Integer, Event.Builder> getEvents(UserBean user, UserLevelBean userLevel) {
		Map<String, String> keyvalue = hget(RedisKey.USEREVENT_PREFIX+userLevel.getUserId());
		Map<Integer, Event.Builder> map = new TreeMap<Integer, Event.Builder>();
		if(keyvalue.isEmpty() && now() - userLevel.getEventTime() > 24*3600){
			List<EventBean> list = mapper.getEvents(userLevel.getUserId());
			Map<Integer, Event.Builder> eventmap = new HashMap<Integer, Event.Builder>();
			for(EventBean bean : list){
				Event.Builder event = bean.build();
				EventConfig config = getEvent(event.getEventid());
//				event.setOrder(event.getDaguan()*300+event.getOrder());
				event.setName(config.getName());
//				event.setType(config.getType());
//				event.setCostid(config.getCostid());
//				event.setCostcount(event.getCostcount());
//				event.setCall(config.getCall());
				eventmap.put(event.getOrder(), event);
//				map.put(event.getOrder(), event);
			}
			productMainEvent(userLevel, eventmap);
		} 
		keyvalue = hget(RedisKey.USEREVENT_PREFIX+userLevel.getUserId());
		for(String value : keyvalue.values()){
			Event.Builder event = Event.newBuilder();
			if(parseJson(value, event))
				map.put(event.getOrder(), event);
		}
		return map;
	}
	
//	public List<Event> getEventList(UserBean user) {
//		Map<String, String> keyvalue = hget(RedisKey.USEREVENT_PREFIX+user.getId());
//		List<Event> eventList = new ArrayList<Event>();
//		for(String value : keyvalue.values()){
//			Event.Builder event = Event.newBuilder();
//			if(parseJson(value, event))
//				eventList.add(event.build());
//		}
//		return eventList;
//	}
	
	// public boolean hasEvent(UserBean user){
	// 	for(Event.Builder event : getEvents(user).values()){
	// 		if(event.getOrder() < 100)
	// 			return true;
	// 	}
	// 	return false;
	// }
//	public void productMainEvent(UserBean user, UserLevelBean userLevel){
//		productMainEvent(user.getId(), userLevel);
//	}
	public void productMainEvent(UserBean user, int eventid){
		Map<String, String> keyvalue = hget(RedisKey.USEREVENTREADY_PREFIX + user.getId());
		for(String value : keyvalue.values()) {
			Event.Builder builder = Event.newBuilder();
			parseJson(value, builder);
			if(builder.getCondition() == eventid) {
				saveEvent(user, builder.build());
				delEventReady(user, builder.build());
			}
		}
	}
	public void productMainEvent(UserLevelBean userLevel, Map<Integer, Event.Builder> map){
		Map<Integer, Event.Builder> eventmap = new HashMap<Integer, Event.Builder>();
		Map<String, String> keyvalue = hget(RedisKey.USEREVENT_PREFIX+userLevel.getUserId());
		for(String value : keyvalue.values()){
			Event.Builder event = Event.newBuilder();
			if(parseJson(value, event))
				eventmap.put(event.getOrder(), event);
		}
		for(Event.Builder builder : map.values()) {
			eventmap.put(builder.getOrder(), builder);
		}
		keyvalue = hget(RedisKey.USEREVENTREADY_PREFIX + userLevel.getUserId());
		for(String value : keyvalue.values()) {
			Event.Builder builder = Event.newBuilder();
			parseJson(value, builder);
			eventmap.put(builder.getOrder(), builder);
			map.put(builder.getOrder(), builder);
		}
		for(Event.Builder builder : map.values()) {
			if((builder.getConditiontype() != 1 || builder.getCondition() == 0) || (isMainEvent(builder.getCondition()) && hasCompleteEvent(userLevel.getUserId(), builder.getCondition(), userLevel, eventmap))) {
				hdelete(RedisKey.USEREVENTREADY_PREFIX + userLevel.getUserId(), builder.getOrder()+"");
				saveEvent(userLevel.getUserId(), builder.build());
			}else {
				saveEventReady(userLevel.getUserId(), builder.build());
			}
		}
	}
	
	public void productEvent(UserBean user, UserLevelBean userLevel){
		productEvent(user, userLevel,  false);
	}
	
	public void productEvent(UserBean user, UserLevelBean userLevel, boolean isBuy){
		long time = now() - userLevel.getEventTime();
		long eventsize = hlen(RedisKey.USEREVENT_PREFIX+user.getId());
		if(!isBuy && eventsize >= EVENTSIZE){
			userLevel.setEventTime((int)now());
			saveUserLevel(userLevel);
		}else if(time >= EVENTTIME){
			AreaEvent.Builder events = getDaguanEvent(userLevel.getLootDaguan());
			if(events != null){
				for(Event.Builder myevent : getEvents(user, userLevel).values()){
					for(Event.Builder event : events.getEventBuilderList()){
						if(event.getCount() > 0 && myevent.getEventid() == event.getEventid()){
							event.setCount(event.getCount()-1);
							if(event.getCount() == 0){
								events.setWeight(events.getWeight()-event.getWeight());
								event.setWeight(0);
							}
						}
					}
				}
				
				for(int i = 0; i < getProductEvnentCount(eventsize, time, isBuy); i++){
					int weight = nextInt(events.getWeight());
					for(Event.Builder event : events.getEventBuilderList()){
						weight -= event.getWeight();
						if(weight < 0){
							Event.Builder builder = Event.newBuilder(event.build());
							builder.setOrder(currentIndex()+i);
							builder.setDaguan(userLevel.getLootDaguan());
							EventRandom random = nextEventLevel(userLevel);
							switch(user.getUserType()) {
							case 4:
								builder.setLevel(random.getLevel4());
								builder.setCount(random.getCount4());
								break;
							case 3:
								builder.setLevel(random.getLevel3());
								builder.setCount(random.getCount3());
								break;
							case 2:
								builder.setLevel(random.getLevel2());
								builder.setCount(random.getCount2());
								break;
							case 1:
							default:
								builder.setLevel(random.getLevel1());
								builder.setCount(random.getCount1());
								break;
							}
							builder.clearWeight();
							saveEvent(user, builder.build());
							event.setCount(event.getCount()-1);
							if(event.getCount() == 0){
								events.setWeight(events.getWeight()-event.getWeight());
								event.setWeight(0);
							}
							break;
						}
					}
				}
			}
			if (!isBuy) {
				userLevel.setEventTime(userLevel.getEventTime()+(int)time/EVENTTIME*EVENTTIME);
				saveUserLevel(userLevel);
			}
		}
	}
	
	private long getProductEvnentCount(long eventsize, long time, boolean isBuy) {
		if (isBuy)
			return EVENT_BUY_COUNT;
		
		return Math.min(EVENTSIZE - eventsize, time / EVENTTIME);
	}
	
	public Event getEvent(long userId, int order) {
		String value = hget(RedisKey.USEREVENT_PREFIX+userId, order+"");
		Event.Builder builder = Event.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		return null;
	}
	public Event getEvent(UserBean user, int order) {
		return getEvent(user.getId(), order);
	}
	public String popEventKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX);
	}
	public void updateEventToDB(long userId, int order){
		String value = hget(RedisKey.USEREVENT_PREFIX+userId, order+"");
		if(value != null){
			EventBean bean = EventBean.fromJson(userId, value);
			mapper.updateEvent(bean);
		}
	}
	public String popEventReadyKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENTREADY_PREFIX);
	}
	public void updateEventReadyToDB(long userId, int order){
		String value = hget(RedisKey.USEREVENTREADY_PREFIX+userId, order+"");
		if(value != null){
			EventBean bean = EventBean.fromJson(userId, value);
			mapper.updateEvent(bean);
		}
	}
	public String popDelEventKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX+"Del");
	}
	public void updateDelEventToDB(long userId, int order){
		mapper.delEvent(userId, order);
	}
	public void saveEvent(long userId, Event event) {
		hput(RedisKey.USEREVENT_PREFIX+userId, event.getOrder()+"", formatJson(event));
		expire(RedisKey.USEREVENT_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		if(event.getOrder() < 10000)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX, userId+"#"+event.getOrder());
	}
	public void saveEvent(UserBean user, Event event) {
		saveEvent(user.getId(), event);
	}
	public void saveEventReady(UserBean user, Event event) {
		saveEventReady(user.getId(), event);
	}
	public void saveEventReady(long userId, Event event) {
		hput(RedisKey.USEREVENTREADY_PREFIX+userId, event.getOrder()+"", formatJson(event));
		expire(RedisKey.USEREVENTREADY_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		if(event.getOrder() < 10000)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENTREADY_PREFIX, userId+"#"+event.getOrder());
	}
//	public Event getEventReady(UserBean user, int eventid) {
//		String value = hget(RedisKey.USEREVENTREADY_PREFIX+user.getId(), eventid+"");
//		Event.Builder builder = Event.newBuilder();
//		if(value != null && parseJson(value, builder))
//			return builder.build();
//		return null;
//	}
	public void delEventReady(UserBean user, Event event) {
		hdelete(RedisKey.USEREVENTREADY_PREFIX+user.getId(),event.getOrder()+"");
//		if(event.getOrder() < 10000)
//			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENTREADY_PREFIX+"Del", user.getId()+"#"+event.getEventid());
	}
	public void delEvent(UserBean user, Event event) {
		hdelete(RedisKey.USEREVENT_PREFIX+user.getId(), event.getOrder()+"");
		if(event.getOrder() < 10000)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX+"Del", user.getId()+"#"+event.getOrder());
	}

	public EventConfig getEvent(int eventid){
		String value = hget(RedisKey.EVENT_CONFIG, eventid+"");
		EventConfig.Builder builder = EventConfig.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else if(exists(RedisKey.EVENT_CONFIG))
			return null;
		buildDaguanEvent();
		value = hget(RedisKey.EVENT_CONFIG, eventid+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}
//	public Map<String, Event> getEventMap(){
//		Map<String, String> keyvalue = hget(RedisKey.EVENT_CONFIG);
//		if(keyvalue.isEmpty()){
//			buildEvent();
//			keyvalue = hget(RedisKey.EVENT_CONFIG);
//			
//		}
//		
//		Map<String, Event> map = new HashMap<String, Event>();
//		for(Entry<String, String> entry : keyvalue.entrySet()){
//			Event.Builder builder = Event.newBuilder();
//			if(parseJson(entry.getValue(), builder))
//				map.put(entry.getKey(), builder.build());
//		}
//		return map;
//		
//	}
	public AreaEvent.Builder getDaguanEvent(int daguanid){
		String value = hget(RedisKey.DAGUANEVENT_CONFIG, daguanid+"");
		AreaEvent.Builder builder = AreaEvent.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder;
		buildDaguanEvent();
		value = hget(RedisKey.DAGUANEVENT_CONFIG, daguanid+"");
		if(value != null && parseJson(value, builder))
			return builder;
		else
			return null;
	}
	public Map<Integer, AreaEvent.Builder> getMainEvent(){
		Map<String, String> keyvalue = hget(RedisKey.MAINEVENT_CONFIG);
		Map<Integer, AreaEvent.Builder> eventmap = new HashMap<Integer, AreaEvent.Builder>();
		if(keyvalue.isEmpty())
			buildDaguanEvent();
		for(String value : keyvalue.values()) {
			AreaEvent.Builder builder = AreaEvent.newBuilder();
			parseJson(value, builder);
			eventmap.put(builder.getId(), builder);
		}
		return eventmap;
	}
	public AreaEvent.Builder getMainEvent(int daguanid){
		String value = hget(RedisKey.MAINEVENT_CONFIG, daguanid+"");
		AreaEvent.Builder builder = AreaEvent.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder;
		buildDaguanEvent();
		value = hget(RedisKey.MAINEVENT_CONFIG, daguanid+"");
		if(value != null && parseJson(value, builder))
			return builder;
		else
			return null;
	}
	public EventExp getEventExp(int level){
		String value = hget(RedisKey.EVENTEXP_CONFIG, level+"");
		EventExp.Builder builder = EventExp.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		Map<String, String> keyvalue = new HashMap<String, String>();
		String xml = ReadConfig("ld_lvexp.xml");
		EventExpList.Builder list = EventExpList.newBuilder();
		parseXml(xml, list);
		for(EventExp exp : list.getDataList()){
			keyvalue.put(exp.getId()+"", formatJson(exp));
		}
		hputAll(RedisKey.EVENTEXP_CONFIG, keyvalue);
		value = hget(RedisKey.EVENTEXP_CONFIG, level+"");
		if(value != null && parseJson(value, builder))
			return builder.build();
		else
			return null;
	}

	private Map<Integer, EventConfig.Builder> getEventConfig(){
		Map<Integer, EventConfig.Builder> map = new HashMap<Integer, EventConfig.Builder>();
		String xml = ReadConfig("ld_event.xml");
		EventConfigList.Builder list = EventConfigList.newBuilder();
		parseXml(xml, list);
//		Map<Integer, EventConfig.Builder> eventmap = new HashMap<Integer, EventConfig.Builder>();
//		for(EventConfig.Builder event : list1.getDataBuilderList()){
//			eventmap.put(event.getId(), event);
//		}
		String xml2 = ReadConfig("ld_enemy.xml");
		EnemyList.Builder list2 = EnemyList.newBuilder();
		parseXml(xml2, list2);
		Map<Integer, Enemy.Builder> enemymap = new HashMap<Integer, Enemy.Builder>();
		for(Enemy.Builder enemyconfig : list2.getDataBuilderList()){
			enemymap.put(enemyconfig.getEnemyid(), enemyconfig);
		}
		for(EventConfig.Builder event : list.getDataBuilderList()){
			if(event.hasEnemygroup())
			for(Enemy.Builder enemy : event.getEnemygroupBuilder().getEnemyBuilderList()){
				Enemy.Builder enemyconfig = enemymap.get(enemy.getEnemyid());
				if(enemyconfig == null){
					logger.error("Event "+event.getId()+" cannot find enemy "+enemy.getEnemyid());
//					throw new RuntimeErrorException(null, "Event "+event.getId()+" cannot find enemy "+enemy.getEnemyid());
				}else {
					enemy.setLoot(enemyconfig.getLoot());
					enemy.setLootweight(enemyconfig.getLootweight());
				}
			}
			map.put(event.getId(), event);
		}
		return map;
	}
	private void buildDaguanEvent(){
		String xml1 = ReadConfig("ld_event1.xml");
		AreaEventList.Builder list1 = AreaEventList.newBuilder();
		parseXml(xml1, list1);
		String xml2 = ReadConfig("ld_event2.xml");
		AreaEventList.Builder list2 = AreaEventList.newBuilder();
		parseXml(xml2, list2);
		Map<Integer, EventConfig.Builder> eventmap = getEventConfig();
		for(AreaEvent.Builder area1 : list1.getDataBuilderList()){
			for(Event.Builder event1 : area1.getEventBuilderList()){
				for(AreaEvent.Builder area2 : list2.getDataBuilderList()){
					for(Event.Builder event2 : area2.getEventBuilderList()){
						if((event1.getTargetid() != 0 && event1.getCondition() != 0 ) 
								|| (event1.getTargetid() != 0 && event1.getEventid() == event2.getEventid()) 
								|| (event1.getCondition() == event2.getEventid() && event1.getDaguan() < event2.getDaguan())) {
							throw new RuntimeErrorException(null, "Error eventid "+event1.getEventid());
						}
					}
				}
			}
		}
		Map<Integer, Integer> areaMap = getAreaConfig();
		Map<Integer, AreaEvent.Builder> map1 = new HashMap<Integer, AreaEvent.Builder>();
		for(AreaEvent.Builder area : list1.getDataBuilderList()){
			for(int id : areaMap.keySet()){
				if(areaMap.get(id) == area.getId()){
					AreaEvent.Builder builder = AreaEvent.newBuilder();
					builder = AreaEvent.newBuilder();
					builder.setId(id);
					map1.put(builder.getId(), builder);
				}
			}
			for(Event.Builder event : area.getEventBuilderList()){
				if(event.getDaguan() != 0){
					AreaEvent.Builder builder = map1.get(event.getDaguan());
					if(builder != null){
						EventConfig.Builder config = eventmap.get(event.getEventid());
						if(config == null)
							throw new RuntimeErrorException(null, "Error eventconfig eventid "+event.getEventid());
						if(config.getDaguan() == 0)
//							throw new RuntimeErrorException(null, "Error daguan eventid "+config.getId());
							config.setDaguan(event.getDaguan());
						event.setName(config.getName());
						event.setOrder(builder.getEventCount());
						builder.addEvent(event);
						for(int i = 1; i < event.getEventcount(); i++) {
							Event.Builder newevent = Event.newBuilder(event.build());
							newevent.setOrder(builder.getEventCount());
							builder.addEvent(newevent);
						}
					}
				}
			}
		}
		Map<Integer, AreaEvent.Builder> map2 = new HashMap<Integer, AreaEvent.Builder>();
		for(AreaEvent.Builder area : list2.getDataBuilderList()){
			for(int id : areaMap.keySet()){
				if(areaMap.get(id) == area.getId()){
					AreaEvent.Builder builder = AreaEvent.newBuilder();
					builder = AreaEvent.newBuilder();
					builder.setId(id);
					map2.put(builder.getId(), builder);
				}
			}
			for(Event.Builder event : area.getEventBuilderList()){
				if(event.getDaguan() != 0){
					AreaEvent.Builder builder = map2.get(event.getDaguan());
					if(builder != null){
						EventConfig.Builder config = eventmap.get(event.getEventid());
						if(config == null)
							throw new RuntimeErrorException(null, "Error eventconfig eventid "+event.getEventid());
						if(config.getDaguan() == 0)
							config.setDaguan(event.getDaguan());
						event.setName(config.getName());
						event.setOrder(builder.getEventCount());
						builder.addEvent(event);
						builder.setWeight(builder.getWeight()+event.getWeight());
					}
				}
			}
			for(Event.Builder event : area.getEventBuilderList()){
				if(event.getDaguan() == 0){
					for(AreaEvent.Builder builder : map2.values()){
						if(areaMap.get(builder.getId()) == area.getId()){
							EventConfig.Builder config = eventmap.get(event.getEventid());
							if(config == null)
								throw new RuntimeErrorException(null, "Error eventconfig eventid "+event.getEventid());
							event.setName(config.getName());
							event.setDaguan(builder.getId());
							event.setOrder(builder.getEventCount());
							builder.addEvent(event);
							builder.setWeight(builder.getWeight()+event.getWeight());
						}
					}
				}
			}
		}
		Map<String, String> keyvalue1 = new HashMap<String, String>();
		for(AreaEvent.Builder daguan : map1.values())
			keyvalue1.put(daguan.getId()+"", formatJson(daguan.build()));
		Map<String, String> keyvalue2 = new HashMap<String, String>();
		for(AreaEvent.Builder daguan : map2.values())
			keyvalue2.put(daguan.getId()+"", formatJson(daguan.build()));
		Map<String, String> keyvalue3 = new HashMap<String, String>();
		for(EventConfig.Builder eventconfig : eventmap.values())
			keyvalue3.put(eventconfig.getId()+"", formatJson(eventconfig.build()));
		hputAll(RedisKey.MAINEVENT_CONFIG, keyvalue1);
		hputAll(RedisKey.DAGUANEVENT_CONFIG, keyvalue2);
		hputAll(RedisKey.EVENT_CONFIG, keyvalue3);
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
		Map<Integer, Integer> merlevelMap = getMerlevelConfig();
		Map<Integer, Daguan.Builder> map = new HashMap<Integer, Daguan.Builder>();
		for(Daguan.Builder daguan : list.getDataBuilderList()){
			// daguan.clearName();
//			daguan.clearDes();
			daguan.setAreaid(areaMap.get(daguan.getId()));
			daguan.setMerlevel(merlevelMap.get(daguan.getId()));
			keyvalue.put(daguan.getId()+"", formatJson(daguan.build()));
			map.put(daguan.getId(), daguan);
		}
		hputAll(RedisKey.DAGUAN_CONFIG, keyvalue);
		return map.get(id);
	}
// 	public Event getEvent(int eventid){
// 		String value = hget(RedisKey.EVENT_CONFIG, eventid+"");
// 		Event.Builder builder = Event.newBuilder();
// 		if(value != null && parseJson(value, builder))
// 			return builder.build();
// 		buildEvent();
// 		value = hget(RedisKey.EVENT_CONFIG, eventid+"");
// 		if(value != null && parseJson(value, builder))
// 			return builder.build();
// 		else
// 			return null;
// 	}
// 	public List<Event> getEvents(){
// 		Map<String, String> keyvalue = hget(RedisKey.EVENT_CONFIG);
// 		if(keyvalue.isEmpty())
// 			buildEvent();
// 		keyvalue = hget(RedisKey.EVENT_CONFIG);
// 		List<Event> events = new ArrayList<Event>();
// 		for(String value : keyvalue.values()) {
// 			Event.Builder builder = Event.newBuilder();
// 			if(parseJson(value, builder))
// 				events.add(builder.build());
// 		}
// 		return events;
// 	}
// 	public Map<String, Event> getEventMap(){
// 		Map<String, String> keyvalue = hget(RedisKey.EVENT_CONFIG);
// 		if(keyvalue.isEmpty()){
// 			buildEvent();
// 			keyvalue = hget(RedisKey.EVENT_CONFIG);
			
// 		}
		
// 		Map<String, Event> map = new HashMap<String, Event>();
// 		for(Entry<String, String> entry : keyvalue.entrySet()){
// 			Event.Builder builder = Event.newBuilder();
// 			if(parseJson(entry.getValue(), builder))
// 				map.put(entry.getKey(), builder.build());
// 		}
// 		return map;
		
// 	}
// 	public AreaEvent.Builder getDaguanEvent(int daguanid){
// 		String value = hget(RedisKey.DAGUANEVENT_CONFIG, daguanid+"");
// 		AreaEvent.Builder builder = AreaEvent.newBuilder();
// 		if(value != null && parseJson(value, builder))
// 			return builder;
// 		buildEvent();
// 		value = hget(RedisKey.DAGUANEVENT_CONFIG, daguanid+"");
// 		if(value != null && parseJson(value, builder))
// 			return builder;
// 		else
// 			return null;
// 	}
// 	private void buildEvent(){
// 		Map<String, String> keyvalue = new HashMap<String, String>();
// 		Map<String, String> keyvalue2 = new HashMap<String, String>();
// 		String xml = ReadConfig("event/ld_event.xml");
// 		AreaEventList.Builder list = AreaEventList.newBuilder();
// 		parseXml(xml, list);
// 		String xml1 = ReadConfig("event/ld_event1.xml");
// 		AreaEventList.Builder list1 = AreaEventList.newBuilder();
// 		parseXml(xml1, list1);
// 		Map<Integer, AreaEvent.Builder> map = new HashMap<Integer, AreaEvent.Builder>();
// 		Map<Integer, Integer> areaMap = getAreaConfig();
// 		for(AreaEvent.Builder area : list.getIdBuilderList()){
// 			for(int id : areaMap.keySet()){
// 				if(areaMap.get(id) == area.getId()){
// 					AreaEvent.Builder builder = AreaEvent.newBuilder();
// 					builder = AreaEvent.newBuilder();
// 					builder.setId(id);
// 					map.put(builder.getId(), builder);
// 				}
// 			}
// 			for(Event.Builder event : area.getEventBuilderList()){
// 				// event.clearName();
// 				event.clearDes();
// 				keyvalue2.put(event.getEventid()+"", formatJson(event.build()));
// 				event.clearReward();
// 				if(event.getDaguan() != 0){
// 					AreaEvent.Builder builder = map.get(event.getDaguan());
// 					if(builder != null){
// 						event.setWeight(0);
// 						builder.addEvent(event);
// 						// builder.setWeight(builder.getWeight()+event.getWeight());
// 					}
// 				}
// 			}
// 		}
// 		for(AreaEvent.Builder area : list1.getIdBuilderList()){
// 			for(Event.Builder event : area.getEventBuilderList()){
// 				// event.clearName();
// 				event.clearDes();
// 				keyvalue2.put(event.getEventid()+"", formatJson(event.build()));
// 				event.clearReward();
// 				if(event.getDaguan() != 0){
// 					AreaEvent.Builder builder = map.get(event.getDaguan());
// 					if(builder != null){
// //						event.setWeight(0);
// 						builder.addEvent(event);
// 						builder.setWeight(builder.getWeight()+event.getWeight());
// 					}
// 				}
// 			}
// 			for(Event.Builder event : area.getEventBuilderList()){
// 				if(event.getDaguan() == 0){
// 					for(AreaEvent.Builder builder : map.values()){
// 						if(areaMap.get(builder.getId()) == area.getId()){
// 							builder.addEvent(event);
// 							builder.setWeight(builder.getWeight()+event.getWeight());
// 						}
// 					}
// 				}
// 			}
// 		}
// 		for(AreaEvent.Builder daguan : map.values())
// 			keyvalue.put(daguan.getId()+"", formatJson(daguan.build()));
// 		hputAll(RedisKey.DAGUANEVENT_CONFIG, keyvalue);
// 		hputAll(RedisKey.EVENT_CONFIG, keyvalue2);
// 	}

// 	public Daguan.Builder getDaguan(int id){
// 		String value = hget(RedisKey.DAGUAN_CONFIG, id+"");
// 		Daguan.Builder builder = Daguan.newBuilder();
// 		if(value != null && parseJson(value, builder))
// 			return builder;
// 		Map<String, String> keyvalue = new HashMap<String, String>();
// 		String xml = ReadConfig("ld_daguan.xml");
// 		DaguanList.Builder list = DaguanList.newBuilder();
// 		parseXml(xml, list);
// 		Map<Integer, Integer> areaMap = getAreaConfig();
// 		Map<Integer, Integer> merlevelMap = getMerlevelConfig();
// 		Map<Integer, Daguan.Builder> map = new HashMap<Integer, Daguan.Builder>();
// 		for(Daguan.Builder daguan : list.getIdBuilderList()){
// 			// daguan.clearName();
// 			daguan.clearDes();
// 			daguan.setAreaid(areaMap.get(daguan.getId()));
// 			daguan.setMerlevel(merlevelMap.get(daguan.getId()));
// 			keyvalue.put(daguan.getId()+"", formatJson(daguan.build()));
// 			map.put(daguan.getId(), daguan);
// 		}
// 		hputAll(RedisKey.DAGUAN_CONFIG, keyvalue);
// 		return map.get(id);
// 	}

	public Map<String, Loot> getLootConfig(){
		Map<String, String> keyvalue = hget(RedisKey.LOOT_CONFIG);
		Map<String, Loot> map = new HashMap<String, Loot>();
		if (keyvalue.isEmpty()) {
			Map<String, String> redismap = new HashMap<String, String>();
			String xml = ReadConfig("ld_loot.xml");
			LootList.Builder list = LootList.newBuilder();
			parseXml(xml, list);
			for(Loot loot : list.getIdList()){
				map.put("" + loot.getOrder(), loot);
				redismap.put("" + loot.getOrder(), formatJson(loot));
			}
			hputAll(RedisKey.LOOT_CONFIG, redismap);
		} else {
			for(Entry<String, String> entry : keyvalue.entrySet()){
				Loot.Builder builder = Loot.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
		}
		
		return map;
	}

	public Map<Integer, Integer> getAreaConfig(){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		String xml = ReadConfig("ld_area.xml");
		AreaList.Builder list = AreaList.newBuilder();
		parseXml(xml, list);
		for(Area.Builder area : list.getDataBuilderList()){
			for(Daguan.Builder daguan : area.getDaguanBuilderList()){
				map.put(daguan.getId(), area.getAreaid());
			}
		}
		return map;
	}

	public Map<Integer, Integer> getMerlevelConfig(){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		String xml = ReadConfig("ld_area.xml");
		AreaList.Builder list = AreaList.newBuilder();
		parseXml(xml, list);
		for(Area.Builder area : list.getDataBuilderList()){
			for(Daguan.Builder daguan : area.getDaguanBuilderList()){
				map.put(daguan.getId(), area.getMerlevel());
			}
		}
		return map;
	}
	
	public EventRandom nextEventLevel(UserLevelBean userLevel) {
		userLevel.setEventRandom(userLevel.getEventRandom()+1);
		String value = hget(RedisKey.EVENTLEVEL_CONFIG, userLevel.getEventRandom()+"");
		EventRandom.Builder builder = EventRandom.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		value = get(RedisKey.EVENTLEVELSEED_CONFIG);
		if(value == null)
			value = buildEventLevel();
		EventRandomsList.Builder list = EventRandomsList.newBuilder();
		parseJson(value, list);
		userLevel.setEventRandom(list.getId((nextInt(list.getIdCount()))).getId());
		value = hget(RedisKey.EVENTLEVEL_CONFIG, userLevel.getEventRandom()+"");
		parseJson(value, builder);
		return builder.build();
	}
	
	private String buildEventLevel() {
		String xml = ReadConfig("ld_eventrandom.xml");
		EventRandomsList.Builder builder = EventRandomsList.newBuilder();
		parseXml(xml, builder);
		xml = ReadConfig("ld_eventlevel.xml");
		EventLevelList.Builder list = EventLevelList.newBuilder();
		parseXml(xml, list);
		for(EventRandoms.Builder randoms : builder.getIdBuilderList()){
			int i = 0;
			for(EventRandom.Builder eventlevel : randoms.getOrderBuilderList()){
				i++;
				for(EventLevel level : list.getLevelList()){
					if(level.getLevel() == eventlevel.getLevel1())
						eventlevel.setCount1(level.getCount());
					if(level.getLevel() == eventlevel.getLevel2())
						eventlevel.setCount2(level.getCount());
					if(level.getLevel() == eventlevel.getLevel3())
						eventlevel.setCount3(level.getCount());
					if(level.getLevel() == eventlevel.getLevel4())
						eventlevel.setCount4(level.getCount());
				}
				hput(RedisKey.EVENTLEVEL_CONFIG, randoms.getId()*100+i+"", formatJson(eventlevel.build()));
			}
			randoms.clearOrder();
			randoms.setId(randoms.getId()*100+1);
		}
		String value = formatJson(builder.build());
		set(RedisKey.EVENTLEVELSEED_CONFIG, value);
		return value;
	}
	
	public List<RewardBean> getNewplayReward(UserBean user, int levelId) {
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		
		if (user.getFirstGetHeroId() == 0)
			return rewardList;
		HeroChoice heroChoice = heroRedisService.getHerochoice(user.getFirstGetHeroId());
		if (heroChoice == null)
			return rewardList;
		switch (levelId) {
			case NEWPLAY_LEVEL_1:
				rewardList.add(RewardBean.init(heroChoice.getEvent1(), 1));
				break;
			case NEWPLAY_LEVEL_2:
				rewardList.add(RewardBean.init(heroChoice.getEvent2(), 1));
				rewardList.add(RewardBean.init(heroChoice.getEvent3(), 1));
				break;
			case NEWPLAY_LEVEL_3:
				rewardList.add(RewardBean.init(heroChoice.getEvent4(), 1));			
				rewardList.add(RewardBean.init(heroChoice.getEvent5(), 1));
				break;
			default:
				break;
		}
		
		return rewardList;
	}

	public boolean isMainEvent(int eventId) {
		Map<Integer, AreaEvent.Builder> eventmap = getMainEvent();
		for(AreaEvent.Builder areaevent : eventmap.values()) {
			for(Event event : areaevent.getEventList()){
				if(eventId == event.getEventid())
					return true;
			}
		}
		return false;
	}
	public boolean hasCompleteTarget(UserBean user, int targetId) {
		UserLevelBean userLevel = getUserLevel(user);
		Map<Integer, Event.Builder> eventmap = getEvents(user, userLevel);
		Map<Integer, AreaEvent.Builder> areaevents = getMainEvent();
		int eventId = 0;
		for(AreaEvent.Builder areaevent : areaevents.values())
			for(Event event : areaevent.getEventList())
				if(event.getTargetid() == targetId) {
					eventId = event.getEventid();
					break;
				}
		if(eventId == 0)
			return false;
		return hasCompleteEvent(user.getId(), eventId, userLevel, eventmap);
	}
	public boolean hasCompleteEvent(UserBean user, int eventId) {
		UserLevelBean userLevel = getUserLevel(user);
		Map<Integer, Event.Builder> eventmap = getEvents(user, userLevel);
		return hasCompleteEvent(user.getId(), eventId, userLevel, eventmap);
	}
	public boolean hasCompleteEvent(long userId, int eventId, UserLevelBean userLevel, Map<Integer, Event.Builder> eventmap) {
		EventConfig eventconfig = getEvent(eventId);
		if (eventconfig.getDaguan() > userLevel.getUnlockDaguan())
			return false;

		for (Event.Builder userEvent : eventmap.values()) {
			if (userEvent.getEventid() == eventId)
				return false;
		}
		
		return true;
	}
	
	public List<UserTalent> unlockZhujue(UserBean user, Event event) {
		List<UserTalent> userTalentList = new ArrayList<UserTalent>();
		int targetId = event.getTargetid();
		if (targetId < 500 || targetId > 509) 
			return userTalentList;
		
		if (targetId == 500) {
			Map<String, HeroChoice> map = heroRedisService.getHerochoiceConfig();
			for (HeroChoice choice : map.values()) {
				if (choice.getId() != user.getFirstGetHeroId()) {
					UserTalent userTalent = userTalentService.initTalent(user, choice.getId());
					if (userTalent != null)
						userTalentList.add(userTalent);
				}
			}
		} else {
			UserTalent userTalent = userTalentService.initTalent(user, targetId - 500);
			if (userTalent != null)
				userTalentList.add(userTalent);
		}
		
		return userTalentList;
	}
	
	public ResultConst buySavingBox(UserBean user, int type) {
		SavingBox savingBox = null;
		if (type == RewardConst.EXP) {
			savingBox = lootRedisService.getSavingBox(user.getExpSavingBox() + 1);
			if (savingBox == null) {
				return ErrorConst.SAVINGBOX_LEVEL_IS_LIMIT_ERROR;
			}
			if (!costService.cost(user, savingBox.getExp().getCost().getItemid(), savingBox.getExp().getCost().getCount()))
				return ErrorConst.NOT_ENOUGH_COIN;
			
			user.setExpSavingBox(user.getExpSavingBox() + 1);
		} else if (type == RewardConst.COIN) {
			savingBox = lootRedisService.getSavingBox(user.getGoldSavingBox() + 1);
			if (savingBox == null) {
				return ErrorConst.SAVINGBOX_LEVEL_IS_LIMIT_ERROR;
			}
			if (!costService.cost(user, savingBox.getGold().getCost().getItemid(), savingBox.getGold().getCost().getCount()))
				return ErrorConst.NOT_ENOUGH_JEWEL;
			
			user.setGoldSavingBox(user.getGoldSavingBox() + 1);
		}
		
		if (savingBox == null) {
			return ErrorConst.SAVINGBOX_BUY_TYPE_ERROR;
		}
		
		userService.updateUser(user);
		
		return SuccessConst.PURCHASE_SUCCESS;
	}
}
