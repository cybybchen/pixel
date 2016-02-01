package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Union;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.redis.UnionRedisService;

@Service
public class UnionService {

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
	private UserBean user = null;
	public void setUserNX(UserBean user) {
		if(this.user != null)
			return;
		this.user = user;
		unionRedisService.setUser(user);
	}
	
	public List<Union> getBaseUnions() {
		return unionRedisService.getBaseUnions();
	}
	
	public Union getUnion() {
		Union union = unionRedisService.getUnion();
		if(union == null && user.getUnionId() != 0){//load from db
			List<UnionBean> unionbeans = unionMapper.selectUnionsByServerId(user.getServerId());
			List<Union> unions = new ArrayList<Union>();
			for(UnionBean bean : unionbeans){
				unions.add(bean.build());
			}
			unionRedisService.saveUnions(unions);
			union = unionRedisService.getUnion();
			if(union != null && union.getMembersCount() == 0){//load from db
				List<UserBean> beans = userService.getUserByUnionId(user.getUnionId());
				List<UserInfo> members = new ArrayList<UserInfo>();
				for(UserBean bean : beans){
					members.add(bean.buildShort());
				}
				unionRedisService.saveMembers(members);
				Union.Builder builder = Union.newBuilder(union);
				builder.addAllMembers(members);
				union = builder.build();
			}
		}
		return union;
	}
	
	public Union create(int icon, String unionName) {
		if(user.getUnionId() != 0)
			return getUnion();
		UnionBean union = new UnionBean();
		union.setName(unionName);
		union.setIcon(icon);
		union.setServerId(user.getServerId());
		unionMapper.createUnion(union);
		unionRedisService.saveUnion(union.build());
		user.setUnionId(union.getId());
		user.setUnionJob(3);//会长
		userService.updateUser(user);
		Union.Builder builder = Union.newBuilder(union.build());
		List<UserInfo> members = new ArrayList<UserInfo>();
		members.add(user.buildShort());
		unionRedisService.saveMembers(members);
		builder.addAllMembers(members);
		
		return builder.build();
	}
	
	public void quit(long id) {
		if(id == user.getId()){
			if(user.getUnionJob() == 3)
				return;
			user.setUnionId(0);
			user.setUnionJob(0);
			userService.updateUser(user);
			unionRedisService.quit(id);
		}else{
			if(user.getUnionJob() < 2)
				return;
			UserBean bean = userService.getUser(id);
			if(bean.getUnionJob() != 0 && user.getUnionJob() < 3)
				return;
			bean.setUnionId(0);
			bean.setUnionJob(0);
			userService.updateUser(bean);
			unionRedisService.quit(id);
		}
	}
	
	public void quit() {
		quit(user.getId());
	}
	
	public boolean delete() {
		return true;
	}
	
	public void apply(int unionId) {
		if(user.getUnionId() != 0)
			return;
		unionRedisService.apply(unionId);
	}
	
	public boolean reply(List<Long> ids, boolean receive) {
		for(Long id : ids){
			unionRedisService.reply(id, receive);
			if(receive){
				UserBean bean = userService.getUser(id);
				if(bean != null && bean.getUnionId() == 0){
					bean.setUnionId(user.getUnionId());
					bean.setUnionJob(0);
					userService.updateUser(bean);
					unionRedisService.saveMember(bean.buildShort());
				}
			}
		}
		
		return receive;
	}
	
	public void upgrade() {
		UnionBean bean = unionMapper.selectUnionById(user.getUnionId());
		bean.setLevel(bean.getLevel()+1);
		unionRedisService.saveUnion(bean.build());
	}
	
	public void handleMember(long id, int job) {
		if(user.getUnionJob() != 3)
			return;
		UserBean bean = userService.getUser(id);
		if(bean.getUnionId() == user.getUnionId()){
			bean.setUnionJob(job);
			unionRedisService.saveMember(bean.buildShort());
			userService.updateUser(bean);
		}
	}
}
