package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.AreaResource;
import com.trans.pixel.protoc.Commands.FightResult;
import com.trans.pixel.protoc.Commands.FightResultList;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.Team;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.AreaRedisService;
import com.trans.pixel.service.redis.UnionRedisService;

@Service
public class UnionService extends FightService{

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
	
	public Union getUnion(UserBean user) {
		if(user.getUnionId() == 0)
			return null;
		Union union = redis.getUnion(user);
		List<UserInfo> members = redis.getMembers(user);
		if(union == null && user.getUnionId() != 0){//load union from db
			UnionBean unionbean = unionMapper.selectUnionById(user.getUnionId());
			redis.saveUnion(unionbean.build(), user);
			union = redis.getUnion(user);
			if(union != null && union.getMembersCount() == 0){//load members from db
				members.clear();
				List<UserBean> beans = userService.getUserByUnionId(user.getUnionId());
				Union.Builder builder = Union.newBuilder(union);
				List<Long> memberIds = new ArrayList<Long>();
				for(UserBean bean : beans){
					memberIds.add(bean.getId());
					members.add(bean.buildShort());
				}
				redis.saveMembers(memberIds, user);
				union = builder.build();
			}
		}
		if(union == null)
			return null;
		Union.Builder builder = Union.newBuilder(union);
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
		if(zhanli != builder.getZhanli()){
			builder.setZhanli(zhanli);
			needupdate = true;
		}
		if(builder.getCount() != members.size()){
			builder.setCount(members.size());
			needupdate = true;
		}
		if(union.hasAttackId() && builder.getAttackCloseTime() < redis.now()){
			builder.clearAttackId();
			builder.clearAttackEndTime();
			builder.clearAttackCloseTime();
			needupdate = true;
		}
		if(union.hasDefendId() && builder.getDefendCloseTime() < redis.now()){
			builder.clearDefendId();
			builder.clearDefendCloseTime();
			builder.clearDefendEndTime();
			needupdate = true;
		}
		if(needupdate && redis.setLock("Union_"+builder.getId()))
			redis.saveUnion(builder.build(), user);

		if(union.hasAttackId()){
			List<UserInfo> users = redis.getFightQueue(union.getId(), union.getAttackId());
			builder.addAllAttacks(users);
		}
		if(union.hasDefendId()){
	 		List<UserInfo> users = redis.getFightQueue(union.getDefendId(), union.getId());
	 		builder.addAllDefends(users);
		}
		builder.addAllMembers(members);
		if(user.getUnionJob() > 0){
			builder.addAllApplies(redis.getApplies(user.getUnionId()));
		}
		return builder.build();
	}
	
