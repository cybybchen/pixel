package com.trans.pixel.service.crontab.cache;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.service.redis.LogRedisService;

@Service
public class LogCacheCrontabService {
	private Logger logger = Logger.getLogger(LogCacheCrontabService.class);

	@Resource
	private LogRedisService logRedisService;

	 @Scheduled(cron = "0 0/5 * * * ? ")
//	@Scheduled(cron = "0 0/1 * * * ? ")
	// @Transactional(rollbackFor=Exception.class)
	public void sendLog() {
		// if (!ConfigUtil.CRONTAB_STATUS)
		// return;
		Socket socket = null;
		BufferedInputStream input = null;
		OutputStream netOut = null;
		String log = null;
		try {
			socket = new Socket(LogString.SERVER, LogString.getPort());
			input = new BufferedInputStream(
					socket.getInputStream());
			netOut = socket.getOutputStream();
			DataOutputStream doc = new DataOutputStream(netOut);
			// List<String> logs = logRedisService.popLog();
			while (true) {
				// for (String log : logs) {
				log = logRedisService.popLog();
				if (log == null)
					break;
				String sendLog = log + "\n";
				logger.warn("send to log server " + sendLog);
				doc.write(sendLog.getBytes());
				// netOut.write(log.getBytes());
				// netOut.flush();
				doc.flush();
//				logger.warn("after send to log server " + sendLog);
				StringBuilder sb = new StringBuilder();
				byte[] b = new byte[64];
				int len = 0;
				len = input.read(b);
				sb.append(new String(b, 0, len));
				logger.warn("log ret is:" + sb.toString());
//				if (sb.toString().equals("[[0]]\n"))
//					logger.debug("ret is true");
//				else
//					logger.debug("ret is false");
			}
			// doc.close();
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
			logRedisService.addLogData(log);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logRedisService.addLogData(log);
		} finally {
			if (socket != null) {
				try {
					logger.warn("close log client");
					socket.close();
					netOut.close();
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
