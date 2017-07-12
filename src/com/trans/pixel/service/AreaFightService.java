package com.trans.pixel.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import com.trans.pixel.model.mapper.UserAreaPropMapper;
import com.trans.pixel.model.userinfo.UserAreaPropBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.AreaProto.AreaBoss;
import com.trans.pixel.protoc.AreaProto.AreaBuff;
import com.trans.pixel.protoc.AreaProto.AreaEquip;
import com.trans.pixel.protoc.AreaProto.AreaInfo;
import com.trans.pixel.protoc.AreaProto.AreaMode;
import com.trans.pixel.protoc.AreaProto.AreaMonster;
import com.trans.pixel.protoc.AreaProto.AreaMonsterReward;
import com.trans.pixel.protoc.AreaProto.AreaResource;
import com.trans.pixel.protoc.AreaProto.FightResult;
import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.AreaProto.ResponseAreaEquipCommand;
import com.trans.pixel.protoc.AreaProto.WeightReward;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.Rank;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.service.command.PushCommandService;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.MailRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

/**
 * 1.1.3.6区域争夺战
 */
@Service
public class AreaFightService extends FightService{
	Logger logger = Logger.getLogger(AreaFightService.class);
	@Resource
	private UserAreaPropMapper mapper;
	@Resource
   private AreaRedisService redis;
	@Resource
   private UserService userService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private MailRedisService mailRedisService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private UnionRedisService unionRedisService;
	@Resource
	private RedisService redisService;
	
	private static final int TYPE_UNION_BUFF = 1;
	
	public void delAreaEquip(long userId, int rewardId){
		UserBean user = new UserBean();
		user.setId(userId);
		AreaEquip.Builder builder = redis.getMyAreaEquip(rewardId, user);
		if(builder == null)
			return;
		builder.setCount(0);
		redis.saveMyAreaEquip(user, builder.build());
		mapper.updateUserAreaProp(new UserAreaPropBean().parse(builder.build(), user));
	}
	
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
	
	private RewardInfo randReward(WeightReward weightreward, int level){
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setItemid(0);
		reward.setCount(0);
		int index = (int)(weightreward.getWeightall()*Math.random());
		if(index < weightreward.getWeight1A() + (int)(weightreward.getWeight1B() * level)){
			reward.setItemid(weightreward.getItemid1());
			reward.setCount(weightreward.getCount1A() + (int)(weightreward.getCount1B() * level));
			
			return reward.build();
		}
		
		if (weightreward.getItemid2().isEmpty())
			return reward.build();
		
		if(index < TypeTranslatedUtil.stringToInt(weightreward.getWeight2A()) + (int)(TypeTranslatedUtil.stringToFloat(weightreward.getWeight2B()) * level)){
			reward.setItemid(TypeTranslatedUtil.stringToInt(weightreward.getItemid2()));
			reward.setCount(TypeTranslatedUtil.stringToInt(weightreward.getCount2A()) + (int)(TypeTranslatedUtil.stringToFloat(weightreward.getCount2B()) * level));
			
			return reward.build();
		}
		
		if (weightreward.getItemid3().isEmpty())
			return reward.build();
		
		if(index < TypeTranslatedUtil.stringToInt(weightreward.getWeight3A()) + (int)(TypeTranslatedUtil.stringToFloat(weightreward.getWeight3B()) * level)){
			reward.setItemid(TypeTranslatedUtil.stringToInt(weightreward.getItemid3()));
			reward.setCount(TypeTranslatedUtil.stringToInt(weightreward.getCount3A()) + (int)(TypeTranslatedUtil.stringToFloat(weightreward.getCount3B()) * level));
		}
		
		return reward.build();
	}
	
	public void costEnergy(UserBean user, int count){
		redis.costEnergy(user, count);
	}

	public boolean AttackMonster(int positionid, UserBean user, MultiReward.Builder rewards){
		AreaMonster monster = redis.getMonster(positionid, user);
		if(monster == null)
			return false;
		redis.getMonsters(user, new ArrayList<Integer>());
		redis.deleteMonster(monster.getPositionid(), user);
		AreaMonsterReward monsterreward = redis.getAreaMonsterReward(monster.getId());
		for(WeightReward weightreward : monsterreward.getLootList()){
			rewards.addLoot(randReward(weightreward, monster.getLevel()));
		}

//		AreaMode.Builder areamode = redis.getAreaMode(user);
		String[] level = redis.addLevel(monster.getBelongto(), monster.getLevel2(), user);
		if(TypeTranslatedUtil.stringToInt(level[2]) == 0) {
//			for(AreaInfo.Builder areainfo : areamode.getRegionBuilderList()){
//				if(user.getAreaUnlock() == monster.getBelongto() && areainfo.getId() > user.getAreaUnlock()){
//					if(level[1] >= areainfo.getZhanli()){
//						unlockArea(areainfo.getId(), level[1], user);
						level[2] = "1";
						redis.saveOriginalLevel(monster.getBelongto(), level, user);
//					}
//				}
//			}
		}
		costEnergy(user, monster.getPl());
		return true;
	}

