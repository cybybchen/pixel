package com.trans.pixel.service.crontab.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.service.redis.LogRedisService;

@Service
public class LogCacheCrontabService {
	private Logger utilLogger = Logger.getLogger(LogCacheCrontabService.class);
	
	@Resource
	private LogRedisService logRedisService;
	
	@Scheduled(cron = "0 0/5 * * * ? ")
//	@Transactional(rollbackFor=Exception.class)
	public void sendLog() {
//		if (!ConfigUtil.CRONTAB_STATUS)
//			return;
		Socket socket = null;
		try {
			socket = new Socket(LogString.SERVER, LogString.getPort());
			OutputStream netOut = socket.getOutputStream();
//			DataOutputStream doc = new DataOutputStream(netOut);
			List<String> logs = logRedisService.popLog();
//			while (true) {
			for (String log : logs) {
//				String log = logRedisService.popLog();
				if (log == null)
					break;
				log += "\n";
				utilLogger.warn("send to log server " + log);
//				doc.writeChars(log);
				netOut.write(log.getBytes());
				netOut.flush();
//				doc.flush();
			}

			netOut.close();
//			doc.close();
		} catch (UnknownHostException e) {
			utilLogger.error(e);
		} catch (IOException e) {
			utilLogger.error(e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
