package com.trans.pixel.service.crontab;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PvpCrontabService {

	@Scheduled(cron = "0 0 0 * * ? ")
	@Transactional(rollbackFor=Exception.class)
	public void fleshPvpRelativeUser() {
		
	}
}
