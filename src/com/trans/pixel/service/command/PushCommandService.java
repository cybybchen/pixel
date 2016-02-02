package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserEquipBean;
import com.trans.pixel.model.userinfo.UserFriendBean;
import com.trans.pixel.model.userinfo.UserHeroBean;
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.model.userinfo.UserLevelLootBean;
import com.trans.pixel.model.userinfo.UserMineBean;
import com.trans.pixel.model.userinfo.UserTeamBean;
import com.trans.pixel.protoc.Commands.MailList;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserEquipCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserHeroCommand;
import com.trans.pixel.protoc.Commands.ResponseGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.ResponseUserInfoCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseUserLootLevelCommand;
import com.trans.pixel.protoc.Commands.ResponseUserTeamListCommand;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LootService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.MessageService;
import com.trans.pixel.service.PvpMapService;
import com.trans.pixel.service.UserEquipService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserHeroService;
import com.trans.pixel.service.UserLevelLootService;
import com.trans.pixel.service.UserLevelService;
import com.trans.pixel.service.UserTeamService;

@Service
public class PushCommandService extends BaseCommandService {

	@Resource
	private LootService lootService;
	@Resource
	private PvpMapService pvpMapService;
	@Resource
	private UserHeroService userHeroService;
	@Resource
	private UserEquipService userEquipService;
	@Resource
	private LadderService ladderService;
	@Resource
	private MailService mailService;
	@Resource
	private UserFriendService userFriendService;
	@Resource
	private UserLevelService userLevelService;
	@Resource
	private UserLevelLootService userLevelLootService;
	@Resource
	private MessageService messageService;
	@Resource
	private UserTeamService userTeamService;
	
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
	
	public void pushUserEquipListCommand(Builder responseBuilder, UserBean user) {
		List<UserEquipBean> userHeroList = userEquipService.selectUserEquipList(user.getId());
		ResponseGetUserEquipCommand.Builder builder = ResponseGetUserEquipCommand.newBuilder();
		builder.addAllUserEquip(super.buildUserEquipList(userHeroList));
		responseBuilder.setUserEquipCommand(builder.build());
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
	
	public void pushUserLevelCommand(Builder responseBuilder, UserBean user) {
		ResponseUserLevelCommand.Builder builder = ResponseUserLevelCommand.newBuilder();
		UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
		if (userLevel.getLastLevelResultTime() > 0) {
			userLevel.setLevelPrepareTime(userLevel.getLevelPrepareTime() + 
					(int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND) - userLevel.getLastLevelResultTime());
			userLevel.setLastLevelResultTime((int)(System.currentTimeMillis() / TimeConst.MILLIONSECONDS_PER_SECOND));
			userLevelService.updateUserLevelRecord(userLevel);
		}
		builder.setUserLevel(userLevel.buildUserLevel());
		responseBuilder.setUserLevelCommand(builder.build());
	}
	
	public void pushUserLootLevelCommand(Builder responseBuilder, UserBean user) {
		ResponseUserLootLevelCommand.Builder builder = ResponseUserLootLevelCommand.newBuilder();
		UserLevelLootBean userLevelLoot = userLevelLootService.selectUserLevelLootRecord(user.getId());
		builder.setUserLootLevel(userLevelLoot.buildUserLootLevel());
		responseBuilder.setUserLootLevelCommand(builder.build());
	}
	
	public void pushLadderRankListCommand(Builder responseBuilder, UserBean user) {	
		super.handleGetUserLadderRankListCommand(responseBuilder, user);
	}
	
	public void pushMessageBoardListCommand(int type, Builder responseBuilder, UserBean user) {
		List<MessageBoardBean> messageBoardList = messageService.getMessageBoardList(type, user);
		ResponseMessageBoardListCommand.Builder builder = ResponseMessageBoardListCommand.newBuilder();
		builder.addAllMessageBoard(super.buildMessageBoardList(messageBoardList));
		builder.setType(type);
		responseBuilder.setMessageBoardListCommand(builder.build());
	}
	
	public void pushUserTeamListCommand(Builder responseBuilder, UserBean user) {
		List<UserTeamBean> userTeamList = userTeamService.selectUserTeamList(user.getId());
		ResponseUserTeamListCommand.Builder builder = ResponseUserTeamListCommand.newBuilder();
		builder.addAllUserTeam(buildUserTeamList(userTeamList));
		
		responseBuilder.setUserTeamListCommand(builder.build());
	}
}
