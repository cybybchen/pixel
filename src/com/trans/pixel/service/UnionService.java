package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.AreaProto.AreaResource;
import com.trans.pixel.protoc.AreaProto.FightResult;
import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UnionBossRecord;
import com.trans.pixel.protoc.Base.UnionBossRecord.UNIONBOSSSTATUS;
import com.trans.pixel.protoc.Base.UnionBossUserRecord;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.UnionProto.RankItem;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.protoc.UnionProto.UnionApply;
import com.trans.pixel.protoc.UnionProto.UnionBoss;
import com.trans.pixel.protoc.UnionProto.UnionBosswin;
import com.trans.pixel.protoc.UnionProto.UnionExp;
import com.trans.pixel.protoc.UserInfoProto.Merlevel;
import com.trans.pixel.protoc.UserInfoProto.MerlevelList;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.service.redis.ZhanliRedisService;
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
	@Resource
	private LogService logService;
	@Resource
	private RewardService rewardService;
	@Resource
	private ZhanliRedisService zhanliRedisService;
	
	Comparator<UserRankBean> comparator = new Comparator<UserRankBean>() {
        public int compare(UserRankBean bean1, UserRankBean bean2) {
            if (bean1.getDps() < bean2.getDps()) {
                return 1;
            } else if (bean1.getDps() == bean2.getDps() && bean1.getZhanli() < bean2.getZhanli()){
                return 1;
            } else {
            		return -1;
            	 }
        }
	};
	
	public List<Union> getBaseUnions(UserBean user) {
		return redis.getBaseUnions(user, 20);
	}
	public List<Union> getUnionsRank(UserBean user) {
		return redis.getBaseUnions(user, 0);
	}
	
	public List<Union> getUnionsByServerId(int serverId) {
		return redis.getBaseUnions(serverId);
	}
	
	public Union getUnionById(int serverId, int unionId) {
		Union.Builder union = redis.getUnion(serverId, unionId);
		List<UserInfo> members = redis.getMembers(unionId, serverId);
		union.addAllMembers(members);
		
		return union.build();
	}
	
	public Union.Builder getBaseUnion(UserBean user) {
		if(user.getUnionId() == 0)
			return null;
		Union.Builder union = redis.getUnion(user);
		if(union == null && user.getUnionId() != 0){//load union from db
			UnionBean unionbean = unionMapper.selectUnionById(user.getUnionId());
			if(unionbean != null){//load members from db
				UnionExp unionExp = redis.getUnionExp(unionbean.getLevel());
				if (unionExp != null)
					unionbean.setMaxCount(unionExp.getUnionsize());
				redis.saveUnion(unionbean.build(), user);
				union = redis.getUnion(user);
				List<UserInfo> members = new ArrayList<UserInfo>();
				List<UserBean> beans = userService.getUserByUnionId(user.getUnionId());
				List<Long> memberIds = new ArrayList<Long>();
				for(UserBean bean : beans){
					memberIds.add(bean.getId());
					UserInfo userinfo = userService.getCache(bean.getServerId(), bean.getId());
					if(userinfo.getZhanli() == 0) {
						userinfo = bean.buildShort();
		    			MerlevelList.Builder list = zhanliRedisService.getMerlevel();
		    			for(Merlevel level : list.getLevelList()){
		    				if(bean.getZhanliMax() >= level.getScore() && bean.getMerlevel() < level.getLevel()) {
		    					bean.setMerlevel(level.getLevel());
		    				}
		    			}
						userService.cache(bean.getServerId(), userinfo);
					}
					members.add(userinfo);
				}
				redis.saveMembers(memberIds, user);
			}
		}
		if(union == null)
			return null;
		
		if (union.getMaxCount() == 0) {//更新之后初始化工会最大人数
			UnionExp unionExp = redis.getUnionExp(union.getLevel());
			if (unionExp != null)
				union.setMaxCount(unionExp.getUnionsize());
		}
		return union;
	}

	public Union getUnion(UserBean user, boolean isNewVersion) {
		Union.Builder union = getBaseUnion(user);
		if(union == null)
			return null;
		if (!union.hasExp())
			union.setExp(0);
		
		List<UserInfo> members = redis.getMembers(user);
		boolean needupdate = false;

		Collections.sort(members, new Comparator<UserInfo>() {
			public int compare(UserInfo bean1, UserInfo bean2) {
				if (bean1.getZhanli() < bean2.getZhanli()) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		int index = 0;
		int zhanli = 0;
		for(UserInfo member : members){
			if(index < 10) {
				zhanli += member.getZhanli()*6/10;
			}else if(index < 20)
				zhanli += member.getZhanli()*3/10;
			else if(index < 30)
				zhanli += member.getZhanli()/10;
//			log.error("zhanli+"+member.getZhanli()+" = "+zhanli);
			index++;
		}
		if(!union.getName().equals(user.getUnionName())){
			user.setUnionName(union.getName());
			userService.updateUser(user);
		}
		zhanli =(int)(zhanli/2.3);
		if(zhanli != union.getZhanli()){
			union.setZhanli(zhanli);
			needupdate = true;
			if (union.getZhanli() > union.getMaxZhanli()) {
				union.setMaxZhanli(union.getZhanli());
				unionMapper.updateUnion(new UnionBean(union));
			}
		}
		if(union.getCount() != members.size()){
			union.setCount(members.size());
			needupdate = true;
		}
		if(union.hasAttackId() && union.getAttackCloseTime() < RedisService.now()){
			union.clearAttackId();
			union.clearAttackEndTime();
			union.clearAttackCloseTime();
			needupdate = true;
		}
		if(union.hasDefendId() && union.getDefendCloseTime() < RedisService.now()){
			union.clearDefendId();
			union.clearDefendCloseTime();
			union.clearDefendEndTime();
			needupdate = true;
		}
		if(needupdate && redis.setLock("Union_"+union.getId())){
			redis.saveUnion(union.build(), user);
			redis.clearLock("Union_"+union.getId());
		}

		/**
		 * 计算镜像世界矿点奖励
		 */
//		calAreaResourceReward(user);
		
//		if(union.hasAttackId()){
//			List<UserInfo> users = redis.getFightQueue(union.getId(), union.getAttackId());
//			union.addAllAttacks(users);
//		}
//		if(union.hasDefendId()){
//	 		List<UserInfo> users = redis.getFightQueue(union.getDefendId(), union.getId());
//	 		union.addAllDefends(users);
//		}
		
		if (!isNewVersion) {
			/**
			 * 刷新定时工会boss
			 */
			crontabUnionBossActivity(user);
			union.addAllUnionBoss(getUnionBossList(user, union));
			
			if(user.getUnionJob() > 0){
				union.addAllApplies(redis.getApplies(user.getUnionId()));
			}
		}
		union.addAllMembers(members);
		
		return union.build();
	}
	
	public List<UnionApply> getUnionApply(UserBean user) {
		List<UnionApply> applylist = new ArrayList<UnionApply>();
		if(user.getUnionJob() > 0){
			applylist.addAll(redis.getApplies(user.getUnionId()));
			return applylist.subList(0, Math.min(20, applylist.size()));
//			return redis.getApplies();
		}
		
		return new ArrayList<UnionApply>();
	}
	
	public List<UnionBossRecord> getUnionBossList(UserBean user) {
		Union.Builder union = getBaseUnion(user);
		if(union == null)
			return new ArrayList<UnionBossRecord>();
		
		return getUnionBossList(user, union);
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
			redis.clearLock("Union_"+attackUnion.getId());
			return;
		}else if(!hasattack && hasdefend){
			defendUnion.clearDefendId();
			redis.waitLock("Union_"+defendUnion.getId());
			redis.saveUnion(defendUnion.build(), serverId);
			redis.clearLock("Union_"+defendUnion.getId());
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
		redis.clearLock("Union_"+attackUnion.getId());
		redis.waitLock("Union_"+defendUnion.getId());
		redis.saveUnion(defendUnion.build(), serverId);
		redis.clearLock("Union_"+defendUnion.getId());
		
		/**
		 * 公会血战的活动
		 */
		for (UserInfo user : attacks) {
			activityService.unionAttackActivity(user.getId(), attacksuccess);
		}
	}
	
	public Union create(int icon, String unionName, UserBean user) {
		if(user.getUnionId() != 0)
			return getUnion(user, false);
		
		UnionBean union = new UnionBean();
		union.setName(unionName);
		union.setIcon(icon);
		union.setServerId(user.getServerId());
		union.setLevel(0);
		UnionExp unionExp = redis.getUnionExp(union.getLevel());
		if (unionExp != null)
			union.setMaxCount(unionExp.getUnionsize());
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

			Union.Builder union = getBaseUnion(user);
//			List<UserInfo> members = redis.getMembers(user);

			union.setCount(union.getCount() - 1);
			if(redis.setLock("Union_"+union.getId())){
				redis.saveUnion(union.build(), user);
				redis.clearLock("Union_"+union.getId());
			}
			
			user.setUnionId(0);
			user.setUnionName("");
			user.setUnionJob(UnionConst.UNION_HUIZHONG);
			userService.updateUser(user);
			userService.cache(user.getServerId(), user.buildShort());
			
			return SuccessConst.QUIT_UNION_SUCCESS;
		}else{
			if(user.getUnionJob() < UnionConst.UNION_FUHUIZHANG)
				return ErrorConst.PERMISSION_DENIED;
			UserBean bean = userService.getUserOther(id);
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
		Map<Integer, AreaResource> resources = areaRedisService.getResources(user);
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
			UserBean bean = userService.getUserOther(userId);
			if(bean != null && bean.getUnionId() == 0){
				if(getAreaFighting(userId, user) == 1)
					return ErrorConst.AREA_FIGHT_BUSY;
				Union.Builder builder = this.getBaseUnion(user); 
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
		redis.clearLock("Union_"+builder.getId());
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
		UserBean bean = userService.getUserOther(id);
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

	public void saveUnion(final Union union, UserBean user) {
		redis.saveUnion(union, user.getServerId());
	}
	
	public ResultConst attack(int attackId, long teamid, UserBean user){
		Union.Builder builder = redis.getUnion(user.getServerId(), user.getUnionId());
		if(builder.hasAttackId()){
			if(RedisService.now() > builder.getAttackCloseTime()){
				return ErrorConst.JOIN_END;
			}
			Team team = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, teamid, team);
			redis.attack(builder.getAttackId(), user);
		}else if(user.getUnionJob() >= UnionConst.UNION_FUHUIZHANG && attackId != 0 && RedisService.now() > builder.getAttackCloseTime()){
			builder.setAttackId(attackId);
			builder.setAttackCloseTime(RedisService.today(24));
			builder.setAttackEndTime(builder.getAttackCloseTime()+300);
			Union.Builder defendUnion = redis.getUnion(user.getServerId(), builder.getAttackId());
			defendUnion.setDefendId(builder.getId());
			defendUnion.setDefendCloseTime(RedisService.today(24));
			builder.setDefendEndTime(builder.getDefendCloseTime()+300);
			if(redis.setLock("Union_"+builder.getId()) 
				&& redis.setLock("Union_"+defendUnion.getId()))
			{
				redis.saveUnion(builder.build(), user);
				redis.saveUnion(defendUnion.build(), user);
				redis.saveFightKey(builder.getId(), defendUnion.getId(), user.getServerId());
				redis.clearLock("Union_"+builder.getId());
				redis.clearLock("Union_"+defendUnion.getId());
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
			if(RedisService.now() > union.getDefendCloseTime()){
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
				redis.waitLock("Union_"+attackUnionId, 60);
				redis.waitLock("Union_"+defendUnionId, 60);
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
	
	public void costUnionBossActivity(UserBean user, int targetId, long count) {
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
			targetCount = count;
		}
		json.put("" + targetId, targetCount);
		union.setKillMonsterRecord(json.toString());
		
		unionMapper.updateUnion(new UnionBean(union));
	}
	public void updateCostRecord(Union.Builder union, int targetId, int count) {
		int targetCount = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getCostRecord());
			targetCount = json.getInt("" + targetId) + count;	
		} catch (Exception e) {
			targetCount = count;
		}
		json.put("" + targetId, targetCount);
		union.setCostRecord(json.toString());
		
		unionMapper.updateUnion(new UnionBean(union));
	}
	public void updateUnionBossRecord(Union.Builder union, int bossId) {
		int bossCount = 0;
		JSONObject json = new JSONObject();
		try {
			json = JSONObject.fromObject(union.getBossRecord());
			bossCount = json.getInt("" + bossId) + 1;	
		} catch (Exception e) {
			bossCount = 1;
		}
		json.put("" + bossId, bossCount);
		union.setBossRecord(json.toString());
		
		unionMapper.updateUnion(new UnionBean(union));
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
			json = new JSONObject();
			json.put("" + bossId, DateUtil.getCurrentDateString());
		}
		union.setBossEndTime(json.toString());
		
		unionMapper.updateUnion(new UnionBean(union));
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
	
	public void doUnionBossRecord(UserBean user, int type, int targetId, long count) {
		Union.Builder union = redis.getUnion(user);
		if (union == null)
			return;
		
		Map<Integer, UnionBoss> map = redis.getUnionBossConfig();
		Iterator<Entry<Integer, UnionBoss>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, UnionBoss> entry = it.next();
			UnionBoss unionBoss = entry.getValue();
			if (unionBoss.getHandbook() == 0)
				continue;
			if (unionBoss.getType() == type) {
				if (unionBoss.getTargetid() == targetId) {
					if (type == UnionConst.UNION_BOSS_TYPE_KILLMONSTER) {
						updateKillMonsterRecord(union, unionBoss.getId(), (int)count);
					} else if (type == UnionConst.UNION_BOSS_TYPE_COST) {
						updateCostRecord(union, unionBoss.getId(), (int)count);
					}
					if (unionBoss.getType() != UnionConst.UNION_BOSS_TYPE_UNDEAD)
						calUnionBossRefresh(union, unionBoss, user.getUnionId(), user.getServerId());
					if (redis.waitLock("Union_"+union.getId())){
						redis.saveUnion(union.build(), user);
						redis.clearLock("Union_"+union.getId());
					}
				}
			}
		}
	}
	
	public boolean calUnionBossRefresh(Union.Builder union, UnionBoss unionBoss, int unionId, int serverId) {
		if (unionBoss.getHandbook() == 0)
			return false;
		
		UnionBossRecord unionBossRecord = redis.getUnionBoss(union.getId(), unionBoss.getId());
		if (unionBossRecord != null && unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_UNDEAD) {
			return false;
		}
		if (unionBossRecord != null) {
			if (!DateUtil.timeIsOver(unionBossRecord.getEndTime()) && unionBossRecord.getPercent() < 10000)
				return false;
			if (!DateUtil.timeIsOver(DateUtil.getFutureHour(DateUtil.getDate(unionBossRecord.getStartTime()), unionBoss.getRefreshtime())))
				return false;
		}
		
//		int bossCount = getUnionBossCount(union, unionBoss.getId());
		JSONObject json = null;
		if (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_COST) {
			if (union.hasCostRecord() && !union.getCostRecord().isEmpty())
				json = JSONObject.fromObject(union.getCostRecord());
		} else if (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_KILLMONSTER) {
			if (union.hasKillMonsterRecord() && !union.getKillMonsterRecord().isEmpty())
				json = JSONObject.fromObject(union.getKillMonsterRecord());
		} 
		
		if ((unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_CRONTAB && canRefreshCrontabBoss(union, unionBoss, unionBossRecord) 
				|| (json != null && json.has("" + unionBoss.getId()) && json.getInt("" + unionBoss.getId()) >= unionBoss.getTargetcount()))
				|| (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_UNDEAD && union.getMaxZhanli() >= unionBoss.getTargetcount())) {
			updateUnionBossRecord(union, unionBoss.getId());
			if (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_KILLMONSTER) {
				updateKillMonsterRecord(union, unionBoss.getId(), -unionBoss.getTargetcount());
			} else if (unionBoss.getType() == UnionConst.UNION_BOSS_TYPE_COST) {
				updateCostRecord(union, unionBoss.getId(), -unionBoss.getTargetcount());
			}
			redis.delUnionBossRankKey(union.getId(), unionBoss.getId());
			unionBossRecord = redis.saveUnionBoss(union, unionBoss);
			
			List<UserInfo> members = redis.getMembers(unionId, serverId);
			for (UserInfo info : members) {
				UserRankBean userRank = new UserRankBean();
				userRank.initByUserCache(info);
				redis.addUnionBossAttackRank(userRank, unionBossRecord, union.getId());
			}
			return true;
		}
		
		return false;	
	}
	
	public List<UnionBossRecord> getUnionBossList(UserBean user, Union.Builder union) {
		Map<Integer, UnionBoss> map = redis.getUnionBossConfig();
		Iterator<Entry<Integer, UnionBoss>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, UnionBoss> entry = it.next();
			if (calUnionBossRefresh(union, entry.getValue(), user.getUnionId(), user.getServerId()))
				redis.saveUnion(union.build(), user);
		}
		
		List<UnionBossRecord> builderList = new ArrayList<UnionBossRecord>();
		List<UnionBossRecord> unionBossList = redis.getUnionBossList(user.getUnionId());
		for (UnionBossRecord unionBoss : unionBossList) {
			UnionBossRecord.Builder builder = UnionBossRecord.newBuilder(unionBoss);
			List<UserRankBean> ranks = getUnionBossRankList(user.getUnionId(), unionBoss.getBossId(), user.getServerId());
			builder.addAllRanks(UserRankBean.buildUserRankList(ranks));
			builderList.add(builder.build());
		}
		return builderList;
	}
	
	public UnionBossRecord attackUnionBoss(UserBean user, Union.Builder union, int bossId, long hp, int percent, MultiReward.Builder rewards) {
		UnionBossRecord.Builder unionBossRecord = UnionBossRecord.newBuilder(redis.getUnionBoss(user.getUnionId(), bossId));
		if (DateUtil.timeIsOver(unionBossRecord.getEndTime())) {
			unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_BOSS_IS_END_VALUE);
			return unionBossRecord.build();
		}
		
		UnionBoss unionBoss = redis.getUnionBoss(bossId);
		if (unionBoss.getType() == 4 && union.getMaxZhanli() < unionBoss.getTargetcount()) {
			unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_ZHANLI_NOT_ENOUGH_VALUE);
			return unionBossRecord.build();
		}
		Map<String, Integer> unionBossMap = user.getUnionBossMap();
		if (unionBossMap.get("" + bossId) != null && unionBossMap.get("" + bossId) >= unionBoss.getCount()) {
			unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_BOSS_USER_HAS_NOT_TIMES_VALUE);
			return unionBossRecord.build();
		}
			
		logService.sendUnionbossLog(user.getServerId(), user.getId(), union.getId(), bossId, percent);
		
//		if (unionBoss.getEnemygroup().getHpbar() == -1 || unionBossRecord.getPercent() < 10000) {
//			UserRankBean userRankBean = redis.getUserRank(union.getId(), bossId, user.getId());
//			if (userRankBean == null)
//				userRankBean = new UserRankBean(user);
//			userRankBean.setDps(userRankBean.getDps() + hp);
//			redis.addUnionBossAttackRank(userRankBean, unionBossRecord.build(), union.getId());
//		} else {
		if (unionBoss.getEnemygroup().getHpbar() != -1 && unionBossRecord.getPercent() >= 10000) {
			return null;//怪物已逃跑
		}
		
		if (unionBoss.getEnemygroup().getHpbar() != -1 && unionBossRecord.getPercent() < 10000 && unionBossRecord.getPercent() + percent >= 10000) {
			if (!redis.setLock("UnionBoss_" + union.getId() + ":" + bossId, 10)) {
				unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_BOSS_IS_BEING_FIGHT_VALUE);
				return unionBossRecord.build();
			}
			if(!redis.waitLock("Union_"+union.getId())) {
				unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_BOSS_IS_BEING_FIGHT_VALUE);
				return unionBossRecord.build();
			}
			UserRankBean userRankBean = redis.getUserRank(union.getId(), bossId, user.getId());
			if (userRankBean == null)
				userRankBean = new UserRankBean(user);
			userRankBean.setDps(userRankBean.getDps() + hp);
			redis.addUnionBossAttackRank(userRankBean, unionBossRecord.build(), union.getId());
			unionBossRecord.setPercent(unionBossRecord.getPercent() + percent);
			doUnionBossRankReward(user.getUnionId(), bossId, user.getServerId());
			updateUnionBossEndTime(union, bossId);
			calUnionBossRefresh(union, redis.getUnionBoss(bossId), user.getUnionId(), user.getServerId());
			redis.saveUnion(union.build(), user);
			redis.clearLock("Union_"+union.getId());
			redis.clearLock("UnionBoss_" + union.getId() + ":" + bossId);
			/**
			 * 累计击败工会boss的活动
			 */
			Set<String> userIds = redis.getMemberIds(union.getId());
			for (String userIdStr : userIds) {
				activityService.handleUnionBoss(TypeTranslatedUtil.stringToLong(userIdStr), bossId);
			}
		} else {
			unionBossRecord.setPercent(unionBossRecord.getPercent() + percent);
			UserRankBean userRankBean = redis.getUserRank(union.getId(), bossId, user.getId());
			if (userRankBean == null)
				userRankBean = new UserRankBean(user);
			userRankBean.setDps(userRankBean.getDps() + hp);
			redis.addUnionBossAttackRank(userRankBean, unionBossRecord.build(), union.getId());
		}
		
		for(RewardInfo reward : unionBoss.getLootlistList()) {
			if(reward.getWeight() > RedisService.nextInt(100))
				rewards.addLoot(reward);
		}
		
		unionBossMap.put("" + bossId, unionBossMap.get("" + bossId) == null ? 1 : unionBossMap.get("" + bossId) + 1);
		user.setUnionBossMap(unionBossMap);
		userService.updateUser(user);
		
		UnionBossUserRecord.Builder builder = UnionBossUserRecord.newBuilder();
		builder.setUserId(user.getId());
		for (int i = 0; i < unionBossRecord.getUserRecordCount(); ++i) {
			UnionBossUserRecord record = unionBossRecord.getUserRecord(i);
			if (record.getUserId() == user.getId()) {
				builder = UnionBossUserRecord.newBuilder(record);
				unionBossRecord.removeUserRecord(i);
				break;
			}
		}
		builder.setPercent(builder.getPercent() + percent);
		unionBossRecord.addUserRecord(builder.build());
		
		redis.saveUnionBoss(user.getUnionId(), unionBossRecord.build());
		List<UserRankBean> ranks = getUnionBossRankList(user.getUnionId(), bossId, user.getServerId());
		unionBossRecord.addAllRanks(UserRankBean.buildUserRankList(ranks));
		
		return unionBossRecord.build();
	}
	
	private void doUnionBossRankReward(int unionId, int bossId, int serverId) {
		UnionBosswin unionBosswin = redis.getUnionBosswin(bossId);
		List<RankItem> rankList = unionBosswin.getRankList();
		List<UserRankBean> userRankList = getUnionBossRankList(unionId, bossId, serverId);
		for (RankItem item : rankList) {
			List<RewardInfo> rewardList = item.getRewardList();
			for (int i = item.getRank() - 1; i < item.getRank1(); ++ i) {
				if (i >= userRankList.size())
					return;
				UserRankBean userRank = userRankList.get(i);
				if (userRank != null) {
					UserBean user = userService.getUserOther(userRank.getUserId());
					if (user == null || user.getUnionId() != unionId)
						continue;
					MailBean mail = MailBean.buildSystemMail(userRank.getUserId(), item.getDes(), rewardList);
					log.debug("unionboss rank mail is:" + mail.toJson());
					mailService.addMail(mail);
				}
			}
//			Set<TypedTuple<String>> ranks = redis.getUnionBossRankList(unionId, bossId, item.getRank() - 1, item.getRank1() - 1);
//			for (TypedTuple<String> rank : ranks) {
//				MailBean mail = MailBean.buildSystemMail(TypeTranslatedUtil.stringToLong(rank.getValue()), item.getDes(), getRankRewardList(item));
//				log.debug("unionboss rank mail is:" + mail.toJson());
//				mailService.addMail(mail);
//			}
		}
	}
	
	public void doUndeadUnionBossRankReward(int serverId) {//type == 4
		List<Union> unions = redis.getBaseUnions(serverId);
		Map<Integer, UnionBoss> bossMap = redis.getUnionBossConfig();
		for (UnionBoss unionBoss : bossMap.values()) {
			if (unionBoss.getType() != 4)
				continue;
			
			for (Union union : unions) {
				/**
				 * 累计击败工会boss的活动
				 */
				Set<String> userIds = redis.getMemberIds(union.getId());
				for (String userIdStr : userIds) {
					activityService.handleUnionBoss(TypeTranslatedUtil.stringToLong(userIdStr), unionBoss.getId());
				}
				doUnionBossRankReward(union.getId(), unionBoss.getId(), serverId);
				redis.delUnionBoss(union.getId(), unionBoss.getId());
				Union.Builder builder = Union.newBuilder(union);
				if (calUnionBossRefresh(builder, unionBoss, union.getId(), serverId))
					redis.saveUnion(builder.build(), serverId);
			}
		}
		
	}
	
