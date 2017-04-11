package com.trans.pixel.service.command;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RechargeProto.RequestCdkeyCommand;
import com.trans.pixel.protoc.ShopProto.Cdkey;
import com.trans.pixel.service.CdkeyService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.redis.RedisService;
import com.trans.pixel.utils.HttpUtil;

@Service
public class CdkeyCommandService extends BaseCommandService {
	private Logger log = Logger.getLogger(CdkeyCommandService.class);
	@Resource
	private CdkeyService service;
	@Resource
	private RewardService rewardService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private LogService logService;
	
	public void useCdkey(RequestCdkeyCommand cmd, Builder responseBuilder, UserBean user) {
		Cdkey.Builder cdkey = Cdkey.newBuilder();
		String rewardedstr = service.getCdkeyRewardedStr(user);
		String key = cmd.getKey().toLowerCase();
		Properties props = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream("/config/advancer.properties");
			props.load(in);
			String masterserver = props.getProperty("masterserver");
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("use-Cdkey-master", key);
			parameters.put("rewarded", rewardedstr);
			String message = HttpUtil.post(masterserver, parameters);
			log.info(masterserver+" : "+message);
			if(!RedisService.parseJson(message, cdkey)){
				logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  RedisService.formatJson(cmd), ErrorConst.CDKEY_INVALID);
				
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_INVALID));
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  RedisService.formatJson(cmd), ErrorConst.CDKEY_INVALID);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_INVALID));
			return;
		}
		if(cdkey.getUsed() == 1){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  RedisService.formatJson(cmd), ErrorConst.CDKEY_USED);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_USED));
		}else if(cdkey.getUsed() == 2){
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(),  RedisService.formatJson(cmd), ErrorConst.CDKEY_REWARDED);
			
			responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_REWARDED));
		}else{
			List<String> rewarded = new ArrayList<String>();
			for(String str : rewardedstr.split(","))
				rewarded.add(str);
			rewarded.add(cdkey.getId()+"");
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.setId(cdkey.getId());
			rewards.setName(cdkey.getName());
			rewards.addAllLoot(cdkey.getRewardList());
			rewardService.doRewards(user, rewards);
			service.saveCdkeyRewarded(user, rewarded);
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.CDKEY_SUCCESS));
		}
	}
}
