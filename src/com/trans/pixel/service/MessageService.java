package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.BossRoomRecord;
import com.trans.pixel.service.redis.MessageRedisService;

@Service
public class MessageService {	
	@Resource
	private MessageRedisService messageRedisService;
	@Resource
	private UserService userService;
	@Resource
	private NoticeService noticeService;
	@Resource
	private BossService bossService;

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
	
	public void createMessageBoard(int type, UserBean user, String message, int groupId, int bossId) {
		switch (type) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				createMessageBoard(user, message);
				break;
			case MessageConst.TYPE_MESSAGE_UNION:
				createUnionMessageBoard(user, message, groupId, bossId);
				break;
			default:
				break;
		}
	}
	
	public MessageBoardBean getMessage(int type, String id, UserBean user) {
		switch (type) {
		case MessageConst.TYPE_MESSAGE_NORMAL:
			return messageRedisService.getMessageBoardById(user.getServerId(), id);
		case MessageConst.TYPE_MESSAGE_UNION:
			return messageRedisService.getUnionMessageBoardById(user.getUnionId(), id);
		default:
			return messageRedisService.getMessageBoardById(user.getServerId(), id);
	}
	}
	
	public MessageBoardBean replyMessage(int type, UserBean user, String id, String message) {
		switch (type) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				return replyMessage(user.getServerId(), id, message);
			case MessageConst.TYPE_MESSAGE_UNION:
				return replyUnionMessage(user.getUnionId(), id, message);
			default:
				return replyMessage(user.getUnionId(), id, message);
		}
	}
	
	private List<MessageBoardBean> getMessageBoardList(UserBean user) {
		List<MessageBoardBean> messageBoardList = messageRedisService.getMessageBoardList(user.getServerId(), user.getReceiveMessageTimeStamp());
		
		user.setReceiveMessageTimeStamp(System.currentTimeMillis());
		userService.updateUser(user);
		
		return messageBoardList;
	}
	
	private void createMessageBoard(UserBean user, String message) {
		MessageBoardBean messageBoard = initMessageBoard(user, message);
		messageRedisService.addMessageBoard(user.getServerId(), messageBoard);
		messageRedisService.addMessageBoardValue(user.getServerId(), messageBoard);
	}
	
	private MessageBoardBean replyMessage(int serverId, String id, String message) {
		MessageBoardBean messageBoard = messageRedisService.getMessageBoardById(serverId, id);
		if (messageBoard != null) {
//			messageRedisService.deleteMessageBoard(serverId, messageBoard);
			messageBoard.addReplyMessage(message);
			messageBoard.setTimeStamp(System.currentTimeMillis());
			messageRedisService.addMessageBoard(serverId, messageBoard);
			messageRedisService.addMessageBoardValue(serverId, messageBoard);
			
			sendMessageNotice(messageBoard.getUserId(), messageBoard.getId());
			
			return messageBoard;
		}
		
		return null;
	}
	
	private void sendMessageNotice(long userId, long messageId) {
		noticeService.pushNotice(userId, NoticeConst.TYPE_NOTICEBOARD, messageId);
	}
	
	private List<MessageBoardBean> getUnionMessageBoardList(UserBean user) {
		List<MessageBoardBean> messageBoardList = messageRedisService.getMessageBoardListOfUnion(user.getUnionId(), user.getReceiveMessageTimeStamp());
		
		user.setReceiveMessageTimeStamp(System.currentTimeMillis());
		userService.updateUser(user);
		
		return messageBoardList;
	}
	
	private void createUnionMessageBoard(UserBean user, String message, int groupId, int bossId) {
		MessageBoardBean messageBoard = initMessageBoard(user, message);
		messageBoard.setGroupId(groupId);
		messageBoard.setBossId(bossId);
		if (groupId != 0 && bossId != 0) {
			BossRoomRecord bossRoom = bossService.getBossRoomRecord(user);
			messageBoard.setStartDate(bossRoom.getCreateTime());
		}
		messageRedisService.addMessageBoardOfUnion(user.getUnionId(), messageBoard);
		messageRedisService.addUnionMessageBoardValue(user.getUnionId(), messageBoard);
	}
	
	private MessageBoardBean replyUnionMessage(int unionId, String id, String message) {
		MessageBoardBean messageBoard = messageRedisService.getUnionMessageBoardById(unionId, id);
		if (messageBoard != null) {
//			messageRedisService.deleteMessageBoardOfUnion(unionId, messageBoard);
			messageBoard.addReplyMessage(message);
			messageBoard.setTimeStamp(System.currentTimeMillis());
			messageRedisService.addMessageBoardOfUnion(unionId, messageBoard);
			messageRedisService.addUnionMessageBoardValue(unionId, messageBoard);
			
			return messageBoard;
		}
		
		return null;
	}
	
	private MessageBoardBean initMessageBoard(UserBean user, String message) {
		MessageBoardBean messageBoard = new MessageBoardBean();
		messageBoard.setMessage(message);
		messageBoard.setUserId(user.getId());
		messageBoard.setUserName(user.getUserName());
		messageBoard.setIcon(user.getIcon());
		messageBoard.setTimeStamp(System.currentTimeMillis());
		messageBoard.setId(System.currentTimeMillis());
		messageBoard.setJob(user.getUnionJob());
		messageBoard.setVip(user.getVip());
		
		return messageBoard;
	}
}
