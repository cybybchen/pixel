package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.service.redis.FightInfoRedisService;

@Service
public class FightInfoService {
	private static Logger log = Logger.getLogger(FightInfoService.class);

	@Resource
	private FightInfoRedisService redis;
	@Resource
	private UserService userService;
	
	public void setFightInfo(String info, UserBean user){
		redis.setFightInfo(info, user);
	}
	
	public List<FightInfo.Builder> getFightInfoList(UserBean user){
		List<FightInfo.Builder> infos = redis.getFightInfoList(user);
		for(FightInfo.Builder info : infos) {
			if(info.hasEnemy())
				info.setEnemy(userService.getCache(user.getServerId(), info.getEnemy().getId()));
		}
		return infos;
	}
	
	public ResultConst save(UserBean user, FightInfo fightinfo) {
		if (redis.hlenFightInfo(user) >= 10) {
			return ErrorConst.FIGHTINFO_IS_LIMIT_ERROR;
		}
		redis.saveFightInfo(user, fightinfo);
		
		return SuccessConst.SAVE_SUCCESS;
	}
	
	public List<FightInfo> getSaveFightInfoList(UserBean user){
		List<FightInfo> infos = redis.getSaveFightInfoList(user);
		if (infos.isEmpty()) {
			
		}
		
		return infos;
	}
}
