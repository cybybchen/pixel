package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.ServerService;

@Service
public class BossCrontabService {
	private static final Logger log = LoggerFactory.getLogger(BossCrontabService.class);
	
	@Resource
	private ActivityService activityService;
	@Resource
	private ServerService serverService;
	@Resource
	private LogService logService;
	
	@Scheduled(cron = "0 0 0 * * ? ")
//	@Scheduled(cron = "0 0/1 * * * ? ")
	public void sendActivityReward() {
		try {
			List<Integer> serverList = serverService.getServerIdList();
			for (int serverId : serverList) {
				logService.sendMonitorRewardLog(serverId, LogString.TYPE_MONITOR_KAIFU2ACTIVITY, "start");
				activityService.sendKaifu2ActivitiesReward(serverId);
				logService.sendMonitorRewardLog(serverId, LogString.TYPE_MONITOR_KAIFU2ACTIVITY, "end");
			}
		} catch (Exception e) {
			log.error("send activity reward error:" + e);
		}
	}
}
