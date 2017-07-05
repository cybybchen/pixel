package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPvpBuffBean;
import com.trans.pixel.protoc.PVPProto.PVPBoss;
import com.trans.pixel.protoc.PVPProto.PVPBossConfig;
import com.trans.pixel.protoc.PVPProto.PVPBossConfigList;
import com.trans.pixel.protoc.PVPProto.PVPBossList;
import com.trans.pixel.protoc.PVPProto.PVPDayConfig;
import com.trans.pixel.protoc.PVPProto.PVPEvent;
import com.trans.pixel.protoc.PVPProto.PVPEventList;
import com.trans.pixel.protoc.PVPProto.PVPMap;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.PVPProto.PVPMine;
import com.trans.pixel.protoc.PVPProto.PVPPosition;
import com.trans.pixel.protoc.PVPProto.PVPPositionList;
import com.trans.pixel.protoc.PVPProto.PVPPositionLists;
import com.trans.pixel.utils.DateUtil;

@Repository
public class PvpMapRedisService extends RedisService{
	private static Logger logger = Logger.getLogger(PvpMapRedisService.class);
	@Resource
	private UserRedisService userRedisService;
	
	public PVPMapList.Builder getMapList(long userId, int pvpUnlock) {
		String value = hget(RedisKey.USERDATA + userId, "PvpMap");
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		PVPMapList.Builder maplist = getBasePvpMapList();
		if(value != null && parseJson(value, builder)) {
			for(PVPMap.Builder map : maplist.getDataBuilderList()){
				for(PVPMap.Builder map2 : builder.getDataBuilderList()){
					 if(map.getFieldid() == map2.getFieldid())
						map.setOpened(map2.getOpened());
				}
			}
			return maplist;
		}
//		maplist.getFieldBuilder(0).setOpened(true);
		for(PVPMap.Builder map : maplist.getDataBuilderList()){
			 if(map.getFieldid() <= pvpUnlock)
				map.setOpened(true);
		}
		saveMapList(maplist.build(), userId);
		return maplist;
	}
	
//	public boolean isMapOpen(UserBean user, int mapId){
//		PVPMapList.Builder maplist = getMapList(user.getId(), user.getPvpUnlock());
//		for(PVPMap map : maplist.getFieldList()){
//			if(map.getFieldid() == mapId && map.getOpened())
//				return true;
//		}
//		return false;
//	}
	
	public void saveMapList(PVPMapList maplist, long userId) {
		PVPMapList.Builder builder = PVPMapList.newBuilder(maplist);
		for(PVPMap.Builder field : builder.getDataBuilderList()) {
			field.clearKuangdian();
			field.clearBuffimg();
		}
		hput(RedisKey.USERDATA + userId, "PvpMap", formatJson(builder.build()));
	}

	public Map<Integer, UserPvpBuffBean> getUserBuffs(UserBean user, PVPMapList.Builder maplist) {
		Map<String, String> keyvalue = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId());
		Map<Integer, UserPvpBuffBean> buffmap = new HashMap<Integer, UserPvpBuffBean>();
		for(String value : keyvalue.values()) {
			UserPvpBuffBean bean = (UserPvpBuffBean)fromJson(value, UserPvpBuffBean.class);
			for(PVPMap map : maplist.getDataList()){
				if(map.getFieldid() == bean.getBuffid() || bean.getBuffid() == 0){
					if(bean.getBuff() > map.getBufflimit())
						bean.setBuff(map.getBufflimit());
					if(bean.getMaxbuff() > map.getBufflimit())
						bean.setMaxbuff(map.getBufflimit());
					break;
				}
			}
			buffmap.put(bean.getBuffid(), bean);
		}
		return buffmap;
	}

