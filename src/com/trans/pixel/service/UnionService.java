package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.RankConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.FightResult;
import com.trans.pixel.protoc.Commands.FightResultList;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RankItem;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UnionBoss;
import com.trans.pixel.protoc.Commands.UnionBossRecord;
import com.trans.pixel.protoc.Commands.UnionBossloot;
import com.trans.pixel.protoc.Commands.UnionBosswin;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class UnionService extends FightService{

	private static final Logger log = LoggerFactory.getLogger(UnionService.class);
	
	@Resource
	private UnionRedisService redis;
	@Resource
	private AreaRedisService areaRedisService;
	@Resource
	private MailService mailService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UnionMapper unionMapper;
	@Resource
	private UserService userService;
	@Resource
	private ServerService serverService;
	@Resource
	private ActivityService activityService;
	
	public List<Union> getBaseUnions(UserBean user) {
		return redis.getBaseUnions(user);
	}
	
	public Union.Builder getBaseUnion(UserBean user) {
		if(user.getUnionId() == 0)
			return null;
		Union.Builder union = redis.getUnion(user);
		if(union == null && user.getUnionId() != 0){//load union from db
			UnionBean unionbean = unionMapper.selectUnionById(user.getUnionId());
			if(unionbean != null){//load members from db
				redis.saveUnion(unionbean.build(), user);
				union = redis.getUnion(user);
				List<UserInfo> members = new ArrayList<UserInfo>();
				List<UserBean> beans = userService.getUserByUnionId(user.getUnionId());
				List<Long> memberIds = new ArrayList<Long>();
				for(UserBean bean : beans){
					memberIds.add(bean.getId());
					members.add(bean.buildShort());
					userService.cache(bean.getServerId(), bean.buildShort());
				}
				redis.saveMembers(memberIds, user);
			}
		}
		if(union == null)
			return null;
		return union;
	}

	public Union getUnion(UserBean user) {
		Union.Builder union = getBaseUnion(user);
		if(union == null)
			return null;
		List<UserInfo> members = redis.getMembers(user);
		boolean needupdate = false;

		int index = 0;
		int zhanli = 0;
		for(UserInfo member : members){
			if(index < 10)
				zhanli += member.getZhanli()*6/10;
			else if(index < 20)
				zhanli += member.getZhanli()*3/10;
			else if(index < 30)
				zhanli += member.getZhanli()/10;
			index++;
		}
		if(zhanli != union.getZhanli()){
			union.setZhanli(zhanli);
			needupdate = true;
		}
		if(union.getCount() != members.size()){
			union.setCount(members.size());
			needupdate = true;
		}
		if(union.hasAttackId() && union.getAttackCloseTime() < redis.now()){
			union.clearAttackId();
			union.clearAttackEndTime();
			union.clearAttackCloseTime();
			needupdate = true;
		}
		if(union.hasDefendId() && union.getDefendCloseTime() < redis.now()){
			union.clearDefendId();
			union.clearDefendCloseTime();
			union.clearDefendEndTime();
			needupdate = true;
		}
		if(needupdate && redis.setLock("Union_"+union.getId()))
			redis.saveUnion(union.build(), user);

		if(union.hasAttackId()){
			List<UserInfo> users = redis.getFightQueue(union.getId(), union.getAttackId());
			union.addAllAttacks(users);
		}
		if(union.hasDefendId()){
	 		List<UserInfo> users = redis.getFightQueue(union.getDefendId(), union.getId());
	 		union.addAllDefends(users);
		}
		union.addAllMembers(members);
		if(user.getUnionJob() > 0){
			union.addAllApplies(redis.getApplies(user.getUnionId()));
		}
		
		/**
		 * 刷新定时工会boss
		 */
		crontabUnionBossActivity(user);
		union.addAllUnionBoss(getUnionBossList(user));
		
		return union.build();
	}
	
	/**
	 * 血战
	 */
	public void bloodFight(int attackUnionId, int defendUnionId, int serverId){
		Union.Builder attackUnion = redis.getUnion(serverId, attackUnionId);
		Union.Builder defendUnion = redis.getUnion(serverId, defendUnionId);
		boolean hasattack = (attackUnion != null);
		boolean hasdefend = (defendUnion != null);
		
		if(hasattack && !hasdefend){
			attackUnion.clearAttackId();
			redis.waitLock("Union_"+attackUnion.getId());
			redis.saveUnion(attackUnion.build(), serverId);
			return;
		}else if(!hasattack && hasdefend){
			defendUnion.clearDefendId();
			redis.waitLock("Union_"+defendUnion.getId());
			redis.saveUnion(defendUnion.build(), serverId);
			return;
		}else if(!hasattack && !hasdefend){
			return;
		}
		attackUnion.clearAttackId();
		defendUnion.clearDefendId();
		List<UserInfo> users = redis.getFightQueue(attackUnionId, defendUnionId);
		List<UserInfo> attacks = new ArrayList<UserInfo>();
		List<UserInfo> defends = new ArrayList<UserInfo>();
		
//		UserInfo.Builder newuser = UserInfo.newBuilder();
//		newuser.setId(58);
//		newuser.setIcon(0);
//		newuser.setName("ss");
//		for(int i = 0;i < 100; i++){
//		attacks.add(newuser.build());
//		defends.add(newuser.build());
//		}
		
		for(UserInfo user : users){
			if(user.getUnionId() == defendUnionId){
				defends.add(user);
			}else{
				attacks.add(user);
			}
		}
		boolean attacksuccess = false;
		if(attacks.size() == 0){
			attacksuccess = false;
		}else if(defends.size() == 0){
			attacksuccess = true;
		}else{
			FightResultList.Builder resultlist = queueFight(attacks, defends);
			if(resultlist.getListCount() > 0){
				FightResult winresult = resultlist.getList(resultlist.getListCount()-1);
				if( winresult.getResult() == 1 ){//攻破
					attacksuccess = true;
				}else{
					attacksuccess = false;
				}
				redis.saveFight(attackUnionId, defendUnionId, resultlist.build());
//				for(FightResult result : resultlist.getListList()){//发奖
//					
//				}
			}
		}
		
		if(attacksuccess){
			attackUnion.setPoint(attackUnion.getPoint()+defendUnion.getPoint()/5);
			defendUnion.setPoint(defendUnion.getPoint()*4/5);
			unionMapper.updateUnion(new UnionBean(attackUnion));
			unionMapper.updateUnion(new UnionBean(defendUnion));
		}
		redis.waitLock("Union_"+attackUnion.getId());
		redis.saveUnion(attackUnion.build(), serverId);
		redis.waitLock("Union_"+defendUnion.getId());
		redis.saveUnion(defendUnion.build(), serverId);
		
		/**
		 * 公会血战的活动
		 */
		for (UserInfo user : attacks) {
			activityService.unionAttackActivity(user.getId(), attacksuccess);
		}
	}
	
	public Union create(int icon, String unionName, UserBean user) {
		if(user.getUnionId() != 0)
			return getUnion(user);
		UnionBean union = new UnionBean();
		union.setName(unionName);
		union.setIcon(icon);
		union.setServerId(user.getServerId());
		unionMapper.createUnion(union);
		redis.saveUnion(union.build(), user);
		user.setUnionId(union.getId());
		user.setUnionName(unionName);
		user.setUnionJob(UnionConst.UNION_HUIZHANG);//会长
		userService.updateUser(user);
		userService.cache(user.getServerId(), user.buildShort());
		Union.Builder builder = Union.newBuilder(union.build());
		List<UserInfo> members = new ArrayList<UserInfo>();
		members.add(user.buildShort());
		redis.saveMember(user.getId(), user);
		builder.addAllMembers(members);
		
		return builder.build();
	}
	
	public ResultConst quit(long id, Builder responseBuilder, UserBean user) {
		int fightstate = getAreaFighting(id, user);
		if(fightstate == 1)
			return ErrorConst.AREA_FIGHT_BUSY;
		else if(fightstate == 2)
			return ErrorConst.AREA_OWNER_BUSY;
		if(isBloodFighting(id, user))
			return ErrorConst.BLOOD_FIGHT_BUSY;
		if(id == user.getId()){
			if(user.getUnionJob() == UnionConst.UNION_HUIZHANG){
				if(delete(user))
					return SuccessConst.DELETE_UNION_SUCCESS;
				else
					return ErrorConst.UNIONLEADER_QUIT;
			}
			redis.quit(id, user);
			user.setUnionId(0);
			user.setUnionName("");
			user.setUnionJob(UnionConst.UNION_HUIZHONG);
			userService.updateUser(user);
			userService.cache(user.getServerId(), user.buildShort());
			return SuccessConst.QUIT_UNION_SUCCESS;
		}else{
			if(user.getUnionJob() < UnionConst.UNION_FUHUIZHANG)
				return ErrorConst.PERMISSION_DENIED;
			UserBean bean = userService.getOther(id);
			if(bean.getUnionJob() >= UnionConst.UNION_ZHANGLAO && user.getUnionJob() < UnionConst.UNION_HUIZHANG)
				return ErrorConst.PERMISSION_DENIED;
			redis.quit(id, user);
			bean.setUnionId(0);
			bean.setUnionName("");
			bean.setUnionJob(UnionConst.UNION_HUIZHONG);
			userService.updateUser(bean);
			userService.cache(user.getServerId(), bean.buildShort());
			return SuccessConst.QUIT_UNION_MEMBER_SUCCESS;
		}
		
		
	}
	
	public boolean delete(UserBean user) {
		if(user.getUnionJob() == UnionConst.UNION_HUIZHANG && redis.getNumOfMembers(user) <= 1){
			redis.deleteMembers(user);
			redis.deleteUnion(user);
			unionMapper.deleteUnion(user.getUnionId());
			user.setUnionId(0);
			user.setUnionName("");
			user.setUnionJob(UnionConst.UNION_HUIZHONG);
			userService.updateUser(user);
			return true;
		}
		return false;
	}
	
	public void apply(int unionId, UserBean user) {
		if(user.getUnionId() != 0)
			return;
		redis.apply(unionId, user);
	}
	
	public int getAreaFighting(long userId, UserBean user){
		Map<String, AreaResource> resources = areaRedisService.getResources(user);
		for(AreaResource resource : resources.values()){
			if(resource.hasOwner() && resource.getOwner().getId() == userId){
				if(resource.getState() != 0)
					return 1;
				else
					return 2;
			}
			for(UserInfo userinfo : resource.getAttacksList()){
				if(userinfo.getId() == userId)
					return 1;
			}
			for(UserInfo userinfo : resource.getDefensesList()){
				if(userinfo.getId() == userId)
					return 1;
			}
		}
		return 0;
	}
	
	public boolean isBloodFighting(long userId, UserBean user){
		if(user.getUnionId() == 0)
			return false;
		Union.Builder builder = redis.getUnion(user.getServerId(), user.getUnionId());
		if(builder == null)
			return false;
		if(builder.hasAttackId()){
			List<UserInfo> users = redis.getFightQueue(builder.getId(), builder.getAttackId());
			for(UserInfo userinfo : users){
				if(userinfo.getId() == userId)
					return true;
			}
		}
		if(builder.hasDefendId()){
			List<UserInfo> users = redis.getFightQueue(builder.getDefendId(), builder.getId());
	 		for(UserInfo userinfo : users){
				if(userinfo.getId() == userId)
					return true;
			}
		}
		return false;
	}
	
	public ResultConst reply(Long userId, boolean receive, UserBean user) {
		if(receive){
			UserBean bean = userService.getOther(userId);
			if(bean != null && bean.getUnionId() == 0){
				if(getAreaFighting(userId, user) == 1)
					return ErrorConst.AREA_FIGHT_BUSY;
				Union.Builder builder = redis.getUnion(user.getServerId(), user.getUnionId());
				if(builder.getCount() >= builder.getMaxCount())
					return ErrorConst.UNION_FULL;
				bean.setUnionId(user.getUnionId());
				bean.setUnionName(builder.getName());
				bean.setUnionJob(UnionConst.UNION_HUIZHONG);
				userService.updateUser(bean);
				userService.cache(user.getServerId(), bean.buildShort());
				redis.saveMember(bean.getId(), user);
			}else{
				redis.reply(userId, receive, user);
				return ErrorConst.USER_HAS_UNION;
			}
		}
		redis.reply(userId, receive, user);
		
		return SuccessConst.HANDLE_UNION_APPLY_SUCCESS;
	}
	
	public ResultConst upgrade(UserBean user) {
		Union.Builder builder = redis.getUnion(user.getServerId(), user.getUnionId());
		if(builder == null || !redis.setLock("Union_"+user.getUnionId()))
			return ErrorConst.ERROR_LOCKED;
		builder.setLevel(builder.getLevel()+1);
		redis.saveUnion(builder.build(), user);
		unionMapper.updateUnion(new UnionBean(builder));
		return SuccessConst.UPGRADE_UNION_SUCCESS;
	}
	
	public ResultConst handleMember(long id, int job, UserBean user) {
		if(user.getUnionJob() < UnionConst.UNION_HUIZHANG)
			return ErrorConst.PERMISSION_DENIED;
		if(user.getId() == id)
			return ErrorConst.PERMISSION_DENIED;
		int vicecount = 0;
		int eldercount = 0;
		if(job == UnionConst.UNION_FUHUIZHANG || job == UnionConst.UNION_ZHANGLAO){
			List<UserInfo> members = redis.getMembers(user);
			for(UserInfo member : members){
				if(member.getUnionJob() == UnionConst.UNION_FUHUIZHANG)
					vicecount++;
				else if(member.getUnionJob() == UnionConst.UNION_ZHANGLAO)
					eldercount++;
			}
			if(job == UnionConst.UNION_FUHUIZHANG && vicecount >= 1)
				return ErrorConst.UNION_VICE_FULL;
			if(job == UnionConst.UNION_ZHANGLAO && eldercount >= 4)
				return ErrorConst.UNION_ELDER_FULL;
		}
		UserBean bean = userService.getOther(id);
		if(bean.getUnionId() == user.getUnionId()){
			if(job == UnionConst.UNION_HUIZHANG){//会长转让
				user.setUnionJob(UnionConst.UNION_HUIZHONG);
				redis.saveMember(user.getId(), user);
				userService.updateUser(user);
				userService.cache(user.getServerId(), user.buildShort());
			}
			bean.setUnionJob(job);
			redis.saveMember(bean.getId(), user);
			userService.updateUser(bean);
			userService.cache(user.getServerId(), bean.buildShort());
		}
		return SuccessConst.HANDLE_UNION_MEMBER_SUCCESS;
	}
	
	public ResultConst attack(int attackId, long teamid, UserBean user){
		Union.Builder builder = redis.getUnion(user.getServerId(), user.getUnionId());
		if(builder.hasAttackId()){
			if(redis.now() > builder.getAttackCloseTime()){
				return ErrorConst.JOIN_END;
			}
			Team team = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, teamid, team);
			redis.attack(builder.getAttackId(), user);
		}else if(user.getUnionJob() >= UnionConst.UNION_FUHUIZHANG && attackId != 0 && redis.now() > builder.getAttackCloseTime()){
			builder.setAttackId(attackId);
			builder.setAttackCloseTime(redis.today(24));
			builder.setAttackEndTime(builder.getAttackCloseTime()+300);
			Union.Builder defendUnion = redis.getUnion(user.getServerId(), builder.getAttackId());
			defendUnion.setDefendId(builder.getId());
			defendUnion.setDefendCloseTime(redis.today(24));
			builder.setDefendEndTime(builder.getDefendCloseTime()+300);
			if(redis.setLock("Union_"+builder.getId()) 
				&& redis.setLock("Union_"+defendUnion.getId()))
			{
				redis.saveUnion(builder.build(), user);
				redis.saveUnion(defendUnion.build(), user);
				redis.saveFightKey(builder.getId(), defendUnion.getId(), user.getServerId());
			}else{
				return ErrorConst.ERROR_LOCKED;
			}
		}else{
			return ErrorConst.UNION_NOT_FIGHT;
		}
		
		return SuccessConst.UNION_FIGHT_SUCCESS;
	}
	
	public ResultConst defend(long teamid, UserBean user){
		Union.Builder union = redis.getUnion(user.getServerId(), user.getUnionId());
		if(union.hasDefendId()){
			if(redis.now() > union.getDefendCloseTime()){
				return ErrorConst.JOIN_END;
			}
			Team team = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, teamid, team);
			redis.defend(union.getDefendId(), user);
			return SuccessConst.UNION_FIGHT_SUCCESS;
		}else{
			return ErrorConst.UNION_NOT_FIGHT;
		}
	}

	public void unionFightTask(){
		List<Integer> servers = serverService.getServerIdList();
		for(Integer serverId : servers){
			Map<String, String> map = redis.getFightKeys(serverId);
			for(Entry<String, String> entry : map.entrySet()){
				int attackUnionId = Integer.parseInt(entry.getKey());
				int defendUnionId = Integer.parseInt(entry.getValue());
				try {
					while(!redis.setLock("Union_"+attackUnionId, 60))
							Thread.sleep(1);
					while(!redis.setLock("Union_"+defendUnionId, 60))
							Thread.sleep(1);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				} 
				bloodFight(attackUnionId, defendUnionId, serverId);
				redis.deleteFightKey(attackUnionId, serverId);
				redis.clearLock("Union_"+attackUnionId);
				redis.clearLock("Union_"+defendUnionId);
			}
		}
	}
	
	public void crontabUnionBossActivity(UserBean user) {
		if (user.getUnionId() > 0)
			doUnionBossRecord(user, UnionConst.UNION_BOSS_TYPE_CRONTAB, 0, 0);
	}
	
	public void costUnionBossActivity(UserBean user, int targetId, int count) {
		if (user.getUnionId() > 0)
			doUnionBossRecord(user, UnionConst.UNION_BOSS_TYPE_COST, targetId, count);
	}
	
	public void killMonsterBossActivity(UserBean user, int targetId, int count) {
		if (user.getUnionId() > 0)
			doUnionBossRecord(user, UnionConst.UNION_BOSS_TYPE_KILLMONSTER, targetId, count);
	}

	public void updateKillMonsterRecord(Union.Builder union, int targetId, int count) {
		int targetCount = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getKillMonsterRecord());
			targetCount = json.getInt("" + targetId) + count;	
		} catch (Exception e) {
			
		}
		json.put("" + targetId, targetCount);
		union.setKillMonsterRecord(json.toString());
	}
	public void updateCostRecord(Union.Builder union, int targetId, int count) {
		int targetCount = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getCostRecord());
			targetCount = json.getInt("" + targetId) + count;	
		} catch (Exception e) {
			
		}
		json.put("" + targetId, targetCount);
		union.setCostRecord(json.toString());
	}
	public void updateUnionBossRecord(Union.Builder union, int bossId) {
		int bossCount = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getBossRecord());
			bossCount = json.getInt("" + bossId) + 1;	
		} catch (Exception e) {
			
		}
		json.put("" + bossId, bossCount);
		union.setBossRecord(json.toString());
	}
	public int getUnionBossCount(Union.Builder union, int bossId) {
		int bossCount = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getBossRecord());
			bossCount = json.getInt("" + bossId);	
		} catch (Exception e) {
			
		}
		return bossCount;
	}
	public void updateUnionBossEndTime(Union.Builder union, int bossId) {
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getBossEndTime());
			json.put("" + bossId, DateUtil.getCurrentDateString());
		} catch (Exception e) {
			
		}
		union.setBossEndTime(json.toString());
	}
	public String getUnionBossEndTime(Union.Builder union, int bossId) {
		JSONObject json = new JSONObject();
		String time = "";
		try {
			json = JSONObject.fromObject(union.getBossEndTime());
			time = json.getString("" + bossId);
		} catch (Exception e) {
			time = "";
		}
		return time;
	}
	
	public void doUnionBossRecord(UserBean user, int type, int targetId, int count) {
		Union.Builder union = redis.getUnion(user);
		if (union == null)
			return;
		Map<String, UnionBoss> map = redis.getUnionBossConfig();
		Iterator<Entry<String, UnionBoss>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, UnionBoss> entry = it.next();
			UnionBoss unionBoss = entry.getValue();
			if (unionBoss.getType() == type) {
				if (unionBoss.getTargetid() == targetId) {
					if (type == UnionConst.UNION_BOSS_TYPE_KILLMONSTER)
						updateKillMonsterRecord(union, targetId, count);
					else if (type == UnionConst.UNION_BOSS_TYPE_COST)
						updateCostRecord(union, targetId, count);
					
					calUnionBossRefresh(union, unionBoss);
					redis.waitLock("Union_"+union.getId());
					redis.saveUnion(union.build(), user);
				}
			}
		}
	}
	
	public void calUnionBossRefresh(Union.Builder union, UnionBoss unionBoss) {
		UnionBossRecord unionBossRecord = redis.getUnionBoss(union.getId(), unionBoss.getId());
		if (unionBossRecord != null) {
			if (unionBossRecord.getEndTime().equals("")) {
				if (unionBossRecord.getHp() > 0)
					return;
			} else if (!DateUtil.timeIsOver(unionBossRecord.getEndTime()))
				return;
		}
		int bossCount = getUnionBossCount(union, unionBoss.getId());
		JSONObject json = null;
		if (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_COST) {
			json = JSONObject.fromObject(union.getCostRecord());
		} else if (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_KILLMONSTER) {
			json = JSONObject.fromObject(union.getKillMonsterRecord());
		} 
		if ((unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_CRONTAB && canRefreshCrontabBoss(union, unionBoss) 
				|| (json != null && json.getInt("" + unionBoss.getTargetid()) >= (bossCount + 1) * unionBoss.getTargetcount()))) {
			updateUnionBossRecord(union, unionBoss.getId());
			redis.delUnionBossRankKey(union.getId(), unionBoss.getId());
			redis.saveUnionBoss(union, unionBoss);
		}	
	}
	
	public List<UnionBossRecord> getUnionBossList(UserBean user) {
		List<UnionBossRecord> builderList = new ArrayList<UnionBossRecord>();
		List<UnionBossRecord> unionBossList = redis.getUnionBossList(user.getUnionId());
		for (UnionBossRecord unionBoss : unionBossList) {
			UnionBossRecord.Builder builder = UnionBossRecord.newBuilder(unionBoss);
			List<UserRankBean> ranks = getUnionBossRankList(user, unionBoss.getBossId());
			builder.addAllRanks(UserRankBean.buildUserRankList(ranks));
			builderList.add(builder.build());
			
		}
		return builderList;
	}
	
	public UnionBossRecord attackUnionBoss(UserBean user, Union.Builder union, int bossId, int hp, MultiReward.Builder rewards) {
		UnionBossRecord.Builder unionBossRecord = UnionBossRecord.newBuilder(redis.getUnionBoss(user.getUnionId(), bossId));
		if (DateUtil.timeIsOver(unionBossRecord.getEndTime())) {
			return unionBossRecord.build();
		}
		
		unionBossRecord.setHp(unionBossRecord.getHp() - hp);
		if (unionBossRecord.getHp() <= 0) {
			doUnionBossRankReward(user.getUnionId(), bossId);
			updateUnionBossEndTime(union, bossId);
			calUnionBossRefresh(union, redis.getUnionBoss(bossId));
			redis.waitLock("Union_"+union.getId());
			redis.saveUnion(union.build(), user);
		}
		redis.saveUnionBoss(user.getUnionId(), unionBossRecord.build());
		redis.addUnionBossAttackRank(user, unionBossRecord.build(), hp);
		
		rewards.addAllLoot(calUnionBossReward(bossId));
		
		return unionBossRecord.build();
	}
	
	private void doUnionBossRankReward(int unionId, int bossId) {
		UnionBosswin unionBosswin = redis.getUnionBosswin(bossId);
		List<RankItem> rankList = unionBosswin.getItemList();
		for (RankItem item : rankList) {
			Set<TypedTuple<String>> ranks = redis.getUnionBossRanks(unionId, bossId, item.getRank() - 1, item.getRank1() - 1);
			for (TypedTuple<String> rank : ranks) {
				MailBean mail = MailBean.buildSystemMail(TypeTranslatedUtil.stringToLong(rank.getValue()), item.getDes(), getRankRewardList(item));
				log.debug("unionboss rank mail is:" + mail.toJson());
				mailService.addMail(mail);
			}
		}
	}
	
	private List<RewardInfo> getRankRewardList(RankItem rankItem) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		RewardInfo.Builder builder = RewardInfo.newBuilder();
		if (rankItem.getItemid1() > 0) {
			builder.setItemid(rankItem.getItemid1());
			builder.setCount(rankItem.getCount1());
			rewardList.add(builder.build());
			builder.clear();
		}
		if (rankItem.getItemid2() > 0) {
			builder.setItemid(rankItem.getItemid2());
			builder.setCount(rankItem.getCount2());
			rewardList.add(builder.build());
			builder.clear();
		}
		if (rankItem.getItemid3() > 0) {
			builder.setItemid(rankItem.getItemid3());
			builder.setCount(rankItem.getCount3());
			rewardList.add(builder.build());
		}
		
		return rewardList;
	}
	
	private List<RewardInfo> calUnionBossReward(int bossId) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		UnionBossloot bossloot = redis.getUnionBossloot(bossId);
		int weightAll = 0;
		List<RewardInfo> itemList = bossloot.getItemList();
		for (RewardInfo item : itemList) {
			weightAll += item.getWeight();
		}
		
		int randomWeight = RandomUtils.nextInt(weightAll);
		for (RewardInfo item : itemList) {
			if (item.getWeight() > randomWeight) {
				rewardList.add(item);
				break;
			}
			
			randomWeight -= item.getWeight();
		}
		
		return rewardList;
	}
	
	private List<UserRankBean> getUnionBossRankList(UserBean user, int bossId) {
		Set<TypedTuple<String>> ranks = redis.getUnionBossRanks(user.getUnionId(), bossId, RankConst.UNIONBOSS_RANK_LIST_START, RankConst.UNIONBOSS_RANK_LIST_END);
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		int rankInit = 1;
		for (TypedTuple<String> rank : ranks) {
			UserRankBean userRank = new UserRankBean();
			UserInfo userInfo = userService.getCache(user.getServerId(), TypeTranslatedUtil.stringToLong(rank.getValue()));
			
			userRank.setRank(rankInit);
			userRank.initByUserCache(userInfo);
			userRank.setZhanli(rank.getScore().intValue());
			
			rankList.add(userRank);
			rankInit++;
		}
		
		return rankList;
	}
	
	private boolean canRefreshCrontabBoss(Union.Builder union, UnionBoss boss) {
		Date current = DateUtil.getDate();
		Date last = DateUtil.getDate(getUnionBossEndTime(union, boss.getId()));
		Date bossTime = DateUtil.setToDayTime(current, boss.getTargetcount());
		if (current.before(bossTime))
			return false;
			
		if (last == null)
			return true;
		
		if (DateUtil.intervalDays(current, last) >= 1)
			return true;
		
		return false;
	}
}
