package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import com.trans.pixel.model.mapper.UserAreaPropMapper;
import com.trans.pixel.model.userinfo.UserAreaPropBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaBoss;
import com.trans.pixel.protoc.Commands.AreaBossReward;
import com.trans.pixel.protoc.Commands.AreaBuff;
import com.trans.pixel.protoc.Commands.AreaEquip;
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
import com.trans.pixel.protoc.Commands.ResponseAreaEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.WeightReward;
import com.trans.pixel.service.command.PushCommandService;
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
	UserAreaPropMapper mapper;
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
	@Resource
	PushCommandService pusher;
	final int WARTIME = 60;
	final int PROTECTTIME = 30;
	
	public void addAreaEquip(UserBean user, int rewardId, int rewardCount){
		Collection<AreaEquip> equips = AreaEquips(user);
		for(AreaEquip equip : equips){
			if(equip.getId() == rewardId){
				AreaEquip.Builder builder = AreaEquip.newBuilder(equip);
				builder.setCount(builder.getCount()+rewardCount);
				redis.saveMyAreaEquip(user, builder.build());
				mapper.updateUserAreaProp(new UserAreaPropBean().parse(builder.build(), user));
				return;
			}
		}
		AreaEquip.Builder builder = AreaEquip.newBuilder(redis.getAreaEquip(rewardId));
		builder.setCount(builder.getCount()+rewardCount);
		UserAreaPropBean bean = new UserAreaPropBean().parse(builder.build(), user);
		mapper.addUserAreaProp(bean);
		builder.setPrimaryId(bean.getPrimaryid());
		redis.saveMyAreaEquip(user, builder.build());
	}
	
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
	
	public ResultConst AttackResource(int id, boolean ret, UserBean user){
		Map<String, AreaResource> resources = redis.getResources(user);
		AreaResource.Builder builder = null;
		for(AreaResource resource : resources.values()){
			if(resource.getId() == id)
				builder = AreaResource.newBuilder(resource);
			if(resource.hasOwner() && resource.getOwner().getId() == user.getId())
				return ErrorConst.RESOURCE_LAIRD;
			for(UserInfo userinfo : resource.getAttacksList()){
				if(userinfo.getId() == user.getId())
					return ErrorConst.JOIN_AGAIN;
			}
			for(UserInfo userinfo : resource.getDefensesList()){
				if(userinfo.getId() == user.getId())
					return ErrorConst.JOIN_AGAIN;
			}
		}
		if(builder == null)
			return ErrorConst.MAPINFO_ERROR;
		if (builder.getState() == 0) {//刺杀
			if(!ret){
				costEnergy(user);
				return SuccessConst.AREA_ATTACK_FAIL;
			}
			builder.setState(1);
			long time = redis.now();
			builder.setStarttime(time);
			builder.setClosetime(time+/*24*3600L*/WARTIME);
			builder.setEndtime(builder.getClosetime()+PROTECTTIME);
			if(builder.hasOwner()){
				builder.setMessage("领主"+builder.getOwner().getName()+"已经被"+user.getUserName()+"刺杀");
				builder.setAttackerId(user.getUnionId());
			}else{
				builder.setMessage("领主已经被"+user.getUserName()+"刺杀");
				builder.setOwner(user.buildShort());
			}
			if(!redis.setLock("S"+user.getServerId()+"_AreaResource_"+id))
				return ErrorConst.ERROR_LOCKED;
			costEnergy(user);
			redis.saveResource(builder.build(), user);
		}else{
			if(builder.getStarttime() > redis.now())
				return ErrorConst.JOIN_NOT_START;
			if(builder.getClosetime() < redis.now())
				return ErrorConst.JOIN_END;
			if(user.getUnionId() == builder.getOwner().getUnionId() && user.getUnionId() != 0){//防守
				builder.addDefenses(user.buildUnionShort());
			}else{//进攻
				builder.addAttacks(user.buildUnionShort());
			}
			if(!redis.setLock("S"+user.getServerId()+"_AreaResource_"+id))
				return ErrorConst.ERROR_LOCKED;
			costEnergy(user);
			redis.saveResource(builder.build(), user);
		}
		return SuccessConst.Add_AREA_FIGHT;
	}
	
	public ResultConst collectMine(Builder responseBuilder, int id, UserBean user){
		ResultConst result = null;
		Map<String, String> map = redis.getMineGains(user);
		redis.delMineGains(user);
		int collect = 0;
		for(Entry<String, String> entry : map.entrySet()){
			collect += Integer.parseInt(entry.getValue());
		}
		AreaResourceMine.Builder mine = AreaResourceMine.newBuilder(redis.getResourceMine(id, user));
		if(mine == null || !mine.hasUser() || mine.getUser().getId() != user.getId())
			result = ErrorConst.MAPINFO_ERROR;
		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+mine.getId()))
			result = ErrorConst.ERROR_LOCKED;
		else{
			collect += gainMine(mine, user, false);
		}
		if(collect > 0){
			user.setPointUnion(user.getPointUnion()+collect);
			userService.updateUser(user);
			pusher.pushRewardCommand(responseBuilder, user, RewardConst.UNIONCOIN, "", collect);
			pusher.pushUserInfoCommand(responseBuilder, user);
		}
		return result;
	}
	
	public ResultConst AttackResourceMine(int id, int teamid, boolean ret, UserBean user, ResponseCommand.Builder responseBuilder){
		Map<String, AreaResourceMine> mines = redis.getResourceMines(user);
		AreaResourceMine mine = null;
		AreaResourceMine.Builder gainmine = null;
		for(AreaResourceMine resourcemine : mines.values()){
			if(resourcemine.getId() == id)
				mine = resourcemine;
			else if(resourcemine.getUser().getId() == user.getId()){
				gainmine = AreaResourceMine.newBuilder(resourcemine);
			}
		}
		
		if(mine == null)
			return ErrorConst.MAPINFO_ERROR;
		List<HeroInfoBean> herolist = userTeamService.getTeam(user, teamid);
		userTeamService.saveTeamCache(user, herolist);
		if(!ret){
			costEnergy(user);
			return SuccessConst.PVP_ATTACK_FAIL;
		}
		if(gainmine != null){
			if(!redis.setLock("S"+user.getServerId()+"_Mine_"+gainmine.getId()))
				return ErrorConst.ERROR_LOCKED;
			else{
				int collect = gainMine(gainmine, user, true);
				collect += redis.getMineGain(gainmine.getId(), user);
				redis.saveMineGain(gainmine.getId(), collect, user);
			}
		}
		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+mine.getId()))
			return ErrorConst.ERROR_LOCKED;

		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder(mine);
		if(mine.hasUser())
			costEnergy(user);
		builder.setUser(user.buildShort());
		long time = System.currentTimeMillis()/1000;
		builder.setEndTime(time+mine.getTime());
		builder.setCollectTime(time);
		redis.saveResourceMine(builder.build(), user);
		return SuccessConst.PVP_ATTACK_SUCCESS;
	}
	
	public Team AttackResourceMineInfo(int id, UserBean user){
		AreaResourceMine mine = redis.getResourceMine(id, user);
		if(mine == null)
			return null;
		else if(!mine.hasUser())
			return Team.newBuilder().build();
		return userTeamService.getTeamCache(mine.getUser().getId());
	}
	
	public int gainMine(AreaResourceMine.Builder builder, UserBean user, boolean release){
//		if(!builder.hasUser())
//			return 0;
//		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+builder.getId()))
//			return false;
		int yield = (int)((builder.getEndTime() - builder.getCollectTime())/3600*builder.getYield());
		if(release){
			if (System.currentTimeMillis() / 1000 < builder.getEndTime()){
				yield = (int)((System.currentTimeMillis() / 1000 + builder.getTime() - builder.getEndTime())*builder.getYield()/3600);
			}
			builder.clearUser();
			builder.clearEndTime();
			builder.clearCollectTime();
		}else{
			if (System.currentTimeMillis() / 1000 < builder.getEndTime()){
				int hour = (int)((System.currentTimeMillis() / 1000 - builder.getCollectTime())/3600);
				yield = hour * builder.getYield();
				builder.setCollectTime(builder.getCollectTime()+hour*3600);
			}else{
				builder.clearUser();
				builder.clearEndTime();
				builder.clearCollectTime();
			}
		}
		redis.saveResourceMine(builder.build(), user);
//		if(yield > 0){
//			MultiReward.Builder multireward = MultiReward.newBuilder();
//			RewardInfo.Builder reward = RewardInfo.newBuilder();
//			reward.setItemid(RewardConst.UNIONCOIN);
//			reward.setCount(yield);
//			multireward.addLoot(reward);
////			rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
//			MailBean mail = new MailBean();
//			mail.setContent("区域争夺矿点占领奖励");
//			mail.setStartDate(DateUtil.getCurrentDateString());
//			mail.setType(MailConst.TYPE_SYSTEM_MAIL);
//			mail.setUserId(builder.getUser().getId());
//			mail.parseRewardList(multireward.getLootList());
//			mailRedisService.addMail(mail);
//		}
		return yield;
	}
	
	public void resourceFight(AreaResource.Builder builder, UserBean user){
		if(!redis.setLock("S"+user.getServerId()+"_AreaResource_"+builder.getId()))
			return;
		//防守玩家最后一个加入战斗
		List<UserInfo> attacks = new ArrayList<UserInfo>();
		attacks.addAll(builder.getAttacksList());
		List<UserInfo> defends = new ArrayList<UserInfo>();
		defends.addAll(builder.getDefensesList());
		if(builder.hasOwner())
			defends.add(builder.getOwner());
		if(attacks.size() == 0){
			builder.setWarDefended(builder.getWarDefended()+1);
			builder.setMessage("当前已成功防守"+builder.getWarDefended()+"波敌人！");
		}else{
			FightResultList.Builder resultlist = queueFight(attacks, defends);
			redis.saveFight(builder.getId(), resultlist.build());
			if(resultlist.getListCount() > 0){
				FightResult winresult = resultlist.getList(resultlist.getListCount()-1);
				if( winresult.getResult() == 1 ){//攻破
					UserInfo owner = null;
					for(UserInfo userinfo : attacks){
						if(userinfo.getId() == winresult.getWin().getUserId())
							owner = userService.getCache(user.getServerId(), userinfo.getId());
					}

					builder.setWarDefended(0);
					if(owner != null){
						builder.setOwner(owner);
						builder.setMessage(builder.getOwner().getName()+"成功拿下了据点！");
					}
				}else{
					builder.setWarDefended(builder.getWarDefended()+1);
					builder.setMessage("当前已成功防守"+builder.getWarDefended()+"波敌人！");
				}
//				for(FightResult result : resultlist.getListList()){//发奖
//					
//				}
			}
		}
		if(builder.getWarDefended() >= 3){//防守结束
			builder.setState(0);
			builder.setWarDefended(0);
			builder.clearMessage();
		}else{
			long time = Math.max(redis.now(), builder.getEndtime());
			builder.setStarttime(time);
			builder.setClosetime(time+WARTIME);
			builder.setEndtime(builder.getClosetime()+PROTECTTIME);
		}
		builder.clearDefenses();
		builder.clearAttacks();
		builder.clearAttackerId();
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
	
	public boolean unlockArea(int id, int zhanli, UserBean user){
		AreaMode.Builder areamode = redis.getAreaMode(user);
		for(AreaInfo.Builder areainfo : areamode.getRegionBuilderList()){
			if(id == areainfo.getId()){
				if(zhanli >= areainfo.getZhanli()){
					areainfo.setOpened(true);
					user.setAreaUnlock(id);
					userService.updateUserDailyData(user);
					redis.saveAreaMode(areamode.build(), user);
					return true;
				}
			}
		}
		return false;
	}

	public AreaResource getResource(int id, UserBean user) {
		AreaResource resource = redis.getResource(id,user);
		if(resource == null)
			return null;
		AreaResource.Builder builder = AreaResource.newBuilder(resource);
		if (builder.hasClosetime() && System.currentTimeMillis() / 1000 >= builder.getClosetime()) {// 攻城开始
			resourceFight(builder, user);
		}
		return builder.build();
	}
	
	public boolean hasReward(UserBean user){
		if(redis.getMineGains(user).isEmpty())
			return false;
		else
			return true;
	}

	public Collection<AreaBuff> AreaBuffs(UserBean user) {
		return redis.getMyAreaBuffs(user);
	}

	public Collection<AreaEquip> AreaEquips(UserBean user) {
		Map<String, AreaEquip> equipMap = redis.getMyAreaEquips(user);
		if(equipMap.isEmpty()){
			List<UserAreaPropBean> beans = mapper.selectUserAreaPropList(user.getId());
			for(UserAreaPropBean bean : beans){
				AreaEquip.Builder builder = bean.build();
				builder.mergeFrom(redis.getAreaEquip(bean.getId()));
				equipMap.put(builder.getId()+"", builder.build());
			}
			redis.saveMyAreaEquips(user, equipMap.values());
		}
		return equipMap.values();
	}
	public ResultConst useAreaEquips(int id, Builder responseBuilder, UserBean user) {
		ResultConst result = SuccessConst.USE_PROP;
		Map<String, AreaEquip> equipMap = redis.getMyAreaEquips(user);
		for(AreaEquip equip : equipMap.values()){
			if(equip.getId() == id){
				AreaEquip.Builder builder = AreaEquip.newBuilder(equip);
				if(builder.getCount() > 0){
					builder.setCount(builder.getCount()-1);
					// builder.setEndTime(redis.now()+builder.getTime()*60);
					// builder.setLevel(builder.getLevel()+1);
					// if(builder.getLevel() > builder.getLayer() && builder.getLayer() > 0)
					// 	builder.setLevel(builder.getLayer());
					redis.saveMyAreaEquip(user, builder.build());
					AreaBuff.Builder buff = redis.getMyAreaBuff(user, builder.getSkill());
					buff.setLevel(buff.getLevel());
					buff.setEndTime(redis.now()+builder.getTime()*60);
					if(buff.getLevel() > builder.getLayer() && builder.getLayer() > 0)
						buff.setLevel(builder.getLayer());
					redis.saveMyAreaBuff(user, buff.build());
					mapper.updateUserAreaProp(new UserAreaPropBean().parse(builder.build(), user));
					equipMap.put(builder.getId()+"", builder.build());
				}else{
					result = ErrorConst.NOT_ENOUGH;
				}
				break;
			}
		}
		ResponseAreaEquipCommand.Builder builder = ResponseAreaEquipCommand.newBuilder();
		builder.addAllEquips(equipMap.values());
		builder.addAllBuffs(redis.getMyAreaBuffs(user));
		responseBuilder.setAreaEquipCommand(builder.build());
		return result;
	}
	
	public AreaMode getAreas(UserBean user) {
		AreaMode.Builder areamodebuilder = redis.getAreaMode(user);
//		Map<String, MultiReward> monsterrewardMap = redis.getAreaMonsterRewards();
		Map<String, AreaMonster> monsterMap = redis.getMonsters(user);
		Map<String, AreaBoss.Builder> bossMap = redis.getBosses(user);
		Map<String, String> bosstimeMap = redis.getBossTimes(user);
		Map<String, AreaResource> resourceMap = redis.getResources(user);
		Map<String, AreaResourceMine> mineMap = redis.getResourceMines(user);
		List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
		for (AreaInfo.Builder areabuilder : areas) {
			if(!areabuilder.getOpened())
				continue;
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
					if (builder.hasClosetime() && System.currentTimeMillis() / 1000 >= builder.getClosetime()) {// 攻城开始
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
							&& builder2.hasUser()){
							int yield = gainMine(builder2, user, true);
							yield += redis.getMineGain(builder2.getId(), user);
							redis.saveMineGain(builder2.getId(), yield, user);
						}
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
