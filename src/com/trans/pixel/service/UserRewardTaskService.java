package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserRewardTaskMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRewardTaskBean;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask;
import com.trans.pixel.protoc.RewardTaskProto.UserRewardTask.REWARDTASK_STATUS;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.RewardTaskRedisService;
import com.trans.pixel.service.redis.UserRewardTaskRedisService;

@Service
public class UserRewardTaskService {
	private static final Logger log = LoggerFactory.getLogger(UserRewardTaskService.class);
	
	@Resource
	private UserRewardTaskRedisService userRewardTaskRedisService;
	@Resource
	private UserRewardTaskMapper mapper;
	@Resource
	private RewardTaskRedisService rewardTaskRedisService;
	
	public UserRewardTask getUserRewardTask(UserBean user, int index) {
		return getUserRewardTask(user.getId(), index);
	}
	
	public UserRewardTask getUserRewardTask(long userId, int index) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, index);

		return ut;
	}
	
	public void updateUserRewardTask(UserBean user, UserRewardTask ut) {
		updateUserRewardTask(user.getId(), ut);
	}
	
	public void updateUserRewardTask(long userId, UserRewardTask ut) {
		userRewardTaskRedisService.updateUserRewardTask(userId, ut);
	}
	
	public List<UserRewardTask> getUserRewardTaskList(UserBean user) {
		List<UserRewardTask> utList = new ArrayList<UserRewardTask>();
		for (UserRewardTask ut : userRewardTaskRedisService.getUserRewardTaskList(user.getId())) {
			if (ut.hasRoomInfo()) {
				log.debug("" + RedisService.formatJson(ut));
				if (rewardTaskRedisService.getUserRewardTaskRoom(ut.getRoomInfo().getUser().getId(), ut.getRoomInfo().getIndex()) == null) {
					if (ut.getRoomInfo().getUser().getId() != user.getId())
						userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
					else {
						UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
						builder.clearRoomInfo();
						utList.add(builder.build());
					}
					
					continue;
				}
			}
			
			utList.add(ut);
		}

		return utList;
	}
	
	public void updateToDB(long userId, int index) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, index);
		if(ut != null && ut.getEnemyid() != 0) {
			mapper.updateUserRewardTask(UserRewardTaskBean.init(userId, ut));
			
			if (ut.getStatus() == REWARDTASK_STATUS.END_VALUE)
				userRewardTaskRedisService.deleteUserRewardTask(userId, ut);
		}
		
	}
	
	public String popDBKey(){
		return userRewardTaskRedisService.popDBKey();
	}
}
