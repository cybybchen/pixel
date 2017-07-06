package com.trans.pixel.service.crontab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.service.HeartBeatService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.utils.ConfigUtil;

@Service
public class HeartBeatCrontabService {
	@SuppressWarnings("unused")
	private Logger log = Logger.getLogger(HeartBeatCrontabService.class);
	
	@Resource
	private HeartBeatService heartBeatService;
	@Resource
	private LogService logService;
	@Resource
	private ServerService serverService;
	
	@Scheduled(cron = "0 0/10 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void handleHeartBeatReward() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		String time = df.format(new Date());
		
		List<Integer> serverList = serverService.getServerIdList();
		for (int serverId : serverList) {
			Map<String, String> params = buildLogParams(serverId, heartBeatService.getHeartBeatCount(serverId), time);
			logService.sendLog(params, LogString.LOGTYPE_HEARTBEAT);
		}
	}
	
	private Map<String, String> buildLogParams(int serverId, long count, String time) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, "" + serverId);
		params.put(LogString.COUNT, "" + count);
		params.put(LogString.TIME, time);
		
		return params;
	}
}
