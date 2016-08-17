package com.trans.pixel.model.mapper;

import java.util.List;

import com.trans.pixel.model.RechargeBean;

public interface RechargeMapper {
	
	public int insertUserRechargeRecord(RechargeBean recharge);
	
	public List<RechargeBean> getRechargeRecord();

}
