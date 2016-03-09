package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaBossReward;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaMonsterReward;
import com.trans.pixel.protoc.Commands.AreaRankReward;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceMine;
import com.trans.pixel.protoc.Commands.FightResult;
import com.trans.pixel.protoc.Commands.FightResultList;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.Rank;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.WeightReward;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.MailRedisService;
import com.trans.pixel.utils.DateUtil;

/**
 * 1.1.3.6区域争夺战
 */
@Service
public class AreaFightService extends FightService{
	Logger logger = Logger.getLogger(AreaFightService.class);
	@Resource
    private AreaRedisService redis;
	@Resource
    private UserService userService;
	@Resource
    private RewardService rewardService;
	@Resource
	UserTeamService userTeamService;
	@Resource
	MailRedisService mailRedisService;
	
	private RewardInfo randReward(WeightReward weightreward){
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		int index = (int)(weightreward.getWeightall()*Math.random());
		if(index < weightreward.getWeight1()){
			reward.setItemid(weightreward.getItemid1());
			reward.setCount(weightreward.getCount1());
		}else if(index < weightreward.getWeight1() + weightreward.getWeight2()){
			reward.setItemid(weightreward.getItemid2());
			reward.setCount(weightreward.getCount2());
		}else{
			reward.setItemid(weightreward.getItemid3());
			reward.setCount(weightreward.getCount3());
		}
		return reward.build();
	}
	
	public void costEnergy(UserBean user){
		redis.costEnergy(user);
	}

	public boolean AttackMonster(int id, UserBean user, MultiReward.Builder rewards){
		AreaMonster monster = redis.getMonster(id, user);
		if(monster == null)
			return false;
		AreaMonster.Builder builder = AreaMonster.newBuilder(monster);
		if(builder.hasKilled())
			return false;
//		redis.deleteMonster(builder.getId());
		builder.setKilled(true);
		redis.deleteMonster(builder.getId(), user);
		AreaMonsterReward monsterreward = redis.getAreaMonsterReward(id);
		for(WeightReward weightreward : monsterreward.getLootList()){
			rewards.addLoot(randReward(weightreward));
		}
		rewardService.doRewards(user, rewards.build());
		return true;
	}

	public ResultConst AttackBoss(int id, int score, UserBean user, MultiReward.Builder rewards){
		AreaBoss boss = redis.getBoss(id, user);
		if(boss == null || boss.getHp() <= 0)
			return ErrorConst.NOT_MONSTER;
		int time = redis.getBossTime(id,user);
		if(time >= boss.getCount())
			return ErrorConst.NOT_ENOUGH_TIMES;
		redis.saveBossTime(id, time+1, user);
		redis.addBossRank(id, score, user);
		AreaBoss.Builder builder = AreaBoss.newBuilder(boss);
		builder.setHp(builder.getHp()-score);
		if(builder.getHp() <= 0){//结算
			if(!redis.setLock("S"+user.getServerId()+"_AreaBoss_"+builder.getId()))
				return ErrorConst.NOT_MONSTER;
			builder.setOwner(user.buildShort());
			//击杀奖励
			AreaMonsterReward monsterreward = redis.getAreaMonsterReward(id);
			for(WeightReward weightreward : monsterreward.getLootList()){
				rewards.addLoot(randReward(weightreward));
			}
			rewardService.doRewards(user, rewards.build());
			//排行奖励
			Set<TypedTuple<String>> ranks = redis.getBossRank(id, user);
			AreaBossReward bossreward = redis.getBossReward(id);
			List<AreaRankReward> rankrewards = bossreward.getRankingList();
			int index = 0;
			int myrank = 1;
			for(TypedTuple<String> rank : ranks){
				if(rankrewards.get(index).getRanking() < myrank && rankrewards.get(index).getRanking() >= 0)
					index++;
				if(index >= rankrewards.size())
					break;
				MultiReward.Builder multireward = MultiReward.newBuilder();
				multireward.addAllLoot(rankrewards.get(index).getRewardList());
//				rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
				MailBean mail = new MailBean();
				mail.setContent("");
//				mail.setRewardList(buildRewardList(ladderDaily));
				mail.setStartDate(DateUtil.getCurrentDateString());
				mail.setType(MailConst.TYPE_SYSTEM_MAIL);
				mail.setUserId(Long.parseLong(rank.getValue()));
				mail.parseRewardList(multireward.getLootList());
				mailRedisService.addMail(mail);
			}
		}
		redis.saveBoss(builder.build(), user);
		return SuccessConst.PVP_ATTACK_SUCCESS;
	}
	
