package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.protoc.MessageBoardProto.Notice;
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
		Notice originalNotice = noticeRedisService.selectUserNotice(userId, NoticeConst.TYPE_NOTICEBOARD);
		Notice.Builder builder = Notice.newBuilder();
		if (originalNotice != null)
			builder = Notice.newBuilder(noticeRedisService.selectUserNotice(userId, NoticeConst.TYPE_NOTICEBOARD));
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
	
	public void pushUnionNotice(int unionId, int type) {
		Notice.Builder builder = Notice.newBuilder();
		builder.setType(type);
		noticeRedisService.pushUnionNotice(unionId, builder.build());
	}
	
	public void deleteUnionNotice(int unionId, int type) {
		noticeRedisService.deleteUnionNotice(unionId, type);
	}
	
	public List<Notice> getUnionNoticeList(int unionId) {
		return noticeRedisService.selectUnionNoticeList(unionId);
	}
}
