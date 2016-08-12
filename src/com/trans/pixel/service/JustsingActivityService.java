package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.MailBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.service.redis.JustsingActivityRedisService;
import com.trans.pixel.utils.DateUtil;
import com.trans.pixel.utils.FileUtil;

@Service
public class JustsingActivityService {
	private static final String JUSTSING_CDK_FILE_PATH = "/var/www/html/Pixel/justsing/cdk_";
	@Resource
	private JustsingActivityRedisService redis;
	@Resource
	private MailService mailService;
	
	public String getJustsingCdk(int type) {
		String cdk = redis.getCdk(type);
		if (cdk == null) {
			List<String> cdkList = FileUtil.readTxtFile(JUSTSING_CDK_FILE_PATH + type + ".txt");
			redis.setCdkList(type, cdkList);
			cdk = redis.getCdk(type);
		}
		
		return cdk;
	}
	
	public void sendJustsingCdk(UserBean user, int type) {
		if (user.getIdfa().isEmpty() || redis.hasinJustsingCdkRecord(user.getIdfa(), type) 
				|| DateUtil.timeIsBefore(user.getRegisterTime(), redis.getJustsingActivityAvailableTime()))
			return;
		
		String cdk = getJustsingCdk(type);
		sendJustsingCdkMail(user, cdk);
		redis.addJustsingCdkRecord(user.getIdfa(), type);
	}
	
	private void sendJustsingCdkMail(UserBean user, String cdk) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		String content = "你获取的心动K歌的兑换码是：" + cdk;
		MailBean mail = MailBean.buildSystemMail(user.getId(), content, rewardList);
		mailService.addMail(mail);
	}
}
