package com.trans.pixel.service.redis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.stereotype.Repository;

import com.trans.pixel.constants.RedisExpiredConst;
import com.trans.pixel.constants.RedisKey;
import com.trans.pixel.protoc.MessageBoardProto.Notice;

@Repository
public class NoticeRedisService extends RedisService {
	
	public void pushNotice(final long userId, final Notice notice) {
		if(userId < 0)
			return;
		String key = buildRedisKey(userId);
		hput(key, "" + notice.getType(), formatJson(notice), userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	public List<Notice> selectUserNoticeList(final long userId) {
		String key = buildRedisKey(userId);
		List<Notice> userNoticeList = new ArrayList<Notice>();
		Iterator<Entry<String, String>> ite = hget(key, userId).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			Notice.Builder builder = Notice.newBuilder();
			if (parseJson(entry.getValue(), builder))
				userNoticeList.add(builder.build());
		}
		
		return userNoticeList;
	}
	
	public Notice selectUserNotice(final long userId, final int type) {
		String key = buildRedisKey(userId);
		String value = this.hget(key, "" + type, userId);
		
		if (value == null)
			return null;
		
		Notice.Builder builder = Notice.newBuilder();
		if (parseJson(value, builder))
			return builder.build();
		
		return null;
	}
	
	public void deleteNotice(final long userId, final int type) {
		String key = buildRedisKey(userId);
		this.hdelete(key, "" + type, userId);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY, userId);
	}
	
	private String buildRedisKey(long userId) {
		return RedisKey.NOTICE_PREFIX + userId;
	}
	
	public void pushUnionNotice(final int unionId, final Notice notice) {
		if(unionId <= 0)
			return;
		String key = RedisKey.UNION_NOTICE_PREFIX + unionId;
		hput(key, "" + notice.getType(), formatJson(notice));
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public void deleteUnionNotice(final int unionId, final int type) {
		String key = RedisKey.UNION_NOTICE_PREFIX + unionId;
		hdelete(key, "" + type);
		expire(key, RedisExpiredConst.EXPIRED_USERINFO_7DAY);
	}
	
	public List<Notice> selectUnionNoticeList(final int unionId) {
		String key = RedisKey.UNION_NOTICE_PREFIX + unionId;
		List<Notice> userNoticeList = new ArrayList<Notice>();
		Iterator<Entry<String, String>> ite = hget(key).entrySet().iterator();
		while (ite.hasNext()) {
			Entry<String, String> entry = ite.next();
			Notice.Builder builder = Notice.newBuilder();
			if (parseJson(entry.getValue(), builder))
				userNoticeList.add(builder.build());
		}
		
		return userNoticeList;
	}
}
