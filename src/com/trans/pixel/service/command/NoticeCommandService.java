package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Notice;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseNoticeCommand;
import com.trans.pixel.service.NoticeMessageService;
import com.trans.pixel.service.NoticeService;

@Service
public class NoticeCommandService {

	@Resource
	private NoticeService noticeService;
	@Resource
	private NoticeMessageService noticeMessageService;
	
	public void pushNotices(Builder responseBuilder, UserBean user) {
		List<Notice> noticeList = noticeService.getNoticeList(user.getId());
		ResponseNoticeCommand.Builder builder = ResponseNoticeCommand.newBuilder();
		builder.addAllNotice(noticeList);
		List<String> noticeMessageList = noticeMessageService.getNoticeMessageList(user);
		builder.addAllMessage(noticeMessageList);
		
		responseBuilder.setNoticeCommand(builder.build());
	}
}
