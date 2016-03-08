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
import com.trans.pixel.protoc.Commands.RequestDeleteMailCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestSendMailCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.UserFriendService;

@Service
public class MailCommandService extends BaseCommandService {
	@Resource
	private MailService mailService;
	@Resource
	private UserFriendService userFriendService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void handleGetUserMailListCommand(RequestGetUserMailListCommand cmd, Builder responseBuilder, UserBean user) {
		pushCommandService.pushUserMailListCommand(responseBuilder, user);
	}
	
	public void handleReadMailCommand(RequestReadMailCommand cmd, Builder responseBuilder, UserBean user) {	
		int type = cmd.getType();
		List<Integer> ids = cmd.getIdList();
		List<MailBean> mailList = mailService.readMail(user, type, ids);
		if (mailList.size() == 0) {
			ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.MAIL_IS_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		responseBuilder.setMessageCommand(super.buildMessageCommand(SuccessConst.MAIL_READ_SUCCESS));
		
		pushCommandService.pushUserInfoCommand(responseBuilder, user);
		pushCommandService.pushUserMailListCommand(responseBuilder, user);
	}
	
	public void sendMail(RequestSendMailCommand cmd, Builder responseBuilder, UserBean user) {
		long toUserId = cmd.getToUserId();
		String content = cmd.getContent();
		int relatedId = 0;
		int type = cmd.getType();
		if (cmd.hasRelatedId())
			relatedId = cmd.getRelatedId();
		MailBean mail = super.buildMail(toUserId, user.getId(), content, type, relatedId);
		mailService.addMail(mail);
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.MAIL_SEND_SUCCESS));
		
		if (type == MailConst.TYPE_CALL_BROTHER_MAILL) {
			userFriendService.updateFriendCallTime(user.getId(), toUserId);
		}
	}
	
	public void handleDeleteMailCommand(RequestDeleteMailCommand cmd, Builder responseBuilder, UserBean user) {	
		int type = cmd.getType();
		List<Integer> ids = cmd.getIdList();
		int count = mailService.deleteMail(user, type, ids);
		if (count == 0) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MAIL_IS_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.MAIL_DELETE_SUCCESS));
		pushCommandService.pushUserMailListCommand(responseBuilder, user);
	}
}
