package com.trans.pixel.service;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
	
	public void refresh(UserRewardTask.Builder builder) {
		RewardTaskList.Builder config = rewardTaskRedisService.getRewardTaskConfig();
		for(RewardTask.Builder task : config.getDataBuilderList()){
			if(builder.getTask().getType() != task.getType())
				continue;
//			for(int i = 0; i < task.getRandcount(); i++) {
//				UserRewardTask.Builder builder = UserRewardTask.newBuilder();
			int eventid = builder.getTask().getEventid();
			while(eventid == builder.getTask().getEventid())
				eventid = task.getEvent(RedisService.nextInt(task.getEventCount())).getEventid();
			task.setEventid(eventid);
			builder.setTask(task);
			builder.getTaskBuilder().clearRandcount();
			builder.getTaskBuilder().clearEvent();
//			if(builder.getTask().getType() == 4 || builder.getTask().getType() == 5)
//				builder.setEndtime(today+24*3600);
			builder.setLeftcount(task.getCount());
			builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
//			builder.setIndex(index);
//			index++;
//			userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
//			map.put(builder.getIndex(), builder.build());
//			}
			break;
		}
	}
	
	public Map<Integer, UserRewardTask> getUserRewardTaskList(UserBean user) {
		Map<Integer, UserRewardTask> map = userRewardTaskRedisService.getUserRewardTask(user.getId());
		long now = RedisService.now();
		boolean needRefresh = false;
		Iterator<Entry<Integer, UserRewardTask>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			int index = it.next().getKey();
			UserRewardTask ut = map.get(index);
			if(ut.hasEndtime() && ut.getEndtime() <= now) {//过0点刷新
				userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
				it.remove();
//				needRefresh = true;
			}else if(ut.getTask().getId() == 7 && user.getVip() < 14){
				it.remove();
			}else if(ut.getTask().getId() == 6 && user.getVip() < 7){
				it.remove();
			}
		}
		if(map.get(1) == null)
			needRefresh = true;
		if(needRefresh){
			RewardTaskList.Builder config = rewardTaskRedisService.getRewardTaskConfig();
			int index = 1;
			for(RewardTask.Builder task : config.getDataBuilderList()){
				for(int i = 0; i < task.getRandcount(); i++) {
					if(map.get(index) != null){
						index++;
						continue;
					}
					UserRewardTask.Builder builder = UserRewardTask.newBuilder();
					task.setEventid(task.getEvent(RedisService.nextInt(task.getEventCount())).getEventid());
					builder.setTask(task);
					builder.getTaskBuilder().clearRandcount();
					builder.getTaskBuilder().clearEvent();
					if(builder.getTask().getType() != 2)
						builder.setEndtime(RedisService.today(24));
					else
						builder.clearEndtime();
					builder.setLeftcount(task.getCount());
					builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
					builder.setIndex(index);
					index++;
					userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
					if((builder.getTask().getId() != 7 || user.getVip() >= 14) && (builder.getTask().getId() != 6 && user.getVip() >= 7))
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
					else {
						UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
						builder.clearRoomInfo();
						userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
						map.put(ut.getIndex(), builder.build());
					}
					
					continue;
				}
			}
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
