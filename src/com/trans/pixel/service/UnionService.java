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
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.FightResult;
import com.trans.pixel.protoc.Commands.FightResultList;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.ServerRedisService;
import com.trans.pixel.service.redis.UnionRedisService;

@Service
public class UnionService extends FightService{

	@Resource
	private UnionRedisService unionRedisService;
	@Resource
	private MailService mailService;
	@Resource
	private UserTeamService userTeamService;
	@Resource
	private UnionMapper unionMapper;
	@Resource
	private UserService userService;
	@Resource
	private ServerRedisService serverRedisService;
	
	public List<Union> getBaseUnions(UserBean user) {
		return unionRedisService.getBaseUnions(user);
	}
	
	public Union getUnion(UserBean user) {
		if(user.getUnionId() == 0)
			return null;
		Union union = unionRedisService.getUnion(user);
		if(union == null && user.getUnionId() != 0){//load union from db
			List<UnionBean> unionbeans = unionMapper.selectUnionsByServerId(user.getServerId());
			List<Union> unions = new ArrayList<Union>();
			for(UnionBean bean : unionbeans){
				unions.add(bean.build());
			}
			unionRedisService.saveUnions(unions, user);
			union = unionRedisService.getUnion(user);
			if(union != null && union.getMembersCount() == 0){//load members from db
				List<UserBean> beans = userService.getUserByUnionId(user.getUnionId());
				List<UserInfo> members = new ArrayList<UserInfo>();
				for(UserBean bean : beans){
					members.add(bean.buildShort());
				}
				unionRedisService.saveMembers(members, user);
				Union.Builder builder = Union.newBuilder(union);
				builder.addAllMembers(members);
				union = builder.build();
			}
		}
		if(union == null)
			return null;
		Union.Builder builder = Union.newBuilder(union);
		
		if(union.hasAttackId()){
			if(builder.getAttackEndTime() < unionRedisService.now()){
				builder.clearAttackId();
				builder.clearAttackEndTime();
				if(unionRedisService.setLock("Union_"+builder.getId()))
					unionRedisService.saveUnion(builder.build(), user);
//			if(System.currentTimeMillis()%(24*3600L*1000L) >= 20.5*3600L*1000L){//进攻结算
//				 bloodFight(union.getId(), union.getAttackId(), user.getServerId());
			}else{
				List<UserInfo> users = unionRedisService.getFightQueue(union.getId(), union.getAttackId());
				builder.addAllAttacks(users);
			}
		}
		if(union.hasDefendId()){
			if(builder.getDefendEndTime() < unionRedisService.now()){
				builder.clearDefendId();
				builder.clearDefendEndTime();
				if(unionRedisService.setLock("Union_"+builder.getId()))
					unionRedisService.saveUnion(builder.build(), user);
//		 	if(System.currentTimeMillis()%(24*3600L*1000L) >= 20.5*3600L*1000L){//防守结算
//		 		bloodFight(union.getDefendId(), union.getId(), user.getServerId());
		 	}else{
		 		List<UserInfo> users = unionRedisService.getFightQueue(union.getDefendId(), union.getId());
		 		builder.addAllDefends(users);
		 	}
		}
		return builder.build();
	}
	
	/**
	 * 血战
	 */
	public void bloodFight(int attackUnionId, int defendUnionId, int serverId){
		Union.Builder attackUnion = Union.newBuilder();
		Union.Builder defendUnion = Union.newBuilder();
		boolean hasattack = unionRedisService.getBaseUnion(attackUnion, attackUnionId, serverId);
		boolean hasdefend = unionRedisService.getBaseUnion(defendUnion, defendUnionId, serverId);
		
		if(hasattack && !hasdefend){
			attackUnion.clearAttackId();
			unionRedisService.saveUnion(attackUnion.build(), serverId);
			return;
		}else if(!hasattack && hasdefend){
			defendUnion.clearDefendId();
			unionRedisService.saveUnion(defendUnion.build(), serverId);
			return;
		}else if(!hasattack && !hasdefend){
			return;
		}
		attackUnion.clearAttackId();
		defendUnion.clearDefendId();
		List<UserInfo> users = unionRedisService.getFightQueue(attackUnionId, defendUnionId);
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
				unionRedisService.saveFight(attackUnionId, defendUnionId, resultlist.build());
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
		unionRedisService.saveUnion(attackUnion.build(), serverId);
		unionRedisService.saveUnion(defendUnion.build(), serverId);
	}
	
	public Union create(int icon, String unionName, UserBean user) {
		if(user.getUnionId() != 0)
			return getUnion(user);
		UnionBean union = new UnionBean();
		union.setName(unionName);
		union.setIcon(icon);
		union.setServerId(user.getServerId());
		unionMapper.createUnion(union);
		unionRedisService.saveUnion(union.build(), user);
		user.setUnionId(union.getId());
		user.setUnionJob(3);//会长
		userService.updateUser(user);
		Union.Builder builder = Union.newBuilder(union.build());
		List<UserInfo> members = new ArrayList<UserInfo>();
		members.add(user.buildShort());
		unionRedisService.saveMembers(members, user);
		builder.addAllMembers(members);
		
		return builder.build();
	}
	
