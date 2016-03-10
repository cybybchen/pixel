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
import com.trans.pixel.service.MessageService;

@Service
public class MessageCommandService extends BaseCommandService {
	
	@Resource
	private MessageService messageService;
	@Resource
	private PushCommandService pushCommandService;
	
	public void getMessageBoardList(RequestMessageBoardListCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		List<MessageBoardBean> messageBoardList = messageService.getMessageBoardList(type, user);
		ResponseMessageBoardListCommand.Builder builder = ResponseMessageBoardListCommand.newBuilder();
		builder.addAllMessageBoard(super.buildMessageBoardList(messageBoardList));
		builder.setType(type);
		responseBuilder.setMessageBoardListCommand(builder.build());
	}
	
	public void createMessage(RequestCreateMessageBoardCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		String message = cmd.getMessage();
		messageService.createMessageBoard(type, user, message);
		pushCommandService.pushMessageBoardListCommand(type, responseBuilder, user);
	}
	
	public void replyMessage(RequestReplyMessageCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseMessageBoardCommand.Builder builder = ResponseMessageBoardCommand.newBuilder();
		int type = cmd.getType();
		String id = cmd.getId();
		String message = cmd.getMessage();
		MessageBoardBean messageBoardBean = messageService.replyMessage(type, user, id, message);
		if (messageBoardBean == null) {
			ErrorCommand errorCommand = buildErrorCommand(ErrorConst.MESSAGE_NOT_EXIST);
            responseBuilder.setErrorCommand(errorCommand);
			return;
		}
		builder.setType(type);
		builder.setMessageBoard(messageBoardBean.buildMessageBoard());
		responseBuilder.setMessageBoardCommand(builder.build());
	}
}
