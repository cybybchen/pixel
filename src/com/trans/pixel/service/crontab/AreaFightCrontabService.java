package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.service.AreaFightService;
import com.trans.pixel.utils.ConfigUtil;

@Service
public class AreaFightCrontabService {
	@Resource
	private AreaFightService areaFightService;
	
//	@Scheduled(cron = "0 0/1 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void areaFight() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		areaFightService.calFight();
	}
}
