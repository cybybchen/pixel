package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.AreaProto.AreaResource;
import com.trans.pixel.protoc.AreaProto.FightResult;
import com.trans.pixel.protoc.AreaProto.FightResultList;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Base.RewardInfo;
import com.trans.pixel.protoc.Base.Team;
import com.trans.pixel.protoc.Base.UnionBossRecord;
import com.trans.pixel.protoc.Base.UnionBossRecord.UNIONBOSSSTATUS;
import com.trans.pixel.protoc.Base.UnionBossUserRecord;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.MessageBoardProto.Notice;
import com.trans.pixel.protoc.UnionProto.FIGHT_STATUS;
import com.trans.pixel.protoc.UnionProto.RankItem;
import com.trans.pixel.protoc.UnionProto.RequestUnionFightCommand.UNION_FIGHT_RET;
import com.trans.pixel.protoc.UnionProto.ResponseUnionFightApplyRecordCommand.UNION_FIGHT_STATUS;
import com.trans.pixel.protoc.UnionProto.Union;
import com.trans.pixel.protoc.UnionProto.UnionApply;
import com.trans.pixel.protoc.UnionProto.UnionBoss;
import com.trans.pixel.protoc.UnionProto.UnionBosswin;
import com.trans.pixel.protoc.UnionProto.UnionExp;
import com.trans.pixel.protoc.UnionProto.UnionFightRecord;
import com.trans.pixel.protoc.UnionProto.UnionFightRecord.EnemyRecord;
import com.trans.pixel.protoc.UnionProto.UnionFightReward;
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
	
	private static int UNION_FIGHT_PIPEI_STATUS = 0;
	
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

	Comparator<Union> unionComparator = new Comparator<Union>() {
		public int compare(Union union1, Union union2) {
			int dvalue = union2.getZhanli() - union1.getZhanli();
			if (dvalue == 0)
				return 1;
			
			return dvalue;
		}
	};
	
	public Union.Builder searchUnions(UserBean user, String name) {
		return redis.getUnionByName(user, name);
	}
	public List<Union> getRandUnions(UserBean user) {
		return redis.getRandUnions(user);
	}
