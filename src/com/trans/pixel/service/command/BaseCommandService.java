package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFriendBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.model.userinfo.UserRankBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.Mail;
import com.trans.pixel.protoc.Commands.MailList;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserLadderRankListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMineCommand;
import com.trans.pixel.protoc.Commands.ResponseLootResultCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.UserEquip;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.protoc.Commands.UserHero;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.protoc.Commands.UserMine;
import com.trans.pixel.protoc.Commands.UserRank;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.utils.DateUtil;

@Service
public class BaseCommandService {
	@Resource
	private LadderService ladderService;
	
	protected void buildUserInfo(ResponseUserInfoCommand.Builder builder, UserBean user) {
		UserInfo.Builder userBuilder = UserInfo.newBuilder();
		userBuilder.setAccount(user.getAccount());
		userBuilder.setId(user.getId());
		userBuilder.setServerId(user.getServerId());
		userBuilder.setUserName(user.getUserName());
		
		builder.setUser(userBuilder.build());
	}
	
	protected ErrorCommand buildErrorCommand(ErrorConst errorConst) {
        ErrorCommand.Builder erBuilder = ErrorCommand.newBuilder();
        erBuilder.setCode(String.valueOf(errorConst.getCode()));
        erBuilder.setMessage(errorConst.getMesssage());
        return erBuilder.build();
    }
	
	protected int calUserFighting() {
		return 100;
	}
	
	protected ResponseLootResultCommand builderLootResultCommand(UserBean user) {
		ResponseLootResultCommand.Builder builder = ResponseLootResultCommand.newBuilder();
		builder.setExp(user.getExp());
		builder.setGold(user.getCoin());
		builder.setLastLootTime(user.getLastLootTime());
		
		return builder.build();
	}
	
	protected ResponseGetUserMineCommand buildGetUserMineCommand(List<UserMineBean> userMineList) {
		ResponseGetUserMineCommand.Builder builder = ResponseGetUserMineCommand.newBuilder();
		List<UserMine> userMineBuilderList = new ArrayList<UserMine>();
		for (UserMineBean userMine : userMineList) {
			userMineBuilderList.add(userMine.buildUserMine());
		}
		builder.addAllUserMine(userMineBuilderList);
		
		return builder.build();
	}
	
	protected List<UserHero> buildUserHeroList(List<UserHeroBean> userHeroList) {
		List<UserHero> userHeroBuilderList = new ArrayList<UserHero>();
		for (UserHeroBean userHero : userHeroList) {
			userHeroBuilderList.add(userHero.buildUserHero());
		}
		
		return userHeroBuilderList;
	}
	
	protected List<UserEquip> buildUserEquipList(List<UserEquipBean> userEquipList) {
		List<UserEquip> userEquipBuilderList = new ArrayList<UserEquip>();
		for (UserEquipBean userEquip : userEquipList) {
			userEquipBuilderList.add(userEquip.buildUserEquip());
		}
		
		return userEquipBuilderList;
	}
	
	protected List<UserRank> buildUserRankList(List<UserRankBean> userRankList) {
		List<UserRank> userRankBuilderList = new ArrayList<UserRank>();
		for (UserRankBean userRank : userRankList) {
			userRankBuilderList.add(userRank.buildUserRank());
		}
		
		return userRankBuilderList;
	}
	
	protected MailList buildMailList(int type, List<MailBean> mailList) {
		MailList.Builder mailListBuilder = MailList.newBuilder();
		List<Mail> mailBuilderList = new ArrayList<Mail>();
		for (MailBean mail : mailList) {
			mailBuilderList.add(mail.buildMail());
		}
		mailListBuilder.setType(type);
		mailListBuilder.addAllMail(mailBuilderList);
		
		return mailListBuilder.build();
	}
	
	protected List<UserFriend> buildUserFriendList(List<UserFriendBean> userFriendList) {
		List<UserFriend> userFriendBuilderList = new ArrayList<UserFriend>();
		for (UserFriendBean userFriend : userFriendList) {
			userFriendBuilderList.add(userFriend.buildUserFriend());
		}
		
		return userFriendBuilderList;
	}
	
	protected void handleGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {	
		ResponseGetUserLadderRankListCommand.Builder builder = ResponseGetUserLadderRankListCommand.newBuilder();
		List<UserRankBean> rankList = ladderService.getRankListByUserId(user.getServerId(), user.getId());
		List<UserRank> userRankBuilderList = buildUserRankList(rankList);
		builder.addAllUserRank(userRankBuilderList);
		
		responseBuilder.setGetUserLadderRankListCommand(builder.build());
	}
	
	protected MailBean buildMail(long userId, String content, int type, List<RewardBean> rewardList) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setContent(content);
		mail.setType(type);
		mail.setRewardList(rewardList);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	protected MailBean buildMail(long userId, long friendId, String content, int type) {
		MailBean mail = new MailBean();
		mail.setUserId(userId);
		mail.setFromUserId(friendId);
		mail.setContent(content);
		mail.setType(type);
		mail.setStartDate(DateUtil.getCurrentDateString());
		
		return mail;
	}
	
	protected ResponseMessageCommand buildMessageCommand(SuccessConst success) {
		ResponseMessageCommand.Builder builder = ResponseMessageCommand.newBuilder();
		builder.setCode(success.getCode());
		builder.setMsg(success.getMesssage());
		
		return builder.build();
	}
}
