package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.BosskillRecord;
import com.trans.pixel.service.redis.BossRedisService;

@Service
public class BossService {

	@Resource
	private BossRedisService bossRedisService;
	
	public List<BosskillRecord> getBosskillRecord(UserBean user) {
		return bossRedisService.getBosskillRecord(user.getId()); 
	}
}
