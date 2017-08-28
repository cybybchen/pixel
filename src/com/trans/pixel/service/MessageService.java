package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.MessageConst;
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.model.MessageBoardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.FightInfo;
import com.trans.pixel.protoc.Base.UserInfo;
import com.trans.pixel.protoc.MessageBoardProto.RequestCreateMessageBoardCommand;
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
	private RewardTaskService rewardTaskService;
	@Resource
	private FightInfoService fightInfoService;
	@Resource
	private BlackListService blackListService;

	public List<MessageBoardBean> getMessageBoardList(int type, UserBean user, int itemId) {
		switch (type) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				return getMessageBoardList(user);
			case MessageConst.TYPE_MESSAGE_UNION:
				return getUnionMessageBoardList(user);
			case MessageConst.TYPE_MESSAGE_HERO:
				return getHeroMessageBoardList(user, itemId);
			default:
				return new ArrayList<MessageBoardBean>();
		}
	}
	
	public void createMessageBoard(RequestCreateMessageBoardCommand cmd, UserBean user) {
		switch (cmd.getType()) {
			case MessageConst.TYPE_MESSAGE_NORMAL:
				createMessageBoard(user, cmd.getMessage());
				break;
			case MessageConst.TYPE_MESSAGE_UNION:
				createUnionMessageBoard(user, cmd.getMessage(), cmd.getGroupId(), cmd.getBossId(), cmd.getFightId());
				break;
			case MessageConst.TYPE_MESSAGE_HERO:
				createHeroMessageBoard(user, cmd.getItemId(), cmd.getMessage());
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
	
	public MessageBoardBean zanHeroMessage(UserBean user, int itemId, String id, boolean zan) {
			return zanHeroMessage(user.getServerId(), itemId, id, zan);
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
			
			sendMessageNotice(messageBoard.getUser().getId(), messageBoard.getId());
			
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
	
	private void createUnionMessageBoard(UserBean user, String message, int groupId, int bossId, int fightId) {
		MessageBoardBean messageBoard = initMessageBoard(user, message);
		messageBoard.setGroupId(groupId);
		messageBoard.setBossId(bossId);
		if (groupId != 0 && bossId != 0) {
			messageRedisService.addMessageBoardOfUnion(user.getUnionId(), messageBoard);
			messageRedisService.addUnionMessageBoardValue(user.getUnionId(), messageBoard);
		}else if(fightId !=0) {
			List<FightInfo.Builder> list = fightInfoService.getFightInfoList(user);
			for(FightInfo.Builder info : list){
				if(fightId == info.getId()){
					messageBoard.setMessage(info.getId()+"|"+info.getFightInfo()+"|"+info.getFightData());
					messageRedisService.addMessageBoardOfUnion(user.getUnionId(), messageBoard);
					messageRedisService.addUnionMessageBoardValue(user.getUnionId(), messageBoard);
					break;
				}
			}
		}else{
			messageRedisService.addMessageBoardOfUnion(user.getUnionId(), messageBoard);
			messageRedisService.addUnionMessageBoardValue(user.getUnionId(), messageBoard);
		}
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
//		messageBoard.setUserId(user.getId());
//		messageBoard.setUserName(user.getUserName());
//		messageBoard.setIcon(user.getIcon());
		messageBoard.setTimeStamp(System.currentTimeMillis());
		messageBoard.setId(System.currentTimeMillis());
//		messageBoard.setJob(user.getUnionJob());
//		messageBoard.setVip(user.getVip());
		messageBoard.setUser(user.buildShort());
		
		return messageBoard;
	}
	
	//hero message
	private void createHeroMessageBoard(UserBean user, int itemId, String message) {
		List<MessageBoardBean> messageBoardList = getHeroMessageBoardList(user, itemId);
		for (MessageBoardBean messageBoard : messageBoardList) {
			if (messageBoard.getUser().getId() == user.getId()) {
				messageRedisService.deleteHeroMessageBoard(user.getServerId(), itemId, "" + messageBoard.getId());
				messageRedisService.delHeroMessage_normal(user.getServerId(), itemId, "" + messageBoard.getId());
				messageRedisService.delHeroMessage_top(user.getServerId(), itemId, "" + messageBoard.getId());
			}
		}
		MessageBoardBean messageBoard = initMessageBoard(user, message);
		messageRedisService.addHeroMessageBoardValue(user.getServerId(), itemId, messageBoard);
		messageRedisService.addHeroMessageBoardTop(user.getServerId(), itemId, messageBoard);
	}
	
	private MessageBoardBean zanHeroMessage(int serverId, int itemId, String id, boolean zan) {
		MessageBoardBean messageBoard = messageRedisService.getHeroMessageBoard(serverId, itemId, id);
		if (messageBoard != null) {
			if (zan)
				messageBoard.setReplyCount(messageBoard.getReplyCount() + 1);
			else
				messageBoard.setReplyCount(messageBoard.getReplyCount() - 1);
			
			messageRedisService.addHeroMessageBoardTop(serverId, itemId, messageBoard);
			messageRedisService.addHeroMessageBoardValue(serverId, itemId, messageBoard);
			
			
			return messageBoard;
		}
		
		return null;
	}
	
	private List<MessageBoardBean> getHeroMessageBoardList(UserBean user, int itemId) {
		List<MessageBoardBean> messageBoardList = new ArrayList<MessageBoardBean>();
		messageBoardList.addAll(messageRedisService.getHeroMessageBoardList_top(user.getServerId(), itemId));
		messageBoardList.addAll(messageRedisService.getHeroMessageBoardList_normal(user.getServerId(), itemId));
		
		for (int i = 0; i < messageBoardList.size(); ++i) {
			MessageBoardBean message = messageBoardList.get(i);
			UserInfo userinfo = message.getUser();
			if (userinfo == null)
				continue;
			
			if (userinfo.getId() != user.getId() && blackListService.isNodiscuss(user.getId())) {
				messageBoardList.remove(i);
				i--;
			}
		}
		
		return messageBoardList;
	}
}