	public ResultConst AttackBoss(int id, int score, UserBean user, MultiReward.Builder rewards){
		AreaBoss boss = redis.getBoss(id, user);
//		if(boss == null || boss.getHp() <= 0)
		if(boss == null)
			return ErrorConst.NOT_MONSTER;
		int time = redis.getBossTime(id,user);
		if(time >= boss.getCount())
			return ErrorConst.NOT_ENOUGH_TIMES;
		redis.saveBossTime(id, time+1, user);
		redis.addBossRank(id, score, user);
		AreaBoss.Builder builder = AreaBoss.newBuilder(boss);
		builder.setHp(builder.getHp()-score);
		if(builder.getHp() <= 0){//结算
			if(!redisService.setLock("S"+user.getServerId()+"_AreaBoss_"+builder.getId()))
				return ErrorConst.NOT_MONSTER;
			builder.setOwner(user.buildShort());
			//击杀奖励
			AreaMonsterReward monsterreward = redis.getAreaMonsterReward(id);
			for(WeightReward weightreward : monsterreward.getLootList()){
				rewards.addLoot(randReward(weightreward, boss.getBosslv()));
			}
//			//排行奖励
//			Set<TypedTuple<String>> ranks = redis.getBossRank(id, user);
//			AreaBossReward bossreward = redis.getBossReward(id);
//			List<AreaRankReward> rankrewards = bossreward.getRankingList();
//			int index = 0;
//			int myrank = 1;
//			for(TypedTuple<String> rank : ranks){
//				if(rankrewards.get(index).getRanking() < myrank && rankrewards.get(index).getRanking() >= 0)
//					index++;
//				if(index >= rankrewards.size())
//					break;
//				MultiReward.Builder multireward = MultiReward.newBuilder();
//				multireward.addAllLoot(rankrewards.get(index).getRewardList());
////				rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
//				MailBean mail = new MailBean();
//				mail.setContent("");
////				mail.setRewardList(buildRewardList(ladderDaily));
//				mail.setStartDate(DateUtil.getCurrentDateString());
//				mail.setType(MailConst.TYPE_SYSTEM_MAIL);
//				mail.setUserId(Long.parseLong(rank.getValue()));
//				mail.parseRewardList(multireward.getLootList());
//				mailRedisService.addMail(mail);
//			}
		}
		redis.saveBoss(builder.build(), user);
		costEnergy(user, boss.getPl());
		return SuccessConst.PVP_ATTACK_SUCCESS;
	}
	
