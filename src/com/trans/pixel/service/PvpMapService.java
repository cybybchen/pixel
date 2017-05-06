package com.trans.pixel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.PvpMapConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.HeroInfo;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.PVPProto.PVPMap;
import com.trans.pixel.protoc.PVPProto.PVPMapList;
import com.trans.pixel.protoc.PVPProto.PVPMine;
import com.trans.pixel.protoc.PVPProto.PVPMonster;
import com.trans.pixel.protoc.PVPProto.PVPMonsterReward;
import com.trans.pixel.protoc.PVPProto.PVPMonsterRewardLoot;
import com.trans.pixel.service.command.PushCommandService;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.PvpMapRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.DateUtil;

@Service
public class PvpMapService {
//	private static Logger logger = Logger.getLogger(PvpMapService.class);
	@Resource
	private PvpMapRedisService redis;
	@Resource
	private UserService userService;
	@Resource
	private RankRedisService rankRedisService;
//	@Resource
//	private UserFriendRedisService userFriendRedisService;
//	@Resource
//	private UnionRedisService unionRedisService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private LogService logService;
	@Resource
	private MailService mailService;
	@Resource
	private UnionService unionService;
	@Resource
	private NoticeMessageService noticeMessageService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private RewardService rewardService;
	@Resource
	private LevelRedisService levelRedisService;

	private int getTarget(int fieldid) {
		if(fieldid > 1)
			return 200+fieldid;
		return 1;
	}
	public ResultConst unlockMap(int fieldid, int zhanli, UserBean user) {
		PVPMapList.Builder maplist = redis.getMapList(user.getId(), user.getPvpUnlock());
		for(PVPMap.Builder map : maplist.getFieldBuilderList()){
			if(map.getFieldid() == fieldid){
//				if(zhanli >= map.getZhanli()){
//				if(user.getMerlevel() >= map.getMerlevel()){
				if(levelRedisService.hasCompleteTarget(user, getTarget(fieldid))) {
					map.setOpened(true);
					redis.saveMapList(maplist.build(), user.getId());
					if(map.getFieldid() > user.getPvpUnlock()){
						user.setPvpUnlock(map.getFieldid());
						userService.updateUser(user);
					}
					
					List<UserInfo> ranks = getRandUser(-10, 0, user);
					if(ranks.size() > 0){
						Map<String, PVPMine> mineMap = new HashMap<String, PVPMine>();
						for(PVPMine mine : map.getKuangdianList()){
							if(ranks.size() > 0){
								PVPMine.Builder builder = PVPMine.newBuilder(mine);
								int index = RedisService.nextInt(ranks.size());
								builder.setOwner(ranks.get(index));
								mineMap.put(builder.getId()+"", builder.build());
								ranks.remove(index);
							}
						}
						redis.saveMines(user.getId(), mineMap);
					}
					return SuccessConst.UNLOCK_AREA;
				}else
					return ErrorConst.EVENT_FIRST;
			}
		}
		return ErrorConst.UNLOCK_ORDER_ERROR;
	}
	
