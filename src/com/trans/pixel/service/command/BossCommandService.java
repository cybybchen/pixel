package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.model.RewardBean;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.RequestSubmitBosskillCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.service.BossService;
import com.trans.pixel.service.RewardService;

@Service
public class BossCommandService extends BaseCommandService {

	@Resource
	private BossService bossService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	
	public void bossKill(RequestSubmitBosskillCommand cmd, Builder responseBuilder, UserBean user) {
		List<RewardBean> rewardList = bossService.submitBosskill(user, cmd.getGroupId(), cmd.getBossId());
		if (rewardList != null && rewardList.size() > 0) {
			rewardService.doRewards(user, rewardList);
			pusher.pushRewardCommand(responseBuilder, user, rewardList);
		}
		pusher.pushUserBosskillRecord(responseBuilder, user);
	}
}