	public ResultConst AttackResource(int id, boolean iskill, boolean ret, UserBean user){
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
		if(builder.getStarttime() > RedisService.now())
			return ErrorConst.JOIN_NOT_START;
		if (iskill) {//刺杀
			if(builder.getState() != 0)
				return ErrorConst.NOT_ENEMY;
			if(user.getUnionId() != 0 && builder.hasOwner() && builder.getOwner().getUnionId() == user.getUnionId())
				return ErrorConst.SAVE_UNION;
			if(!ret){
				// costEnergy(user, 5);
				return SuccessConst.AREA_ATTACK_FAIL;
			}
			builder.setState(1);
			redis.addFight(user.getServerId(), builder.getId(), builder.getClosetime());
			long time = RedisService.now();
			builder.setStarttime(time);
			builder.setClosetime(time+/*24*3600L*/redis.WARTIME);
			builder.setEndtime(builder.getClosetime()+redis.PROTECTTIME);
			builder.clearMessage();
			if(builder.hasOwner()){
				builder.addMessage(new SimpleDateFormat("MM-dd HH:mm").format(new Date(System.currentTimeMillis()))+" 领主"+builder.getOwner().getName()+"已被"+user.getUserName()+"刺杀");
				builder.setAttackerId(user.getUnionId());
			}else{
				builder.addMessage(new SimpleDateFormat("MM-dd HH:mm").format(new Date(System.currentTimeMillis()))+" 领主已被"+user.getUserName()+"刺杀");
				Union.Builder union = unionRedisService.getUnion(user);
				UserInfo.Builder userInfoBuilder = UserInfo.newBuilder(user.buildShort());
				if (union != null) {
					userInfoBuilder.setUnionIcon(union.getIcon());
				}
				builder.setOwner(userInfoBuilder.build());
			}
			if(!redisService.setLock("S"+user.getServerId()+"_AreaResource_"+id))
				return ErrorConst.ERROR_LOCKED;
			costEnergy(user, 5);
			redis.saveResource(builder.build(), user.getServerId());
			redisService.clearLock("S"+user.getServerId()+"_AreaResource_"+id);
			return SuccessConst.AREA_ATTACK_SUCCESS;
		}else{
			if(builder.getState() == 0)
				return ErrorConst.JOIN_NOT_START;
			if(builder.getStarttime() > RedisService.now())
				return ErrorConst.JOIN_NOT_START;
			if(builder.getClosetime() < RedisService.now())
				return ErrorConst.JOIN_END;
			if(user.getUnionId() == builder.getOwner().getUnionId() && user.getUnionId() != 0){//防守
				builder.addDefenses(user.buildShort());
			}else{//进攻
				builder.addAttacks(user.buildShort());
			}
			if(!redisService.setLock("S"+user.getServerId()+"_AreaResource_"+id))
				return ErrorConst.ERROR_LOCKED;
			costEnergy(user, 5);
			redis.saveResource(builder.build(), user.getServerId());
			redisService.clearLock("S"+user.getServerId()+"_AreaResource_"+id);
			return SuccessConst.Add_AREA_FIGHT;
		}
	}
	
//	public ResultConst collectMine(Builder responseBuilder, int id, UserBean user){
//		ResultConst result = null;
//		Map<String, String> map = redis.getMineGains(user);
//		redis.delMineGains(user);
//		int collect = 0;
//		for(Entry<String, String> entry : map.entrySet()){
//			collect += Integer.parseInt(entry.getValue());
//		}
//		AreaResourceMine.Builder mine = AreaResourceMine.newBuilder(redis.getResourceMine(id, user));
//		if(mine == null || !mine.hasUser() || mine.getUser().getId() != user.getId())
//			result = ErrorConst.MAPINFO_ERROR;
//		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+mine.getId()))
//			result = ErrorConst.ERROR_LOCKED;
//		else{
//			collect += gainMine(mine, user, false);
//		}
//		if(collect > 0){
//			rewardService.doReward(user, RewardConst.UNIONCOIN, collect);
//			userService.updateUser(user);
//			pusher.pushRewardCommand(responseBuilder, user, RewardConst.UNIONCOIN, "", collect);
//			pusher.pushUserInfoCommand(responseBuilder, user);
//		}
//		return result;
//	}
//	
//	public ResultConst AttackResourceMine(int id, long teamid, boolean ret, UserBean user, ResponseCommand.Builder responseBuilder){
//		Map<String, AreaResourceMine> mines = redis.getResourceMines(user);
//		AreaResourceMine mine = null;
//		AreaResourceMine.Builder gainmine = null;
//		for(AreaResourceMine resourcemine : mines.values()){
//			if(resourcemine.getId() == id)
//				mine = resourcemine;
//			else if(resourcemine.getUser().getId() == user.getId()){
//				gainmine = AreaResourceMine.newBuilder(resourcemine);
//			}
//		}
//		
//		if(mine == null)
//			return ErrorConst.MAPINFO_ERROR;
//		Team team = userTeamService.getTeam(user, teamid);
//		userTeamService.saveTeamCache(user, teamid, team);
//		if(!ret){
//			costEnergy(user);
//			return SuccessConst.PVP_ATTACK_FAIL;
//		}
//		if(gainmine != null){
//			if(!redis.setLock("S"+user.getServerId()+"_Mine_"+gainmine.getId()))
//				return ErrorConst.ERROR_LOCKED;
//			else{
//				int collect = gainMine(gainmine, user, true);
//				collect += redis.getMineGain(gainmine.getId(), user);
//				redis.saveMineGain(gainmine.getId(), collect, user);
//			}
//		}
//		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+mine.getId()))
//			return ErrorConst.ERROR_LOCKED;
//
//		AreaResourceMine.Builder builder = AreaResourceMine.newBuilder(mine);
//		if(mine.hasUser())
//			costEnergy(user);
//		builder.setUser(user.buildShort());
//		long time = redis.now();
//		builder.setEndTime(time+mine.getTime());
//		builder.setCollectTime(time);
//		redis.saveResourceMine(builder.build(), user);
//		return SuccessConst.PVP_ATTACK_SUCCESS;
//	}
//	
//	public Team AttackResourceMineInfo(int id, UserBean user){
//		AreaResourceMine mine = redis.getResourceMine(id, user);
//		if(mine == null)
//			return null;
//		else if(!mine.hasUser())
//			return Team.newBuilder().build();
//		return userTeamService.getTeamCache(mine.getUser().getId());
//	}
//	
//	public int gainMine(AreaResourceMine.Builder builder, UserBean user, boolean release){
////		if(!builder.hasUser())
////			return 0;
////		if(!redis.setLock("S"+user.getServerId()+"_Mine_"+builder.getId()))
////			return false;
//		int yield = (int)((builder.getEndTime() - builder.getCollectTime())/3600*builder.getYield());
//		long time = redis.now();
//		if(release){
//			if (time < builder.getEndTime()){
//				yield = (int)((time + builder.getTime() - builder.getEndTime())*builder.getYield()/3600);
//			}
//			builder.clearUser();
//			builder.clearEndTime();
//			builder.clearCollectTime();
//		}else{
//			if (time < builder.getEndTime()){
//				int hour = (int)((time - builder.getCollectTime())/3600);
//				yield = hour * builder.getYield();
//				builder.setCollectTime(builder.getCollectTime()+hour*3600);
//			}else{
//				builder.clearUser();
//				builder.clearEndTime();
//				builder.clearCollectTime();
//			}
//		}
//		redis.saveResourceMine(builder.build(), user);
////		if(yield > 0){
////			MultiReward.Builder multireward = MultiReward.newBuilder();
////			RewardInfo.Builder reward = RewardInfo.newBuilder();
////			reward.setItemid(RewardConst.UNIONCOIN);
////			reward.setCount(yield);
////			multireward.addLoot(reward);
//////			rewardService.doRewards(Long.parseLong(rank.getValue()), multireward.build());
////			MailBean mail = new MailBean();
////			mail.setContent("区域争夺矿点占领奖励");
////			mail.setStartDate(DateUtil.getCurrentDateString());
////			mail.setType(MailConst.TYPE_SYSTEM_MAIL);
////			mail.setUserId(builder.getUser().getId());
////			mail.parseRewardList(multireward.getLootList());
////			mailRedisService.addMail(mail);
////		}
//		return yield;
//	}

