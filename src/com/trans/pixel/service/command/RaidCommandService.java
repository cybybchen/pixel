package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.RequestOpenRaidCommand;
import com.trans.pixel.protoc.TaskProto.RequestStartRaidCommand;
import com.trans.pixel.protoc.TaskProto.ResponseRaidCommand;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RaidRedisService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class RaidCommandService extends BaseCommandService{
	@Resource
    private RaidRedisService redis;
	@Resource
    private PushCommandService pusher;
	@Resource
    private CostService costService;
	@Resource
	private LogService logService;
	@Resource
	private ActivityService activityService;
	@Resource
	private LevelRedisService levelRedisService;


	public void openRaid(RequestOpenRaidCommand cmd, Builder responseBuilder, UserBean user){
		Raid raid = redis.getRaid(cmd.getId());
		int lastid = redis.getRaid(user);
		if(costService.cost(user, raid.getCost().getItemid(), raid.getCost().getCount())){
			redis.saveRaid(user, raid.getId()*100000+raid.getEventList().get(0).getEventid());
			ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
			builder.setId(raid.getId());
			builder.setEventid(raid.getEventList().get(0).getEventid());
			responseBuilder.setRaidCommand(builder);
			pusher.pushUserDataByRewardId(responseBuilder, user, raid.getCost().getItemid());

			Map<String, String> params = new HashMap<String, String>();
			params.put(LogString.USERID, "" + user.getId());
			params.put(LogString.SERVERID, "" + user.getServerId());
			params.put(LogString.RESULT, "2");
			params.put(LogString.INSTANCEID, "" + raid.getId());
			params.put(LogString.BOSSID, "0");
			params.put(LogString.PREINSTANCEID, "" + lastid);
			logService.sendLog(params, LogString.LOGTYPE_RAID);
		}else{
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ENOUGH);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ENOUGH);
            responseBuilder.setErrorCommand(errorCommand);
		}
	}

	public void startRaid(RequestStartRaidCommand cmd, Builder responseBuilder, UserBean user){
		int myid = redis.getRaid(user);
		int id = myid;
		int raidid = myid/100000;
		int eventid = myid%100000;
		int oldeventid = eventid;
		Raid raid = redis.getRaid(id);
		EventConfig event = levelRedisService.getEvent(eventid);
		if(raidid != cmd.getId() || eventid != cmd.getEventid() || raid == null || event == null){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
            responseBuilder.setErrorCommand(errorCommand);
		}else if(cmd.getRet()){
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.addAllLoot(event.getLootlistList());
			handleRewards(responseBuilder, user, rewards.build());
			
			for(int i = 0; i < raid.getEventCount(); i++) {
				if(eventid == raid.getEvent(i).getEventid()){
					eventid = i+1==raid.getEventCount() ? 0 : raid.getEvent(i+1).getEventid();
				}
			}
			if(eventid == oldeventid) {//非法值
				id = 0;
			} else if (eventid == 0) {//通关
				/**
				 * 通关副本的活动
				 */
				activityService.raidKill(user, raidid);
				id = 0;
			} else {//下一关
				id = raidid*100000+eventid;
			}
			
			redis.saveRaid(user, id);
		}else if(!cmd.getRet() && cmd.getTurn() == 0){
			id = 0;
			redis.saveRaid(user, id);
		}
		if(responseBuilder.hasErrorCommand()) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.USERID, "" + user.getId());
		params.put(LogString.SERVERID, "" + user.getServerId());
		params.put(LogString.RESULT, cmd.getRet() ? "1":"0");
		params.put(LogString.INSTANCEID, "" + myid);
		params.put(LogString.BOSSID, "" + (event.hasEnemygroup()?event.getEnemygroup().getEnemy(0).getEnemyid() : 0));
		params.put(LogString.PREINSTANCEID, "" + (!cmd.getRet() && cmd.getTurn() == 0 ? myid : 0));
		logService.sendLog(params, LogString.LOGTYPE_RAID);
		}

		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
		builder.setId(id);
		responseBuilder.setRaidCommand(builder);
	}
	
	public void getRaid(Builder responseBuilder, UserBean user){
		int id = redis.getRaid(user);
		ResponseRaidCommand.Builder builder = ResponseRaidCommand.newBuilder();
		builder.setId(id);
		responseBuilder.setRaidCommand(builder);
	}
}
