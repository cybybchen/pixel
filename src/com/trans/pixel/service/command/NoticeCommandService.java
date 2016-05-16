package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Notice;
import com.trans.pixel.protoc.Commands.ResponseNoticeCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.NoticeService;

@Service
public class NoticeCommandService {

	@Resource
	private NoticeService noticeService;
	
	public void pushNotices(Builder responseBuilder, UserBean user) {
		List<Notice> noticeList = noticeService.getNoticeList(user.getId());
		ResponseNoticeCommand.Builder builder = ResponseNoticeCommand.newBuilder();
		builder.addAllNotice(noticeList);
		
		responseBuilder.setNoticeCommand(builder.build());
	}
}