	public void quit(long id, UserBean user) {
		if(id == user.getId()){
			if(user.getUnionJob() == 3)
				return;
			user.setUnionId(0);
			user.setUnionJob(0);
			userService.updateUser(user);
			unionRedisService.quit(id, user);
		}else{
			if(user.getUnionJob() < 2)
				return;
			UserBean bean = userService.getUser(id);
			if(bean.getUnionJob() != 0 && user.getUnionJob() < 3)
				return;
			bean.setUnionId(0);
			bean.setUnionJob(0);
			userService.updateUser(bean);
			unionRedisService.quit(id, user);
		}
	}
	
	public void quit(UserBean user) {
		quit(user.getId(), user);
	}
	
	public boolean delete(UserBean user) {
		if(user.getUnionJob() == 3 && unionRedisService.getNumOfMembers(user) <= 1){
			unionRedisService.deleteMembers(user);
			unionRedisService.deleteUnion(user);
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
		unionRedisService.apply(unionId, user);
	}
	
	public boolean reply(List<Long> ids, boolean receive, UserBean user) {
		for(Long id : ids){
			unionRedisService.reply(id, receive, user);
			if(receive){
				UserBean bean = userService.getUser(id);
				if(bean != null && bean.getUnionId() == 0){
					bean.setUnionId(user.getUnionId());
					bean.setUnionJob(0);
					userService.updateUser(bean);
					unionRedisService.saveMember(bean.buildShort(), user);
				}
			}
		}
		
		return receive;
	}
	
	public ResultConst upgrade(UserBean user) {
		Union.Builder builder = Union.newBuilder();
		unionRedisService.getBaseUnion(builder, user.getUnionId(), user.getServerId());
		if(!unionRedisService.setLock("Union_"+user.getUnionId()))
			return ErrorConst.ERROR_LOCKED;
		builder.setLevel(builder.getLevel()+1);
		unionRedisService.saveUnion(builder.build(), user);
		unionMapper.updateUnion(new UnionBean(builder.build()));
		return SuccessConst.UPGRADE_UNION_SUCCESS;
	}
	
	public boolean handleMember(long id, int job, UserBean user) {
		if(user.getUnionJob() < 2 || id == user.getId())
			return false;
		if(user.getUnionJob() == 3 && job == 3){
			user.setUnionJob(0);
			unionRedisService.saveMember(user.buildShort(), user);
			userService.updateUser(user);
		}
		UserBean bean = userService.getUser(id);
		if(bean.getUnionId() == user.getUnionId()){
			bean.setUnionJob(job);
			unionRedisService.saveMember(bean.buildShort(), user);
			userService.updateUser(bean);
		}
		return true;
	}
	
	public ResultConst attack(int attackId, int teamid, UserBean user){
		Union.Builder builder = Union.newBuilder();
		unionRedisService.getBaseUnion(builder, user.getUnionId(), user.getServerId());
		if(builder.hasAttackId()){
			List<HeroInfoBean> herolist = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, herolist);
			unionRedisService.attack(builder.getAttackId(), user);
		}else if(user.getUnionJob() >= 2){
			Union.Builder defendUnion = Union.newBuilder();
			unionRedisService.getBaseUnion(defendUnion, builder.getAttackId(), user.getServerId());
			builder.setAttackId(attackId);
			builder.setAttackEndTime(unionRedisService.today(24));
			defendUnion.setDefendId(builder.getId());
			defendUnion.setDefendEndTime(unionRedisService.today(24));
			if(unionRedisService.setLock("Union_"+builder.getId()) 
				&& unionRedisService.setLock("Union_"+defendUnion.getId()))
			{
				unionRedisService.saveUnion(builder.build(), user);
				unionRedisService.saveUnion(defendUnion.build(), user);
				unionRedisService.saveFightKey(builder.getId(), defendUnion.getId(), user.getServerId());
			}else{
				return ErrorConst.ERROR_LOCKED;
			}
		}else{
			return ErrorConst.UNION_NOT_FIGHT;
		}
		return SuccessConst.UNION_FIGHT_SUCCESS;
	}
	
	public boolean defend(int teamid, UserBean user){
		Union.Builder union = Union.newBuilder();
		unionRedisService.getBaseUnion(union, user.getUnionId(), user.getServerId());
		if(union.hasDefendId()){
			List<HeroInfoBean> herolist = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, herolist);
			unionRedisService.defend(union.getDefendId(), user);
			return true;
		}else{
			return false;
		}
	}

	public void unionFightTask(){
		List<Integer> servers = serverRedisService.getServerIdList();
		for(Integer serverId : servers){
			Map<String, String> map = unionRedisService.getFightKeys(serverId);
			for(Entry<String, String> entry : map.entrySet()){
				int attackUnionId = Integer.parseInt(entry.getKey());
				int defendUnionId = Integer.parseInt(entry.getValue());
				try {
					while(!unionRedisService.setLock("Union_"+attackUnionId, 60))
							Thread.sleep(1);
					while(!unionRedisService.setLock("Union_"+defendUnionId, 60))
							Thread.sleep(1);
				} catch (InterruptedException e) {
					logger.debug(e);
					return;
				}
				bloodFight(attackUnionId, defendUnionId, serverId);
				unionRedisService.deleteFightKey(attackUnionId, serverId);
				unionRedisService.clearLock("Union_"+attackUnionId);
				unionRedisService.clearLock("Union_"+defendUnionId);
			}
		}
	}
}
