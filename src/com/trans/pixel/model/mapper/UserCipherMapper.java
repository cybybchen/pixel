package com.trans.pixel.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserCipherMapper {
	
	public int addCipher(@Param("cipherId") long cipherId, @Param("userId") long userId);
	
	public List<Long> getUserIds(int cipherId);
}
