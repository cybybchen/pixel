package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.EventBean;
import com.trans.pixel.model.userinfo.UserLevelBean;

public interface UserLevelMapper {
	public UserLevelBean getUserLevel(long userId);
	public int updateUserLevel(UserLevelBean userLevelRecord);
	public List<EventBean> getEvents(long userId);
	public void delEvent(@Param("userId")long userId, @Param("eventid")int eventid);
	public int updateEvent(EventBean bean);
}