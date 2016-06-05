package com.trans.pixel.service.crontab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.constants.TimeConst;
import com.trans.pixel.service.HeartBeatService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class HeartBeatCrontabService {
	private Logger logger = Logger.getLogger(HeartBeatCrontabService.class);
	
	@Resource
	private HeartBeatService heartBeatService;
	@Resource
	private LogService logService;
	
	@Scheduled(cron = "0 0/10 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void handleHeartBeatReward() {
		Map<String, String> heartBeatMap = heartBeatService.getHeartBeatDetail();
		
		SimpleDateFormat df = new SimpleDateFormat(TimeConst.DEFAULT_DATETIME_FORMAT);
		String time = df.format(new Date());
		
		Iterator<Entry<String, String>> it = heartBeatMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			Map<String, String> params = buildLogParams(entry.getKey(), entry.getValue(), time);
			logService.sendLog(params, LogString.LOGTYPE_HEARTBEAT);
		}
	}
	
	private Map<String, String> buildLogParams(String serverId, String count, String time) {
		Map<String, String> params = new HashMap<String, String>();
		params.put(LogString.SERVERID, serverId);
		params.put(LogString.CHANNEL, count);
		params.put(LogString.TIME, time);
		
		return params;
	}
}
