package com.trans.pixel.service.crontab;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.service.LadderService;

@Service
public class LadderCrontabService {
	@Resource
	private LadderService ladderService;
	
	@Scheduled(cron = "0 0 21 * * ? ")
//	@Scheduled(cron = "0 0/5 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void sendLadderDailyReward() {
		ladderService.sendLadderDailyReward();
	}
}
