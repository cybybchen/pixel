package com.trans.pixel.service;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.PVPBoss;
import com.trans.pixel.protoc.Commands.PVPMap;
import com.trans.pixel.protoc.Commands.PVPMapList;
import com.trans.pixel.protoc.Commands.PVPMine;
import com.trans.pixel.protoc.Commands.PVPMonster;
import com.trans.pixel.service.redis.PvpMapRedisService;

@Service
public class PvpMapService {
//	private static final float RELATIVE_PERCENT = 0.2f;
	@Resource
	private PvpMapRedisService redis;
	@Resource
	private UserService userService;
//	@Resource
//	private RankRedisService rankRedisService;
//	@Resource
//	private UserMineService userMineService;
//	@Resource
//	private PvpXiaoguaiService pvpXiaoguaiService;
	
	public PVPMapList getMapList(UserBean user){
		PVPMapList.Builder maplist = PVPMapList.newBuilder(redis.getMapList());
		Map<String, PVPMap> pvpMap = redis.getUserMaps(user);
		Map<String, PVPMine> mineMap = redis.getUserMines(user);
		List<PVPMonster> monsters = redis.getMonsters(user);
		List<PVPBoss> bosses = redis.getBosses(user);
		for(PVPMap.Builder mapBuilder : maplist.getFieldBuilderList()){
			PVPMap map = pvpMap.get(mapBuilder.getFieldid()+"");
			if(map != null)
				mapBuilder.mergeFrom(map);
			for(PVPMine.Builder mineBuilder : mapBuilder.getKuangdianBuilderList()){
				PVPMine mine = mineMap.get(mineBuilder.getId());
				if(mine != null)
					mineBuilder.mergeFrom(mine);
			}
			for(PVPMonster monster : monsters){
				if(monster.getBelongto() == mapBuilder.getFieldid())
					mapBuilder.addMonster(monster);
			}
			for(PVPBoss boss : bosses){
				if(boss.getBelongto() == mapBuilder.getFieldid())
					mapBuilder.addBoss(boss);
			}
		}
		return maplist.build();
	}
	
	public boolean attackMonster(UserBean user, int id, boolean ret){
		PVPMonster monster = redis.getMonster(user, id);
		if(monster == null)
			return false;
		if(ret){
			redis.deleteMonster(user, id);
			PVPMap map = redis.getUserMap(user, monster.getBelongto());
			if(map == null)
				return true;
			PVPMap.Builder builder = PVPMap.newBuilder(map);
			builder.setBuff(builder.getBuff());
			redis.saveUserMap(user, builder.build());
		}
		return true;
	}
	
	public boolean attackBoss(UserBean user, int id, boolean ret){
		PVPBoss boss = redis.getBoss(user, id);
		if(boss == null)
			return false;
		if(ret){
			redis.deleteBoss(user, id);
			PVPMap map = redis.getUserMap(user, boss.getBelongto());
			if(map == null)
				return true;
			// PVPMap.Builder builder = PVPMap.newBuilder(map);
			// builder.setBuff(builder.getBuff());
			// redis.saveUserMap(user, builder.build());
		}
		return true;
	}
	
	public boolean attackMine(UserBean user, int id, boolean ret){
		PVPMine mine = redis.getMine(user, id);
		if(mine == null)
			return false;
		if(ret){
			PVPMine.Builder builder = PVPMine.newBuilder(mine);
			builder.clearOwner();
			builder.setEndTime(System.currentTimeMillis()/1000+3600*12);
			redis.saveMine(user, builder.build());
		}
		return true;
	}
	
	public boolean refreshMine(UserBean user, int id){
		PVPMine mine = redis.getMine(user, id);
		if(mine == null)
			return false;
		if(user.getPvpMineLeftTime() > 0){
			user.setPvpMineLeftTime(user.getPvpMineLeftTime()-1);
			PVPMine.Builder builder = PVPMine.newBuilder(mine);
			builder.setOwner(userService.getRandUser(user.getServerId()));
			redis.saveMine(user, builder.build());
		}
		return true;
	}
	
	public PVPMine getUserMine(UserBean user, int id){
		return redis.getMine(user, id);
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
