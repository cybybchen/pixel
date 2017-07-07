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
import com.trans.pixel.utils.DateUtil;

@Repository
public class RaidRedisService extends RedisService{
	Logger logger = Logger.getLogger(RaidRedisService.class);
	@Resource
	RaidMapper mapper;

	public ResponseRaidCommand.Builder getRaid(UserBean user){
		ResponseRaidCommand.Builder builder = getRaidList();
		Map<String, String> keyvalue = hget(RedisKey.USERRAID_PREFIX+user.getId());
		if(keyvalue.isEmpty()) {
			List<RaidBean> list = mapper.getRaids(user.getId());
			for(RaidBean bean : list) {
				keyvalue.put(bean.getId()+"", bean.toJson());
			}
			hputAll(RedisKey.USERRAID_PREFIX+user.getId(), keyvalue);
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

	public void saveRaid(UserBean user, Raid.Builder raid){
		hput(RedisKey.USERRAID_PREFIX+user.getId(), raid.getId()+"", formatJson(raid.build()));
//		if(raid.getId() < 50)
			sadd(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERRAID_PREFIX, user.getId()+"#"+raid.getId());
	}

	public String popDBKey() {
		return spop(RedisKey.PUSH_MYSQL_KEY+RedisKey.USERRAID_PREFIX);
	}
	public void updateToDB(long userId, int id){
		String value = hget(RedisKey.USERRAID_PREFIX+userId, id+"");
		RaidBean bean = RaidBean.fromJson(userId, value);
		if(bean != null)
			mapper.updateRaid(bean);
	}
	
//	public void deleteRaid(UserBean user, int id){
//		hdelete(RedisKey.USERRAID_PREFIX+user.getId(), id+"");
//	}

	public Raid getRaid(int id){
		String value = hget(RedisKey.RAID_CONFIG, id+"");
		Raid.Builder builder = Raid.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else{
			RaidList.Builder list = buildRaid();
			Map<String, String> raidvalue = new HashMap<String, String>();
			for(Raid.Builder raid : list.getDataBuilderList()){
				raidvalue.put(raid.getId()+"", formatJson(raid.build()));
			}
			hputAll(RedisKey.RAID_CONFIG, raidvalue);
			
			value = hget(RedisKey.RAID_CONFIG, id+"");
			if(value != null && parseJson(value, builder))
				return builder.build();
		}
		return null;
	}
	public ResponseRaidCommand.Builder getRaidList(){
		String value = get(RedisKey.RAIDLIST_CONFIG);
		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
		if(value == null || !parseJson(value, builder)) {
			RaidList.Builder list = buildRaid();
			for(Raid.Builder raid : list.getDataBuilderList()) {
				raid.clearEvent();
				raid.clearCost();
				raid.setEventid(0);
				raid.clearTurn();
				raid.setMaxlevel(3);
				raid.setLevel(0);
				if("".equals(raid.getStarttime()))
					raid.clearStarttime();
				if("".equals(raid.getEndtime()))
					raid.clearEndtime();
			}
			builder.clearRaid();
			builder.addAllRaid(list.getDataList());
			set(RedisKey.RAIDLIST_CONFIG, formatJson(builder.build()));
		}
		return builder;
	}

	public RaidList.Builder buildRaid(){
//		Map<String, String> raidvalue = new HashMap<String, String>();
		String xml = ReadConfig("ld_raid.xml");
		RaidList.Builder list = RaidList.newBuilder();
		parseXml(xml, list);
//		for(Raid.Builder raid : list.getDataBuilderList()){
//			raidvalue.put(raid.getId()+"", formatJson(raid.build()));
//		}
//		hputAll(RedisKey.RAID_CONFIG, raidvalue);
		return list;
	}
}