//	private List<RewardInfo> calUnionBossReward(int bossId) {
//		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
//		UnionBossloot bossloot = redis.getUnionBossloot(bossId);
//		List<UnionBosslootItem> itemList = bossloot.getItemList();
//		for (UnionBosslootItem item : itemList) {
//			int randomWeight = RandomUtils.nextInt(item.getWeightall());
//			if (randomWeight < item.getWeight1()) {
//				rewardList.add(RewardBean.init(item.getItemid1(), item.getCount1()).buildRewardInfo());
//				continue;
//			}
//			
//			if (randomWeight < item.getWeight2()) {
//				rewardList.add(RewardBean.init(item.getItemid2(), item.getCount2()).buildRewardInfo());
//				continue;
//			}
//			
//			if (randomWeight < item.getWeight3()) {
//				rewardList.add(RewardBean.init(item.getItemid3(), item.getCount3()).buildRewardInfo());
//				continue;
//			}
//		}
//		
//		return rewardList;
//	}
	
//	private List<UserRankBean> getUnionBossRankList(UserBean user, int bossId) {
//		Set<TypedTuple<String>> ranks = redis.getUnionBossRanks(user.getUnionId(), bossId, RankConst.UNIONBOSS_RANK_LIST_START, RankConst.UNIONBOSS_RANK_LIST_END);
//		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
//		int rankInit = 1;
//		for (TypedTuple<String> rank : ranks) {
//			UserRankBean userRank = new UserRankBean();
//			UserInfo userInfo = userService.getCache(user.getServerId(), TypeTranslatedUtil.stringToLong(rank.getValue()));
//			
//			userRank.setRank(rankInit);
//			userRank.initByUserCache(userInfo);
//			userRank.setZhanli(rank.getScore().intValue());
//			
//			rankList.add(userRank);
//			rankInit++;
//		}
//		
//		return rankList;
//	}
	
	private List<UserRankBean> getUnionBossRankList(int unionId, int bossId, int serverId) {
		Map<String, String> map = redis.getUnionBossRanks(unionId, bossId);
		List<UserRankBean> rankList = new ArrayList<UserRankBean>();
		Iterator<Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			UserRankBean userRankBean = UserRankBean.fromJson(entry.getValue());
			if (userRankBean != null) {
				UserInfo userInfo = userService.getCache(serverId, userRankBean.getUserId());
				if (userInfo != null)
					userRankBean.initByUserCache(userInfo);
				rankList.add(userRankBean);
			}
		}
		
		Collections.sort(rankList, comparator);
		
		return rankList;
	}
	
