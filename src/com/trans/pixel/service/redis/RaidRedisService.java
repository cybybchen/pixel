package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.mapper.RaidMapper;
import com.trans.pixel.model.userinfo.RaidBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.RaidList;
import com.trans.pixel.protoc.TaskProto.ResponseRaidCommand;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.protoc.UserInfoProto.EventConfigList;
import com.trans.pixel.service.cache.CacheService;
import com.trans.pixel.utils.DateUtil;

@Repository
public class RaidRedisService extends RedisService{
	Logger logger = Logger.getLogger(RaidRedisService.class);
	@Resource
	private RaidMapper mapper;
	
	public RaidRedisService() {
		buildRaidLevelConfig();
		buildRaidListConfig();
	}

	public ResponseRaidCommand.Builder getRaid(UserBean user){
		ResponseRaidCommand.Builder builder = getRaidList();
		Map<String, String> keyvalue = hget(RedisKey.USERRAID_PREFIX+user.getId(), user.getId());
		if(keyvalue.isEmpty()) {
			List<RaidBean> list = mapper.getRaids(user.getId());
			for(RaidBean bean : list) {
				keyvalue.put(bean.getId()+"", bean.toJson());
			}
			hputAll(RedisKey.USERRAID_PREFIX+user.getId(), keyvalue, user.getId());
		}
//		for(Raid.Builder raid : builder.getRaidBuilderList()) {
		for(int i = builder.getRaidCount()-1; i >= 0; i--) {
			Raid.Builder raid = builder.getRaidBuilder(i);
			if(raid.hasEndtime() && !DateUtil.timeIsAvailable(raid.getStarttime(), raid.getEndtime())) {
				builder.removeRaid(i);
				continue;
			}
			String value = keyvalue.get(raid.getId()+"");
			Raid.Builder myraid = Raid.newBuilder();
			if(value != null && parseJson(value, myraid)) {
				raid.setEventid(myraid.getEventid());
				raid.addAllTurn(myraid.getTurnList());
				raid.setLevel(myraid.getLevel());
				raid.setMaxlevel(myraid.getMaxlevel());
			}
		}
		return builder;
	}

	public void saveRaid(UserBean user, Raid.Builder raid, boolean updateDB){
		hput(RedisKey.USERRAID_PREFIX+user.getId(), raid.getId()+"", formatJson(raid.build()), user.getId());
//		if(raid.getId() < 50)
		if(updateDB)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERRAID_PREFIX, user.getId()+"#"+raid.getId());
	}

	public String popDBKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERRAID_PREFIX);
	}
	public void updateToDB(long userId, int id){
		String value = hget(RedisKey.USERRAID_PREFIX+userId, id+"", userId);
		RaidBean bean = RaidBean.fromJson(userId, value);
		if(bean != null)
			mapper.updateRaid(bean);
	}
	
//	public void deleteRaid(UserBean user, int id){
//		hdelete(RedisKey.USERRAID_PREFIX+user.getId(), id+"");
//	}

	public EventConfig getRaidLevel(int level) {
		Map<Integer, EventConfig> map = CacheService.hgetcache(RedisKey.RAIDLEVEL_CONFIG);
		return map.get(level);
	}

	private void buildRaidLevelConfig() {
		EventConfigList.Builder list = EventConfigList.newBuilder();
		Map<Integer, EventConfig> map = new HashMap<Integer, EventConfig>();
		String xml = ReadConfig("ld_raidlevel.xml");
		parseXml(xml, list);
		for(EventConfig config : list.getDataList()) {
			map.put(config.getId(), config);
		}
		CacheService.hputcacheAll(RedisKey.RAIDLEVEL_CONFIG, map);
	}
	
	public Raid getRaid(int id){
//		Map<Integer, Raid> map = CacheService.hgetcache(RedisKey.RAID_CONFIG);
		RaidList raidList = CacheService.getcache(RedisKey.RAID_CONFIG);
		for (Raid raid : raidList.getDataList()) {
			if (raid.getId() == id)
				return raid;
		}
//		return map.get(id);
		return null;
	}

	public ResponseRaidCommand.Builder getRaidList(){
		ResponseRaidCommand raids = CacheService.getcache(RedisKey.RAIDLIST_CONFIG);
		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder(raids);
		return builder;
	}

	private void buildRaidListConfig(){
		RaidList.Builder list = getRaidConfig();
		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
		for(Raid.Builder raid : list.getDataBuilderList()) {
			raid.clearEvent();
			raid.clearCost();
			raid.setEventid(0);
			raid.clearTurn();
			raid.setMaxlevel(Math.max(raid.getLevel(), 3));
			raid.setLevel(0);
			if("".equals(raid.getStarttime()))
				raid.clearStarttime();
			if("".equals(raid.getEndtime()))
				raid.clearEndtime();
		}
		builder.clearRaid();
		builder.addAllRaid(list.getDataList());
		CacheService.setcache(RedisKey.RAIDLIST_CONFIG, builder.build());
	}

	private RaidList.Builder getRaidConfig(){
		String xml = ReadConfig("ld_raid.xml");
		RaidList.Builder list = RaidList.newBuilder();
		parseXml(xml, list);
		CacheService.setcache(RedisKey.RAID_CONFIG, list.build());
		return list;
	}
}
