package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.userinfo.RaidBean;
import com.trans.pixel.model.userinfo.RaidBean;

public interface RaidMapper {
	public List<RaidBean> getRaids(long userid);
	public void delRaid(@Param("userid")long userid, @Param("id")int id);
	public int updateRaid(RaidBean bean);
}