//	private boolean canRefreshCrontabBoss(Union.Builder union, UnionBoss boss, UnionBossRecord unionBossRecord) {
//		Date current = DateUtil.getDate();
//		Date last = DateUtil.getDate(getUnionBossEndTime(union, boss.getId()));
//		Date bossTime = DateUtil.setToDayTime(current, boss.getTargetcount());
//		
//		if (unionBossRecord != null) {
//			Date bossStartTime = DateUtil.getFutureHour(DateUtil.getDate(unionBossRecord.getStartTime()), boss.getRefreshtime());
////			String bossEnd = getUnionBossEndTime(union, boss.getId());
////			if (!bossEnd.isEmpty()) {
////				if (DateUtil.getDate(bossEnd).after(bossStartTime)) {
////					return false;
////				}
////			}
//			if (last != null && last.after(bossStartTime))
//				return false;
//			
//			if (!bossStartTime.after(bossTime) && current.after(bossTime))
//				return true;
//			
//			Date bossEndTime = DateUtil.getFutureDay(DateUtil.getDate(unionBossRecord.getEndTime()), 1);
//			if (current.before(bossEndTime) && current.after(bossStartTime))
//				return true;
//		}
//		if (current.before(bossTime))
//			return false;
//		
//		if (last == null)
//			return true;
//		
//		if (DateUtil.intervalDays(current, last) >= 1)
//			return true;
//		
//		return false;
//	}
	
	private boolean canRefreshCrontabBoss(Union.Builder union, UnionBoss boss, UnionBossRecord unionBossRecord) {
		Date last = DateUtil.getDate(getUnionBossEndTime(union, boss.getId()));
		if (last == null)
			return true;
		
		if (unionBossRecord == null)
			return true;
		
		Date current = DateUtil.getDate();
		
		Date date = DateUtil.getFutureHour(DateUtil.setToDayStartTime(current), boss.getTargetcount() + boss.getLasttime());;
		Date startDate = DateUtil.getFutureHour(DateUtil.setToDayStartTime(current), boss.getTargetcount());;
		if (current.before(startDate)) {
			startDate = DateUtil.getPastDay(startDate, 1);
			date = DateUtil.getPastDay(date, 1);
		}
		
		if (unionBossRecord != null) {
			if (last.after(startDate))
				return false;
	
			if (current.before(date) && current.after(startDate))
				return true;
		}
		
		return false;
	}
	
	private void calAreaResourceReward(UserBean user) {
		Map<Integer, AreaResource> resources = areaRedisService.getResources(user);
		for(AreaResource resource : resources.values()){
			if (!resource.hasOwner())
				continue;
			
			UserInfo userInfo = resource.getOwner();
			int unionId = 0;
			if (userInfo.hasUnionId())
				unionId = userInfo.getUnionId();
			
			
			AreaResource.Builder builder = AreaResource.newBuilder(resource);
			if (builder.getLastRewardTime() == 0)
				builder.setLastRewardTime(RedisService.now());
			int hours = DateUtil.intervalHours(builder.getLastRewardTime(), RedisService.now());
			builder.setLastRewardTime(builder.getLastRewardTime() + hours * TimeConst.SECONDS_PER_HOUR);
			areaRedisService.saveResource(builder.build(), user.getServerId());
			if (unionId == 0) {
				rewardService.doReward(userInfo.getId(), RewardConst.UNIONCOIN, resource.getYield() * hours);
				continue;
			}
			
			List<UserInfo> members = redis.getMembers(unionId, user.getServerId());
			for (UserInfo cache : members) {
				rewardService.doReward(cache.getId(), RewardConst.UNIONCOIN, resource.getYield() * hours);
			}
		}
	}
	
	public long calLootExp(UserBean user, long addExp) {
		if (user.getUnionId() <= 0)
			return addExp;
		
		Union.Builder union = getBaseUnion(user);
		if (union == null)
			return addExp;
		
		UnionExp unionExp = redis.getUnionExp(union.getLevel());
		if (unionExp == null)
			return addExp;
		
		return (long) (addExp * (1.0 * unionExp.getLootexp() / 100));
	}
	
	public void addUnionExp(UserBean user, int exp) {
		Union.Builder union = getBaseUnion(user);
		if (union == null)
			return;
		
		union.setExp(union.getExp() + exp);
		calUnionLevel(union);
		
		redis.saveUnion(union.build(), user);
		unionMapper.updateUnion(new UnionBean(union));
	}
	
	private void calUnionLevel(Union.Builder union) {
		UnionExp unionExp = redis.getUnionExp(union.getLevel() + 1);
		if (unionExp == null)
			return;
		
		if (union.getExp() >= unionExp.getExp()) {
			union.setLevel(union.getLevel() + 1);
			union.setExp(union.getExp() - unionExp.getExp());
			union.setMaxCount(unionExp.getUnionsize());
		}
	}
}
