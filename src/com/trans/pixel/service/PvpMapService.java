package com.trans.pixel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.AchieveConst;
import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.PVPMap;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.protoc.Commands.PVPMonsterReward;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.redis.PvpMapRedisService;
import com.trans.pixel.service.redis.RankRedisService;

@Service
public class PvpMapService {
//	private static final float RELATIVE_PERCENT = 0.2f;
	@Resource
	private PvpMapRedisService redis;
	@Resource
	private UserService userService;
	@Resource
	private RewardService rewardService;
	@Resource
	private RankRedisService rankService;
//	@Resource
//	private RankRedisService rankRedisService;
//	@Resource
//	private UserMineService userMineService;
//	@Resource
//	private PvpXiaoguaiService pvpXiaoguaiService;
	@Resource
	private AchieveService achieveService;

	public boolean unlockMap(int fieldid, int zhanli, UserBean user){
		PVPMapList.Builder maplist = redis.getMapList(user);
		for(PVPMap.Builder map : maplist.getFieldBuilderList()){
			if(fieldid == map.getFieldid()){
				if(zhanli >= map.getZhanli()){
					map.setOpened(true);
					redis.saveMapList(maplist.build(), user);
					return true;
				}
			}
		}
		return false;
	}
	
	public void refreshMine(PVPMapList.Builder maplist, Map<String, PVPMine> mineMap, UserBean user){
		long time = redis.today(6);
		if(user.getPvpMineRefreshTime() < time){//刷新对手
			int count = (int)(time - user.getPvpMineRefreshTime())/24/3600+redis.nextInt((int)(time - user.getPvpMineRefreshTime())/12/3600);
			user.setPvpMineRefreshTime(time);
			userService.updateUserDailyData(user);
			if(count >= 20)
				count = 20;
			long myrank = rankService.getMyZhanliRank(user);
			List<UserInfo> ranks = null;
			if(myrank - count <= 0)
				ranks = rankService.getZhanliRanks(user, 0, myrank+count);
			else
				ranks = rankService.getZhanliRanks(user, myrank - count, myrank+count);
			Iterator<UserInfo> it = ranks.iterator();
			while(it.hasNext()){
				UserInfo rank = it.next();
				if(rank.getId() == user.getId()){
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
					rankService.delZhanliRank(user.getServerId(), rank.getId());
					if(ranks.size() > count+1)
						it.remove();
				}
			}
			if(ranks.size() == 0)
				return;
			if(count >= 20){
				for(PVPMap map : maplist.getFieldList()){
					for(PVPMine mine : map.getKuangdianList()){
						PVPMine.Builder builder = PVPMine.newBuilder(mine);
						builder.setOwner(ranks.get(redis.nextInt(ranks.size())));
						mineMap.put(builder.getId()+"", builder.build());
					}
				}
				redis.saveMines(user.getId(), mineMap);
			}else{
				while(count > 0){
					PVPMap map = maplist.getField(redis.nextInt(maplist.getFieldCount()));
					PVPMine.Builder builder = PVPMine.newBuilder(map.getKuangdian(redis.nextInt(map.getKuangdianCount())));
					PVPMine mine = mineMap.get(builder.getId()+"");
					if(mine != null && mine.getEndTime() > System.currentTimeMillis()/1000 )
						continue;
					builder.setOwner(ranks.get(redis.nextInt(ranks.size())));
					redis.saveMine(user.getId(), builder.build());
					mineMap.put(builder.getId()+"", builder.build());
					count--;
				}
			}
		}
	}
	
