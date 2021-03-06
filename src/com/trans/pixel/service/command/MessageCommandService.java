package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.RequestCreateMessageBoardCommand;
import com.trans.pixel.protoc.Commands.RequestMessageBoardListCommand;
import com.trans.pixel.protoc.Commands.RequestReplyMessageCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardListCommand;
import com.trans.pixel.service.BlackService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MessageService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class MessageCommandService extends BaseCommandService {
	
	@Resource
	private MessageService messageService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private BlackService blackService;
	@Resource
	private LogService logService;
	
	public void getMessageBoardList(RequestMessageBoardListCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		List<MessageBoardBean> messageBoardList = messageService.getMessageBoardList(type, user);
		ResponseMessageBoardListCommand.Builder builder = ResponseMessageBoardListCommand.newBuilder();
		builder.addAllMessageBoard(super.buildMessageBoardList(messageBoardList));
		builder.setType(type);
		responseBuilder.setMessageBoardListCommand(builder.build());
	}
	
	public void createMessage(RequestCreateMessageBoardCommand cmd, Builder responseBuilder, UserBean user) {
		if (blackService.isBlackNosay(user)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BLACK_NOSAY_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_NOSAY_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			messageService.createMessageBoard(cmd.getType(), user, cmd.getMessage());
		}
		pushCommandService.pushMessageBoardListCommand(cmd.getType(), responseBuilder, user);
	}
	
	public void replyMessage(RequestReplyMessageCommand cmd, Builder responseBuilder, UserBean user) {
		if (blackService.isBlackNosay(user)) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BLACK_NOSAY_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_NOSAY_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		ResponseMessageBoardCommand.Builder builder = ResponseMessageBoardCommand.newBuilder();
		int type = cmd.getType();
		String id = cmd.getId();
		String message = cmd.getMessage();
		MessageBoardBean messageBoardBean = messageService.replyMessage(type, user, id, message);
		if (messageBoardBean == null) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.MESSAGE_NOT_EXIST);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MESSAGE_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		builder.setType(type);
		builder.setMessageBoard(messageBoardBean.buildMessageBoard());
		responseBuilder.setMessageBoardCommand(builder.build());
	}
}