//	public int getUserBuff(UserBean user, int id) {
//		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), id+"");
//		if(value != null)
//			return Integer.parseInt(value);
//		return 0;
//	}

	public int addUserBuff(UserBean user, int buffid, int buffcount) {
		String value = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), buffid+"");
		UserPvpBuffBean bean = new UserPvpBuffBean();
		if(value != null){
			bean = (UserPvpBuffBean)fromJson(value, UserPvpBuffBean.class);
		}else{
			bean.setUserid(user.getId());
			bean.setBuffid(buffid);
		}
		bean.setBuff(bean.getBuff() + buffcount);
		if(bean.getBuff() > bean.getMaxbuff())
			bean.setMaxbuff(bean.getBuff());
		saveUserBuff(user, bean);
		return bean.getBuff();
	}

	public void deleteUserBuff(UserBean user) {
//		delete(RedisKey.PVPMAPBUFF_PREFIX+user.getId());
		Map<String, String> keyvalue = hget(RedisKey.PVPMAPBUFF_PREFIX+user.getId());
		for(String value : keyvalue.values()) {
			UserPvpBuffBean bean = (UserPvpBuffBean)fromJson(value, UserPvpBuffBean.class);
			bean.setBuff(0);
			keyvalue.put(bean.getBuffid()+"", toJson(bean));
		}
		hputAll(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), keyvalue);
	}
	
	public void saveUserBuff(UserBean user, UserPvpBuffBean buff) {
		hput(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), buff.getBuffid()+"", toJson(buff));
		expire(RedisKey.PVPMAPBUFF_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public Map<String, PVPMine> getUserMines(long userId) {
		Map<String, PVPMine> map = new HashMap<String, PVPMine>();
		Map<String, String> keyvalue = hget(RedisKey.PVPMINE_PREFIX+userId);
		for(Entry<String, String> entry : keyvalue.entrySet()){
			PVPMine.Builder builder = PVPMine.newBuilder();
			if(parseJson(entry.getValue(), builder))
				map.put(entry.getKey(), builder.build());
		}
		return map;
	}

	public PVPEvent getEvent(UserBean user, int positionid) {
		PVPEvent.Builder builder = PVPEvent.newBuilder();
		String value = hget(RedisKey.PVPMONSTER_PREFIX+user.getId(), positionid+"");
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}
		PVPEventList.Builder bosses = PVPEventList.newBuilder();
		value = get(RedisKey.PVPBOSS_PREFIX+user.getId());
		if(value != null){
			parseJson(value, bosses);
			for(PVPEvent boss : bosses.getDataList()){
				if(boss.getPositionid() == positionid)
					return boss;
			}
		}
		return null;
	}

	public void deleteEvent(UserBean user, int positionid) {
		hdelete(RedisKey.PVPMONSTER_PREFIX+user.getId(), positionid+"");
	}

	public void deleteBoss(UserBean user, int positionid) {
		delete(RedisKey.PVPBOSS_PREFIX+user.getId());
	}

	public PVPMine getMine(long userId, int id) {
		PVPMine.Builder builder = PVPMine.newBuilder();
		if(id > 1000){
			String value = hget(RedisKey.PVPINBREAK_PREFIX+userId, id+"");
			if(value != null && parseJson(value, builder)){
				return builder.build();
			}
		}else{
			String value = hget(RedisKey.PVPMINE_PREFIX+userId, id+"");
			if(value != null && parseJson(value, builder)){
				return builder.build();
			}
		}
		return null;
	}

	public void delInbreakList(long userId) {
		delete(RedisKey.PVPINBREAK_PREFIX+userId);
	}
	
	public void saveInbreakList(long userId, List<PVPMine> mines) {
//		delete(RedisKey.PVPINBREAK_PREFIX+userId);
		Map<String, String> map = new HashMap<String, String>();
		for(PVPMine mine : mines){
			map.put(mine.getId()+"", formatJson(mine));
		}
		hputAll(RedisKey.PVPINBREAK_PREFIX+userId, map);
		expire(RedisKey.PVPINBREAK_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void saveMine(long userId, PVPMine mine) {
		hput(RedisKey.PVPMINE_PREFIX+userId, mine.getId()+"", formatJson(mine));
		expire(RedisKey.PVPMINE_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public void saveMines(long userId, Map<String, PVPMine> mines) {
		Map<String, String> map = new HashMap<String, String>();
		for(Entry<String, PVPMine> entry : mines.entrySet()){
			map.put(entry.getKey(), formatJson(entry.getValue()));
		}
		hputAll(RedisKey.PVPMINE_PREFIX+userId, map);
		expire(RedisKey.PVPMINE_PREFIX+userId, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}

	public void deleteMine(long userId, int id) {
		hdelete(RedisKey.PVPMINE_PREFIX+userId, id+"");
	}

	public void deleteEvents(UserBean user) {
		delete(RedisKey.PVPMONSTER_PREFIX+user.getId());
	}

	public List<PVPEvent> getEvents(UserBean user, Map<Integer, UserPvpBuffBean> pvpMap){
		return getEvents(user, pvpMap, false);
	}

	public List<PVPEvent> getEvents(UserBean user, Map<Integer, UserPvpBuffBean> pvpMap, boolean forceRefresh) {
		boolean refreshBoss = today(0) > user.getPvpMonsterRefreshTime();
		boolean refreshEvent = canRefreshEvent(user);
		if(forceRefresh){
			refreshBoss = refreshEvent = true;
		}
		List<PVPEvent> events = new ArrayList<PVPEvent>();
		Map<String, String> keyvalue = this.hget(RedisKey.PVPMONSTER_PREFIX+user.getId());
		Iterator<Entry<String, String>> it = keyvalue.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, String> entry = it.next();
			String value = entry.getValue();
			PVPEvent.Builder builder = PVPEvent.newBuilder();
			if(parseJson(value, builder)){
				if(!builder.hasEndtime() || (DateUtil.timeIsAvailable(builder.getStarttime(), builder.getEndtime()) && !refreshEvent))
					events.add(builder.build());
				else{
					hdelete(RedisKey.PVPMONSTER_PREFIX+user.getId(), builder.getPositionid()+"");
					it.remove();
				}
			}
		}
		PVPEventList.Builder bosses = PVPEventList.newBuilder();
		String value = get(RedisKey.PVPBOSS_PREFIX+user.getId());
		if(value != null)
			parseJson(value, bosses);
		if(refreshEvent){
			Map<String, PVPMap> eventmap = getPvpMap();
			PVPEventList activityevents = getActivityEventConfig();
			Map<String, PVPPositionList> positionMap = getPositionConfig();
			PVPMapList.Builder pvpmap = getMapList(user.getId(), user.getPvpUnlock());
			if(refreshBoss){
				List<Integer> fieldids = new ArrayList<Integer>();
				for(PVPMap map : pvpmap.getDataList()){
					if(map.getOpened())
						fieldids.add(map.getFieldid());
				}
				if(fieldids.isEmpty())
					fieldids.add(pvpmap.getData(0).getFieldid());
				bosses.clearData();
				for(PVPBoss boss : getBossConfig().getDataList()) {
					if(boss.getDay() == weekday()){
						PVPEvent.Builder builder = PVPEvent.newBuilder();
						builder.setEventid(boss.getId());
//						Object[] fields = positionMap.keySet().toArray();
						builder.setFieldid(fieldids.get(nextInt(fieldids.size())));
						bosses.addData(builder);
					}
				}
			}
			for(PVPMap list : eventmap.values()){
				List<Integer> positionValues = new ArrayList<Integer>();
				int count = 0;
				for(PVPEvent event : events){
					if(event.getFieldid() == list.getFieldid()){
						positionValues.add(event.getPositionid());
						count++;
					}
				}
				for(PVPEvent.Builder bossbuilder : bosses.getDataBuilderList()){
					if(bossbuilder.getFieldid() == list.getFieldid()){
						if(!bossbuilder.hasPositionid()) {//随机boss位置
							PVPPositionList positions = positionMap.get(bossbuilder.getFieldid()+"");
							PVPPosition position = positions.getOrder(nextInt(positions.getOrderCount()));
							while(positionValues.contains(position.getOrder())){
								position = positions.getOrder(nextInt(positions.getOrderCount()));
							}
							bossbuilder.setPositionid(position.getOrder());
							bossbuilder.setX(position.getX());
							bossbuilder.setY(position.getY());
							UserPvpBuffBean buff = pvpMap.get(bossbuilder.getFieldid());
							int level = nextInt(11)-5;
							if(buff != null)
								level += buff.getBuff();
							if(level > 300)
								level = 300;
							else if(level < 1)
								level = 1;
							bossbuilder.setLevel(level);
							set(RedisKey.PVPBOSS_PREFIX+user.getId(), formatJson(bosses.build()));
						}
						events.add(bossbuilder.build());
						positionValues.add(bossbuilder.getPositionid());
					}
				}
				
				while(count < 5){//添加怪物
					int weight = 0;
					for(PVPEvent event : list.getEventList()){
						weight += event.getWeight();
					}
					weight = nextInt(weight);
					PVPEvent.Builder eventbuilder = null;
					for(PVPEvent event : list.getEventList()){
						if(weight < event.getWeight()){
							eventbuilder = PVPEvent.newBuilder(event);
							eventbuilder.setFieldid(list.getFieldid());
							break;
						}else{
							weight -= event.getWeight();
						}
					}
					PVPPositionList positions = positionMap.get(eventbuilder.getFieldid()+"");
					PVPPosition position = positions.getOrder(nextInt(positions.getOrderCount()));
					while(positionValues.contains(position.getOrder())){
						position = positions.getOrder(nextInt(positions.getOrderCount()));
					}
					positionValues.add(position.getOrder());
					eventbuilder.setPositionid(position.getOrder());
					eventbuilder.setX(position.getX());
					eventbuilder.setY(position.getY());
					UserPvpBuffBean buff = pvpMap.get(eventbuilder.getFieldid());
					int level = nextInt(11)-5;
					if(buff != null)
						level += buff.getBuff();
					if(level > 300)
						level = 300;
					else if(level < 1)
						level = 1;
					eventbuilder.setLevel(level);
					events.add(eventbuilder.build());
					keyvalue.put(eventbuilder.getPositionid()+"", formatJson(eventbuilder.build()));
					count++;
				}

				for(PVPEvent event : activityevents.getDataList()){//添加活动怪物
					if(!DateUtil.timeIsAvailable(event.getStarttime(), event.getEndtime()))
						continue;
					for(int i = 0; i < 2; i++){
						PVPEvent.Builder eventbuilder = PVPEvent.newBuilder(event);
						eventbuilder.setFieldid(list.getFieldid());
						PVPPositionList positions = positionMap.get(eventbuilder.getFieldid()+"");
						PVPPosition position = positions.getOrder(nextInt(positions.getOrderCount()));
						while(positionValues.contains(position.getOrder())){
							position = positions.getOrder(nextInt(positions.getOrderCount()));
						}
						positionValues.add(position.getOrder());
						eventbuilder.setPositionid(position.getOrder());
						eventbuilder.setX(position.getX());
						eventbuilder.setY(position.getY());
						UserPvpBuffBean buff = pvpMap.get(eventbuilder.getFieldid());
						int level = nextInt(11)-5;
						if(buff != null)
							level += buff.getBuff();
						if(level > 300)
							level = 300;
						else if(level < 1)
							level = 1;
						eventbuilder.setLevel(level);
						events.add(eventbuilder.build());
						keyvalue.put(eventbuilder.getPositionid()+"", formatJson(eventbuilder.build()));
					}
				}
			}
			hputAll(RedisKey.PVPMONSTER_PREFIX+user.getId(), keyvalue);
			expire(RedisKey.PVPMONSTER_PREFIX+user.getId(), RedisExpiredConst.EXPIRED_USERINFO_7DAY);
		}else{
			for(PVPEvent.Builder bossbuilder : bosses.getDataBuilderList())
				events.add(bossbuilder.build());
		}
		return events;
	}

//	public Map<String, PVPEventList> getEventConfig() {
//		Map<String, String> keyvalue = hget(RedisKey.PVPMONSTER_CONFIG);
//		if(keyvalue.isEmpty()){
//			Map<String, PVPEventList> map = buildEventConfig();
//			Map<String, String> redismap = new HashMap<String, String>();
//			for(Entry<String, PVPEventList> entry : map.entrySet()){
//				redismap.put(entry.getKey(), formatJson(entry.getValue()));
//			}
//			hputAll(RedisKey.PVPMONSTER_CONFIG, redismap);
//			return map;
//		}else{
//			Map<String, PVPEventList> map = new HashMap<String, PVPEventList>();
//			for(Entry<String, String> entry : keyvalue.entrySet()){
//				PVPEventList.Builder builder = PVPEventList.newBuilder();
//				if(parseJson(entry.getValue(), builder))
//					map.put(entry.getKey(), builder.build());
//			}
//			return map;
//		}
//	}
	
	public PVPEventList getActivityEventConfig() {
		String value = get(RedisKey.PVPACTIVITYMONSTER_CONFIG);
		PVPEventList.Builder builder = PVPEventList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			String xml = ReadConfig("ld_pvphuodong.xml");
			parseXml(xml, builder);
			set(RedisKey.PVPACTIVITYMONSTER_CONFIG, formatJson(builder.build()));
			return builder.build();
		}
	}
	
	public Map<String, PVPPositionList> getPositionConfig() {
		Map<String, String> keyvalue = hget(RedisKey.PVPPOSITION_CONFIG);
		if(keyvalue.isEmpty()){
			Map<String, PVPPositionList> map = buildPositionConfig();
			Map<String, String> redismap = new HashMap<String, String>();
			for(Entry<String, PVPPositionList> entry : map.entrySet()){
				redismap.put(entry.getKey(), formatJson(entry.getValue()));
			}
			hputAll(RedisKey.PVPPOSITION_CONFIG, redismap);
			return map;
		}else{
			Map<String, PVPPositionList> map = new HashMap<String, PVPPositionList>();
			for(Entry<String, String> entry : keyvalue.entrySet()){
				PVPPositionList.Builder builder = PVPPositionList.newBuilder();
				if(parseJson(entry.getValue(), builder))
					map.put(entry.getKey(), builder.build());
			}
			return map;
		}
	}
	
	public boolean canRefreshEvent(UserBean user){
		long times[] = {today(17), today(11), today(0)};
		for(long time : times){
			if(time > user.getPvpMonsterRefreshTime() && time < now()){
				user.setPvpMonsterRefreshTime(time);
				userRedisService.updateUser(user);
				return true;
			}
		}
		return false;
	}
	
	public PVPBossList getBossConfig() {
		PVPBossList.Builder builder = PVPBossList.newBuilder(); 
		String value = get(RedisKey.PVPBOSS_CONFIG);
		if(value != null && parseJson(value, builder)){
			return builder.build();
		}else{
			PVPBossConfigList.Builder configlist = PVPBossConfigList.newBuilder(); 
			String xml = ReadConfig("ld_pvpboss.xml");
			if(!parseXml(xml, configlist)){
				logger.warn("cannot build PVPBossLists");
				return builder.build();
			}
			for(PVPBossConfig config : configlist.getDataList()){
				for(PVPDayConfig day : config.getDayList()){
					PVPBoss.Builder boss = PVPBoss.newBuilder();
					boss.setDay(day.getDay());
					boss.setId(config.getId());
					builder.addData(boss);
				}
			}
			set(RedisKey.PVPBOSS_CONFIG, formatJson(builder.build()));
			return builder.build();
		}
	}

	public Map<String, PVPMap> getPvpMap(){
		Map<String, String> keyvalue = hget(RedisKey.PVPFIELD_CONFIG);
		Map<String, PVPMap> map = new HashMap<String, PVPMap>();
		if(!keyvalue.isEmpty()) {
			for(String value : keyvalue.values()) {
				PVPMap.Builder builder = PVPMap.newBuilder();
				parseJson(value, builder);
				map.put(builder.getFieldid()+"", builder.build());
			}
		}else {
			PVPMapList.Builder list = buildPvpMapList();
			for(PVPMap pvpmap : list.getDataList()) {
				keyvalue.put(pvpmap.getFieldid()+"", formatJson(pvpmap));
				map.put(pvpmap.getFieldid()+"", pvpmap);
			}
			hputAll(RedisKey.PVPFIELD_CONFIG, keyvalue);
		}
		return map;
	}

	public PVPMapList.Builder getBasePvpMapList(){
		String value = get(RedisKey.PVPMAP_CONFIG);
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		if(value != null && parseJson(value, builder)){
			return builder;
		}else{
			builder = buildPvpMapList();
			for(PVPMap.Builder map : builder.getDataBuilderList()) {
				map.clearEvent();
//				map.clearLootlist();
			}
			set(RedisKey.PVPMAP_CONFIG, formatJson(builder.build()));
			return builder;
		}
	}

	public PVPMapList.Builder buildPvpMapList(){
		String xml = ReadConfig("ld_pvpfield.xml");
		PVPMapList.Builder builder = PVPMapList.newBuilder();
		parseXml(xml, builder);
		return builder;
	}

//	public Map<String, PVPEventList> buildEventConfig(){
//		String xml = ReadConfig("ld_pvpenemy.xml");
//		PVPEventList.Builder builder = PVPEventList.newBuilder();
//		if(!parseXml(xml, builder)){
//			logger.warn("cannot build PVPEventLists");
//			return null;
//		}
//		Map<String, PVPEventList> map = new HashMap<String, PVPEventList>();
//		for(PVPEvent.Builder event : builder.getDataBuilderList()){
//			PVPEventList.Builder nbuilder = PVPEventList.newBuilder();
//			if(map.containsKey(event.getFieldid()+""))
//				nbuilder = PVPEventList.newBuilder(map.get(event.getFieldid()+""));
//			nbuilder.addData(event);
//			map.put(event.getFieldid()+"", nbuilder.build());
//		}
//		return map;
//	}

	public Map<String, PVPPositionList> buildPositionConfig(){
		String xml = ReadConfig("ld_pvpposition.xml");
		PVPPositionLists.Builder builder = PVPPositionLists.newBuilder();
		if(!parseXml(xml, builder)){
			logger.warn("cannot build PVPPositionLists");
			return null;
		}
		Map<String, PVPPositionList> map = new HashMap<String, PVPPositionList>();
		for(PVPPositionList.Builder positionlist : builder.getDataBuilderList()){
			for(PVPPosition.Builder position : positionlist.getOrderBuilderList())
				position.setOrder(position.getOrder()+positionlist.getFieldid()*100);
			map.put(positionlist.getFieldid()+"", positionlist.build());
		}
		return map;
	}
}