	public PVPMapList getMapList(Builder responseBuilder, UserBean user){
		PVPMapList.Builder maplist = redis.getMapList(user);
		Map<String, String> pvpMap = redis.getUserBuffs(user);
		Map<String, PVPMine> mineMap = redis.getUserMines(user.getId());
		List<PVPMonster> monsters = redis.getMonsters(user, pvpMap);
		refreshMine(maplist, mineMap, user);
		for(PVPMap.Builder mapBuilder : maplist.getFieldBuilderList()){
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
		if(time >= user.getPvpMineGainTime()){//收取资源
			int resource = user.getPointPVP();
			for(PVPMap map : maplist.getFieldList()){
				resource += map.getYield();
				for(PVPMine mine : map.getKuangdianList()){
					resource += mine.getYield();
				}
			}
			user.setPointPVP(resource);
			user.setPvpMineGainTime(time);
			userService.updateUserDailyData(user);
			ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
			builder.setUser(user.build());
			responseBuilder.setUserInfoCommand(builder.build());
		}
		return maplist.build();
	}
	
	public MultiReward attackMonster(UserBean user, int positionid, boolean ret){
		PVPMonster monster = redis.getMonster(user, positionid);
		if(monster == null)
			return null;
		MultiReward.Builder rewards = MultiReward.newBuilder();
		if(ret){
			redis.deleteMonster(user, positionid);
			PVPMonsterReward reward = redis.getMonsterReward(monster.getId());
			RewardInfo.Builder rewardinfo = RewardInfo.newBuilder();
			rewardinfo.setItemid(RewardConst.PVPCOIN);
			rewardinfo.setCount(reward.getA()+reward.getB()*monster.getLevel());
			rewards.addLoot(rewardinfo);
			if(redis.nextInt(reward.getWeightall()) < reward.getWeight1()){
				rewardinfo.setItemid(reward.getItemid1());
				rewardinfo.setCount(reward.getCount1());
				rewards.addLoot(rewardinfo);
			}else{
				rewardinfo.setItemid(reward.getItemid2());
				rewardinfo.setCount(reward.getCount2());
				rewards.addLoot(rewardinfo);
			}
			rewardService.doRewards(user, rewards.build());
			redis.addUserBuff(user, monster.getFieldid(), monster.getBuffcount());
			if (monster.getId() > 2000) {
				/**
				 * achieve type 111
				 */
				achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOOTPVP_KILLBOSS);
			}
		}
		return rewards.build();
	}
	
	public boolean attackMine(UserBean user, int id, boolean ret){
		PVPMine.Builder mine = PVPMine.newBuilder(redis.getMine(user.getId(), id));
		if(mine == null)
			return false;
		if(ret){
			if(mine.hasOwner()){
				long userId = mine.getOwner().getId();
				PVPMapList.Builder maplist = redis.getMapList(user);
				Map<String, PVPMine> mineMap = redis.getUserMines(userId);
				int mineCount = 0;
				for(PVPMap.Builder mapBuilder : maplist.getFieldBuilderList()){
					mineCount += mapBuilder.getKuangdianCount();
				}
				int enemyCount = mineMap.size();
				if(mineCount/5 >= enemyCount){
					mine.setOwner(user.buildShort());
					mine.setEndTime(System.currentTimeMillis()/1000+24*3600);
					mine.setLevel(mine.getLevel()+1);
					redis.saveMine(userId, mine.build());
				}
				mine.clearOwner();
				redis.saveMine(user.getId(), mine.build());
			}
		}
		/**
		 * achieve type 110
		 */
		achieveService.sendAchieveScore(user.getId(), AchieveConst.TYPE_LOOTPVP_ATTACK);
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
			builder.setOwner(userService.getRandUser(user));
			redis.saveMine(user.getId(), builder.build());
		}else{
			builder.clearOwner();
		}
		return builder.build();
	}
	
	public PVPMine getUserMine(UserBean user, int id){
		return redis.getMine(user.getId(), id);
	}
	
