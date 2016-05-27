package com.trans.pixel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
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
import com.trans.pixel.protoc.Commands.HeroInfo;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.PVPMap;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.protoc.Commands.PVPMonsterReward;
import com.trans.pixel.protoc.Commands.PVPMonsterRewardLoot;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.PvpMapRedisService;
import com.trans.pixel.service.redis.RankRedisService;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.service.redis.UserFriendRedisService;

@Service
public class PvpMapService {
	private static Logger logger = Logger.getLogger(PvpMapService.class);
	@Resource
	private PvpMapRedisService redis;
	@Resource
	private UserService userService;
	@Resource
	private RewardService rewardService;
	@Resource
	private RankRedisService rankRedisService;
	@Resource
	private UserFriendRedisService userFriendRedisService;
	@Resource
	private UnionRedisService unionRedisService;
	@Resource
	private ActivityService activityService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private LogService logService;
	@Resource
	private MailService mailService;

	public ResultConst unlockMap(int fieldid, int zhanli, UserBean user){
		PVPMapList.Builder maplist = redis.getMapList(user.getId(), user.getPvpUnlock());
		for(PVPMap.Builder map : maplist.getFieldBuilderList()){
			if(map.getFieldid() == fieldid){
				/*if(map.getFieldid() != fieldid){
					return ErrorConst.UNLOCK_ORDER_ERROR;
				}else */if(zhanli >= map.getZhanli()){
					map.setOpened(true);
					redis.saveMapList(maplist.build(), user.getId());
					if(map.getFieldid() > user.getPvpUnlock()){
						user.setPvpUnlock(map.getFieldid());
						userService.updateUserDailyData(user);
					}
					
					List<UserInfo> ranks = getRandUser(50, user);
					if(ranks.size() > 0){
						Map<String, PVPMine> mineMap = new HashMap<String, PVPMine>();
						for(PVPMine mine : map.getKuangdianList()){
							PVPMine.Builder builder = PVPMine.newBuilder(mine);
							builder.setOwner(ranks.get(redis.nextInt(ranks.size())));
							mineMap.put(builder.getId()+"", builder.build());
						}
						redis.saveMines(user.getId(), mineMap);
					}
					return SuccessConst.UNLOCK_AREA;
				}else
					return ErrorConst.NOT_ENOUGH_ZHANLI;
			}
		}
		return ErrorConst.UNLOCK_ORDER_ERROR;
	}
	
	public List<UserInfo> getRandUser(int count, UserBean user){
		long myrank = rankRedisService.getMyZhanliRank(user);
		List<UserInfo> ranks = new ArrayList<UserInfo>();
		if(myrank < 0)
			return ranks;
		ranks = rankRedisService.getZhanliRanks(user, myrank+1, myrank+count+30);
		List<Long> friends = userFriendRedisService.getFriendIds(user.getId());
		Set<String> members = unionRedisService.getMemberIds(user);
		Iterator<UserInfo> it = ranks.iterator();
		while(it.hasNext()){
			UserInfo rank = it.next();
			if(rank.getId() == user.getId() || friends.contains(rank.getId()) || members.contains(rank.getId()+"")){
				it.remove();
				continue;
			}
			boolean timeout = true;
			if (rank.hasLastLoginTime()) {
				try {
					Date date = new SimpleDateFormat(TimeConst.DEFAULT_DATE_FORMAT).parse(rank.getLastLoginTime());
					if(new Date().getTime() - date.getTime() < RedisExpiredConst.EXPIRED_USERINFO_7DAY)
						timeout = false;
				} catch (ParseException e) {
				}
			}
			if(timeout){
				rankRedisService.delZhanliRank(user.getServerId(), rank.getId());
				if(ranks.size() > count)
					it.remove();
			}
		}
		while(ranks.size() > count){
			ranks.remove(ranks.size()-1);
		}
		return ranks;
	}
	
