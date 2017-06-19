package com.trans.pixel.service.command;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
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
import com.trans.pixel.service.RankService;
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
	@Resource
	private RankService rankService;


	public void openRaid(RequestOpenRaidCommand cmd, Builder responseBuilder, UserBean user){
		Raid raid = redis.getRaid(cmd.getId());
		ResponseRaidCommand.Builder raidlist = redis.getRaid(user);
		//判断消耗和最大层数
		if(costService.cost(user, raid.getCost().getItemid(), raid.getCost().getCount())){
			int lastid = 0;
			for(Raid.Builder myraid : raidlist.getRaidBuilderList()) {
				if(myraid.getEventid() == 0)
					continue;
				lastid = myraid.getEventid();
				myraid.clearEventid();
				myraid.clearTurn();
				myraid.clearLevel();
				redis.saveRaid(user, myraid);
			}
			for(Raid.Builder myraid : raidlist.getRaidBuilderList()) {
				if(myraid.getId() != raid.getId())
					continue;
				myraid.setEventid(raid.getEventList().get(0).getEventid());
				myraid.clearTurn();
				myraid.setLevel(cmd.getLevel());
				redis.saveRaid(user, myraid);
				responseBuilder.setRaidCommand(raidlist);
				pusher.pushUserDataByRewardId(responseBuilder, user, raid.getCost().getItemid());
				break;
			}

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
		ResponseRaidCommand.Builder raidlist = redis.getRaid(user);
		for(Raid.Builder myraid : raidlist.getRaidBuilderList()) {
			if(myraid.getEventid() == 0)
				continue;
			int oldeventid = myraid.getEventid();
			Raid raid = redis.getRaid(myraid.getId());
			EventConfig event = levelRedisService.getEvent(myraid.getEventid());
			if(myraid.getId() != cmd.getId() || myraid.getEventid() != cmd.getEventid() || raid == null || event == null){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
	            responseBuilder.setErrorCommand(errorCommand);
			}else if(cmd.getRet()){
				MultiReward.Builder rewards = MultiReward.newBuilder();
				rewards.addAllLoot(event.getLootlistList());
				handleRewards(responseBuilder, user, rewards.build());
				
				for(int i = 0; i < raid.getEventCount(); i++) {
					if(myraid.getEventid() == raid.getEvent(i).getEventid()){
						myraid.setEventid(i+1==raid.getEventCount() ? 0 : raid.getEvent(i+1).getEventid());//通关or下一关
						myraid.addTurn(cmd.getTurn());
						break;
					}
				}
				if(myraid.getEventid() == oldeventid) {//非法值
					myraid.clearEventid();
					myraid.clearTurn();
					myraid.clearLevel();
				} else if (myraid.getEventid() == 0) {//通关
					/**
					 * 副本排行榜
					 */
					rankService.addRaidRank(user, myraid.build());
					/**
					 * 通关副本的活动
					 */
					activityService.raidKill(user, myraid.getId());
					myraid.clearEventid();
//					myraid.clearTurn();
					myraid.setMaxlevel(Math.min(180, Math.max(myraid.getMaxlevel(), myraid.getLevel()+1)));
					myraid.clearLevel();
				}
				
				redis.saveRaid(user, myraid);
			}else if(!cmd.getRet() && cmd.getTurn() == 0){
				myraid.clearEventid();
				myraid.clearTurn();
				myraid.clearLevel();
				redis.saveRaid(user, myraid);
			}
			if(!responseBuilder.hasErrorCommand()) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(LogString.USERID, "" + user.getId());
			params.put(LogString.SERVERID, "" + user.getServerId());
			params.put(LogString.RESULT, cmd.getRet() ? "1":"0");
			params.put(LogString.INSTANCEID, "" + myraid.getId());
			params.put(LogString.BOSSID, "" + (event.hasEnemygroup()?event.getEnemygroup().getEnemy(0).getEnemyid() : 0));
			params.put(LogString.PREINSTANCEID, "" + (!cmd.getRet() && cmd.getTurn() == 0 ? myraid.getEventid() : 0));
			logService.sendLog(params, LogString.LOGTYPE_RAID);
			}
		}

		responseBuilder.setRaidCommand(raidlist);
	}
	
	public void getRaid(Builder responseBuilder, UserBean user){
		ResponseRaidCommand.Builder raidlist = redis.getRaid(user);
		responseBuilder.setRaidCommand(raidlist);
	}
}
