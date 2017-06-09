package com.trans.pixel.service.redis;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.RaidList;
import com.trans.pixel.protoc.TaskProto.ResponseRaidCommand;

@Repository
public class RaidRedisService extends RedisService{
	Logger logger = Logger.getLogger(RaidRedisService.class);

	public ResponseRaidCommand.Builder getRaid(UserBean user){
		ResponseRaidCommand.Builder builder = getRaidList();
		Map<String, String> keyvalue = hget(RedisKey.USERRAID_PREFIX+user.getId());
		for(Raid.Builder raid : builder.getRaidBuilderList()) {
			String value = keyvalue.get(raid.getId()+"");
			Raid.Builder myraid = Raid.newBuilder();
			if(parseJson(value, myraid)) {
				raid.setEventid(myraid.getEventid());
				raid.setTurn(myraid.getTurn());
				raid.setLevel(myraid.getLevel());
				raid.setMaxlevel(myraid.getMaxlevel());
			}
		}
		return builder;
	}

	public void saveRaid(UserBean user, Raid.Builder raid){
		hput(RedisKey.USERRAID_PREFIX+user.getId(), raid.getId()+"", formatJson(raid.build()));
	}
	
	public void deleteRaid(UserBean user, int id){
		hdelete(RedisKey.USERRAID_PREFIX+user.getId(), id+"");
	}

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
				raid.setTurn(0);
				raid.setMaxlevel(3);
				raid.setLevel(0);
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