	public int getRefreshCount(UserBean user){
		long time[] = {redis.today(0), redis.today(12), redis.today(18)};
		long now = redis.now();
		int count = 0;
		if(now - user.getPvpMineRefreshTime() >= 24*3600){
			count = (int)(now - user.getPvpMineRefreshTime())/24/3600;
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
		if(count > 0)
			userService.updateUserDailyData(user);
		return count;
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
				entry.setValue(builder.build());
			}else if(redis.now() > mine.getEndTime()){
				it.remove();
				redis.deleteMine(user.getId(), mine.getId());
			}
		}
		int count = getRefreshCount(user);
		if(count > 0){//刷新对手
			List<UserInfo> ranks = getRandUser(50, user);//每张地图刷新一个对手
			if(ranks.isEmpty())
				return;
			for(PVPMap map : maplist.getFieldList()){
				if(map.getOpened()){
					for(int i = 0, enemy = count;i < 10 && enemy > 0; i++){
						PVPMine.Builder builder = PVPMine.newBuilder(map.getKuangdian(redis.nextInt(map.getKuangdianCount())));
						PVPMine mine = mineMap.get(builder.getId()+"");
						if(mine != null && mine.getEndTime() > System.currentTimeMillis()/1000 )
							continue;

						if(redis.nextInt(3) < enemy && !(mine != null && mine.hasOwner())){
							builder.setOwner(ranks.get(redis.nextInt(ranks.size())));
							redis.saveMine(user.getId(), builder.build());
							mineMap.put(builder.getId()+"", builder.build());
						}
						enemy-=3;
					}
				}
			}
		}
	}
	
	public PVPMapList getMapList(Builder responseBuilder, UserBean user){
		PVPMapList.Builder maplist = redis.getMapList(user.getId(), user.getPvpUnlock());
		if(user.getPvpUnlock() == 0){
			int fieldid = maplist.getField(0).getFieldid();
			user.setPvpUnlock(fieldid);
			userService.updateUserDailyData(user);
			unlockMap(fieldid, 1000, user);
		}
		Map<String, String> pvpMap = redis.getUserBuffs(user);
		Map<String, PVPMine> mineMap = redis.getUserMines(user.getId());
		List<PVPMonster> monsters = redis.getMonsters(user, pvpMap);
		refreshMine(maplist, mineMap, user);
		for(PVPMap.Builder mapBuilder : maplist.getFieldBuilderList()){
			if(!mapBuilder.getOpened())
				continue;
			String buff = pvpMap.get(mapBuilder.getFieldid()+"");
			if(buff != null)
				mapBuilder.setBuff(Integer.parseInt(buff));
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
		long time = System.currentTimeMillis()/1000/3600*3600;
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
			userService.updateUserDailyData(user);
			ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
			builder.setUser(user.build());
			responseBuilder.setUserInfoCommand(builder.build());
		}
		return maplist.build();
	}
	
	public MultiReward attackMonster(UserBean user, int positionid, boolean ret, int time){
		int logType = PvpMapConst.TYPE_MONSTER;
		PVPMonster monster = redis.getMonster(user, positionid);
		if(monster == null)
			return null;
		
		if (monster.getId() > 2000) {
			logType = PvpMapConst.TYPE_BOSS;
		}
		
		MultiReward.Builder rewards = MultiReward.newBuilder();
		int buff = -1;
		if(ret){
			if(monster.getId() > 2000)
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
			rewardService.doRewards(user, rewards.build());
			buff = redis.addUserBuff(user, monster.getFieldid(), monster.getBuffcount());
			if (monster.getId() > 2000) {
				/**
				 * PVP攻击BOSS的活动
				 */
				activityService.pvpAttackBossSuccessActivity(user.getId());
			}
		}
		
		/**
		 * send log
		 */
		sendLog(user, time, logType, ret, userTeamService.getTeamCache(user.getId()).getHeroInfoList(), monster.getId());
		
//		logger.warn(monster.getFieldid()+":+"+monster.getName()+ret+":"+monster.getBuffcount()+"->"+buff);
		return rewards.build();
	}
	
	public boolean attackMine(UserBean user, int id, boolean ret, int time){
		long enemyId = 0;
		int logType = PvpMapConst.TYPE_ATTACK;
		PVPMine.Builder mine = PVPMine.newBuilder(redis.getMine(user.getId(), id));
		if(mine == null)
			return false;
		if (mine.getLevel() > 0)
			logType = PvpMapConst.TYPE_DEFEND;
		/**
		 * PVP攻击玩家的活动
		 */
		if(mine.hasOwner())
			activityService.pvpAttackEnemyActivity(user.getId(), ret);
		if(ret){
			if(mine.hasOwner()){
				enemyId = mine.getOwner().getId();
				long userId = mine.getOwner().getId();
				mine.setOwner(user.buildShort());
				mine.setEndTime(System.currentTimeMillis()/1000+24*3600);
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
				
				mine.clearOwner();
				mine.setEnemyid(userId);
				redis.saveMine(user.getId(), mine.build());
				
				PVPMapList.Builder maplist = redis.getMapList(userId, 0);
				for (PVPMap map : maplist.getFieldList()) {
					if (map.getFieldid() == id) {
						String content = "玩家" + user.getUserName() + "占领了你的矿点";
						
						sendMineAttackedMail(userId, user, content, id);
						/*if(mineCount/5 >= enemyCount)*/{
							PVPMine othermine = redis.getMine(userId, id);
							if(othermine == null || othermine.getEndTime() <= redis.now() || othermine.getEnemyid() == user.getId())
								redis.saveMine(userId, mine.build());
						}
						break;
					}
				}
				
			}
		}
		/**
		 * send log
		 */
		sendLog(user, time, logType, ret, userTeamService.getTeamCache(user.getId()).getHeroInfoList(), enemyId);
		
		return true;
	}
	
	public boolean refreshMap(UserBean user){
		long now = redis.now();
		if(user.getRefreshPvpMapTime() > now)
			return false;
		
		redis.deleteMonsters(user);
		redis.deleteUserBuff(user);
		redis.getMonsters(user, new HashMap<String, String>(), true);

		user.setRefreshPvpMapTime(now+48*3600);
		userService.updateUserDailyData(user);
		return true;
	}
	
	public PVPMine refreshMine(UserBean user, int id){
		PVPMine mine = redis.getMine(user.getId(), id);
		if(mine == null || !mine.hasOwner())
			return null;
		PVPMine.Builder builder = PVPMine.newBuilder(mine);
		if(user.getPvpMineLeftTime() > 0){
			user.setPvpMineLeftTime(user.getPvpMineLeftTime()-1);
			userService.updateUserDailyData(user);
			List<UserInfo> ranks = getRandUser(50, user);
			builder.setOwner(ranks.get(redis.nextInt(ranks.size())));
			redis.saveMine(user.getId(), builder.build());
		}else{
			builder.clearOwner();
		}
		return builder.build();
	}
	
	public PVPMine getUserMine(UserBean user, int id){
		return redis.getMine(user.getId(), id);
	}
	
	private void sendLog(UserBean user, int time, int type, boolean ret, List<HeroInfo> heroList, long enemyId) {
		Map<String, String> logMap = new HashMap<String, String>();
		logMap.put(LogString.USERID, "" + user.getId());
		logMap.put(LogString.SERVERID, "" + user.getServerId());
		logMap.put(LogString.ATTACK_TIME, "" + time);
		logMap.put(LogString.OPERATION, "" + type);
		logMap.put(LogString.RESULT, "" + (ret ? 1 : 0));
		logMap.put(LogString.TEAM_LIST, userTeamService.getTeamString(heroList));
		logMap.put(LogString.ENEMYID, "" + enemyId);
		
		logService.sendLog(logMap, LogString.LOGTYPE_LOOTPVP);
	}
	
	private void sendMineAttackedMail(long userId, UserBean enemy, String content, int id) {
		MailBean mail = MailBean.buildMail(userId, enemy.getId(), enemy.getVip(), enemy.getIcon(), enemy.getUserName(), content, MailConst.TYPE_MINE_ATTACKED_MAIL, id);
		mailService.addMail(mail);
	}
}
