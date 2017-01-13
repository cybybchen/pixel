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
import com.trans.pixel.protoc.Commands.RequestQueryNoticeBoardCommand;
import com.trans.pixel.protoc.Commands.RequestReplyMessageCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardCommand;
import com.trans.pixel.protoc.Commands.ResponseMessageBoardListCommand;
import com.trans.pixel.service.BlackListService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.MessageService;
import com.trans.pixel.service.NoticeService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class MessageCommandService extends BaseCommandService {
	
	@Resource
	private MessageService messageService;
	@Resource
	private PushCommandService pushCommandService;
	@Resource
	private BlackListService blackService;
	@Resource
	private LogService logService;
	@Resource
	private NoticeService noticeService;
	
	public void getMessageBoardList(RequestMessageBoardListCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		List<MessageBoardBean> messageBoardList = messageService.getMessageBoardList(type, user);
		ResponseMessageBoardListCommand.Builder builder = ResponseMessageBoardListCommand.newBuilder();
		builder.addAllMessageBoard(super.buildMessageBoardList(messageBoardList));
		builder.setType(type);
		responseBuilder.setMessageBoardListCommand(builder.build());
	}
	
	public void createMessage(RequestCreateMessageBoardCommand cmd, Builder responseBuilder, UserBean user) {
		if (blackService.isNotalk(user.getId())) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), ErrorConst.BLACK_NOSAY_ERROR);
			
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.BLACK_NOSAY_ERROR);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			messageService.createMessageBoard(cmd.getType(), user, cmd.getMessage());
		}
		pushCommandService.pushMessageBoardListCommand(cmd.getType(), responseBuilder, user);
	}
	
	public void replyMessage(RequestReplyMessageCommand cmd, Builder responseBuilder, UserBean user) {
		if (blackService.isNotalk(user.getId())) {
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
	
	public void queryNoticeBoard(RequestQueryNoticeBoardCommand cmd, Builder responseBuilder, UserBean user) {
		noticeService.delNoticeId(user.getId(), cmd.getMessageId());
	}
}
