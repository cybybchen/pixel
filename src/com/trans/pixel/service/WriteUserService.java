package com.trans.pixel.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.mapper.WriteUserMapper;

@Service
public class WriteUserService {
	
	@Resource
	private WriteUserMapper writeUserMapper;
	
	public boolean isWriteUser(String account) {
		Object id = writeUserMapper.selectWriteUser(account);
		if (id != null) {
			return true;
		}
		
		return false;
	}
	
	public void insertWriteUser(String account) {
		writeUserMapper.insertWriteUser(account);
	}
	
	public void deleteWriteUser(String account) {
		writeUserMapper.deleteWriteUser(account);
	}
}
