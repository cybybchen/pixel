package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.mapper.UserLevelMapper;
import com.trans.pixel.model.userinfo.EventBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.HeroProto.HeroChoice;
import com.trans.pixel.protoc.UserInfoProto.Area;
import com.trans.pixel.protoc.UserInfoProto.AreaEvent;
import com.trans.pixel.protoc.UserInfoProto.AreaEventList;
import com.trans.pixel.protoc.UserInfoProto.AreaList;
import com.trans.pixel.protoc.UserInfoProto.Daguan;
import com.trans.pixel.protoc.UserInfoProto.DaguanList;
import com.trans.pixel.protoc.UserInfoProto.Event;
import com.trans.pixel.protoc.UserInfoProto.EventLevel;
import com.trans.pixel.protoc.UserInfoProto.EventLevelList;
import com.trans.pixel.protoc.UserInfoProto.EventRandoms;
import com.trans.pixel.protoc.UserInfoProto.EventRandomsList;
import com.trans.pixel.protoc.UserInfoProto.Loot;
import com.trans.pixel.protoc.UserInfoProto.LootList;

@Repository
public class LevelRedisService extends RedisService {
//	private static Logger logger = Logger.getLogger(LevelRedisService.class);
	private static final int NEWPLAY_LEVEL_1 = 1012;
	private static final int NEWPLAY_LEVEL_2 = 1013;
	private static final int NEWPLAY_LEVEL_3 = 1014;
	@Resource
	private UserLevelMapper mapper;
	@Resource
	private HeroRedisService heroRedisService;
	
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
			userLevel.setEventTime((int)now());
			userLevel.setUnlockDaguan(daguan.getId());
			userLevel.setLeftCount(daguan.getCount());
			userLevel.setLootDaguan(daguan.getId());
			userLevel.setCoin(daguan.getGold());
			userLevel.setExp(daguan.getExperience());
			AreaEvent.Builder events = getDaguanEvent(daguan.getId());
			for(Event.Builder event : events.getEventBuilderList()){
				if(daguan.getId() == event.getDaguan() && event.getWeight() == 0){
					event.setOrder(events.getId()*30+event.getOrder());
					saveEvent(userId, event.build());
				}
			}
		}else{
			Daguan.Builder daguan = getDaguan(userLevel.getLootDaguan());
			userLevel.setCoin(daguan.getGold());
			userLevel.setExp(daguan.getExperience());
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
	public Map<Integer, Event.Builder> getEvents(UserLevelBean userLevel) {
		Map<String, String> keyvalue = hget(RedisKey.USEREVENT_PREFIX+userLevel.getUserId());
		Map<Integer, Event.Builder> map = new TreeMap<Integer, Event.Builder>();
		if(keyvalue.isEmpty() && now() - userLevel.getEventTime() > 24*3600){
			List<EventBean> list = mapper.getEvents(userLevel.getUserId());
			for(EventBean bean : list){
				Event.Builder event = bean.build();
				Event config = getEvent(event.getEventid());
				event.setOrder(config.getDaguan()*30+config.getOrder());
				event.setName(config.getName());
				event.setType(config.getType());
				event.setCostid(config.getCostid());
				event.setCostcount(event.getCostcount());
				event.setCall(config.getCall());
				saveEvent(userLevel.getUserId(), event.build());
				map.put(event.getOrder(), event);
			}
		}
		for(String value : keyvalue.values()){
			Event.Builder event = Event.newBuilder();
			if(parseJson(value, event))
				map.put(event.getOrder(), event);
		}
		return map;
	}
	// public boolean hasEvent(UserBean user){
	// 	for(Event.Builder event : getEvents(user).values()){
	// 		if(event.getOrder() < 100)
	// 			return true;
	// 	}
	// 	return false;
	// }
	public void productEvent(UserBean user, UserLevelBean userLevel){
		long time = now() - userLevel.getEventTime();
		long eventsize = hlen(RedisKey.USEREVENT_PREFIX+user.getId());
		if(eventsize >= 20){
			userLevel.setEventTime((int)now());
			saveUserLevel(userLevel);
		}else if(time >= 60){
			AreaEvent.Builder events = getDaguanEvent(userLevel.getLootDaguan());
			if(events != null){
				for(Event.Builder myevent : getEvents(userLevel).values()){
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
				for(int i = 0; i < Math.min(20-eventsize,time/60); i++){
					int weight = nextInt(events.getWeight());
					for(Event.Builder event : events.getEventBuilderList()){
						weight -= event.getWeight();
						if(weight <= 0){
							Event.Builder builder = Event.newBuilder(event.build());
							builder.setOrder(currentIndex()+i);
							builder.setDaguan(userLevel.getLootDaguan());
							EventLevel level = nextEventLevel(userLevel);
							builder.setLevel(level.getLevel());
							builder.setCount(level.getCount());
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
			userLevel.setEventTime(userLevel.getEventTime()+(int)time/60*60);
			saveUserLevel(userLevel);
		}
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
		Event event = getEvent(userId, order);
		if(event != null){
			EventBean bean = new EventBean();
			mapper.updateEvent(bean);
		}
	}
	public String popDelEventKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX+"Del");
	}
	public void updateDelEventToDB(long userId, int eventid){
		mapper.delEvent(userId, eventid);
	}
	public void saveEvent(long userId, Event event) {
		hput(RedisKey.USEREVENT_PREFIX+userId, event.getOrder()+"", formatJson(event));
		expire(RedisKey.USEREVENT_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		if(event.getOrder() < 10000)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX, userId+"|"+event.getOrder());
	}
	public void saveEvent(UserBean user, Event event) {
		saveEvent(user.getId(), event);
	}
	public void delEvent(UserBean user, Event event) {
		hdelete(RedisKey.USEREVENT_PREFIX+user.getId(), event.getOrder()+"");
		if(event.getOrder() < 10000)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USEREVENT_PREFIX+"Del", user.getId()+"|"+event.getEventid());
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
	public List<Event> getEvents(){
		Map<String, String> keyvalue = hget(RedisKey.EVENT_CONFIG);
		if(keyvalue.isEmpty())
			buildEvent();
		keyvalue = hget(RedisKey.EVENT_CONFIG);
		List<Event> events = new ArrayList<Event>();
		for(String value : keyvalue.values()) {
			Event.Builder builder = Event.newBuilder();
			if(parseJson(value, builder))
				events.add(builder.build());
		}
		return events;
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
		String xml1 = ReadConfig("ld_event1.xml");
		AreaEventList.Builder list1 = AreaEventList.newBuilder();
		parseXml(xml1, list1);
		Map<Integer, AreaEvent.Builder> map = new HashMap<Integer, AreaEvent.Builder>();
		Map<Integer, Integer> areaMap = getAreaConfig();
		for(AreaEvent.Builder area : list.getIdBuilderList()){
			for(int id : areaMap.keySet()){
				if(areaMap.get(id) == area.getId()){
					AreaEvent.Builder builder = AreaEvent.newBuilder();
					builder = AreaEvent.newBuilder();
					builder.setId(id);
					map.put(builder.getId(), builder);
				}
			}
			for(Event.Builder event : area.getEventBuilderList()){
				// event.clearName();
				event.clearDes();
				keyvalue2.put(event.getEventid()+"", formatJson(event.build()));
				event.clearReward();
				if(event.getDaguan() != 0){
					AreaEvent.Builder builder = map.get(event.getDaguan());
					if(builder != null){
						event.setWeight(0);
						builder.addEvent(event);
						// builder.setWeight(builder.getWeight()+event.getWeight());
					}
				}
			}
		}
		for(AreaEvent.Builder area : list1.getIdBuilderList()){
			for(Event.Builder event : area.getEventBuilderList()){
				// event.clearName();
				event.clearDes();
				keyvalue2.put(event.getEventid()+"", formatJson(event.build()));
				event.clearReward();
				if(event.getDaguan() != 0){
					AreaEvent.Builder builder = map.get(event.getDaguan());
					if(builder != null){
//						event.setWeight(0);
						builder.addEvent(event);
						builder.setWeight(builder.getWeight()+event.getWeight());
					}
				}
			}
			for(Event.Builder event : area.getEventBuilderList()){
				if(event.getDaguan() == 0){
					for(AreaEvent.Builder builder : map.values()){
						if(areaMap.get(builder.getId()) == area.getId()){
							builder.addEvent(event);
							builder.setWeight(builder.getWeight()+event.getWeight());
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
		Map<Integer, Integer> merlevelMap = getMerlevelConfig();
		Map<Integer, Loot> lootMap = getLootConfig();
		Map<Integer, Daguan.Builder> map = new HashMap<Integer, Daguan.Builder>();
		for(Daguan.Builder daguan : list.getIdBuilderList()){
			// daguan.clearName();
			daguan.clearDes();
			daguan.setAreaid(areaMap.get(daguan.getId()));
			daguan.setMerlevel(merlevelMap.get(daguan.getId()));
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

	public Map<Integer, Integer> getMerlevelConfig(){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		String xml = ReadConfig("ld_area.xml");
		AreaList.Builder list = AreaList.newBuilder();
		parseXml(xml, list);
		for(Area.Builder area : list.getAreaBuilderList()){
			for(Daguan.Builder daguan : area.getIdBuilderList()){
				map.put(daguan.getId(), area.getMerlevel());
			}
		}
		return map;
	}
	
	public EventLevel nextEventLevel(UserLevelBean userLevel) {
		userLevel.setEventRandom(userLevel.getEventRandom()+1);
		String value = hget(RedisKey.EVENTLEVEL_CONFIG, userLevel.getEventRandom()+"");
		EventLevel.Builder builder = EventLevel.newBuilder();
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
			for(EventLevel.Builder eventlevel : randoms.getOrderBuilderList()){
				i++;
				for(EventLevel level : list.getLevelList()){
					if(level.getLevel() == eventlevel.getLevel())
						hput(RedisKey.EVENTLEVEL_CONFIG, randoms.getId()*100+i+"", formatJson(level));
				}
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
}
