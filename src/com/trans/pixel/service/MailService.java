package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.MailRedisService;

@Service
public class MailService {
	@Resource
	private MailRedisService mailRedisService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserService userService;
	@Resource
	private RewardService rewardService;
	
	public void addMail(MailBean mail) {
		mailRedisService.addMail(mail);
	}
	
	public List<MailBean> getMailList(long userId, int type) {
		return mailRedisService.getMailListByUserIdAndType(userId, type);
	}
	
	public void delMail(long userId, int type, int id) {
		mailRedisService.deleteMail(userId, type, id);
	}
	
	public int getMailCount(long userId, int type) {
		return mailRedisService.getMailCountByUserIdAndType(userId, type);
	}
	
	public List<MailBean> readMail(UserBean user, int type, List<Integer> ids) {
		List<MailBean> mailList = mailRedisService.getMailList(user.getId(), type, ids);
		
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		for (MailBean mail : mailList) {
			mail.setRead(true);
			mailRedisService.updateMail(mail);
			rewardList.addAll(mail.getRewardList());
		}
		
		rewardService.doRewards(user, rewardList);
		return mailList;
	}
	
	public int deleteMail(UserBean user, int type, List<Integer> ids) {
		return mailRedisService.deleteMail(user.getId(), type, ids);
	}
}
