package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.JustsingConst;
import com.trans.pixel.constants.MailConst;
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
		sendJustsingCdkMail(user, cdk, type);
		redis.addJustsingCdkRecord(user.getIdfa(), type);
	}
	
	private void sendJustsingCdkMail(UserBean user, String cdk, int justsingType) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		String content = "恭喜您获得《心动K歌》";
		if (justsingType == JustsingConst.TYPE_REGISTER)
			content += "翅膀礼包兑换码";
		else
			content += "对戒礼包兑换码";
		
		content += cdk + "。您可以去AppStore或各大安卓市场下载《心动K歌》游戏，在\"活动-礼品兑换\"界面输入兑换码，领取奖励。";
		MailBean mail = MailBean.buildSpecialSystemMail(user.getId(), content, rewardList, MailConst.TYPE_SPECIAL_SYSTEM_MAIL);
		mailService.addMail(mail);
	}
}