	public boolean AttackResource(int id, UserBean user){
		AreaResource resource = redis.getResource(id, user);
		if(resource == null)
			return false;
		if(!redis.setLock("S"+user.getServerId()+"_AreaResource_"+id))
			return false;
		AreaResource.Builder builder = AreaResource.newBuilder(resource);
		if (builder.getState() == 0) {//刺杀
			builder.setState(1);
			builder.setEndtime(System.currentTimeMillis()/1000+24*3600L);
			builder.setAttackerId(user.getUnionId());
			redis.saveResource(builder.build(), user);
		}else{
			costEnergy(user);
			if(user.getUnionId() == builder.getOwner().getUnionId()){//防守
				builder.addDefenses(user.buildShort());
			}else{//进攻
				builder.addAttacks(user.buildShort());
			}
			redis.saveResource(builder.build(), user);
		}
		return true;
	}
	
	public ResultConst AttackResourceMine(int id, int teamid, boolean ret, UserBean user, ResponseCommand.Builder responseBuilder){
		AreaResourceMine mine = redis.getResourceMine(id, user);
		if(mine == null)
			return ErrorConst.MAPINFO_ERROR;
		List<HeroInfoBean> herolist = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, herolist);
		if(mine.hasUser())
			costEnergy(user);
		if(!ret)
			return SuccessConst.PVP_ATTACK_FAIL;
		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder(mine);
		if(!gainMine(builder, user))
			return ErrorConst.AREAMINE_HURRY;
		builder.setUser(user.buildShort());
		builder.setEndTime(System.currentTimeMillis()/1000+mine.getTime());
		redis.saveResourceMine(builder.build(), user);
		return SuccessConst.PVP_ATTACK_SUCCESS;
	}
	
	public Team AttackResourceMineInfo(int id, UserBean user){
		AreaResourceMine mine = redis.getResourceMine(id, user);
		if(mine == null || !mine.hasUser())
			return null;
		return userTeamService.getTeamCache(mine.getUser().getId());
	}
	
	public boolean gainMine(AreaResourceMine.Builder builder, UserBean user){
		if(!builder.hasUser())
			return true;
		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+builder.getId()))
			return false;
		int yield = builder.getYield();
		if (System.currentTimeMillis() / 1000 < builder.getEndTime())
			yield = (int)((System.currentTimeMillis() / 1000 + builder.getTime() - builder.getEndTime())*yield/builder.getTime());
		if(yield > 0){
			MultiReward.Builder multireward = MultiReward.newBuilder();
			RewardInfo.Builder reward = RewardInfo.newBuilder();
			reward.setItemid(RewardConst.UNIONCOIN);
			reward.setCount(yield);
			multireward.addLoot(reward);
//			rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
			MailBean mail = new MailBean();
			mail.setContent("区域争夺矿点占领奖励");
			mail.setStartDate(DateUtil.getCurrentDateString());
			mail.setType(MailConst.TYPE_SYSTEM_MAIL);
			mail.setUserId(builder.getUser().getId());
			mail.parseRewardList(multireward.getLootList());
			mailRedisService.addMail(mail);
		}
		builder.clearUser();
		builder.clearEndTime();
		return true;
	}
	
	public void resourceFight(AreaResource.Builder builder, UserBean user){
		if(!redis.setLock("S"+user.getServerId()+"_AreaResource_"+builder.getId()))
			return;
		//防守玩家最后一个加入战斗
		List<UserInfo> attacks = builder.getAttacksList();
		List<UserInfo> defends = builder.getDefensesList();
		if(builder.hasOwner())
			defends.add(builder.getOwner());
		if(attacks.size() == 0){
			builder.setWarDefended(builder.getWarDefended()+1);
		}else if(defends.size() == 0){
			builder.setWarDefended(0);
			builder.setOwner(attacks.get(attacks.size()-1));
		}else{
			FightResultList.Builder resultlist = queueFight(attacks, defends);
			if(resultlist.getListCount() > 0){
				FightResult winresult = resultlist.getList(resultlist.getListCount()-1);
				if( winresult.getResult() == 1 ){//攻破
					UserInfo defendUser = null;
					for(UserInfo userinfo : attacks){
						if(userinfo.getId() == winresult.getWin().getUserId())
							defendUser = userinfo;
					}

					builder.setWarDefended(0);
					if(defendUser != null)
						builder.setOwner(defendUser);
				}else{
					builder.setWarDefended(builder.getWarDefended()+1);
				}
//				for(FightResult result : resultlist.getListList()){//发奖
//					
//				}
			}
		}
		if(builder.getWarDefended() >= 3){//防守结束
			builder.setState(0);
			builder.setWarDefended(0);
		}else{
			builder.setEndtime(System.currentTimeMillis()/1000+10*60);
		}
		builder.clearDefenses();
		builder.clearAttacks();
		redis.saveResource(builder.build(), user);
	}
	
	private void getBossRank(AreaBoss.Builder bossbuilder, UserBean user){
		if(bossbuilder.getHp() >= bossbuilder.getHpMax())
			return;
		Set<TypedTuple<String>> ranks = redis.getBossRank(bossbuilder.getId(), user);
		if(ranks.isEmpty())
			return;
		List<UserInfo> users = userService.getCaches(user.getServerId(), ranks);
		int i = 0;
		for(TypedTuple<String> rank : ranks){
			if(i >= users.size())
				break;
			Rank.Builder builder = Rank.newBuilder();
			builder.setRank(i+1);
			builder.setScore(rank.getScore().intValue());
			builder.setUser(users.get(i));
			bossbuilder.addRanks(builder.build());
			i++;
		}
	}

	public AreaMode getAreas(UserBean user) {
		AreaMode.Builder areamodebuilder = AreaMode.newBuilder(redis.getAreaMode());
//		Map<String, MultiReward> monsterrewardMap = redis.getAreaMonsterRewards();
		Map<String, AreaMonster> monsterMap = redis.getMonsters(user);
		Map<String, AreaBoss.Builder> bossMap = redis.getBosses(user);
		Map<String, String> bosstimeMap = redis.getBossTimes(user);
		Map<String, AreaResource> resourceMap = redis.getResources(user);
		Map<String, AreaResourceMine> mineMap = redis.getResourceMines(user);
		List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
		for (AreaInfo.Builder areabuilder : areas) {
			// 更新世界BOSS
			for (AreaBoss.Builder bossbuilder : bossMap.values()) {
				if(bossbuilder.getBelongto() == areabuilder.getId()){
					if(bosstimeMap.containsKey(bossbuilder.getId()+""))
						bossbuilder.setCount(bossbuilder.getCount() - Integer.parseInt(bosstimeMap.get(bossbuilder.getId()+"")));
					getBossRank(bossbuilder, user);
					areabuilder.addBosses(bossbuilder);
				}
			}
			// 更新区域怪物
			for (AreaMonster monster : monsterMap.values()) {
				if(monster.getBelongto() == areabuilder.getId()){
					areabuilder.addMonsters(monster);
				}
			}
			// 更新资源开采点
			// 更新资源点
			for (AreaResource.Builder resourcebuilder : areabuilder.getResourcesBuilderList()) {
				AreaResource resource = resourceMap.get(resourcebuilder.getId() + "");
				if (resource != null) {
					AreaResource.Builder builder = AreaResource.newBuilder(resource);
					if (builder.hasEndtime() && System.currentTimeMillis() / 1000 >= builder.getEndtime()) {// 攻城开始
						resourceFight(builder, user);
					}
					resourcebuilder.mergeFrom(builder.build());
				}
//				for (int i = 1; i <= resourcebuilder.getCount(); i++)
//					resourcebuilder.addMines(redis.buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId() - 1) * 20 + i));
				for (AreaResourceMine.Builder minebuilder : resourcebuilder.getMinesBuilderList()) {
					AreaResourceMine mine = mineMap.get(minebuilder.getId() + "");
					if (mine != null) {
						AreaResourceMine.Builder builder2 = AreaResourceMine.newBuilder(mine);
						if (System.currentTimeMillis() / 1000 >= builder2.getEndTime()// 收获
							&& gainMine(builder2, user))
							redis.saveResourceMine(builder2.build(), user);
						minebuilder.mergeFrom(builder2.build());
					} else {
						minebuilder.mergeFrom(redis.buildAreaResourceMine(resourcebuilder.build(), minebuilder.getId()));
						redis.saveResourceMine(minebuilder.build(), user);
					}
				}
			}
		}
		return areamodebuilder.build();
	}
}
