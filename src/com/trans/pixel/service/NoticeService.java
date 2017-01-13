package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.protoc.Commands.Notice;
import com.trans.pixel.service.redis.NoticeRedisService;

@Service
public class NoticeService {
	@Resource
	private NoticeRedisService noticeRedisService;
	
	public void pushNotice(long userId, int type) {
		Notice.Builder builder = Notice.newBuilder();
		builder.setType(type);
		noticeRedisService.pushNotice(userId, builder.build());
	}
	
	public void pushNotice(long userId, int type, long messageId) {
		Notice.Builder builder = Notice.newBuilder();
		builder.setType(type);
		builder.addNoticeId(messageId);
		noticeRedisService.pushNotice(userId, builder.build());
	}
	
	public void delNoticeId(long userId, long messageId) {
		Notice originalNotice = noticeRedisService.selectUserNotice(userId, NoticeConst.TYPE_NOTICEBOARD);
		if (originalNotice == null)
			return;
		Notice.Builder builder = Notice.newBuilder(originalNotice);
		List<Long> messageList = new ArrayList<Long>(builder.getNoticeIdList());
		messageList.remove(messageId);
		if (messageList.isEmpty())
			deleteNotice(userId, NoticeConst.TYPE_NOTICEBOARD);
		else {
			builder.clearNoticeId();
			builder.addAllNoticeId(messageList);
			noticeRedisService.pushNotice(userId, builder.build());
		}
	}
	
	public List<Notice> getNoticeList(long userId) {
		return noticeRedisService.selectUserNoticeList(userId);
	}
	
	public void deleteNotice(long userId, int type) {
		noticeRedisService.deleteNotice(userId, type);
	}
}
