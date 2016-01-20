package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserFriendBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.protoc.Commands.MailList;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LootService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserHeroService;

@Service
public class PushCommandService extends BaseCommandService {

	@Resource
	private LootService lootService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private LadderService ladderService;
	@Resource
	private MailService mailService;
	@Resource
	private UserFriendService userFriendService;
	
	public void pushLootResultCommand(Builder responseBuilder, UserBean user) {
		user = lootService.updateLootResult(user);
		responseBuilder.setLootResultCommand(super.builderLootResultCommand(user));
	}
	
	public void pushUserMineListCommand(Builder responseBuilder, UserBean user) {
		List<UserMineBean> userMineList = pvpMapService.relateUser(user);
		responseBuilder.setGetUserMineCommand(super.buildGetUserMineCommand(userMineList));
	}
	
	public void pushUserHeroListCommand(Builder responseBuilder, UserBean user) {
		List<UserHeroBean> userHeroList = userHeroService.selectUserHeroList(user.getId());
		ResponseGetUserHeroCommand.Builder builder = ResponseGetUserHeroCommand.newBuilder();
		builder.addAllUserHero(super.buildUserHeroList(userHeroList));
		responseBuilder.setGetUserHeroCommand(builder.build());
	}
	
	public void pushGetUserLadderRankListCommand(Builder responseBuilder, UserBean user) {
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
	
	public void pushUserInfoCommand(Builder responseBuilder, UserBean user) {
		ResponseUserInfoCommand.Builder builder = ResponseUserInfoCommand.newBuilder();
		super.buildUserInfo(builder, user);
		responseBuilder.setUserInfoCommand(builder.build());
	}
	
	public void pushUserMailListCommand(Builder responseBuilder, UserBean user) {
		ResponseGetUserMailListCommand.Builder builder = ResponseGetUserMailListCommand.newBuilder();
		List<MailList> mailBuilderList = new ArrayList<MailList>();
		for (Integer type : MailConst.MAIL_TYPES) {
			List<MailBean> mailList = mailService.getMailList(user.getId(), type);
			mailBuilderList.add(super.buildMailList(type, mailList));
		}
		builder.addAllMailList(mailBuilderList);
		responseBuilder.setGetUserMailListCommand(builder.build());
	}
	
	public void pushUserFriendListCommand(Builder responseBuilder, UserBean user) {
		ResponseGetUserFriendListCommand.Builder builder = ResponseGetUserFriendListCommand.newBuilder();
		List<UserFriendBean> userFriendList = userFriendService.getUserFriendList(user.getId());
		builder.addAllFriend(super.buildUserFriendList(userFriendList));
		responseBuilder.setGetUserFriendListCommand(builder.build());
	}
}
