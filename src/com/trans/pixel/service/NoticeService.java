package com.trans.pixel.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

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
	
	public List<Notice> getNoticeList(long userId) {
		return noticeRedisService.selectUserNoticeList(userId);
	}
	
	public void deleteNotice(long userId, int type) {
		noticeRedisService.deleteNotice(userId, type);
	}
}
