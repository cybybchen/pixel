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

	// @Scheduled(cron = "0 0/5 * * * ? ")
	@Scheduled(cron = "0 0/1 * * * ? ")
	// @Transactional(rollbackFor=Exception.class)
	public void sendLog() {
		// if (!ConfigUtil.CRONTAB_STATUS)
		// return;
		Socket socket = null;
		BufferedInputStream input = null;
		OutputStream netOut = null;
		try {
			socket = new Socket(LogString.SERVER, LogString.getPort());
			input = new BufferedInputStream(
					socket.getInputStream());
			netOut = socket.getOutputStream();
			DataOutputStream doc = new DataOutputStream(netOut);
			// List<String> logs = logRedisService.popLog();
			while (true) {
				// for (String log : logs) {
				String log = logRedisService.popLog();
				if (log == null)
					break;
				log += "\n";
				logger.warn("send to log server " + log);
				doc.write(log.getBytes());
				// netOut.write(log.getBytes());
				// netOut.flush();
				doc.flush();
				logger.warn("after send to log server " + log);
				StringBuilder sb = new StringBuilder();
				byte[] b = new byte[1024];
				int len = 0;
//				String message = "";
				while ((len = input.read(b)) != -1) {
					sb.append(new String(b, 0, len));
				}
				logger.debug("log ret is:" + sb.toString());
				if (!sb.toString().equals("[[0]]"))
					logRedisService.addLogData(log);
			}
			// doc.close();
		} catch (UnknownHostException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			if (socket != null) {
				try {
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
