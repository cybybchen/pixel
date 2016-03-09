package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.service.redis.MessageRedisService;

@Service
public class MessageService {	
	@Resource
	private MessageRedisService messageRedisService;
	@Resource
	private UserService userService;

	public List<MessageBoardBean> getMessageBoardList(int type, UserBean user) {
		switch (type) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				return getMessageBoardList(user);
			case MessageConst.TYPE_MESSAGE_UNION:
				return getUnionMessageBoardList(user);
			default:
				return new ArrayList<MessageBoardBean>();
		}
	}
	
	public void createMessageBoard(int type, UserBean user, String message) {
		switch (type) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				createMessageBoard(user, message);
				break;
			case MessageConst.TYPE_MESSAGE_UNION:
				createUnionMessageBoard(user, message);
				break;
			default:
				break;
		}
	}
	
	public MessageBoardBean replyMessage(int type, UserBean user, long timeStamp, String message) {
		switch (type) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				return replyMessage(user.getServerId(), timeStamp, message);
			case MessageConst.TYPE_MESSAGE_UNION:
				return replyUnionMessage(user.getUnionId(), timeStamp, message);
			default:
				return replyMessage(user.getUnionId(), timeStamp, message);
		}
	}
	
	private List<MessageBoardBean> getMessageBoardList(UserBean user) {
		List<MessageBoardBean> messageBoardList = messageRedisService.getMessageBoardList(user.getServerId(), user.getReceiveMessageTimeStamp());
		
		user.setReceiveMessageTimeStamp(System.currentTimeMillis());
		userService.updateUser(user);
		
		return messageBoardList;
	}
	
	private void createMessageBoard(UserBean user, String message) {
		MessageBoardBean messageBoard = initMessageBoard(user.getId(), user.getUserName(), message);
		messageRedisService.addMessageBoard(user.getServerId(), messageBoard);
	}
	
	private MessageBoardBean replyMessage(int serverId, long timeStamp, String message) {
		MessageBoardBean messageBoard = messageRedisService.getMessageBoard(serverId, timeStamp);
		if (messageBoard != null) {
			messageRedisService.deleteMessageBoard(serverId, messageBoard);
			messageBoard.addReplyMessage(message);
			messageRedisService.addMessageBoard(serverId, messageBoard);
			
			return messageBoard;
		}
		
		return null;
	}
	
	private List<MessageBoardBean> getUnionMessageBoardList(UserBean user) {
		List<MessageBoardBean> messageBoardList = messageRedisService.getMessageBoardListOfUnion(user.getUnionId(), user.getReceiveMessageTimeStamp());
		
		user.setReceiveMessageTimeStamp(System.currentTimeMillis());
		userService.updateUser(user);
		
		return messageBoardList;
	}
	
	private void createUnionMessageBoard(UserBean user, String message) {
		MessageBoardBean messageBoard = initMessageBoard(user.getId(), user.getUserName(), message);
		messageRedisService.addMessageBoardOfUnion(user.getUnionId(), messageBoard);
	}
	
	private MessageBoardBean replyUnionMessage(int unionId, long timeStamp, String message) {
		MessageBoardBean messageBoard = messageRedisService.getMessageBoardOfUnion(unionId, timeStamp);
		if (messageBoard != null) {
			messageRedisService.deleteMessageBoardOfUnion(unionId, messageBoard);
			messageBoard.addReplyMessage(message);
			messageRedisService.addMessageBoardOfUnion(unionId, messageBoard);
			
			return messageBoard;
		}
		
		return null;
	}
	
	private MessageBoardBean initMessageBoard(long userId, String userName, String message) {
		MessageBoardBean messageBoard = new MessageBoardBean();
		messageBoard.setMessage(message);
		messageBoard.setUserId(userId);
		messageBoard.setUserName(userName);
		messageBoard.setTimeStamp(System.currentTimeMillis());
		
		return messageBoard;
	}
}
