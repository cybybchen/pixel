package com.trans.pixel.service.command;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.ShopProto.Libao;
import com.trans.pixel.protoc.TaskProto.Raid;
import com.trans.pixel.protoc.TaskProto.RequestOpenRaidCommand;
import com.trans.pixel.protoc.TaskProto.RequestStartRaidCommand;
import com.trans.pixel.protoc.TaskProto.ResponseRaidCommand;
import com.trans.pixel.protoc.UserInfoProto.EventConfig;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.CostService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RankService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.RaidRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class RaidCommandService extends BaseCommandService{
	Logger logger = Logger.getLogger(RaidCommandService.class);
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
	@Resource
	private UserService userService;


	public void openRaid(RequestOpenRaidCommand cmd, Builder responseBuilder, UserBean user){
		Raid raid = redis.getRaid(cmd.getId());
		ResponseRaidCommand.Builder raidlist = redis.getRaid(user);
		int index = 0;
		for(index = 0; index < raidlist.getRaidCount(); index++) {
			Raid.Builder myraid = raidlist.getRaidBuilder(index);
			if(myraid.getId() == cmd.getId())
				break;
		}
		if(index >= raidlist.getRaidCount()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.RAID_NOT_OPEN);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.RAID_NOT_OPEN);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		//判断消耗和最大层数
		if(costService.cost(user, raid.getCost().getItemid(), raid.getCost().getCount(), true)){
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
			Raid raidconfig = redis.getRaid(myraid.getId());
			EventConfig event = levelRedisService.getEvent(myraid.getEventid());
			if(myraid.getId() != cmd.getId() || myraid.getEventid() != cmd.getEventid() || raidconfig == null || event == null){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_MONSTER);
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_MONSTER);
	            responseBuilder.setErrorCommand(errorCommand);
			}else if(cmd.getRet()){
				MultiReward.Builder rewards = levelRedisService.eventReward(user, event, myraid.getLevel());
				EventConfig.Builder config = EventConfig.newBuilder(redis.getRaidLevel(myraid.getLevel()));
				for(RewardInfo.Builder reward : config.getLootlistBuilderList()) {
					if(reward.getItemid() != RewardConst.ZHUJUEEXP)
						continue;
					long expcount = reward.getCount();
					Libao.Builder libao = Libao.newBuilder(userService.getLibao(user.getId(), 17));//初级月卡
					Libao.Builder libao2 = Libao.newBuilder(userService.getLibao(user.getId(), 18));//高级月卡
					if(libao.hasValidtime() && DateUtil.getDate(libao.getValidtime()).after(new Date())){
						reward.setCount(reward.getCount()+(int)(expcount*0.1));
					}
					if(libao2.hasValidtime() && DateUtil.getDate(libao2.getValidtime()).after(new Date())){
						reward.setCount(reward.getCount()+(int)(expcount*0.2));
					}
				}
				rewards.addAllLoot(config.getLootlistList());
				handleRewards(responseBuilder, user, rewards.build());
				
				for(int i = 0; i < raidconfig.getEventCount(); i++) {
					if(myraid.getEventid() == raidconfig.getEvent(i).getEventid()){
						myraid.setEventid(i+1==raidconfig.getEventCount() ? 0 : raidconfig.getEvent(i+1).getEventid());//通关or下一关
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
					activityService.raidKill(user, myraid.getId(), myraid.getMaxlevel());
					myraid.clearEventid();
//					myraid.clearTurn();
					myraid.setMaxlevel(Math.min(180, Math.max(myraid.getMaxlevel(), myraid.getLevel()+2)));
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
