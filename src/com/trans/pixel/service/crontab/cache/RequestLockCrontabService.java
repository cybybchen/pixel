package com.trans.pixel.service.crontab.cache;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.service.ManagerService;

@Service
public class RequestLockCrontabService {
//	private Logger utilLogger = Logger.getLogger(RequestLockCrontabService.class);
	
	@Resource
	private ManagerService managerService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
	public void buildConfig() {
		managerService.buildRequest();
	}
}
