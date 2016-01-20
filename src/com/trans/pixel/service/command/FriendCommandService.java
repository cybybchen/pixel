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
import com.trans.pixel.protoc.Commands.RequestReceiveFriendCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserFriendService;

@Service
public class FriendCommandService extends BaseCommandService {
	@Resource
	private MailService mailService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private UserFriendService userFriendService;
	
	public void handleAddFriendCommand(RequestAddFriendCommand cmd, Builder responseBuilder, UserBean user) {	
		long friendId = cmd.getUserId();
		if (userFriendService.isFriend(user.getId(), friendId)) {
			ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.FRIEND_HAS_ADDED);
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
		doAddFriends(user.getId(), mailList);
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.FRIEND_ADDED_SUCCESS));
		
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
	}
	
	private void doAddFriends(long userId, List<MailBean> mailList) {
		for (MailBean mail : mailList) {
			if (!userFriendService.isFriend(userId, mail.getFromUserId()))
				userFriendService.insertUserFriend(userId, mail.getFromUserId());
			
			mailService.delMail(userId, MailConst.TYPE_ADDFRIEND_MAIL, mail.getId());
		}
	}
	
	private void buildAddFriendMail(long userId, long friendId) {
		String content = "添加你为好友";
		MailBean mail = super.buildMail(userId, friendId, content, MailConst.TYPE_ADDFRIEND_MAIL);
		mailService.addMail(mail);
	}
}
