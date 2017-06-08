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
		String value = get(RedisKey.USERRAID_PREFIX+user.getId());
		ResponseRaidCommand.Builder raid = ResponseRaidCommand.newBuilder();
		if(value != null && parseJson(value, raid))
			return raid;
		else
			return ResponseRaidCommand.newBuilder();
	}

	public void saveRaid(UserBean user, ResponseRaidCommand.Builder raid){
		set(RedisKey.USERRAID_PREFIX+user.getId(), formatJson(raid.build()));
	}
	
	public void deleteRaid(UserBean user){
		delete(RedisKey.USERRAID_PREFIX+user.getId());
	}

	public Raid getRaid(int id){
		String value = hget(RedisKey.RAID_CONFIG, id+"");
		Raid.Builder builder = Raid.newBuilder();
		if(value != null && parseJson(value, builder))
			return builder.build();
		else{
			buildRaid();
			value = hget(RedisKey.RAID_CONFIG, id+"");
			if(value != null && parseJson(value, builder))
				return builder.build();
		}
		return null;
	}

//	public boolean hasRaidOrder(int id){
//		return hget(RedisKey.RAIDORDER_CONFIG, id+"") != null;
//	}
//	public RaidOrder getRaidOrder(int id){
//		String value = hget(RedisKey.RAIDORDER_CONFIG, id+"");
//		RaidOrder.Builder builder = RaidOrder.newBuilder();
//		if(value != null && parseJson(value, builder))
//			return builder.build();
//		else{
//			buildRaid();
//			value = hget(RedisKey.RAIDORDER_CONFIG, id+"");
//			if(value != null && parseJson(value, builder))
//				return builder.build();
//		}
//		return null;
//	}

	public void buildRaid(){
		Map<String, String> raidvalue = new HashMap<String, String>();
		String xml = ReadConfig("ld_raid.xml");
		RaidList.Builder list = RaidList.newBuilder();
		parseXml(xml, list);
		for(Raid.Builder raid : list.getDataBuilderList()){
			raidvalue.put(raid.getId()+"", formatJson(raid.build()));
		}
		hputAll(RedisKey.RAID_CONFIG, raidvalue);
	}
}
