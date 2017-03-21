package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.RewardConst;
import com.trans.pixel.model.EquipmentBean;
import com.trans.pixel.model.HeroInfoBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserPokedeBean;
import com.trans.pixel.protoc.EquipProto.Equiptucao;
import com.trans.pixel.protoc.HeroProto.Hero;
import com.trans.pixel.service.redis.EquipRedisService;
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
	@Resource
	private EquipRedisService equipRedisService;
	
	public void composeLotteryMessage(UserBean user, List<RewardBean> rewardList) {
		for (RewardBean reward : rewardList) {
			if (reward.getItemid() < RewardConst.HERO || reward.getItemid() > RewardConst.HEAD)
				continue;
			
			Hero hero = heroService.getHero(reward.getItemid() % RewardConst.HERO_STAR);
			if (hero == null)
				continue;
			
			StringBuilder sb = new StringBuilder();
			sb.append(user.getUserName()).append("召唤了%s,");
			if (hero.getQuality() < 5)
				continue;
			else if (hero.getQuality() == 6)
				sb.append("6,神话");
			else if (hero.getQuality() == 5)
				sb.append("5,传说");
			
			sb.append("英雄").append(hero.getName()).append("！");
			
			redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
		}
	}
	
	public void composeStrengthen(UserBean user, UserPokedeBean userPokede) {
		if (userPokede.getStrengthen() == 3 || userPokede.getStrengthen() == 6 || userPokede.getStrengthen() >= 9) {
			Hero hero = heroService.getHero(userPokede.getHeroId());
			StringBuilder sb = new StringBuilder();
			sb.append("恭喜").append(user.getUserName()).append("将").append(hero.getName()).append("%s,6,")
				.append("强化到了").append(userPokede.getStrengthen()).append("级").append("！");
			
			redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
		}	
	}
	
	public void composeEquipLevelup(UserBean user, EquipmentBean equip) {
		Equiptucao equiptucao = equipRedisService.getEquiptucao(equip.getItemid());
		if (equiptucao == null)
			return;
		
		StringBuilder sb = new StringBuilder();
		sb.append(user.getUserName()).append("合成了%s").append("，").append("小伙伴们都惊呆了！").append(",").append(equiptucao.getRare()).append(",").append(equiptucao.getItemname());
		redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
	}
	
	public void composeLadderLevelup(UserBean user, long rank) {
		if (rank != 1)
			return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("%s,6,恭喜").append(user.getUserName()).append("登顶天梯第一！");
		redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
	}
	
	public void composeHeroRankup(UserBean user, HeroInfoBean heroInfo) {
		if (heroInfo.getRank() == 6 || heroInfo.getRank() == 9 || heroInfo.getRank() == 12 || heroInfo.getRank() == 15
				|| heroInfo.getRank() >= 18) {
			Hero hero = heroService.getHero(heroInfo.getHeroId());
			StringBuilder sb = new StringBuilder();
			sb.append("恭喜").append(user.getUserName()).append("将").append(hero.getName()).append("%s,5,")
			.append("进阶到了").append(heroInfo.getRank()).append("级").append("！");
		
			redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
		}
	}
	
	public void composeCallbrotherHelpAttackMine(UserBean user, String enemyName) {
		StringBuilder sb = new StringBuilder();
		sb.append("%s").append(user.getUserName()).append("帮助好友击败了").append(enemyName).append("，").append("夺回矿点").append(",6,战报:");
		redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
	}
	
	public void composeCallBrotherHelpLevelResult(UserBean user, int level) {
		StringBuilder sb = new StringBuilder();
		sb.append("%s").append(user.getUserName()).append("帮助好友通关");
		if (level > 3000)
			sb.append("地狱");
		else if (level > 2000)
			sb.append("困难");
		
		level = level % 1000;
		sb.append(1 + level / 5).append("-").append(level % 5 == 0 ? 5 : level % 5).append(",6,战报:");
		redis.addNoticeMessage(user.getServerId(), sb.toString(), System.currentTimeMillis());
	}
	
	public List<String> getNoticeMessageList(UserBean user) {
		List<String> messageList = redis.getNoticeMessageList(user.getServerId(), user.getReceiveNoticeMessageTimeStamp());
		user.setReceiveNoticeMessageTimeStamp(System.currentTimeMillis());
		userService.updateUser(user);
		return messageList;
	}
}
