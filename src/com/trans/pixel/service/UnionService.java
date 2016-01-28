package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.TeamConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.UnionBean;
import com.trans.pixel.model.mapper.UnionMapper;
import com.trans.pixel.model.userinfo.UnionUserBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.service.redis.UnionRedisService;
import com.trans.pixel.utils.DateUtil;

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
	
	public void applyToUnion(UserBean user, int unionId) {
		MailBean mail = buildApplyUnionMail(user.getId(), unionId, user.getUserName());
		unionRedisService.addUnionApply(unionId, mail);
		
		mail = buildUserApplyUnionMail(user.getId(), unionId);
		mailService.addMail(mail);
	}
	
	public UnionBean createUnion(UserBean user, String unionName) {
		UnionBean union = initUnion(user, unionName);
		unionMapper.createUnion(union);
		unionRedisService.updateUnion(union);
		user.setUnionId(union.getId());
		userService.updateUser(user);
		
		return union;
	}
	
	public UnionBean handleUnionApply(UserBean user, int unionId, int mailId, boolean receive) {
		UnionBean union = unionRedisService.getUnion(user.getServerId(), unionId);
		MailBean mail = union.getMail(mailId);
		if (receive) {
			UnionUserBean unionUser = initUnionUser(mail.getFromUserId(), mail.getFromUserName());
			union.addUnionUser(unionUser);
			userService.updateUserUnion(mail.getFromUserId(), unionId);
		} else {
			MailBean userMail = buildUserRefudedByUnionMail(mail.getFromUserId(), unionId);
			mailService.addMail(userMail);
		}
		
		union.deleteMail(mail);
		
		return union;
	}
	
	public void upgradeUserUnionLevel(UserBean user, long upgradeUserId) {
		
	}
	
	private UnionBean initUnion(UserBean user, String unionName) {
		UnionBean union = new UnionBean();
		union.setServerId(user.getServerId());
		union.setUnionName(unionName);
		List<UnionUserBean> unionUserList = new ArrayList<UnionUserBean>();
		unionUserList.add(initUnionUser(user.getId(), user.getUserName()));
		
		return union;
	}
	
	private UnionUserBean initUnionUser(long userId, String userName) {
		UnionUserBean unionUser = new UnionUserBean();
		unionUser.setUserId(userId);
		unionUser.setUserName(userName);
		UserTeamBean userTeam = userTeamService.selectUserTeam(userId, TeamConst.TYPE_TEAM_TYPE_NORMAL);
		if (userTeam != null)
			unionUser.setRecord(userTeam.getTeamRecord());
		
		return unionUser;
	}
	
	private MailBean buildUserRefudedByUnionMail(long fromUserId, int unionId) {
		MailBean mail = new MailBean();
		mail.setUserId(fromUserId);
		mail.setFromUserId(unionId);
		mail.setContent("该工会拒绝你的申请");
		mail.setType(MailConst.TYPE_UNION_REFUSED_MAIL);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	private MailBean buildApplyUnionMail(long fromUserId, int unionId, String userName) {
		MailBean mail = new MailBean();
		mail.setUserId(unionId);
		mail.setFromUserId(fromUserId);
		mail.setFromUserName(userName);
		mail.setContent("加联盟");
		mail.setType(MailConst.TYPE_APPLY_UNION_MAIL);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	private MailBean buildUserApplyUnionMail(long fromUserId, int unionId) {
		MailBean mail = new MailBean();
		mail.setUserId(fromUserId);
		mail.setFromUserId(unionId);
		mail.setContent("加联盟");
		mail.setType(MailConst.TYPE_APPLY_UNION_MAIL);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
}
