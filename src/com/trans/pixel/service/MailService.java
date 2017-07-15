package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.NoticeConst;
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
	private NoticeService noticeService;
	
	public void addMail(MailBean mail) {
		if (mail.getUser() != null && hasSend(mail.getUserId(), mail.getType(), mail.getUser().getId()))//好友邮件已发送
			return;
		
		mailRedisService.addMail(mail);
		if (mail.getType() == MailConst.TYPE_SYSTEM_MAIL || mail.getType() == MailConst.TYPE_MINE_ATTACKED_MAIL)
			noticeService.pushNotice(mail.getUserId(), NoticeConst.TYPE_SYSTEM_MAIL);
		else
			noticeService.pushNotice(mail.getUserId(), NoticeConst.TYPE_FRIEND);
	}
	
	public List<MailBean> getMailList(long userId, int type) {
		return mailRedisService.getMailListByUserIdAndType(userId, type);
	}
	
	public void isDeleteNotice(long userId, int type) {
		if (type == MailConst.TYPE_SYSTEM_MAIL || type == MailConst.TYPE_MINE_ATTACKED_MAIL) {
			for (Integer friendMailType : MailConst.SYSTEM_MAIL_TYPES) {
				List<MailBean> mailList = getMailList(userId, friendMailType);
				for (MailBean mail : mailList) {
					if (!mail.isRead())
						return;
				}
			}
			noticeService.deleteNotice(userId, NoticeConst.TYPE_SYSTEM_MAIL);
		} else {
			for (Integer friendMailType : MailConst.FRIEND_MAIL_TYPES) {
				List<MailBean> mailList = getMailList(userId, friendMailType);
				for (MailBean mail : mailList) {
					if (!mail.isRead())
						return;
				}
			}
			
			noticeService.deleteNotice(userId, NoticeConst.TYPE_FRIEND);
		}
	}
	
	public void delMail(long userId, int type, int id) {
		mailRedisService.deleteMail(userId, type, id);
		isDeleteNotice(userId, type);
	}
	
	public int getMailCount(long userId, int type) {
		return mailRedisService.getMailCountByUserIdAndType(userId, type);
	}
	
	public List<MailBean> readMail(UserBean user, int type, List<Integer> ids, List<RewardBean> rewardList, boolean isAll) {
		List<MailBean> mailList = new ArrayList<MailBean>();
		if (isAll) {
			for (Integer systemType : MailConst.SYSTEM_MAIL_TYPES) {
				mailList.addAll(getMailList(user.getId(), systemType));
			}
			
			isDeleteNotice(user.getId(), MailConst.TYPE_SYSTEM_MAIL);
			
		} else {
			mailList = mailRedisService.getMailList(user.getId(), type, ids);
			isDeleteNotice(user.getId(), type);
		}
		
		for (MailBean mail : mailList) {
			mail.setRead(true);
			mailRedisService.updateMail(mail);
			if (rewardList != null)
				rewardList.addAll(mail.getRewardList());
		}
		
		return mailList;
	}
	
	public int deleteMail(UserBean user, int type, List<Integer> ids) {
		int count = mailRedisService.deleteMail(user.getId(), type, ids);
		isDeleteNotice(user.getId(), type);
		return count;
	}
	
	private boolean hasSend(long userId, int type, long friendId) {
		if (type == MailConst.TYPE_ADDFRIEND_MAIL) {
			List<MailBean> mailList = getMailList(userId, type);
			for (MailBean mail : mailList) {
				if (mail.getUser().getId() == friendId)
					return true;
			}
		}
		
		return false;
	}
}
