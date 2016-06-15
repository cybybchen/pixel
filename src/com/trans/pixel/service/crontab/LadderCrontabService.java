package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.service.LadderService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.ServerService;

@Service
public class LadderCrontabService {
	private static Logger logger = Logger.getLogger(LadderCrontabService.class);
	
	@Resource
	private LadderService ladderService;
	@Resource
	private ServerService serverService;
	@Resource
	private LogService logService;
	
	@Scheduled(cron = "0 0 21 * * ? ")
//	@Scheduled(cron = "0 0/30 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
//	@Scheduled(cron = "0 55 10 * * ? ")
	public void sendLadderDailyReward() {
		try {
			List<Integer> serverIds = serverService.getServerIdList();
			for (final int serverId : serverIds) {
				Thread thread = new Thread() {
					public void run() {
						logger.debug("send ladder reward:" + serverId);
						logService.sendMonitorRewardLog(serverId, LogString.TYPE_MONITOR_LADDERDAILY, "start");
						ladderService.sendLadderDailyReward(serverId);
						logService.sendMonitorRewardLog(serverId, LogString.TYPE_MONITOR_LADDERDAILY, "end");
					}
				};
				thread.start();
			}
		} catch (Exception e) {
			logger.error("send ladder daily error:" + e);
		}
	}
}
