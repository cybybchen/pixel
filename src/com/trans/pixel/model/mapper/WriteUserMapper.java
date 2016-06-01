package com.trans.pixel.model.mapper;


public interface WriteUserMapper {
	
	public Object selectWriteUser(String account);
	
	public int insertWriteUser(String account);
	
	public int deleteWriteUser(String account);
}
