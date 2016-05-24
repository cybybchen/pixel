package com.trans.pixel.service.command;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.MailConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.model.userinfo.UserFriendBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestDeleteMailCommand;
import com.trans.pixel.protoc.Commands.RequestGetUserMailListCommand;
import com.trans.pixel.protoc.Commands.RequestReadMailCommand;
import com.trans.pixel.protoc.Commands.RequestSendMailCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.UserInfo;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MailService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserFriendService;
import com.trans.pixel.service.UserService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class MailCommandService extends BaseCommandService {
	private static final int LIMIT_VIP_LEVEL = 1;
	@Resource
	private MailService mailService;
	@Resource
	private UserFriendService userFriendService;
	@Resource
	private UserService userService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private RewardService rewardService;
	@Resource
	private LogService logService;
	
	public void handleGetUserMailListCommand(RequestGetUserMailListCommand cmd, Builder responseBuilder, UserBean user) {
		int type = 0;
		if (cmd.hasType())
			type = cmd.getType();
		pushCommandService.pushUserMailListCommand(responseBuilder, user, type);
	}
	
	public void handleReadMailCommand(RequestReadMailCommand cmd, Builder responseBuilder, UserBean user) {	
		int type = cmd.getType();
		List<Integer> ids = cmd.getIdList();
		List<RewardBean> rewardList = new ArrayList<RewardBean>();
		List<MailBean> mailList = mailService.readMail(user, type, ids, rewardList);
		if (mailList.size() == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.MAIL_IS_NOT_EXIST);
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MAIL_IS_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.MAIL_READ_SUCCESS));
		
		rewardService.doRewards(user, rewardList);
		
		pushCommandService.pushRewardCommand(responseBuilder, user, rewardList);
		pushCommandService.pushUserMailListCommand(responseBuilder, user, type);
	}
	
	public void sendMail(RequestSendMailCommand cmd, Builder responseBuilder, UserBean user) {
		long toUserId = cmd.getToUserId();
		String content = cmd.getContent();
		int relatedId = 0;
		int type = cmd.getType();
		
		if (type == MailConst.TYPE_CALL_BROTHER_MAILL) {
			UserInfo userCache = userService.getCache(user.getServerId(), toUserId);
			if (userCache == null || userCache.getVip() < LIMIT_VIP_LEVEL) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.VIP_IS_NOT_ENOUGH);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.VIP_IS_NOT_ENOUGH);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			if (!userFriendService.canCallBrother(user.getId(), toUserId)) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.CALL_BROTHER_TIME_NOT_ENOUGH_ERROR);
				
				ErrorCommand errorCommand = buildErrorCommand(ErrorConst.CALL_BROTHER_TIME_NOT_ENOUGH_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			UserFriendBean userFriend = userFriendService.updateFriendCallTime(user.getId(), toUserId);
			if (userFriend == null) {
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.SEND_MAIL_ERROR);
				
				ErrorCommand errorCommand = super.buildErrorCommand(ErrorConst.SEND_MAIL_ERROR);
	            responseBuilder.setErrorCommand(errorCommand);
	            return;
			}
			pushCommandService.pushUserFriendListCommand(responseBuilder, user);
		}
		
		if (cmd.hasRelatedId())
			relatedId = cmd.getRelatedId();
		MailBean mail = buildMail(toUserId, user.getId(), user.getVip(), user.getUserName(), content, type, relatedId);
		mail.setIcon(user.getIcon());
		mailService.addMail(mail);
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.MAIL_SEND_SUCCESS));
		
	}
	
	public void handleDeleteMailCommand(RequestDeleteMailCommand cmd, Builder responseBuilder, UserBean user) {	
		int type = cmd.getType();
		List<Integer> ids = cmd.getIdList();
		int count = mailService.deleteMail(user, type, ids);
		if (count == 0) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass().toString(), RedisService.formatJson(cmd), ErrorConst.MAIL_IS_NOT_EXIST);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MAIL_IS_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.MAIL_DELETE_SUCCESS));
		pushCommandService.pushUserMailListCommand(responseBuilder, user, type);
	}
}
