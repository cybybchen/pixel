package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.Cdkey;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestCdkeyCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.CdkeyService;
import com.trans.pixel.service.RewardService;

@Service
public class CdkeyCommandService extends BaseCommandService {
	private Logger log = Logger.getLogger(CdkeyCommandService.class);
	@Resource
	private CdkeyService service;
	@Resource
	private RewardService rewardService;
	@Resource
	private PushCommandService pusher;
	
	public void useCdkey(RequestCdkeyCommand cmd, Builder responseBuilder, UserBean user) {
		String key = cmd.getKey();
		String value = service.getCdkey(key);
		if(value == null){
			value = service.getCdkeyOld(key);
			if(value == null)
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_INVALID));
			else
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_USED));
		}else{
			Cdkey cdkey = service.getCdkeyConfig(value);
			if(cdkey == null){
				log.error("unknown cdkey type:"+value);
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_INVALID));
				return;
			}
			List<String> rewarded = service.getCdkeyRewarded(user);
			if(rewarded.contains(cdkey.getId()+"")){
				responseBuilder.setErrorCommand(buildErrorCommand(ErrorConst.CDKEY_REWARDED));
				return;
			}
			rewarded.add(cdkey.getId()+"");
			MultiReward.Builder rewards = MultiReward.newBuilder();
			rewards.setId(cdkey.getId());
			rewards.setName(cdkey.getName());
			rewards.addAllLoot(cdkey.getRewardList());
			rewardService.doRewards(user, rewards.build());
			service.delCdkey(key, value);
			service.saveCdkeyRewarded(user, rewarded);
			pusher.pushRewardCommand(responseBuilder, user, rewards.build());
			responseBuilder.setMessageCommand(buildMessageCommand(SuccessConst.CDKEY_SUCCESS));
		}
	}
}
