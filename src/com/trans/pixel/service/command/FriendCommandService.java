package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestAddFriendCommand;
import com.trans.pixel.protoc.Commands.RequestDelFriendCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.Commands.UserFriend;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserService;

@Service
public class FriendCommandService extends BaseCommandService {
	@Resource
	private MailService mailService;
	@Resource
	private UserService userService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private UserFriendService userFriendService;
	
	public void handleAddFriendCommand(RequestAddFriendCommand cmd, Builder responseBuilder, UserBean user) {	
		long friendId = 0;
		String friendName = "";
		if (cmd.hasUserId())
			friendId = cmd.getUserId();
		if (cmd.hasUserName())
			friendName = cmd.getUserName();
		UserBean friend = new UserBean();
		if (friendId == 0) {
			friend = userService.getUserByName(user.getServerId(), friendName);
			if (friend == null) {
				ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.FRIEND_NOT_EXIST);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
		}
		if (userFriendService.isFriend(user.getId(), friendId)) {
			ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.FRIEND_HAS_ADDED);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		if (friendId == user.getId()) {
			ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.NOT_ADD_SELF_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		buildAddFriendMail(friendId, user.getId());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.SEND_FRIEND_ADDED_SUCCESS));
	}
	
	public void handleReceiveFriendCommand(RequestReceiveFriendCommand cmd, Builder responseBuilder, UserBean user) {
		List<Integer> ids = cmd.getIdList();
		List<MailBean> mailList = mailService.readMail(user, MailConst.TYPE_ADDFRIEND_MAIL, ids);
		if (mailList.size() == 0) {
			ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.MAIL_IS_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		boolean receive = cmd.getReceive();
		if (receive) {
			doAddFriends(user.getId(), mailList);
			responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.FRIEND_ADDED_SUCCESS));
		} else
			responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.FRIEND_ADDED_FAILED));
//		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		
		pushCommandService.pushUserMailListCommand(responseBuilder, user);
	}
	
	public void getUserFriendList(RequestGetUserFriendListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseGetUserFriendListCommand.Builder builder = ResponseGetUserFriendListCommand.newBuilder();
		List<UserFriend> userFriendList = userFriendService.getUserFriendList(user);
		builder.addAllFriend(userFriendList);
		responseBuilder.setGetUserFriendListCommand(builder.build());
	}
	
	public void delUserFriend(RequestDelFriendCommand cmd, Builder responseBuilder, UserBean user) {
		userFriendService.deleteUserFriend(user.getId(), cmd.getUserId());
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.DEL_FRIEND_SUCCESS));
	}
	
	private void doAddFriends(long userId, List<MailBean> mailList) {
		for (MailBean mail : mailList) {
			if (!userFriendService.isFriend(userId, mail.getFromUserId())) {
				userFriendService.insertUserFriend(userId, mail.getFromUserId());
				userFriendService.insertUserFriend(mail.getFromUserId(), userId);
			}
			
			mailService.delMail(userId, MailConst.TYPE_ADDFRIEND_MAIL, mail.getId());
		}
	}
	
	private void buildAddFriendMail(long userId, long friendId) {
		String content = "添加你为好友";
		MailBean mail = super.buildMail(userId, friendId, content, MailConst.TYPE_ADDFRIEND_MAIL);
		mailService.addMail(mail);
	}
}
