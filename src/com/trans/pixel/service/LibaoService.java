package com.trans.pixel.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.JewelPool;
import com.trans.pixel.protoc.Commands.JewelPoolList;
import com.trans.pixel.protoc.Commands.YueKa;
import com.trans.pixel.service.redis.LibaoRedisService;

@Service
public class LibaoService {
	Logger logger = Logger.getLogger(LibaoService.class);
	@Resource
    private LibaoRedisService redis;
	
	public YueKa getYueKa(int id){
		return redis.getYueKa(id);
	}

//	public JewelPool getJewelPool(int id){
//		return redis.getJewelPool(id);
//	}

	public JewelPool getJewelPool(UserBean user, int id){
		JewelPool.Builder builder = redis.getJewelPool(id);
		builder.setRecharged(user.getGrowJewelCount());
		calJewelPoolRewarded(builder, user.getGrowJewelCountStatus());
		return builder.build();
	}

//	public JewelPool getExpPool(int id){
//		return redis.getExpPool(id);
//	}
	
	public JewelPool getExpPool(UserBean user, int id){
		JewelPool.Builder builder = redis.getExpPool(id);
		builder.setRecharged(user.getGrowJewelCount());
		calJewelPoolRewarded(builder, user.getGrowExpCountStatus());
		return builder.build();
	}
	
	public JewelPoolList getExpPoolList(UserBean user){
		JewelPoolList.Builder listbuilder = redis.getExpPoolList();
		for(JewelPool.Builder builder : listbuilder.getOrderBuilderList()){
			builder.setRecharged(user.getGrowJewelCount());
		}
		return listbuilder.build();
	}

	public static void calJewelPoolRewarded(JewelPool.Builder builder, int status){
		int index = 7;
		index = index<<(3*builder.getOrder()-3);
		index = index & status;
		index = index>>(3*builder.getOrder()-3);
		builder.setRewarded(index);
	}

	public long now(){
		return redis.now();
	}

	public long caltoday(long time, int hour){
		return redis.caltoday(time, hour);
	}
}