//	public List<Union> getUnionsRank(UserBean user) {
//		return redis.getBaseUnions(user, 0);
//	}
	
	public List<Union> getUnionsByServerId(int serverId) {
		return redis.getUnionsRank(serverId);
	}
	public boolean canUseUnionName(int serverId, String name) {
		if(unionMapper.selectUnionByServerIdAndName(serverId, name) != null)
			return false;
		else 
			return redis.setLock("unionname"+serverId+"_"+name);
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
		if (union != null)
			union = redis.getUnionInfo(union);//补充额外信息
		
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
				union.setCount(members.size());
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
//		for(UserInfo member : members){
		for(int i = 0; i < members.size(); ++ i){
			UserInfo member = members.get(i);
			if (member.getUnionId() != union.getId()) {
				redis.quit(member.getId(), user);
				members.remove(member);
				--i;
				continue;
			}
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
		redis.updateUnionName(user.getServerId(), union);
		redis.updateUnionRank(user.getServerId(), union);

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
			
			if (!canQuitUnion(user.getId(), user.getUnionId(), user.getUnionJob())) {
				return ErrorConst.UNION_FIGHT_TIME_CAN_NOT_EQUI_ERROR;
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
			
			if (!canQuitUnion(id, user.getUnionId(), UnionConst.UNION_HUIZHONG)) {
				return ErrorConst.UNION_FIGHT_TIME_CAN_NOT_EQUI_ERROR;
			}
			
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
	
	public void apply(int unionId, UserBean user, String content) {
		if(user.getUnionId() != 0)
			return;
		redis.apply(unionId, user, content);
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
				if(!redis.waitLock(redis.getUnionMemberKey(user.getUnionId())))
					return ErrorConst.REDISKEY_BUSY_ERROR;
				if(redis.getNumOfMembers(user) >= builder.getMaxCount())
					return ErrorConst.UNION_FULL;
				bean.setUnionId(user.getUnionId());
				bean.setUnionName(builder.getName());
				bean.setUnionJob(UnionConst.UNION_HUIZHONG);
				userService.updateUser(bean);
				userService.cache(user.getServerId(), bean.buildShort());
				redis.saveMember(bean.getId(), user);
				redis.clearLock(redis.getUnionMemberKey(user.getUnionId()));
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
			if(!redis.waitLock(redis.getUnionMemberKey(user.getUnionId())))
				return ErrorConst.REDISKEY_BUSY_ERROR;
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
			redis.clearLock(redis.getUnionMemberKey(user.getUnionId()));
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
//			Team team = userTeamService.getTeam(user, teamid);
//			userTeamService.saveTeamCache(user, teamid, team);
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
//			Team team = userTeamService.getTeam(user, teamid);
//			userTeamService.saveTeamCache(user, teamid, team);
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
		
		redis.updateUnionValue(union.getId(), UnionBean.KILL_MONSTER_RECORD, union.getKillMonsterRecord());
		
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
		
		redis.updateUnionValue(union.getId(), UnionBean.COST_RECORD, union.getCostRecord());
		
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
		
		redis.updateUnionValue(union.getId(), UnionBean.BOSS_RECORD, union.getBossRecord());
		
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
		
		redis.updateUnionValue(union.getId(), UnionBean.BOSS_ENDTIME, union.getBossEndTime());
		
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
		Union.Builder union = getBaseUnion(user);
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
//					if (redis.waitLock("Union_"+union.getId())){
//						redis.saveUnion(union.build(), user);
//						redis.clearLock("Union_"+union.getId());
//					}
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
			if (calUnionBossRefresh(union, entry.getValue(), user.getUnionId(), user.getServerId())) {
//				if(redis.setLock("Union_"+union.getId())) {
//					redis.saveUnion(union.build(), user);
//					redis.clearLock("Union_"+union.getId());
//				}
			}
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
		
		if (!redis.setLock("UnionBoss_" + union.getId() + ":" + bossId, 10)) {
			unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_BOSS_IS_BEING_FIGHT_VALUE);
			return unionBossRecord.build();
		}
		if (unionBoss.getEnemygroup().getHpbar() != -1 && unionBossRecord.getPercent() < 10000 && unionBossRecord.getPercent() + percent >= 10000) {
//			if(!redis.waitLock("Union_"+union.getId())) {
//				unionBossRecord.setStatus(UNIONBOSSSTATUS.UNION_BOSS_IS_BEING_FIGHT_VALUE);
//				return unionBossRecord.build();
//			}
			UserRankBean userRankBean = redis.getUserRank(union.getId(), bossId, user.getId());
			if (userRankBean == null)
				userRankBean = new UserRankBean(user);
			userRankBean.setDps(userRankBean.getDps() + hp);
			redis.addUnionBossAttackRank(userRankBean, unionBossRecord.build(), union.getId());
			unionBossRecord.setPercent(unionBossRecord.getPercent() + percent);
			doUnionBossRankReward(user.getUnionId(), bossId, user.getServerId());
			updateUnionBossEndTime(union, bossId);
			calUnionBossRefresh(union, redis.getUnionBoss(bossId), user.getUnionId(), user.getServerId());
//			redis.saveUnion(union.build(), user);
//			redis.clearLock("Union_"+union.getId());
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
		redis.clearLock("UnionBoss_" + union.getId() + ":" + bossId);
		
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
					if (user == null || user.getUnionId() != unionId || !redis.canReward(user, bossId))
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
				if (calUnionBossRefresh(builder, unionBoss, union.getId(), serverId)) {
					redis.waitLock("Union_"+builder.getId());
					redis.saveUnion(builder.build(), serverId);
					redis.clearLock("Union_"+builder.getId());
				}
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
		if(redis.waitLock("Union_"+union.getId())) {
			redis.saveUnion(union.build(), user);
			redis.clearLock("Union_"+union.getId());
		}
		unionMapper.updateUnion(new UnionBean(union));
	}
	
	/**
	 * 下面是公会战内容
	 * @param user
	 */
	public void applyFight(UserBean user) {
		redis.applyFight(user);
		redis.addApplyUnion(user.getUnionId());
		
		UserTeamBean userTeam = userTeamService.getUserTeam(user.getId(), user.getCurrentTeamid());
		if (userTeam != null)
			updateApplyTeam(user, userTeam);
	}
	
	public void updateApplyTeam(UserBean user, UserTeamBean userTeam) {
		redis.updateApplyTeam(userTeam, user.getUnionId());
	}
	
	public UserTeamBean getUserUnionFightTeam(UserBean user) {
		return redis.getUserUnionFightTeam(user);
	}
	
	public <T> void handlerUnionFightTeamCache(T unionId) {
		if (redis.hasApplyTeamCache(unionId))
			return;
		
		Map<String, String> map = new HashMap<String, String>();
		List<UserTeamBean> userTeamList = redis.getUnionFightTeamList(unionId);
		for (UserTeamBean userTeam : userTeamList) {
			UserBean user = userService.getUserOther(userTeam.getUserId());
			map.put("" + userTeam.getUserId(), RedisService.formatJson(userTeamService.composeTeam(userTeam, user)));
		}
		redis.updateApplyTeamCache(unionId, map);
	}
	
	public Team getUnionFightTeamCache(long userId) {
		UserBean user = userService.getUserOther(userId);
		return redis.getApplyTeamCache(user);
	}
	
	public ResultConst handlerFightMembers(UserBean user, List<Long> userIds, FIGHT_STATUS fightStatus) {
		Map<String, String> map = new HashMap<String, String>();
		List<UnionFightRecord> applyList = getUnionFightApplyNotRefresh(user.getUnionId());
		int fightCount = 0;
		for (UnionFightRecord record : applyList) {
			if (record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT))
				fightCount++; 
			if (userIds.contains(record.getUser().getId())) {
				UnionFightRecord.Builder builder = UnionFightRecord.newBuilder(record);
				builder.setStatus(fightStatus);
				map.put("" + builder.getUser().getId(), RedisService.formatJson(builder.build()));
				if (record.getStatus().equals(FIGHT_STATUS.NOT_FIGHT) && builder.getStatus().equals(FIGHT_STATUS.CAN_FIGHT))
					fightCount++;
				if (record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT) && builder.getStatus().equals(FIGHT_STATUS.NOT_FIGHT))
					fightCount--;
				
				userIds.remove(record.getUser().getId());
			}
		}
		
		if (!userIds.isEmpty()) {
			return ErrorConst.USER_HAS_QUIT_ERROR;
		}
		
		if (fightCount > UnionConst.UNION_FIGHT_MEMBER_LIMIT)
			return ErrorConst.UNION_FIGHT_MEMBER_IS_LIMIT_ERROR;
		
		redis.updateApplyFight(user, map);
		
		return SuccessConst.HANDLER_SUCCESS;
	}
	
	public <T> List<UnionFightRecord> getUnionFightApply(T unionId) {
		List<UnionFightRecord> applyList = redis.getUnionFightApply(unionId);
		for (int i = 0; i < applyList.size(); ++i) {
			UnionFightRecord.Builder builder = UnionFightRecord.newBuilder(applyList.get(i));
			builder.setUser(userService.getCache(1, builder.getUser().getId()));
			applyList.set(i, builder.build());
		}
		
		UNION_FIGHT_STATUS status = calUnionFightStatus(0);
		if (!status.equals(UNION_FIGHT_STATUS.APPLY_TIME)
				&& !status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME)) {
			for (int i = 0; i < applyList.size(); ++i) {
				UnionFightRecord record = applyList.get(i);
				if (!record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT)) {
					applyList.remove(i);
					i--;
				}
			}
		}
		
		if (status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME))
			handlerUnionFightTeamCache(unionId);
		
		return applyList;
	}
	
	public <T> List<UnionFightRecord> getUnionFightEnemy(T unionId) {
		int enemyUnionId = redis.getEnemyUnionId(unionId);
		if (enemyUnionId == 0)
			return new ArrayList<UnionFightRecord>();
		
		return getUnionFightApply(enemyUnionId);
	}
	
	public <T> List<UnionFightRecord> getUnionFightApplyNotRefresh(T unionId) {
		return redis.getUnionFightApply(unionId);
	}
	
	public <T> Union getEnemyUnion(T unionId) {
		int enemyUnionId = redis.getEnemyUnionId(unionId);
		if (enemyUnionId == 0)
			return null;
		
		return redis.getUnion(1, enemyUnionId).build();
	}
	
	public ResultConst unionFight(UserBean user, long userId, UNION_FIGHT_RET ret, FightInfo fightinfo) {
		String lockKey = "unionfight:" + userId;
		if (!redis.setLock(lockKey, 4))
			return ErrorConst.USER_IS_BEING_ATTACKED_ERROR;
		
		String selfLockKey = "unionfight:" + user.getId();
		if (!redis.setLock(selfLockKey, 4))
			return ErrorConst.SELF_IS_BEING_ATTACKED_ERROR;
		
		UnionFightRecord record = redis.getUnionFightRecord(user.getUnionId(), user.getId());
		if (record == null)
			return ErrorConst.SELF_CAN_NOT_ATTACKED_ERROR;
		
		if (!record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT))
			return ErrorConst.SELF_CAN_NOT_ATTACKED_ERROR;
		
		if (record.getAttackCount() >= 2)
			return ErrorConst.USER_HAS_NO_ATTACK_TIMES_ERROR;
		
		int enemyUnionId = redis.getEnemyUnionId(user.getUnionId());
		if (enemyUnionId == 0 || enemyUnionId == user.getUnionId()) 
			return ErrorConst.UNION_FIGHT_TIME_IS_OVER_ERROR;
		
		UnionFightRecord otherRecord = redis.getUnionFightRecord(enemyUnionId, userId);
		if (otherRecord == null)
			return ErrorConst.USER_CAN_NOT_ATTACKED_ERROR;
		
		if (otherRecord.getEnemyRecordCount() >= 2)
			return ErrorConst.USER_HAS_NO_BEING_ATTACKED_TIMES_ERROR;
		
		UnionFightRecord.Builder recordBuilder = UnionFightRecord.newBuilder(record);
		recordBuilder.setAttackCount(recordBuilder.getAttackCount() + 1);
		
		UnionFightRecord.Builder otherBuilder = UnionFightRecord.newBuilder(otherRecord);
		EnemyRecord.Builder enemy = EnemyRecord.newBuilder();
		enemy.setEnemy(user.buildShort());
		enemy.setTime(otherBuilder.getEnemyRecordCount() + 1);
		switch (ret.getNumber()) {
			case UNION_FIGHT_RET.WIN_VALUE:
				enemy.setStar(3);
				break;
			case UNION_FIGHT_RET.LOSE_VALUE:
				enemy.setStar(0);
				break;
			case UNION_FIGHT_RET.DRAW_VALUE:
				enemy.setStar(1);
				break;
			default:
				enemy.setStar(0);
				break;
		}
		otherBuilder.addEnemyRecord(enemy.build());
		
		redis.updateApplyFight(user.getUnionId(), recordBuilder.build());
		redis.updateApplyFight(enemyUnionId, otherBuilder.build());
		
		redis.saveUnionFightFightInfo(enemyUnionId, otherBuilder.getUser().getId(), otherBuilder.getEnemyRecordCount(), fightinfo);
		
		redis.clearLock(lockKey);
		redis.clearLock(selfLockKey);
		
		return SuccessConst.ATTACK_SUCCESS;
	}
	
	public FightInfo viewUnionFightFightInfo(UserBean user, long userId, int time) {
		UserInfo other = userService.getCache(user.getServerId(), userId);
		if (other != null && other.getUnionId() == user.getUnionId())
			return redis.getFightInfo(user.getUnionId(), userId, time);
		
		int enemyUnionId = redis.getEnemyUnionId(user.getUnionId());
		
		return redis.getFightInfo(enemyUnionId, userId, time);
	}
	
	public void deleteLastRecord() {
		Set<String> unions = redis.getApplyUnionIds();
		for (String unionId : unions) {
			redis.delUnionFightFightInfo(unionId);
			redis.delUnionFightApply(unionId);
			redis.deleteApplyTeamCache(unionId);
		}
		redis.deleteApplyUnionRecord();
		redis.delUnionFightRewardRecord();
	}
	
	public void calUnionFight() {
		UNION_FIGHT_PIPEI_STATUS = 1;
		Set<String> unionIds = redis.getApplyUnionIds();
		for (String unionId : unionIds) {
			if (calFightMemberCount(unionId) < UnionConst.UNION_FIGHT_MEMBER_LIMIT) {//人数不够，无法参加
				redis.deleteApplyUnion(unionId);
				redis.renameUnionFightApply(unionId);
				redis.deleteApplyTeamCache(unionId);
			}
		}
		unionIds = redis.getApplyUnionIds();
		List<Union> unions = redis.getUnions(1, unionIds);
		for (int i = 0;i < unions.size(); ++i) {
			Union.Builder builder = Union.newBuilder(unions.get(i));
			builder.setZhanli(calUnionFightZhanli(builder.getId()));
			unions.set(i, builder.build());
		}
		log.debug("union size is:" + unions.size());
		Collections.sort(unions, unionComparator);
		for (int i = 0; i < unions.size(); i += 2) {
			if (i + 1 >= unions.size())
				continue;
			
			Union union = unions.get(i);
			Union nextUnion = unions.get(i + 1);
			
			redis.updateApplyUnion(union.getId(), nextUnion.getId());
			redis.updateApplyUnion(nextUnion.getId(), union.getId());
		}
		
		UNION_FIGHT_PIPEI_STATUS = 0;
	}
	
	private int calUnionFightZhanli(int unionId) {
		int zhanli = 0;
		List<UnionFightRecord> applyList = getUnionFightApply(unionId);
		for (UnionFightRecord record : applyList) {
			if (record.getStatus().equals(FIGHT_STATUS.NOT_FIGHT))
				continue;
			
			UserInfo user = userService.getCache(1, record.getUser().getId());
			zhanli += user.getZhanliMax();
		}
		
		return zhanli;
	}
	
	private int calFightMemberCount(String unionId) {
		List<UnionFightRecord> applyList = getUnionFightApplyNotRefresh(unionId);
		int fightCount = 0;
		for (UnionFightRecord record : applyList) {
			if (record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT))
				fightCount++; 
		}
		
		return fightCount;
	}
	
	public UNION_FIGHT_STATUS calUnionFightStatus(int unionId) {
		int cheatStatus = redis.getUnionFightCheatStatus();
		if (cheatStatus != 0) {
			switch (cheatStatus) {
				case 1:
					return UNION_FIGHT_STATUS.APPLY_TIME;
				case 2:
					return UNION_FIGHT_STATUS.HUIZHANG_TIME;
				case 3:
					return UNION_FIGHT_STATUS.PIPEI_TIME;
				case 4:
					if (unionId != 0 && !isInFightUnions(unionId))
						return UNION_FIGHT_STATUS.NOT_IN_FIGHT_UNIONS;
					
					return UNION_FIGHT_STATUS.FIGHT_TIME;
				case 5:
					if (unionId == 0)
						return UNION_FIGHT_STATUS.SEND_REWARD_TIME;
					
					return UNION_FIGHT_STATUS.NO_TIME;
//				case 6:
//					return UNION_FIGHT_STATUS.NOT_IN_FIGHT_UNIONS;
				default:
					return UNION_FIGHT_STATUS.NO_TIME;
			}
		}
		UNION_FIGHT_STATUS status = UNION_FIGHT_STATUS.NO_TIME;
		int day = DateUtil.getDayOfWeek();
		log.error("current week day is:" + DateUtil.getDayOfWeek());
		if (day == UnionConst.FIGHT_APPLY_DAY)//周四
			status = UNION_FIGHT_STATUS.APPLY_TIME;
		else if (day == UnionConst.FIGHT_HUIZHANG_DAY)
			status = UNION_FIGHT_STATUS.HUIZHANG_TIME;
		else if (day == UnionConst.FIGHT_DAY) {
			status = UNION_FIGHT_STATUS.FIGHT_TIME;
			if (unionId != 0 && !isInFightUnions(unionId))
				status = UNION_FIGHT_STATUS.NOT_IN_FIGHT_UNIONS;
		} else if (unionId == 0 && day == UnionConst.FIGHT_SENDREWARD_DAY) {
			status = UNION_FIGHT_STATUS.SEND_REWARD_TIME;
		}
		
		if (status.equals(UNION_FIGHT_STATUS.FIGHT_TIME) && (RedisService.now() - RedisService.today(0)) < 12 * 3600)
			status = UNION_FIGHT_STATUS.PIPEI_TIME;
			
		return status;
	}
	
	/**
	 * 公会战结算奖励
	 */
	public void sendUnionFightReward() {
		Map<String, String> unionMap = redis.getApplyUnionMap();
		Iterator<Entry<String, String>> it = unionMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			String unionId1 = entry.getKey();
			String unionId2 = entry.getValue();
			if (unionId2.isEmpty()) {
				sendUnionFightWinReward(unionId1);
				continue;
			}
			int winStar = winStar(unionId1, unionId2);
			if (winStar > 0) {
				sendUnionFightWinReward(unionId1);
				sendUnionFightLoseReward(unionId2);
			} else if (winStar < 0){
				sendUnionFightWinReward(unionId2);
				sendUnionFightLoseReward(unionId1);
			} else {
				sendUnionFightDrwaReward(unionId1);
				sendUnionFightDrwaReward(unionId2);
			}
			
		}
	}
	
	public boolean canQuitUnion(long userId, int unionId, int unionJob) {
		if (!isInFightUnions(unionId))
			return true;
		
		UNION_FIGHT_STATUS status = calUnionFightStatus(unionId);
		if (!status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME) && !status.equals(UNION_FIGHT_STATUS.FIGHT_TIME))
			return true;
		
		if (unionJob == UnionConst.UNION_HUIZHANG)
			return false;
		
		List<UnionFightRecord> applies = getUnionFightApplyNotRefresh(unionId);
		for (UnionFightRecord record : applies) {
			if (record.getUser().getId() == userId) {
				if (status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME))//会长选人阶段，报名的都不能退出公会
					return false;
				
				if (record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT))
					return false;
				
				break;
			}
		}
		
		return true;
	}
	
	public List<Notice> getUnionNotice(UserBean user) {
		List<Notice> noticeList = new ArrayList<Notice>();
		boolean isApply = false;
		boolean isAttack = false;
		int fightCount = 0;
		List<UnionFightRecord> applies = getUnionFightApplyNotRefresh(user.getUnionId());
		for (UnionFightRecord record : applies) {
			if (record.getUser().getId() == user.getId()) {
				isApply = true;
				if (record.getAttackCount() >= 2)
					isAttack = true;
			}
			
			if (record.getStatus().equals(FIGHT_STATUS.CAN_FIGHT))
				fightCount++;
		}
		
		UNION_FIGHT_STATUS status = calUnionFightStatus(user.getUnionId());
		if (status.equals(UNION_FIGHT_STATUS.APPLY_TIME) && !isApply) {
			Notice.Builder notice = Notice.newBuilder();
			notice.setType(NoticeConst.TYPE_FIGHTINFO_NOT_APPLY);
			noticeList.add(notice.build());
		}
		
		if (status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME) && fightCount < UnionConst.UNION_FIGHT_MEMBER_LIMIT
				&& fightCount < applies.size()) {
			Notice.Builder notice = Notice.newBuilder();
			notice.setType(NoticeConst.TYPE_FIGHTINFO_HUIZHANG_NOT_PICK);
			noticeList.add(notice.build());
		}
		
		if (status.equals(UNION_FIGHT_STATUS.FIGHT_TIME) && !isAttack) {
			Notice.Builder notice = Notice.newBuilder();
			notice.setType(NoticeConst.TYPE_FIGHTINFO_NOT_FIGHT);
			noticeList.add(notice.build());
		}
		
		return noticeList;
	}
	
	public int calCountTime() {
//		UNION_FIGHT_STATUS status = calUnionFightStatus(0);
//		if (status.equals(UNION_FIGHT_STATUS.APPLY_TIME)
//				|| status.equals(UNION_FIGHT_STATUS.HUIZHANG_TIME)
//				|| status.equals(UNION_FIGHT_STATUS.FIGHT_TIME))
//			return (int)(RedisService.nextDay(0) - RedisService.now());
//		
//		return DateUtil.intervalSeconds(DateUtil.getNextWeekDay(Calendar.THURSDAY), DateUtil.getDate());
		
		if (NEXT_TIME == 0)
			setUnionFightTime();
		return NEXT_TIME - RedisService.now();
	}
	
	public static int NEXT_TIME = 0;
	
	public void setUnionFightTime() {
		NEXT_TIME = RedisService.now() + 1 * 60;
	}
	
	private void sendUnionFightWinReward(String unionId) {
		if (redis.hasRewardUnionFight(unionId))
			return;
		
		List<RewardInfo> rewardList = buildUnionFightReward(true);
		List<UserInfo> members = redis.getMembers(TypeTranslatedUtil.stringToInt(unionId), 1);
		for (UserInfo user : members) {
			sendUnionFightMail(user, "恭喜您的公会获得公会战胜利！", rewardList);
		}
		redis.addUnionFightRewardUnionId(unionId);
	}
	
	private void sendUnionFightDrwaReward(String unionId) {
		if (redis.hasRewardUnionFight(unionId))
			return;
		
		List<RewardInfo> rewardList = buildUnionFightReward(true);
		List<UserInfo> members = redis.getMembers(TypeTranslatedUtil.stringToInt(unionId), 1);
		for (UserInfo user : members) {
			sendUnionFightMail(user, "很可惜您的公会在公会战中和对方战成了平手，请再接再厉！", rewardList);
		}
		redis.addUnionFightRewardUnionId(unionId);
	}
	
	private void sendUnionFightLoseReward(String unionId) {
		if (redis.hasRewardUnionFight(unionId))
			return;
		
		List<RewardInfo> rewardList = buildUnionFightReward(true);
		List<UserInfo> members = redis.getMembers(TypeTranslatedUtil.stringToInt(unionId), 1);
		for (UserInfo user : members) {
			sendUnionFightMail(user, "很可惜您的公会在公会战中输给了对手，请再接再厉！", rewardList);
		}
		redis.addUnionFightRewardUnionId(unionId);
	}
	
	private List<RewardInfo> buildUnionFightReward(boolean isWin) {
		UnionFightReward reward = redis.getUnionFightReward(1);
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		for (RewardInfo rewardinfo : reward.getRewardList()) {
			RewardInfo.Builder builder = RewardInfo.newBuilder(rewardinfo);
			if (isWin) 
				builder.setCount((int)(builder.getCount() * 1.5));
			
			rewardList.add(builder.build());
		}
		
		return rewardList;
	}
	
	private void sendUnionFightMail(UserInfo user, String content, List<RewardInfo> rewardList) {
//		String content = ret ? "恭喜您的公会获得公会战胜利！" : "很可惜您的公会在公会战中输给了对手，请再接再厉！";
		MailBean mail = MailBean.buildSystemMail(user.getId(), content, rewardList);
		mailService.addMail(mail);
	}
	
	private int winStar(String unionId, String enemyUnionId) {
		List<UnionFightRecord> applyList = getUnionFightApplyNotRefresh(enemyUnionId);
		int winStar = 0;
		int loseStar = 0;
		for (UnionFightRecord record : applyList) {
			for (EnemyRecord enemy : record.getEnemyRecordList())
				winStar += enemy.getStar();
		}
		
		List<UnionFightRecord> enemyApplyList = getUnionFightApplyNotRefresh(unionId);
		for (UnionFightRecord record : enemyApplyList) {
			for (EnemyRecord enemy : record.getEnemyRecordList())
				loseStar += enemy.getStar();
		}
		
		return winStar - loseStar;
//		if (winStar >= loseStar)
//			return true;
//		else 
//			return false;
	}
	
	private boolean isInFightUnions(int unionId) {
		return redis.isInFightUnions(unionId);
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
