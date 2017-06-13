package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.service.AreaFightService;

@Service
public class AreaFightCrontabService {
	@Resource
	private AreaFightService areaFightService;
	
//	@Scheduled(cron = "0 0/1 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void areaFight() {
		areaFightService.calFight();
	}
}
