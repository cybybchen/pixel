package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.userinfo.UserTaskBean;

public interface UserTaskMapper {
	
	public int updateUserTask1(UserTaskBean userTask);
	
	public List<UserTaskBean> selectUserTask1List(long userId);
	
	public int updateUserTask2(UserTaskBean userTask);
	
	public List<UserTaskBean> selectUserTask2List(long userId);
}
