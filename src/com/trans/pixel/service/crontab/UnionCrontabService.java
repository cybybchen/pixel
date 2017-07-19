package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.UnionService;
import com.trans.pixel.utils.ConfigUtil;

@Service
public class UnionCrontabService {
	@Resource
	private UnionService unionService;
	@Resource
	private ServerService serverService;
	
	@Scheduled(cron = "0 0 21 * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void unionFight() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		unionService.unionFightTask();
	}
	
//	@Scheduled(cron = "0 0 0 * * ? ")
//	@Scheduled(cron = "0 0 13 * * ? ")
	@Scheduled(cron = "0 0/30 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void unionBoss() {//type=4
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		
		List<Integer> serverIds = serverService.getServerIdList();
		for (int serverId : serverIds)
			unionService.doUndeadUnionBossRankReward(serverId);
	}
}
