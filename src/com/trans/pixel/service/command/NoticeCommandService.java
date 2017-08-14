package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.MessageBoardProto.Notice;
import com.trans.pixel.protoc.MessageBoardProto.ResponseNoticeCommand;
import com.trans.pixel.service.NoticeMessageService;
import com.trans.pixel.service.NoticeService;
import com.trans.pixel.service.UnionService;

@Service
public class NoticeCommandService {

	@Resource
	private NoticeService noticeService;
	@Resource
	private NoticeMessageService noticeMessageService;
	@Resource
	private UnionService unionService;
	
	public void pushNotices(Builder responseBuilder, UserBean user) {
		List<Notice> noticeList = noticeService.getNoticeList(user.getId());
		ResponseNoticeCommand.Builder builder = null;
		if (responseBuilder.hasNoticeCommand())
			builder = responseBuilder.getNoticeCommandBuilder();
		else 
			builder = ResponseNoticeCommand.newBuilder();
		builder.addAllNotice(noticeList);
		List<String> noticeMessageList = noticeMessageService.getNoticeMessageList(user);
		builder.addAllMessage(noticeMessageList);
		responseBuilder.setNoticeCommand(builder.build());
//		pushUnionNotices(responseBuilder, user);
	}
	
	public void pushUnionNotices(Builder responseBuilder, UserBean user) {
		if (user.getUnionId() == 0)
			return;
		ResponseNoticeCommand.Builder builder = null;
		if (responseBuilder.hasNoticeCommand())
			builder = responseBuilder.getNoticeCommandBuilder();
		else 
			builder = ResponseNoticeCommand.newBuilder();
		List<Notice> unionNitice = unionService.getUnionNotice(user);
		builder.addAllNotice(unionNitice);
		
		responseBuilder.setNoticeCommand(builder.build());
	}
}
