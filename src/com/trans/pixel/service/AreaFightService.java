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
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaMonsterReward;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceMine;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.Rank;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.WeightReward;
import com.trans.pixel.service.redis.AreaRedisService;

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

	public boolean AttackMonster(int id, UserBean user, MultiReward.Builder rewards){
		AreaMonster monster = redis.getMonster(id, user);
		if(monster == null)
			return false;
		AreaMonster.Builder builder = AreaMonster.newBuilder(monster);
		if(builder.hasKilled())
			return false;
//		redis.deleteMonster(builder.getId());
		builder.setKilled(true);
		redis.saveMonster(builder.build(), user);
		AreaMonsterReward monsterreward = redis.getAreaMonsterReward(id);
		for(WeightReward weightreward : monsterreward.getLootList()){
			rewards.addLoot(randReward(weightreward));
		}
		rewardService.doRewards(user, rewards.build());
		return true;
	}
	
	public boolean AttackBoss(int id, int score, UserBean user, MultiReward.Builder rewards){
		AreaBoss boss = redis.getBoss(id, user);
		if(boss == null || boss.getHp() <= 0)
			return false;
		redis.addBossRank(id, score, user);
		AreaBoss.Builder builder = AreaBoss.newBuilder(boss);
		builder.setHp(builder.getHp()-score);
		if(builder.getHp() <= 0){//结算
			builder.setOwner(user.buildShort());
			//击杀奖励
			AreaMonsterReward monsterreward = redis.getAreaMonsterReward(id);
			for(WeightReward weightreward : monsterreward.getLootList()){
				rewards.addLoot(randReward(weightreward));
			}
			rewardService.doRewards(user, rewards.build());
			//排行奖励
			Set<TypedTuple<String>> ranks = redis.getBossRank(id, user);
			for(TypedTuple<String> rank : ranks){
				rewardService.doRewards(Long.parseLong(rank.getValue()), rewards.build()/*debug only*/);
			}
		}
		redis.saveBoss(builder.build(), user);
		return true;
	}
	
	public boolean AttackResource(int id, UserBean user){
		AreaResource resource = redis.getResource(id, user);
		if(resource == null)
			return false;
		AreaResource.Builder builder = AreaResource.newBuilder(resource);
		if (builder.getState() == 0) {//刺杀
			builder.setState(1);
			builder.setEndtime(System.currentTimeMillis()/1000+24*3600L);
			builder.setAttackerId(user.getUnionId());
			redis.saveResource(builder.build(), user);
		}else{
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
		if(!ret)
			return SuccessConst.PVP_ATTACK_FAIL;
		List<HeroInfoBean> herolist = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user.getId(), herolist);
		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder(mine);
		gainMine(builder, user);
		builder.setUser(user.buildShort());
		builder.setEndTime(System.currentTimeMillis()/1000+12*3600);
		redis.saveResourceMine(builder.build(), user);
		return SuccessConst.PVP_ATTACK_SUCCESS;
	}
	
	public List<HeroInfoBean> AttackResourceMineInfo(int id, UserBean user){
		AreaResourceMine mine = redis.getResourceMine(id, user);
		if(mine == null || !mine.hasUser())
			return new ArrayList<HeroInfoBean>();
		return userTeamService.getTeamCache(mine.getUser().getId());
	}
	
	public boolean gainMine(AreaResourceMine.Builder builder, UserBean user){
		if(!builder.hasUser())
			return false;
		if(!redis.setLock(user.getServerId()+"_Mine_"+builder.getId(), System.currentTimeMillis()))
			return false;
		int yield = builder.getYield();
		if (System.currentTimeMillis() / 1000 < builder.getEndTime())
			yield = (int)((System.currentTimeMillis() / 1000 + builder.getTime()*3600 - builder.getEndTime())*yield/builder.getTime()/3600);
		if(yield > 0){
			UserBean bean = userService.getUser(builder.getUser().getId());
			bean.setCoin(bean.getCoin()+yield);
			userService.updateUser(bean);
		}
		builder.clearUser();
		builder.clearEndTime();
		return true;
	}
	
	public void resourceFight(AreaResource.Builder builder, UserBean user){
		if(!redis.setLock(user.getServerId()+"_AreaResourceFight_"+builder.getId(), System.currentTimeMillis()))
			return;
		//防守玩家最后一个加入战斗
		List<UserInfo> attacks = builder.getAttacksList();
		List<UserInfo> defenses = builder.getDefensesList();
		if(queueFight(attacks, defenses)){
			builder.setWarDefended(0);
			builder.setOwner(attacks.get(0));
		}else{
			builder.setWarDefended(builder.getWarDefended()+1);
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
		List<UserInfo> users = userService.getCaches(ranks);
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
		AreaMode.Builder areamodebuilder = AreaMode.newBuilder(redis.getAreaMode(user));
//		Map<String, MultiReward> monsterrewardMap = redis.getAreaMonsterRewards();
		Map<String, AreaMonster> monsterMap = redis.getMonsters(user);
		Map<String, AreaBoss> bossMap = redis.getBosses(user);
		Map<String, AreaResource> resourceMap = redis.getResources(user);
		Map<String, AreaResourceMine> mineMap = redis.getResourceMines(user);
		List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
		for (AreaInfo.Builder areabuilder : areas) {
			// 更新世界BOSS
			List<AreaBoss.Builder> bosses = areabuilder.getBossesBuilderList();
			for (AreaBoss.Builder bossbuilder : bosses) {
				AreaBoss boss = bossMap.get(bossbuilder.getId() + "");
				if (boss != null) {
					bossbuilder.mergeFrom(boss);
					getBossRank(bossbuilder, user);
				} else {
					AreaBoss newboss = redis.createBoss(bossbuilder.getId(), user);
					bossbuilder.mergeFrom(newboss);
				}
//				MultiReward reward = monsterrewardMap.get(bossbuilder.getId() + "");
//				if (reward != null)
//					bossbuilder.setReward(reward);
			}
			// 更新区域怪物
			for (AreaMonster monster : monsterMap.values()) {
				if(monster.getBelongto() == areabuilder.getId()){
					areabuilder.addMonsters(monster);
	//				MultiReward reward = monsterrewardMap.get(monsterbuilder.getId() + "");
	//				if (reward != null)
	//					monsterbuilder.setReward(reward);
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
				} else {
					resourcebuilder.mergeFrom(redis.buildAreaResource(resourcebuilder.getId()));
					redis.saveResource(resourcebuilder.build(), user);
				}
				for (int i = 1; i <= resourcebuilder.getCount(); i++)
					resourcebuilder.addMines(redis.buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId() - 1) * 20 + i));
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
