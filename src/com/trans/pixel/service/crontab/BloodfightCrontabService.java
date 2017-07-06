package com.trans.pixel.service.crontab;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.service.BloodfightService;
import com.trans.pixel.service.ServerService;
import com.trans.pixel.utils.ConfigUtil;

@Service
public class BloodfightCrontabService {

	@Resource
	private BloodfightService bloodfightService;
	@Resource
	private ServerService serverService;
	
//	@Scheduled(cron = "0 0 0 * * ? ")
	public void handle() {
		if (!ConfigUtil.CRONTAB_STATUS)
			return;
		List<Integer> serverList = serverService.getServerIdList();
		for (int serverId : serverList) {
			bloodfightService.handle(serverId);
		}
	}
}
