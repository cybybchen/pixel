package com.trans.pixel.service;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.UserRewardTaskMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRewardTaskBean;
import com.trans.pixel.protoc.RewardTaskProto.RewardTask;
import com.trans.pixel.protoc.RewardTaskProto.RewardTaskList;
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
	
	public Map<Integer, UserRewardTask> getUserRewardTaskList(UserBean user) {
		Map<Integer, UserRewardTask> map = userRewardTaskRedisService.getUserRewardTask(user.getId());
		long today = RedisService.today(0);
		boolean needFlash = false;
		Iterator<Integer> it = map.keySet().iterator();
		while (it.hasNext()) {
			int index = it.next();
			UserRewardTask ut = map.get(index);
			if(ut.hasEndtime() && ut.getEndtime() >= today) {//过0点刷新
				userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
				map.remove(index);
				needFlash = true;
			}
		}
		if(needFlash){
			RewardTaskList.Builder config = rewardTaskRedisService.getRewardTaskConfig();
			int index = 1;
			for(RewardTask.Builder task : config.getDataBuilderList()){
				for(int i = 0; i < task.getRandcount(); i++) {
					UserRewardTask.Builder builder = UserRewardTask.newBuilder();
					task.setEventid(task.getEvent(RedisService.nextInt(task.getEventCount())).getEventid());
					builder.setTask(task);
					builder.getTaskBuilder().clearRandcount();
					builder.getTaskBuilder().clearEvent();
					builder.setEndtime(today+24*3600);
					builder.setLeftcount(task.getCount());
					builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
					builder.setIndex(index);
					index++;
					userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
					map.put(builder.getIndex(), builder.build());
				}
			}
			
		}
		for (UserRewardTask ut : map.values()) {
			if (ut.hasRoomInfo()) {
				log.debug("" + RedisService.formatJson(ut));
				if (rewardTaskRedisService.getUserRewardTaskRoom(ut.getRoomInfo().getUser().getId(), ut.getRoomInfo().getIndex()) == null) {
					if (ut.getRoomInfo().getUser().getId() != user.getId() && ut.getStatus() != REWARDTASK_STATUS.CANREWARD_VALUE){
						userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
					}
//					else {
//						UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
//						builder.clearRoomInfo();
//						utList.add(builder.build());
//					}
					
					continue;
				}
			}
			
//			utList.add(ut);
		}

		return map;
	}
	
	public void updateToDB(long userId, int index) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, index);
		if(ut != null && ut.getTask().getEventid() != 0) {
			mapper.updateUserRewardTask(UserRewardTaskBean.init(userId, ut));
			
			if (ut.getStatus() == REWARDTASK_STATUS.END_VALUE)
				userRewardTaskRedisService.deleteUserRewardTask(userId, ut);
		}
		
	}
	
	public String popDBKey(){
		return userRewardTaskRedisService.popDBKey();
	}
}
