package com.trans.pixel.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaInfo;
import com.trans.pixel.protoc.Commands.AreaMode;
import com.trans.pixel.protoc.Commands.AreaMonster;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.AreaResourceMine;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.Rank;
import com.trans.pixel.protoc.Commands.UserInfo;
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
	private UserBean user = null;

	public List<AreaInfo> Area(){
		List<AreaInfo> areas = getAreas().getRegionList();
		return areas;
	}

	public boolean AttackMonster(int id){
		AreaMonster monster = redis.getMonster(id);
		if(monster == null)
			return false;
		AreaMonster.Builder builder = AreaMonster.newBuilder(monster);
		if(builder.hasKilled())
			return false;
//		redis.deleteMonster(builder.getId());
		builder.setKilled(true);
		redis.saveMonster(builder.build());
		rewardService.doRewards(user, builder.getReward());
		return true;
	}
	
	public boolean AttackBoss(int id, int score){
		AreaBoss boss = redis.getBoss(id);
		if(boss == null || boss.getHp() <= 0)
			return false;
		redis.addBossRank(id, score);
		AreaBoss.Builder builder = AreaBoss.newBuilder(boss);
		builder.setHp(builder.getHp()-score);
		if(builder.getHp() <= 0){//结算
			builder.setOwner(user.buildShort());
			Set<TypedTuple<String>> ranks = redis.getBossRank(id);
			for(TypedTuple<String> rank : ranks){
				rewardService.doRewards(Long.parseLong(rank.getValue()), boss.getReward());
			}
		}
		redis.saveBoss(builder.build());
		return true;
	}
	
	public boolean AttackResource(int id){
		AreaResource resource = redis.getResource(id);
		if(resource == null)
			return false;
		AreaResource.Builder builder = AreaResource.newBuilder(resource);
		if (builder.getState() == 0) {//刺杀
			builder.setState(1);
			builder.setEndtime(System.currentTimeMillis()/1000+24*3600L);
			builder.setAttackerId(user.getUnionId());
			redis.saveResource(builder.build());
		}else{
			if(user.getUnionId() == builder.getOwner().getUnionId()){//防守
				builder.addDefenses(user.buildShort());
			}else{//进攻
				builder.addAttacks(user.buildShort());
			}
			redis.saveResource(builder.build());
		}
		return true;
	}
	
	public boolean AttackResourceMine(int id){
		AreaResourceMine mine = redis.getResourceMine(id);
		if(mine == null)
			return false;
		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder(mine);
		gainMine(builder);
		builder.setUser(user.buildShort());
		builder.setEndTime(System.currentTimeMillis()/1000+12*3600);
		redis.saveResourceMine(builder.build());
		return true;
	}
	
	public boolean gainMine(AreaResourceMine.Builder builder){
		if(!builder.hasUser())
			return false;
		if(!redis.setLock("AreaResourceMine_"+builder.getId(), System.currentTimeMillis()))
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
	
	public void resourceFight(AreaResource.Builder builder){
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
		redis.saveResource(builder.build());
	}
	
	private void getBossRank(AreaBoss.Builder bossbuilder){
		if(bossbuilder.getHp() >= bossbuilder.getHpMax())
			return;
		Set<TypedTuple<String>> ranks = redis.getBossRank(bossbuilder.getId());
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

	private AreaMode getAreas() {
		AreaMode.Builder areamodebuilder = AreaMode.newBuilder(redis.getAreaMode());
		Map<String, MultiReward> monsterrewardMap = redis.getAreaMonsterRewards();
		Map<String, AreaMonster> monsterMap = redis.getMonsters();
		Map<String, AreaBoss> bossMap = redis.getBosses();
		Map<String, AreaResource> resourceMap = redis.getResources();
		Map<String, AreaResourceMine> mineMap = redis.getResourceMines();
		List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
		for (AreaInfo.Builder areabuilder : areas) {
			// 更新世界BOSS
			List<AreaBoss.Builder> bosses = areabuilder.getBossesBuilderList();
			for (AreaBoss.Builder bossbuilder : bosses) {
				AreaBoss boss = bossMap.get(bossbuilder.getId() + "");
				if (boss != null) {
					bossbuilder.mergeFrom(boss);
					getBossRank(bossbuilder);
				} else {
					AreaBoss newboss = redis.createBoss(bossbuilder.getId());
					bossbuilder.mergeFrom(newboss);
				}
				MultiReward reward = monsterrewardMap.get(bossbuilder.getId() + "");
				if (reward != null)
					bossbuilder.setReward(reward);
			}
			// 更新区域怪物
			List<AreaMonster.Builder> monsters = areabuilder.getMonstersBuilderList();
			for (AreaMonster.Builder monsterbuilder : monsters) {
				AreaMonster monster = monsterMap.get(monsterbuilder.getId() + "");
				if (monster != null) {
					monsterbuilder.mergeFrom(monster);
				} else {
					AreaMonster newmonster = redis.createMonster(monsterbuilder.getId());
					monsterbuilder.mergeFrom(newmonster);
				}
				MultiReward reward = monsterrewardMap.get(monsterbuilder.getId() + "");
				if (reward != null)
					monsterbuilder.setReward(reward);
			}
			// 更新资源开采点
			// 更新资源点
			for (AreaResource.Builder resourcebuilder : areabuilder.getResourcesBuilderList()) {
				AreaResource resource = resourceMap.get(resourcebuilder.getId() + "");
				if (resource != null) {
					AreaResource.Builder builder = AreaResource.newBuilder(resource);
					if (builder.hasEndtime() && System.currentTimeMillis() / 1000 >= builder.getEndtime()) {// 攻城开始
						resourceFight(builder);
					}
					resourcebuilder.mergeFrom(builder.build());
				} else {
					resourcebuilder.mergeFrom(redis.buildAreaResource(resourcebuilder.getId()));
					redis.saveResource(resourcebuilder.build());
				}
				for (int i = 1; i <= resourcebuilder.getCount(); i++)
					resourcebuilder.addMines(redis.buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId() - 1) * 20 + i));
				for (AreaResourceMine.Builder minebuilder : resourcebuilder.getMinesBuilderList()) {
					AreaResourceMine mine = mineMap.get(minebuilder.getId() + "");
					if (mine != null) {
						AreaResourceMine.Builder builder2 = AreaResourceMine.newBuilder(mine);
						if (System.currentTimeMillis() / 1000 >= builder2.getEndTime()// 收获
							&& gainMine(builder2))
							redis.saveResourceMine(builder2.build());
						minebuilder.mergeFrom(builder2.build());
					} else {
						minebuilder.mergeFrom(redis.buildAreaResourceMine(resourcebuilder.build(), minebuilder.getId()));
						redis.saveResourceMine(minebuilder.build());
					}
				}
			}
		}
		return areamodebuilder.build();
	}

	public void setUserNX(UserBean user) {
		if(this.user != null)
			return;
		this.user = user;
		redis.setUser(user);
	}
}