//	public List<UserMineBean> relateUser(UserBean user) {
//		long userId = user.getId();
//		long userRank = rankRedisService.getUserRank(user.getServerId(), RankConst.TYPE_ZHANLI, userId);
//		List<UserMineBean> userMineList = userMineService.selectUserMineList(userId);
//		int count = 0;
//		for(UserMineBean userMine : userMineList) {
//			if (randomRelative()) {
//				if (userMine.getRelativeUserId() == 0) {
//					long relativeUserRank = getRelativeUserRank(userRank);
//					userMine.setRelativeUserId(rankRedisService.getUserIdByRank(user.getServerId(), relativeUserRank, RankConst.TYPE_ZHANLI));
//					userMine.setPreventTime(calUserMinePreventTime());
//					userMineService.updateUserMine(userMine);
//					count++;
//					if (count * 1.f / userMineList.size() > RELATIVE_PERCENT)
//						break;
//				}
//			}
//		}
//		
//		return userMineList;
//	}
//	
//	public long refreshRelatedUser(UserBean user, int mapId, int mineId) {
//		long userId = user.getId();
//		long userRank = rankRedisService.getUserRank(user.getServerId(), RankConst.TYPE_ZHANLI, userId);
//		return getRelatedUserId(user.getServerId(), userRank);
//	}
//	
//	public UserPvpMapBean getXiaoguai(UserBean user, int mapId) {
//		UserPvpMapBean userPvpMap = userPvpMapService.selectUserPvpMap(user.getId(), mapId);
//		if (userPvpMap == null) {
//			return null;
//		}
//		if (isRefreshXiaoguai(userPvpMap.getLastRefreshTime())) {
//			userPvpMap.emptyField();
//			userPvpMap.setFieldList(pvpXiaoguaiService.refreshXiaoguai(userPvpMap.getMapId()));
//		}
//		
//		return userPvpMap;
//	}
//	
//	private long getRelatedUserId(int serverId, long myRank) {
//		long relatedRank = getRelativeUserRank(myRank);
//		return rankRedisService.getUserIdByRank(serverId, relatedRank, RankConst.TYPE_ZHANLI);
//	}
//	
//	private void relative(long userId, long relativeUserId, int mapId, int mineId) {
//		UserMineBean userMine = userMineService.selectUserMine(userId, mapId, mineId);
//		if (userMine == null) {
//			return;
//		}
//		userMine.setRelativeUserId(relativeUserId);
//		userMine.setPreventTime(calUserMinePreventTime());
//		userMineService.updateUserMine(userMine);
//	}
//	
//	public UserMineBean attackRelativeUser(long userId, int mapId, int mineId) {
//		UserMineBean userMine = userMineService.selectUserMine(userId, mapId, mineId);
//		if (userMine == null)
//			return null;
////		if (userMine.getRelativeUserId() == 0) {
////			return null;
////		}
//		if (attackSuccess(userId, userMine.getRelativeUserId())) {
//			relative(userMine.getRelativeUserId(), userId, mapId, mineId);
//		}
//		
//		userMine.setPreventTime(0);
//		userMine.setLevel(userMine.getLevel() + 1);
//		userMineService.updateUserMine(userMine);
//		
//		return userMine;
//	}
//	
//	public long getRelativeUserRank(long rank) {
//		return rank - 1;
//	}
//	
//	private boolean randomRelative() {
//		Random rand = new Random();
//		return rand.nextBoolean();
//	}
//	
//	private long calUserMinePreventTime() {
//		return PvpMapConst.PREVENT_TIME + System.currentTimeMillis();
//	}
//	
//	private boolean attackSuccess(long attackUserId, long relativeUserId) {
//		return true;
//	}
//	
//	private boolean isRefreshXiaoguai(String lastRefreshTime) {
//		if (lastRefreshTime == null || lastRefreshTime.isEmpty())
//			return true;
//		
//		Date last = DateUtil.getDate(lastRefreshTime, TimeConst.DEFAULT_DATETIME_FORMAT, null);
//		Date time1 = DateUtil.getCurrentDayDate(TimeConst.PVP_REFRESH_XIAOGUAI_TIME_1);
//		Date time2 = DateUtil.getCurrentDayDate(TimeConst.PVP_REFRESH_XIAOGUAI_TIME_2);
//		Date time3 = DateUtil.getCurrentDayDate(TimeConst.PVP_REFRESH_XIAOGUAI_TIME_3);
//		
//		Calendar calendar = Calendar.getInstance();
//		Date now = calendar.getTime();
//		if (last.before(time1)) {
//			if (now.after(time1))
//				return true;
//		} else if (last.before(time2)) {
//			if (now.after(time2))
//				return true;
//		} else if (last.before(time3)) {
//			if (now.after(time3))
//				return true;
//		}
//		
//		return false;
//	}
}
