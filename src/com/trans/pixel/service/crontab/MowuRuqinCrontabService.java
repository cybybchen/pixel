package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.redis.PvpMapRedisService;

@Service
public class MowuRuqinCrontabService {
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MowuRuqinCrontabService.class);

	@Resource
	private PvpMapRedisService pvpMapRedisService;
	@Resource
	private ServerService serverService;
	
//	@Scheduled(cron = "0 0 0 * * ? ")
	@Scheduled(cron = "0 0/30 * * * ? ")
	public void createMowu() {
		List<Integer> serverList = serverService.getServerIdList();
		for(int serverId : serverList) {
			pvpMapRedisService.createMowu(serverId);
		}
	}
	
//	@Scheduled(cron = "0 0 21 * * ? ")
//	public void sendMowuReward() {
//		List<Integer> serverList = serverService.getServerIdList();
//		for(int serverId : serverList) {
//			pvpMapRedisService.sendMowuReward(serverId);
//		}
//	}
}