	public void calFight(){
		Set<TypedTuple<String>> set = redis.getFightSet();
		for (TypedTuple<String> key : set){
			redis.removeFight(key.getValue());
			String[] value = key.getValue().split("#");
			if(value.length == 2){
				int serverId = TypeTranslatedUtil.stringToInt(value[0]);
				int resourceId = TypeTranslatedUtil.stringToInt(value[1]);
				resourceFight(resourceId, serverId);
			}
		}
	}

	public void resourceFight(int resourceId, int serverId){
		AreaResource resource = redis.getResource(resourceId,serverId);
		if(resource == null)
			return;
		AreaResource.Builder builder = AreaResource.newBuilder(resource);
		if (builder.getState() == 0)
			return;
		// if (builder.getState() == 1 && redis.now() >= builder.getClosetime()) {// 攻城开始
		resourceFight(builder, serverId);
		if(builder.getState() == 1)
			redis.addFight(serverId, resourceId, builder.getClosetime());
	}

	public void resourceFight(AreaResource.Builder builder, int serverId){
		if(!redisService.setLock("S"+serverId+"_AreaResource_"+builder.getId(), 120))
			return;
		logger.info("S"+serverId+":"+RedisService.formatJson(builder.build()));
		//防守玩家最后一个加入战斗
		List<UserInfo> attacks = new ArrayList<UserInfo>();
		attacks.addAll(builder.getAttacksList());
		List<UserInfo> defends = new ArrayList<UserInfo>();
		defends.addAll(builder.getDefensesList());
		if(builder.hasOwner())
			defends.add(builder.getOwner());
		String message = new SimpleDateFormat("MM-dd HH:mm").format(new Date(System.currentTimeMillis()))+" ";
		if(attacks.size() == 0){
			builder.setWarDefended(builder.getWarDefended()+1);
			message += ("领主"+(builder.hasOwner()?builder.getOwner().getName():"")+"已成功防守"+builder.getWarDefended()+"波敌人！");
		}else{
			FightResultList.Builder resultlist = queueFight(attacks, defends);
			redis.saveFight(builder.getId(), resultlist.build());
			if(resultlist.getListCount() > 0){
				FightResult winresult = resultlist.getList(resultlist.getListCount()-1);
				if( winresult.getResult() == 1 ){//攻破
					UserInfo owner = null;
					for(UserInfo userinfo : attacks){
						if(userinfo.getId() == winresult.getWin().getUserId())
							owner = userService.getCache(serverId, userinfo.getId());
					}

					builder.setWarDefended(0);
					if(owner != null){
						Union.Builder union = unionRedisService.getUnion(userService.getUserOther(owner.getId()));
						UserInfo.Builder userInfoBuilder = UserInfo.newBuilder(owner);
						if (union != null) {
							userInfoBuilder.setUnionIcon(union.getIcon());
						}
						builder.setOwner(userInfoBuilder.build());
//						message += "玩家"+(builder.getOwner().getName()+"成功拿下了据点，成为新的领主！");
						message += "玩家"+(builder.getOwner().getName()+"成功夺取了据点！");
					}
				}else{
					builder.setWarDefended(builder.getWarDefended()+1);
					message += ("领主"+(builder.hasOwner()?builder.getOwner().getName():"")+"已成功防守"+builder.getWarDefended()+"波敌人！");
					if (builder.getWarDefended() == 3)
						message += "成为了新的领主！";
				}
//				for(FightResult result : resultlist.getListList()){//发奖
//					
//				}
			}
		}
		builder.addMessage(message);
		long time = Math.max(RedisService.now(), builder.getEndtime());
		if(builder.getWarDefended() >= 3){//防守结束
			builder.setState(0);
			builder.setWarDefended(0);
			// builder.clearMessage();
			builder.clearClosetime();
			builder.setStarttime(time+redis.PROTECTTIME);
			builder.clearEndtime();
			builder.setLastRewardTime(time);
		}else{
			builder.setStarttime(time);
			builder.setClosetime(time+redis.WARTIME);
			builder.setEndtime(builder.getClosetime()+redis.PROTECTTIME);
		}
		builder.clearDefenses();
		builder.clearAttacks();
		builder.clearAttackerId();
		redis.saveResource(builder.build(), serverId);
		redisService.clearLock("S"+serverId+"_AreaResource_"+builder.getId());
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
	
	public boolean refreshArea(UserBean user){
		if(user.getAreaRefreshTime() > RedisService.now())
			return false;
		redis.deleteMonsters(user);
		user.setAreaMonsterRefreshTime(RedisService.now()-24*3600);
		user.setAreaRefreshTime(RedisService.now()+24*3600);
		redis.clearLevel(user);
		userService.updateUser(user);
		return true;
	}
	
	public void unlockArea(int zhanli, UserBean user){
		AreaMode.Builder areamode = redis.getAreaMode(user);
		
		for(AreaInfo.Builder areainfo : areamode.getRegionBuilderList()){
			if(zhanli >= areainfo.getZhanli()) {
				String[] level = redis.addLevel(areainfo.getId(), areainfo.getLevel(), user);
				if(TypeTranslatedUtil.stringToInt(level[2]) == 0) {
					if(areainfo.getId() > user.getAreaUnlock()){
							unlockArea(areainfo.getId(), zhanli, user);
							level[2] = "1";
							redis.saveOriginalLevel(areainfo.getId(), level, user);
					}
				}
			}
		}
	}
	
	private boolean unlockArea(int id, int zhanli, UserBean user){
		AreaMode.Builder areamode = redis.getAreaMode(user);
		for(AreaInfo.Builder areainfo : areamode.getRegionBuilderList()){
			if(id == areainfo.getId()){
				if(zhanli >= areainfo.getZhanli()){
					areainfo.setOpened(true);
					user.setAreaUnlock(id);
					userService.updateUser(user);
					redis.saveAreaMode(areamode.build(), user);
					return true;
				}
			}
		}
		return false;
	}

	public AreaResource getResource(int id, UserBean user) {
		AreaResource resource = redis.getResource(id,user.getServerId());
		if(resource == null)
			return null;
		AreaResource.Builder builder = AreaResource.newBuilder(resource);
		// if (builder.getState() == 1 && redis.now() >= builder.getClosetime()) {// 攻城开始
		// 	resourceFight(builder, user);
		// }
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
			Map<Integer, AreaEquip> map = redis.getAreaEquipConfig();
			for(UserAreaPropBean bean : beans){
				AreaEquip.Builder builder = bean.build();
				if(map.containsKey(bean.getId())){
					builder.mergeFrom(map.get(bean.getId()));
					equipMap.put(builder.getId()+"", builder.build());
				}
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
					// builder.setEndTime(redis.now()+builder.getTime());
					// builder.setLevel(builder.getLevel()+1);
					// if(builder.getLevel() > builder.getLayer() && builder.getLayer() > 0)
					// 	builder.setLevel(builder.getLayer());
					redis.saveMyAreaEquip(user, builder.build());
					AreaBuff.Builder buff = redis.getMyAreaBuff(user, builder.getSkill());
					// if(buff == null){
					// 	buff = AreaBuff.newBuilder();
					// 	buff.setSkill(builder.getSkill());
					// 	buff.setType(builder.getType());
					// }
					if(buff.getEndTime() > RedisService.now())
						buff.setLevel(Math.min(buff.getCountlimit(), buff.getLevel()+1));
					else
						buff.setLevel(1);
					buff.setEndTime(RedisService.now()+buff.getTime());
					if(buff.getLevel() > builder.getLayer() && builder.getLayer() > 0)
						buff.setLevel(builder.getLayer());
					if (user.getUnionId() > 0 && buff.getBufftype() == TYPE_UNION_BUFF)
						redis.saveUnionAreaBuff(user, buff.build());
					else
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
		List<Integer> positionids = new ArrayList<Integer>();
		Map<String, AreaBoss.Builder> bossMap = redis.getBosses(user, positionids);
		Map<String, AreaMonster> monsterMap = redis.getMonsters(user, positionids);
		Map<String, String> bosstimeMap = redis.getBossTimes(user);
		Map<String, AreaResource> resourceMap = redis.getResources(user);
//		Map<String, AreaResourceMine> mineMap = redis.getResourceMines(user);
		Map<Integer, Integer[]> levelMap = redis.getLevels(user);
		List<AreaInfo.Builder> areas = areamodebuilder.getRegionBuilderList();
		for (AreaInfo.Builder areabuilder : areas) {
			if(levelMap.containsKey(areabuilder.getId())){
				Integer[] level = levelMap.get(areabuilder.getId());
				areabuilder.setLevel(level[0]);
				areabuilder.setZhanlicount(level[1]);
			}
			areabuilder.setLevel(levelMap.containsKey(areabuilder.getId())? levelMap.get(areabuilder.getId())[0] : 0);
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
//			long time = redis.now();
			for (AreaResource.Builder resourcebuilder : areabuilder.getResourcesBuilderList()) {
				AreaResource resource = resourceMap.get(resourcebuilder.getId() + "");
				if (resource != null) {
					AreaResource.Builder builder = AreaResource.newBuilder(resource);
//					if (builder.getState() == 1 && time >= builder.getClosetime()) {// 攻城开始
//						resourceFight(builder, user);
//					}
					resourcebuilder.mergeFrom(builder.build());
				}
//				for (int i = 1; i <= resourcebuilder.getCount(); i++)
//					resourcebuilder.addMines(redis.buildAreaResourceMine(resourcebuilder.build(), (resourcebuilder.getId() - 1) * 20 + i));
//				for (AreaResourceMine.Builder minebuilder : resourcebuilder.getMinesBuilderList()) {
//					AreaResourceMine mine = mineMap.get(minebuilder.getId() + "");
//					if (mine != null) {
//						AreaResourceMine.Builder builder2 = AreaResourceMine.newBuilder(mine);
//						if (time >= builder2.getEndTime()// 收获
//							&& builder2.hasUser()){
//							int yield = gainMine(builder2, user, true);
//							yield += redis.getMineGain(builder2.getId(), user);
//							redis.saveMineGain(builder2.getId(), yield, user);
//						}
//						minebuilder.mergeFrom(builder2.build());
//					} else {
//						minebuilder.mergeFrom(redis.buildAreaResourceMine(resourcebuilder.build(), minebuilder.getId()));
//						redis.saveResourceMine(minebuilder.build(), user);
//					}
//				}
			}
		}
		return areamodebuilder.build();
	}
	
	public boolean isAreaOwner(UserBean user) {
		Map<String, AreaResource> resources = redis.getResources(user);
		for (AreaResource resource : resources.values()) {
			if (resource.hasOwner()) {
				UserInfo userInfo = resource.getOwner();
				if (userInfo != null && userInfo.getId() == user.getId())
					return true;
			}
		}
		
		return false;
	}
}
