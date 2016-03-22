package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.service.UnionService;

@Service
public class UnionFightCrontabService {
	@Resource
	private UnionService unionService;
	
	@Scheduled(cron = "0 0 21 * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void unionFight() {
		unionService.unionFightTask();
	}
}
