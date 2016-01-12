package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.service.PvpMapService;

@Service
public class PvpCrontabService {

	@Resource
	private PvpMapService pvpMapService;
	
	@Scheduled(cron = "0 0 0 * * ? ")
	@Transactional(rollbackFor=Exception.class)
	public void fleshPvpRelativeUser() {
		
	}
}