	public List<UserInfo> getRandUser(int index1, int index2, UserBean user){
		long myrank = rankRedisService.getMyZhanliRank(user);
		List<UserInfo> ranks = new ArrayList<UserInfo>();
		if(myrank < 0)
			ranks = rankRedisService.getZhanliRanks(user, Math.min(index1, -10), -1);
		else if(myrank+index1 > 0)
			ranks = rankRedisService.getZhanliRanks(user, myrank+index1, myrank+index2);
		else
			ranks = rankRedisService.getZhanliRanks(user, 0, myrank+index2);
		// List<Long> friends = userFriendRedisService.getFriendIds(user.getId());
		// Set<String> members = unionRedisService.getMemberIds(user);
		Iterator<UserInfo> it = ranks.iterator();
		while(it.hasNext()){
			UserInfo rank = it.next();
			if(rank.getId() == user.getId()/* || friends.contains(rank.getId()) || members.contains(rank.getId()+"")*/){
				it.remove();
				continue;
			}
			boolean timeout = true;
			if (rank.hasLastLoginTime()) {
				try {
					Date date = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT).parse(rank.getLastLoginTime());
					if(new Date().getTime() - date.getTime() < RedisExpiredConst.EXPIRED_USERINFO_7DAY)
						timeout = false;
				} catch (ParseException e) {
				}
			}
			if(timeout){
				rankRedisService.delZhanliRank(user.getServerId(), rank.getId());
				if(ranks.size() > index2 - index1)
					it.remove();
			}
		}
		while(ranks.size() > index2 - index1){
			ranks.remove(ranks.size()-1);
		}
		return ranks;
	}
	
	public int getRefreshCount(UserBean user){
		long time[] = {redis.today(0), redis.today(12), redis.today(18)};
		long now = redis.now();
		int count = 0;
		if(now - user.getPvpMineRefreshTime() >= 24*3600){
			count = (int)((now - user.getPvpMineRefreshTime())/24/3600);
			user.setPvpMineRefreshTime(user.getPvpMineRefreshTime()+count*24*3600);
			count*=3;
		}
		if(now >= time[0] && user.getPvpMineRefreshTime() < time[0]){
			count++;
			user.setPvpMineRefreshTime(time[0]);
		}
		if(now >= time[1] && user.getPvpMineRefreshTime() < time[1]){
			count++;
			user.setPvpMineRefreshTime(time[1]);
		}
		if(now >= time[2] && user.getPvpMineRefreshTime() < time[2]){
			count++;
			user.setPvpMineRefreshTime(time[2]);
		}
		if(count > 18)
			count = 18;
		if(count > 0)
			userService.updateUser(user);
		return count;
	}

	public void refreshAMine(UserBean user){
		PVPMapList.Builder maplist = redis.getMapList(user.getId(), user.getPvpUnlock());
		Map<String, PVPMine> mineMap = redis.getUserMines(user.getId());
		List<UserInfo> ranks = getRandUser(-5, 10, user);//刷新一个对手
		List<PVPMap> fields = new ArrayList<PVPMap>();
		for(PVPMap map : maplist.getFieldList()){
			if(map.getOpened())
				fields.add(map);
		}
		if(!ranks.isEmpty() && !fields.isEmpty()){
			PVPMap map = fields.get(redis.nextInt(fields.size()));
			refreshMine(map, ranks, mineMap, user, 3);
		}
		for(PVPMap.Builder mapBuilder : maplist.getFieldBuilderList()){
			if(!mapBuilder.getOpened())
				continue;
			for(PVPMine.Builder mineBuilder : mapBuilder.getKuangdianBuilderList()){
				PVPMine mine = mineMap.get(mineBuilder.getId()+"");
				if(mine != null)
					mineBuilder.mergeFrom(mine);
			}
		}
		gainMine(maplist, user);
	}

	public void refreshMine(PVPMapList.Builder maplist, Map<String, PVPMine> mineMap, UserBean user){
		Iterator<Map.Entry<String, PVPMine>> it = mineMap.entrySet().iterator();
		while(it.hasNext()){  
            Map.Entry<String, PVPMine> entry=it.next();
			PVPMine mine = entry.getValue();
			if(mine.hasOwner()){
				UserInfo owner = userService.getCache(user.getServerId(), mine.getOwner().getId());
				PVPMine.Builder builder = PVPMine.newBuilder(mine);
				builder.setOwner(owner);
				builder.setPvpyield((int)Math.pow(mine.getYield()*(Math.min(mine.getLevel(), 10)/2.0+3), Math.max(1, Math.min(1.2, Math.max(owner.getZhanli(), owner.getZhanliMax())/(user.getZhanliMax()+1.0)))));
				entry.setValue(builder.build());
			}else if(redis.now() > mine.getEndTime()){
				it.remove();
				redis.deleteMine(user.getId(), mine.getId());
			}
		}
		int count = getRefreshCount(user);//count=3*enemy
		if(count > 0){//刷新对手
			List<UserInfo> ranks = getRandUser(-5, 10, user);//每张地图刷新一个对手
			if(ranks.isEmpty())
				return;
			for(PVPMap map : maplist.getFieldList())
				refreshMine(map, ranks, mineMap, user, count);
		}
	}

	public void refreshMine(PVPMap map, List<UserInfo> ranks, Map<String, PVPMine> mineMap, UserBean user, int count){
		if(!map.getOpened())
			return;
		for(int i = 0, enemy = count;i < 10 && enemy > 0; i++){
			PVPMine.Builder builder = PVPMine.newBuilder(map.getKuangdian(redis.nextInt(map.getKuangdianCount())));
			PVPMine mine = mineMap.get(builder.getId()+"");
			// if(mine != null && mine.getEndTime() > redis.now() )
			// 	continue;
			
			Iterator<UserInfo> it = ranks.iterator();
			while(it.hasNext()){
				UserInfo userinfo = it.next();
				if(userinfo.getMerlevel() < map.getMerlevel())
					it.remove();
			}
			if(ranks.isEmpty())
				return;
			if(redis.nextInt(3) < enemy && !(mine != null && mine.hasOwner())){
				int index = redis.nextInt(ranks.size());
				builder.setOwner(ranks.get(index));
				ranks.remove(index);
				redis.saveMine(user.getId(), builder.build());
				mineMap.put(builder.getId()+"", builder.build());

				String content = "霸占了你的矿点(" + map.getName() + ")";
				UserBean bean = new UserBean();
				bean.setId(builder.getOwner().getId());
				bean.setIcon(builder.getOwner().getIcon());
				bean.setVip(builder.getOwner().getVip());
				bean.setUserName(builder.getOwner().getName());
				bean.setZhanli(builder.getOwner().getZhanli());
				bean.setLastLoginTime(builder.getOwner().getLastLoginTime());
				sendMineAttackedMail(user.getId(), bean, content, builder.getId() / 100);
			}
			enemy-=3;
		}
	}
	
	public PVPMapList getMapList(Builder responseBuilder, UserBean user){
		PVPMapList.Builder maplist = redis.getMapList(user.getId(), user.getPvpUnlock());
		if(user.getPvpUnlock() == 0){
		}else{
			Map<String, String> pvpMap = redis.getUserBuffs(user);
			Map<String, PVPMine> mineMap = redis.getUserMines(user.getId());
			List<PVPMonster> monsters = redis.getMonsters(user, pvpMap);
			refreshMine(maplist, mineMap, user);
			String buff = pvpMap.get("0");
			if(buff != null)
				maplist.setBuff(Integer.parseInt(buff));
			else
				maplist.setBuff(0);
			for(PVPMap.Builder mapBuilder : maplist.getFieldBuilderList()){
				if(!mapBuilder.getOpened())
					continue;
				buff = pvpMap.get(mapBuilder.getFieldid()+"");
				if(buff != null)
					mapBuilder.setBuff(Integer.parseInt(buff));
				else{
					redis.addUserBuff(user, mapBuilder.getFieldid(), 1);
					mapBuilder.setBuff(1);
				}
				for(PVPMine.Builder mineBuilder : mapBuilder.getKuangdianBuilderList()){
					PVPMine mine = mineMap.get(mineBuilder.getId()+"");
					if(mine != null)
						mineBuilder.mergeFrom(mine);
				}
				for(PVPMonster monster : monsters){
					if(monster.getFieldid() == mapBuilder.getFieldid())
						mapBuilder.addMonster(monster);
				}
			}
		}
		if(gainMine(maplist, user)){
			pushCommandService.pushUserInfoCommand(responseBuilder, user);
		}
		return maplist.build();
	}

	public boolean gainMine(PVPMapList.Builder maplist, UserBean user){
		long time = redis.now()/3600*3600;
		if(time > user.getPvpMineGainTime()){//收取资源
			int resource = 0;
			for(PVPMap map : maplist.getFieldList()){
				if(!map.getOpened())
					continue;
				resource += map.getYield();
				for(PVPMine mine : map.getKuangdianList()){
					if(!mine.hasOwner())
						resource += mine.getYield();
				}
			}
			resource = Math.min(resource*7*24, (int)(resource*(time - user.getPvpMineGainTime())/3600));
			if(user.getVip() >= 10)
				resource = (int)(resource * 1.1);
			rewardService.doReward(user, RewardConst.PVPCOIN, resource);
			user.setPvpMineGainTime(time);
			userService.updateUser(user);
			return true;
		}
		return false;
	}
	
	public MultiReward attackMonster(UserBean user, int positionid, boolean ret, int time){
		int logType = PvpMapConst.TYPE_MONSTER;
		PVPMonster monster = redis.getMonster(user, positionid);
		if(monster == null)
			return null;
		if(monster.hasEndtime() && !DateUtil.timeIsAvailable(monster.getStarttime(), monster.getEndtime()))
			return null;
		
		if (monster.getId()/1000 == 2) {
			logType = PvpMapConst.TYPE_BOSS;
		}
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
//		int buff = -1;
		if(ret){
			if(monster.getId()/1000 == 2)
				redis.deleteBoss(user, positionid);
			else
				redis.deleteMonster(user, positionid);
			PVPMonsterReward reward = redis.getMonsterReward(monster.getId());
			RewardInfo.Builder rewardinfo = RewardInfo.newBuilder();
			for(PVPMonsterRewardLoot loot : reward.getLootList()){
				if(loot.getWeighta()+(int)(loot.getWeightb()*monster.getLevel()) > redis.nextInt(loot.getWeightall())){
					rewardinfo.setItemid(loot.getItemid());
					rewardinfo.setCount(loot.getCounta()+(int)(loot.getCountb()*monster.getLevel()));
					if(rewardinfo.getCount() > 0)
						rewards.addLoot(rewardinfo);
				}
			}
			int buff = redis.addUserBuff(user, monster.getFieldid(), monster.getBuffcount());
			if (monster.getId()/1000 == 2) {
				/**
				 * PVP攻击BOSS的活动
				 */
				activityService.pvpAttackBossSuccessActivity(user);
			}
			/**
			 * 攻击怪物的活动
			 */
			activityService.attackMonster(user);
			
			unionService.killMonsterBossActivity(user, monster.getId(), 1);
			/**
			 * buff的活动
			 */
			activityService.upPvpBuff(user, monster.getFieldid(), buff);
		}

		user.addMyactive();
		if(user.getMyactive() >= 100){
			user.setMyactive(user.getMyactive() - 100);
			refreshAMine(user);
		}
		userService.updateUser(user);
		
		/**
		 * send log
		 */
		sendLog(user, time, logType, ret, userTeamService.getTeamCache(user.getId()).getHeroInfoList(), null, monster.getId(), 0);
		
//		logger.warn(monster.getFieldid()+":+"+monster.getName()+ret+":"+monster.getBuffcount()+"->"+buff);
		return rewards.build();
	}

	public RewardInfo attackMine(UserBean user, int id, boolean ret, int time, boolean isme, UserBean my){
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		reward.setItemid(RewardConst.PVPCOIN);
		reward.setCount(0);
		long enemyId = 0;
		int logType = PvpMapConst.TYPE_ATTACK;
		PVPMine pvpmine = redis.getMine(user.getId(), id);
		if(pvpmine == null)
			return null;
		PVPMine.Builder mine = PVPMine.newBuilder(pvpmine);
		if (mine.getLevel() > 0)
			logType = PvpMapConst.TYPE_DEFEND;
		/**
		 * PVP攻击玩家的活动
		 */
		if(mine.hasOwner()){
			activityService.pvpAttackEnemyActivity(user, ret);
			enemyId = mine.getOwner().getId();
			
			//全服通告
			if (!isme && ret)
				noticeMessageService.composeCallbrotherHelpAttackMine(my, mine.getOwner().getName());
		}
		if(ret){
			if(mine.hasOwner()){
				long userId = mine.getOwner().getId();
				mine.setOwner(user.buildShort());
				mine.setLevel(mine.getLevel()+1);
//				UserBean bean = userService.getUser(userId);
//				PVPMapList.Builder maplist = redis.getBasePvpMapList();
//				Map<String, PVPMine> mineMap = redis.getUserMines(userId);
//				int mineCount = 0;
//				for(PVPMap map : maplist.getFieldList()){
//					if(bean.getPvpUnlock() >= map.getFieldid())
//						mineCount += map.getKuangdianCount();
//				}
//				int enemyCount = 0;
//				for(PVPMine pvpmine : mineMap.values()){
//					if(pvpmine.hasOwner() && bean.getPvpUnlock() >= pvpmine.getId()/100)
//						enemyCount++;
//				}
				
				/*if(mineCount/5 >= enemyCount)*/{
					PVPMine othermine = redis.getMine(userId, id);
					if(othermine == null || othermine.getEndTime() <= redis.now() || othermine.getEnemyid() == user.getId()) {
						PVPMapList.Builder maplist = redis.getMapList(userId, 0);
						for (PVPMap map : maplist.getFieldList()) {
							if (map.getFieldid() == id / 100 && map.getOpened()) {
								mine.setEndTime(redis.now()+24*3600);

								redis.saveMine(userId, mine.build());
								String content = "霸占了你的矿点(" + map.getName() + ")";
								
								sendMineAttackedMail(userId, user, content, id / 100);
								
								break;
							}
						}
					}
				}
				
				mine.clearOwner();
				mine.setEnemyid(userId);
				redis.saveMine(user.getId(), mine.build());
				
				if(isme){
					redis.addUserBuff(user, 0, 1);
					UserInfo owner = userService.getCache(user.getServerId(), userId);
					reward.setCount((int)Math.pow(mine.getYield()*(Math.min(mine.getLevel()-1, 10)/2.0+3), Math.max(1, Math.min(1.2, Math.max(owner.getZhanli(), owner.getZhanliMax())/(user.getZhanliMax()+1.0)))));
				}
			}
		}

		if(isme){
			user.addMyactive();
			if(user.getMyactive() >= 100){
				user.setMyactive(user.getMyactive() - 100);
				refreshAMine(user);
			}
			userService.updateUser(user);
		}

		/**
		 * send log
		 */
		
		sendLog(user, time, logType, ret, userTeamService.getTeamCache(user.getId()).getHeroInfoList(), userTeamService.getTeamCache(enemyId).getHeroInfoList(), enemyId, mine.getLevel());
		
		return reward.build();
	}
	
	public boolean refreshMap(UserBean user){
		long now = redis.now();
		if(user.getRefreshPvpMapTime() > now)
			return false;
		
		redis.deleteMonsters(user);
		redis.deleteUserBuff(user);
		redis.getMonsters(user, new HashMap<String, String>(), true);

		user.setRefreshPvpMapTime(now+48*3600);
		userService.updateUser(user);
		return true;
	}
	
	public PVPMine refreshMine(UserBean user, int id){
		PVPMine mine = redis.getMine(user.getId(), id);
		if(mine == null || !mine.hasOwner())
			return null;
		PVPMine.Builder builder = PVPMine.newBuilder(mine);
		if(user.getPvpMineLeftTime() > 0){
			user.setPvpMineLeftTime(user.getPvpMineLeftTime()-1);
			userService.updateUser(user);
			List<UserInfo> ranks = getRandUser(0, 10, user);
			// UserInfo owner = builder.getOwner();
			if(!ranks.isEmpty()){
				UserInfo owner = ranks.get(redis.nextInt(ranks.size()));
				if(owner.getId() == builder.getOwner().getId() && ranks.size() > 1)
					owner = ranks.get(redis.nextInt(ranks.size()));
				builder.setOwner(owner);
				builder.setPvpyield((int)Math.pow(mine.getYield()*(Math.min(mine.getLevel(), 10)/2.0+3), Math.max(1, Math.min(1.2, Math.max(owner.getZhanli(), owner.getZhanliMax())/(user.getZhanliMax()+1.0)))));
			}else{
				builder.clearOwner();
			}
			redis.saveMine(user.getId(), builder.build());
		// }else{
		// 	builder.clearOwner();
		}
		return builder.build();
	}
	
	public PVPMine getUserMine(UserBean user, int id){
		return redis.getMine(user.getId(), id);
	}
	
	private void sendLog(UserBean user, int time, int type, boolean ret, List<HeroInfo> heroList, List<HeroInfo> enemyList, long enemyId, int defencelevel) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + user.getId());
		logMap.put(LogString.SERVERID, "" + user.getServerId());
		logMap.put(LogString.ATTACK_TIME, "" + time);
		logMap.put(LogString.OPERATION, "" + type);
		logMap.put(LogString.RESULT, "" + (ret ? 1 : 0));
		logMap.put(LogString.TEAM_LIST, userTeamService.getTeamString(heroList));
		logMap.put(LogString.ENEMYID, "" + enemyId);
		if (enemyList == null || enemyList.size() == 0)
			logMap.put(LogString.ENEMY_LIST, "");
		else
			logMap.put(LogString.ENEMY_LIST, userTeamService.getTeamString(enemyList));
		logMap.put(LogString.DEFENCELEVEL, "" + defencelevel);
		
		logService.sendLog(logMap, LogString.LOGTYPE_LOOTPVP);
	}
	
	private void sendMineAttackedMail(long userId, UserBean enemy, String content, int id) {
		MailBean mail = MailBean.buildMail(userId, enemy.getId(), enemy.getVip(), enemy.getIcon(), enemy.getUserName(), content, MailConst.TYPE_MINE_ATTACKED_MAIL, id);
		mailService.addMail(mail);
	}
}
