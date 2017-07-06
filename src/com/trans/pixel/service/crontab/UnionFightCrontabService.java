package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.service.UnionService;
import com.trans.pixel.utils.ConfigUtil;

@Service
public class UnionFightCrontabService {
	@Resource
	private UnionService unionService;
	
	@Scheduled(cron = "0 0 21 * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void unionFight() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		unionService.unionFightTask();
	}
}