	/**
	 * 血战
	 */
	public void bloodFight(int attackUnionId, int defendUnionId, int serverId){
		Union.Builder attackUnion = Union.newBuilder();
		Union.Builder defendUnion = Union.newBuilder();
		boolean hasattack = redis.getBaseUnion(attackUnion, attackUnionId, serverId);
		boolean hasdefend = redis.getBaseUnion(defendUnion, defendUnionId, serverId);
		
		if(hasattack && !hasdefend){
			attackUnion.clearAttackId();
			redis.saveUnion(attackUnion.build(), serverId);
			return;
		}else if(!hasattack && hasdefend){
			defendUnion.clearDefendId();
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
			unionMapper.updateUnion(new UnionBean(attackUnion.build()));
			unionMapper.updateUnion(new UnionBean(defendUnion.build()));
		}
		redis.saveUnion(attackUnion.build(), serverId);
		redis.saveUnion(defendUnion.build(), serverId);
		
		/**
		 * 工会血战的活动
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
		user.setUnionJob(3);//会长
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
		if(isAreaFighting(id, user))
			return ErrorConst.AREA_FIGHT_BUSY;
		if(isBloodFighting(id, user))
			return ErrorConst.BLOOD_FIGHT_BUSY;
		if(id == user.getId()){
			if(user.getUnionJob() == 3){
				if(delete(user))
					return SuccessConst.DELETE_UNION_SUCCESS;
				else
					return ErrorConst.UNIONLEADER_QUIT;
			}
			redis.quit(id, user);
			user.setUnionId(0);
			user.setUnionJob(0);
			userService.updateUser(user);
			userService.cache(user.getServerId(), user.buildShort());
			return SuccessConst.QUIT_UNION_SUCCESS;
		}else{
			if(user.getUnionJob() < 2)
				return ErrorConst.PERMISSION_DENIED;
			UserBean bean = userService.getOther(id);
			if(bean.getUnionJob() >= 1 && user.getUnionJob() < 3)
				return ErrorConst.PERMISSION_DENIED;
			redis.quit(id, user);
			bean.setUnionId(0);
			bean.setUnionJob(0);
			userService.updateUser(bean);
			userService.cache(user.getServerId(), bean.buildShort());
			return SuccessConst.QUIT_UNION_MEMBER_SUCCESS;
		}
		
		
	}
	
	public boolean delete(UserBean user) {
		if(user.getUnionJob() == 3 && redis.getNumOfMembers(user) <= 1){
			redis.deleteMembers(user);
			redis.deleteUnion(user);
			unionMapper.deleteUnion(user.getUnionId());
			user.setUnionId(0);
			user.setUnionJob(0);
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
	
	public boolean isAreaFighting(long userId, UserBean user){
		Map<String, AreaResource> resources = areaRedisService.getResources(user);
		for(AreaResource resource : resources.values()){
			if(resource.getState() != 0 && resource.hasOwner() && resource.getOwner().getId() == userId)
				return true;
			for(UserInfo userinfo : resource.getAttacksList()){
				if(userinfo.getId() == userId)
					return true;
			}
			for(UserInfo userinfo : resource.getDefensesList()){
				if(userinfo.getId() == userId)
					return true;
			}
		}
		return false;
	}
	
	public boolean isBloodFighting(long userId, UserBean user){
		if(user.getUnionId() == 0)
			return false;
		Union.Builder builder = Union.newBuilder();
		redis.getBaseUnion(builder, user.getUnionId(), user.getServerId());
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
				if(isAreaFighting(userId, user))
					return ErrorConst.AREA_FIGHT_BUSY;
				Union.Builder builder = Union.newBuilder();
				redis.getBaseUnion(builder, user.getUnionId(), user.getServerId());
				if(builder.getCount() >= builder.getMaxCount())
					return ErrorConst.UNION_FULL;
				bean.setUnionId(user.getUnionId());
				bean.setUnionJob(0);
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
		Union.Builder builder = Union.newBuilder();
		redis.getBaseUnion(builder, user.getUnionId(), user.getServerId());
		if(!redis.setLock("Union_"+user.getUnionId()))
			return ErrorConst.ERROR_LOCKED;
		builder.setLevel(builder.getLevel()+1);
		redis.saveUnion(builder.build(), user);
		unionMapper.updateUnion(new UnionBean(builder.build()));
		return SuccessConst.UPGRADE_UNION_SUCCESS;
	}
	
	public ResultConst handleMember(long id, int job, UserBean user) {
		if(user.getUnionJob() < 3)
			return ErrorConst.PERMISSION_DENIED;
		if(user.getId() == id)
			return ErrorConst.PERMISSION_DENIED;
		int vicecount = 0;
		int eldercount = 0;
		if(job == 2 || job == 1){
			List<UserInfo> members = redis.getMembers(user);
			for(UserInfo member : members){
				if(member.getUnionJob() == 2)
					vicecount++;
				else if(member.getUnionJob() == 1)
					eldercount++;
			}
			if(job == 2 && vicecount >= 1)
				return ErrorConst.UNION_VICE_FULL;
			if(job == 1 && eldercount >= 4)
				return ErrorConst.UNION_ELDER_FULL;
		}
		UserBean bean = userService.getOther(id);
		if(bean.getUnionId() == user.getUnionId()){
			if(job == 3){//会长转让
				user.setUnionJob(0);
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
		Union.Builder builder = Union.newBuilder();
		redis.getBaseUnion(builder, user.getUnionId(), user.getServerId());
		if(builder.hasAttackId()){
			if(redis.now() > builder.getAttackCloseTime()){
				return ErrorConst.JOIN_END;
			}
			Team team = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, teamid, team);
			redis.attack(builder.getAttackId(), user);
		}else if(user.getUnionJob() >= 2 && attackId != 0 && redis.now() > builder.getAttackCloseTime()){
			Union.Builder defendUnion = Union.newBuilder();
			builder.setAttackId(attackId);
			builder.setAttackCloseTime(redis.today(24));
			builder.setAttackEndTime(builder.getAttackCloseTime()+300);
			redis.getBaseUnion(defendUnion, builder.getAttackId(), user.getServerId());
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
		Union.Builder union = Union.newBuilder();
		redis.getBaseUnion(union, user.getUnionId(), user.getServerId());
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
}
