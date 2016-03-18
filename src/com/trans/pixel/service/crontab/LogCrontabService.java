package com.trans.pixel.service.crontab;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trans.pixel.constants.LogString;
import com.trans.pixel.service.redis.LogRedisService;

@Service
public class LogCrontabService {
	private Logger utilLogger = Logger.getLogger(LogCrontabService.class);
	
	@Resource
	private LogRedisService logRedisService;
	
	@Scheduled(cron = "0 0/30 * * * ? ")
	@Transactional(rollbackFor=Exception.class)
	public void sendLog() {
		Socket socket = null;
		try {
			socket = new Socket(LogString.SERVER, LogString.PORT);
			OutputStream netOut = socket.getOutputStream();
			DataOutputStream doc = new DataOutputStream(netOut);
			while (true) {
				String log = logRedisService.popLog();
				if (log == null)
					break;
				log += "\n";
				utilLogger.debug("send to log server " + log);
				doc.writeBytes(log);
				doc.flush();
			}

			netOut.close();
			doc.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
