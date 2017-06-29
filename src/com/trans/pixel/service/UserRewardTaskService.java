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
import com.trans.pixel.utils.DateUtil;

@Service
public class UserRewardTaskService {
	private static final Logger log = LoggerFactory.getLogger(UserRewardTaskService.class);
	
	@Resource
	private UserRewardTaskRedisService userRewardTaskRedisService;
	@Resource
	private UserRewardTaskMapper mapper;
	@Resource
	private RewardTaskRedisService rewardTaskRedisService;
	@Resource
	private UserService userService;
	
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
	
	public void refresh(UserRewardTask.Builder builder, UserBean user) {
		RewardTaskList.Builder config = rewardTaskRedisService.getRewardTaskConfig();
		for(RewardTask.Builder task : config.getDataBuilderList()){
			if(builder.getTaskBuilder().getId() != task.getId() || builder.getTaskBuilder().getRandcount() != task.getRandcount())
				continue;
			userRewardTaskRedisService.deleteUserRewardTask(user.getId(), builder.build());
			int eventid = builder.getTaskBuilder().getEventid();
			while(eventid == builder.getTaskBuilder().getEventid())
				eventid = task.getEvent(RedisService.nextInt(task.getEventCount())).getEventid();
			task.setEventid(eventid);
			task.clearEvent();
			task.clearStarttime();
			task.clearEndtime();
			builder.setTask(task);
			if(task.getType() != 2)
				builder.setEndtime(RedisService.today(24));
			else
				builder.clearEndtime();
//			builder.setLeftcount(task.getCount());
			builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
			user.setRewardTaskIndex(Math.max(20, user.getRewardTaskIndex() + 1)%1000);
			builder.setIndex(user.getRewardTaskIndex());
			userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
			userService.updateUser(user);
			break;
		}
	}
	
	public Map<Integer, UserRewardTask> getUserRewardTaskList(UserBean user) {
		Map<Integer, UserRewardTask> map = userRewardTaskRedisService.getUserRewardTask(user.getId());
		long now = RedisService.now();
		boolean needRefresh = false;
		if(map.isEmpty())
			needRefresh = true;
		Iterator<Entry<Integer, UserRewardTask>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			int index = it.next().getKey();
			UserRewardTask ut = map.get(index);
			if(ut.hasEndtime() && ut.getEndtime() <= now) {//过0点刷新
				needRefresh = true;
				if (ut.hasRoomInfo()) {//保留组队
					if (rewardTaskRedisService.getUserRewardTaskRoom(ut.getRoomInfo().getUser().getId(), ut.getRoomInfo().getIndex()) == null) {
						if (ut.getStatus() != REWARDTASK_STATUS.CANREWARD_VALUE){//非法情况：房间不存在
							userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
							it.remove();
//						}else {//可以领奖
//							UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
//							builder.clearRoomInfo();
//							builder.setEndtime(RedisService.today(24));
//							userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
//							map.put(ut.getIndex(), builder.build());
						}
					}
				}else {
					userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
					it.remove();
				}
			}else if(ut.getTask().getId() == 7 && user.getVip() < 14){
				it.remove();
			}else if(ut.getTask().getId() == 6 && user.getVip() < 7){
				it.remove();
			}
		}
		if(needRefresh){
			RewardTaskList.Builder config = rewardTaskRedisService.getRewardTaskConfig();
			it = map.entrySet().iterator();
			while (it.hasNext()) {//过滤深渊和组队
				int index = it.next().getKey();
				UserRewardTask ut = map.get(index);
				int i = config.getDataCount()-1;
				for(; i >= 0; i--){
					RewardTask.Builder task = config.getDataBuilder(i);
					if(task.getId() == ut.getTask().getId() && task.getRandcount() == ut.getTask().getRandcount()) {
						UserRewardTask.Builder builder = UserRewardTask.newBuilder(ut);
						if (ut.getStatus() == REWARDTASK_STATUS.CANREWARD_VALUE)//可以领奖
							builder.clearRoomInfo();
//						if(task.getType() == 2 && ut.getStatus() == REWARDTASK_STATUS.LIVE_VALUE){
						if(task.getType() != 2){//补充次数
							if (ut.getStatus() == REWARDTASK_STATUS.CANREWARD_VALUE) {
								builder.setLeftcount(task.getCount()+1);
							}else{
								builder.setLeftcount(task.getCount());
							}
						}
						builder.setEndtime(RedisService.today(24));
						userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
						map.put(ut.getIndex(), builder.build());
						config.removeData(i);
						break;
					}
				}
				if(i < 0){//配置更新
					userRewardTaskRedisService.deleteUserRewardTask(user.getId(), ut);
					it.remove();
				}
			}
			for(int i = 0; i < config.getDataCount(); i++) {
				RewardTask.Builder task = config.getDataBuilder(i);
				if(!"".equals(task.getEndtime()) && !DateUtil.timeIsAvailable(task.getStarttime(), task.getEndtime()))
					continue;//不在有效期
				
				UserRewardTask.Builder builder = UserRewardTask.newBuilder();
				int eventindex = RedisService.nextInt(task.getEventCount());
				for(int j = i+1; j < config.getDataCount() && task.getId() == config.getDataBuilder(j).getId(); j++)
					config.getDataBuilder(j).removeEvent(eventindex);
				task.setEventid(task.getEvent(eventindex).getEventid());
				task.clearEvent();
				task.clearStarttime();
				task.clearEndtime();
				builder.setTask(task);
//				builder.getTaskBuilder().clearRandcount();
//				builder.getTaskBuilder().clearEvent();
				if(task.getType() != 2)
					builder.setEndtime(RedisService.today(24));
				else
					builder.clearEndtime();
				builder.setLeftcount(task.getCount());
				builder.setStatus(REWARDTASK_STATUS.LIVE_VALUE);
				user.setRewardTaskIndex(Math.max(20, user.getRewardTaskIndex() + 1)%1000);
				builder.setIndex(user.getRewardTaskIndex());
				userRewardTaskRedisService.updateUserRewardTask(user.getId(), builder.build());
				if((builder.getTask().getId() != 7 || user.getVip() >= 14) && (builder.getTask().getId() != 6 || user.getVip() >= 7))
					map.put(builder.getIndex(), builder.build());
			}
			userService.updateUser(user);
		}

		return map;
	}
	
	public void updateToDB(long userId, int index) {
		UserRewardTask ut = userRewardTaskRedisService.getUserRewardTask(userId, index);
		if(ut != null && ut.getTask().getEventid() != 0) {
			mapper.updateUserRewardTask(UserRewardTaskBean.init(userId, ut));
			
//			if (ut.getStatus() == REWARDTASK_STATUS.END_VALUE)
//				userRewardTaskRedisService.deleteUserRewardTask(userId, ut);
		}
		
	}
	
	public String popDBKey(){
		return userRewardTaskRedisService.popDBKey();
	}
}
