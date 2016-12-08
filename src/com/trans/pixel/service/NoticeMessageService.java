package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.hero.HeroBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.NoticeMessageRedisService;

@Service
public class NoticeMessageService {
	
	private static final Logger log = LoggerFactory.getLogger(NoticeMessageService.class);
	@Resource
	private HeroService heroService;
	@Resource
	private NoticeMessageRedisService redis;
	@Resource
	private UserService userService;
	
	public void composeLotteryMessage(UserBean user, List<RewardBean> rewardList) {
		for (RewardBean reward : rewardList) {
			if (reward.getItemid() < RewardConst.HERO || reward.getItemid() > RewardConst.HEAD)
				continue;
			log.debug("111");
			HeroBean hero = heroService.getHero(reward.getItemid() % RewardConst.HERO_STAR);
			if (hero == null)
				continue;
			log.debug("222");
			StringBuilder sb = new StringBuilder();
			sb.append(user.getUserName()).append("召唤了");
			if (hero.getQuality() < 5)
				continue;
			else if (hero.getQuality() == 6)
				sb.append("传说");
			else if (hero.getQuality() == 5)
				sb.append("神话");
			
			log.debug("333");
			sb.append("英雄").append(hero.getName());
			
			redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
		}
	}
	
	public List<String> getNoticeMessageList(UserBean user) {
		List<String> messageList = redis.getNoticeMessageList(user.getServerId(), user.getReceiveNoticeMessageTimeStamp());
		user.setReceiveNoticeMessageTimeStamp(System.currentTimeMillis());
		userService.updateUser(user);
		return messageList;
	}
}
