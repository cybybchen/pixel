package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.googlecode.protobuf.format.JsonFormat;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.service.redis.AreaRedisService;

/**
 * 1.1.3.6区域争夺战
 */
@Service
public class AreaFightService {
	Logger logger = Logger.getLogger(AreaFightService.class);
	@Resource
    private AreaRedisService redis;

	public List<AreaInfo> Area(UserBean user){
		List<AreaInfo> areas = redis.getAreas();
		return areas;
	}

	public boolean AttackMonster(UserBean user, int id){
		AreaMonster.Builder builder = AreaMonster.newBuilder();
//		builder.mergeFrom(AreaRedisService.buildAreaMonster(id));
		AreaInfo.Builder areabuilder = AreaInfo.newBuilder();
		areabuilder.mergeFrom(redis.getArea(id));
		builder.mergeFrom(areabuilder.getMonsters(0));
		builder.setLevel(builder.getLevel()+1);
		areabuilder.setMonsters(0, builder);
		redis.saveArea(id,JsonFormat.printToString(areabuilder.build()));
		return true;
	}
	
	public boolean AttackBoss(UserBean user, int id){
		return true;
	}
	
	public boolean AttackResource(UserBean user, int id){
		return true;
	}
	
	public boolean AttackResourceMine(UserBean user, int id){
		return true;
	}
}
