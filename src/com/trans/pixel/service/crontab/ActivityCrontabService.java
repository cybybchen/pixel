package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.ServerService;

@Service
public class ActivityCrontabService {

	@Resource
	private ActivityService activityService;
	@Resource
	private ServerService serverService;
	@Resource
	private LogService logService;
	
	@Scheduled(cron = "0 0 0 * * ? ")
//	@Scheduled(cron = "0 0/5 * * * ? ")
	public void sendActivityReward() {
		List<Integer> serverList = serverService.getServerIdList();
		for (int serverId : serverList) {
			logService.sendMonitorRewardLog(serverId, LogString.TYPE_MONITOR_KAIFU2ACTIVITY, "start");
			activityService.sendKaifu2ActivitiesReward(serverId);
			logService.sendMonitorRewardLog(serverId, LogString.TYPE_MONITOR_KAIFU2ACTIVITY, "end");
		}
	}
}
