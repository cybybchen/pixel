package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.trans.pixel.model.RechargeBean;

public interface RechargeMapper {
	
	public int insertUserRechargeRecord(RechargeBean recharge);

	public RechargeBean getUserRechargeRecord(@Param("orderId")String orderId);
	
	public List<RechargeBean> getRechargeRecord();

}
