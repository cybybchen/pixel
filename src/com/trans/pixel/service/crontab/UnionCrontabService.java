package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.UnionConst;
import com.trans.pixel.protoc.UnionProto.ResponseUnionFightApplyRecordCommand.UNION_FIGHT_STATUS;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.service.UnionService;
import com.trans.pixel.utils.ConfigUtil;
import com.trans.pixel.utils.DateUtil;

@Service
public class UnionCrontabService {
	private static final Logger log = LoggerFactory.getLogger(UnionCrontabService.class);
	@Resource
	private UnionService unionService;
	@Resource
	private ServerService serverService;
	
//	@Scheduled(cron = "0 0 21 * * ? ")
////	@Transactional(rollbackFor=Exception.class)
//	public void unionFight() {
//		if (!ConfigUtil.CRONTAB_STATUS)
//			return;
//		unionService.unionFightTask();
//	}
	
	@Scheduled(cron = "0 0 0 * * ? ")
//	@Scheduled(cron = "0 0 13 * * ? ")
//	@Scheduled(cron = "0 0/30 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void unionBoss() {//type=4
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		
		List<Integer> serverIds = serverService.getServerIdList();
		for (int serverId : serverIds)
			unionService.doUndeadUnionBossRankReward(serverId);
	}
	
//	@Scheduled(cron = "0 0 0 * * ? ")
	@Scheduled(cron = "0 0/5 * * * ? ")
	public void unionFight() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		
		log.error("current day is:" + DateUtil.getDayOfWeek());
		UNION_FIGHT_STATUS status = unionService.calUnionFightStatus(0);
		if (status.equals(UNION_FIGHT_STATUS.APPLY_TIME))
			unionService.deleteLastRecord();
		else if (status.equals(UNION_FIGHT_STATUS.FIGHT_TIME))
			unionService.calUnionFight();
	}
}
