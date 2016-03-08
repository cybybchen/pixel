package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.hero.info.HeroInfoBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UserInfo;
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
			if(System.currentTimeMillis()%(24*3600L*1000L) >= 20.5*3600L*1000L){//进攻结算
				bloodFight(union.getId(), union.getAttackId(), user.getServerId());
			}else{
				List<UserInfo> users = unionRedisService.getFightQueue(union.getId(), union.getAttackId());
				builder.addAllAttacks(users);
			}
		}
		if(union.hasDefendId()){
			if(System.currentTimeMillis()%(24*3600L*1000L) >= 20.5*3600L*1000L){//防守结算
				bloodFight(union.getDefendId(), union.getId(), user.getServerId());
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
		if(!unionRedisService.setLock(unionRedisService.getUnionFightKey(attackUnionId, defendUnionId)))
			return;
		Union.Builder attackUnion = Union.newBuilder();
		Union.Builder defendUnion = Union.newBuilder();
		if(unionRedisService.getBaseUnion(attackUnion, attackUnionId, serverId)){
			attackUnion.clearAttackId();
		}
		if(unionRedisService.getBaseUnion(defendUnion, defendUnionId, serverId)){
			defendUnion.clearDefendId();
		}
		List<UserInfo> users = unionRedisService.getFightQueue(attackUnionId, defendUnionId);
		List<UserInfo> attacks = new ArrayList<UserInfo>();
		List<UserInfo> defends = new ArrayList<UserInfo>();
		for(UserInfo user : users){
			if(user.getUnionId() == defendUnionId){
				defends.add(user);
			}else{
				attacks.add(user);
			}
		}
		if(queueFight(attacks, defends)){
			attackUnion.setPoint(attackUnion.getPoint()+defendUnion.getPoint()/5);
			defendUnion.setPoint(defendUnion.getPoint()*4/5);
		}
		unionRedisService.saveUnion(attackUnion.build(), serverId);
		unionRedisService.saveUnion(defendUnion.build(), serverId);
		unionMapper.updateUnion(new UnionBean(attackUnion.build()));
		unionMapper.updateUnion(new UnionBean(defendUnion.build()));
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
	
	public void upgrade(UserBean user) {
		Union.Builder builder = Union.newBuilder();
		unionRedisService.getBaseUnion(builder, user.getUnionId(), user.getServerId());
		builder.setLevel(builder.getLevel()+1);
		unionRedisService.saveUnion(builder.build(), user);
		unionMapper.updateUnion(new UnionBean(builder.build()));
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
	
	public boolean attack(int attackId, int teamid, UserBean user){
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
			defendUnion.setDefendId(builder.getId());
			if(unionRedisService.setLock("Union_"+builder.getId()) 
				&& unionRedisService.setLock("Union_"+defendUnion.getId()))
			{
				unionRedisService.saveUnion(builder.build(), user);
				unionRedisService.saveUnion(defendUnion.build(), user);
			}else{
				return false;
			}
		}
		return true;
	}
	
	public void defend(int teamid, UserBean user){
		Union.Builder union = Union.newBuilder();
		unionRedisService.getBaseUnion(union, user.getUnionId(), user.getServerId());
		if(union.hasDefendId()){
			List<HeroInfoBean> herolist = userTeamService.getTeam(user, teamid);
			userTeamService.saveTeamCache(user, herolist);
			unionRedisService.defend(union.getDefendId(), user);
		}
	}
}
