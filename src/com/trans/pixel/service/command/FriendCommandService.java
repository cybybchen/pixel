package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.FriendConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.MailProto.RequestAddFriendCommand;
import com.trans.pixel.protoc.MailProto.RequestDelFriendCommand;
import com.trans.pixel.protoc.MailProto.RequestGetUserFriendListCommand;
import com.trans.pixel.protoc.MailProto.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.MailProto.ResponseGetUserFriendListCommand;
import com.trans.pixel.protoc.MailProto.UserFriend;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

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
	@Resource
	private LogService logService;
	
	public void handleAddFriendCommand(RequestAddFriendCommand cmd, Builder responseBuilder, UserBean user) {	
		long friendId = 0;
		String friendName = "";
		if (cmd.hasUserId())
			friendId = cmd.getUserId();
		if (cmd.hasUserName())
			friendName = cmd.getUserName();
		if (friendId == 0) {
			friendId = userService.queryUserIdByUserName(user.getServerId(), friendName);
			if (friendId == 0) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.FRIEND_NOT_EXIST);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.FRIEND_NOT_EXIST);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
		}
		if (userFriendService.isFriend(user.getId(), friendId)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.FRIEND_HAS_ADDED);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.FRIEND_HAS_ADDED);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		if (friendId == user.getId()) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.NOT_ADD_SELF_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.NOT_ADD_SELF_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		if (userFriendService.getFriendCount(user.getId()) >= FriendConst.FRIEND_COUNT_MAX) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.YOUR_FRIEND_MAX_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.YOUR_FRIEND_MAX_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		if (userFriendService.getFriendCount(friendId) >= FriendConst.FRIEND_COUNT_MAX) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.THE_PERSON_FRIEND_MAX_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.THE_PERSON_FRIEND_MAX_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		buildAddFriendMail(friendId, user);
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.SEND_FRIEND_ADDED_SUCCESS));
	}
	
	public void handleReceiveFriendCommand(RequestReceiveFriendCommand cmd, Builder responseBuilder, UserBean user) {
		List<Integer> ids = cmd.getIdList();
		boolean receive = cmd.getReceive();
		List<MailBean> mailList = mailService.readMail(user, MailConst.TYPE_ADDFRIEND_MAIL, ids, null);
		if (mailList.size() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.MAIL_IS_NOT_EXIST);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MAIL_IS_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
		}else if (receive) {
			if (userFriendService.getFriendCount(user.getId()) >= FriendConst.FRIEND_COUNT_MAX) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.YOUR_FRIEND_MAX_ERROR);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.YOUR_FRIEND_MAX_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
			}else if (!doAddFriends(user.getId(), mailList)) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.THE_PERSON_FRIEND_MAX_ERROR);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.THE_PERSON_FRIEND_MAX_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
			}else
				responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.FRIEND_ADDED_SUCCESS));
		}
		// else
		// 	responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.FRIEND_ADDED_FAILED));
//		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		
		pushCommandService.pushUserMailListCommand(responseBuilder, user, MailConst.TYPE_ADDFRIEND_MAIL);
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
	
	private boolean doAddFriends(long userId, List<MailBean> mailList) {
		for (MailBean mail : mailList) {
			if (!userFriendService.isFriend(userId, mail.getUser().getId())) {
				if (userFriendService.getFriendCount(mail.getUser().getId()) >= FriendConst.FRIEND_COUNT_MAX) {
		            return false;
				}
				userFriendService.insertUserFriend(userId, mail.getUser().getId());
				userFriendService.insertUserFriend(mail.getUser().getId(), userId);
			}
			
			mailService.delMail(userId, MailConst.TYPE_ADDFRIEND_MAIL, mail.getId());
		}
		
		return true;
	}
	
	private void buildAddFriendMail(long userId, UserBean user) {
		String content = "添加你为好友";
		MailBean mail = buildMail(userId, user, content, MailConst.TYPE_ADDFRIEND_MAIL);
		mailService.addMail(mail);
	}
}
