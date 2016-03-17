package com.trans.pixel.service.command;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ActivityConst;
import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RequestKaifu2ActivityCommand;
import com.trans.pixel.protoc.Commands.RequestKaifu2RewardCommand;
import com.trans.pixel.protoc.Commands.RequestKaifuListCommand;
import com.trans.pixel.protoc.Commands.RequestKaifuRewardCommand;
import com.trans.pixel.protoc.Commands.RequestRichangListCommand;
import com.trans.pixel.protoc.Commands.RequestRichangRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseKaifu2ActivityCommand;
import com.trans.pixel.protoc.Commands.ResponseKaifu2RewardCommand;
import com.trans.pixel.protoc.Commands.ResponseKaifuListCommand;
import com.trans.pixel.protoc.Commands.ResponseKaifuRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseRichangListCommand;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserActivityService;

@Service
public class ActivityCommandService extends BaseCommandService {

	@Resource
	private ActivityService activityService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private RewardService rewardService;
	@Resource
	private UserActivityService userActivityService;
	
	public void richangReward(RequestRichangRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		int id = cmd.getId();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserRichang.Builder ur = UserRichang.newBuilder(userActivityService.selectUserRichang(user.getId(), type));
		ResultConst result = activityService.handleRichangReward(multiReward, ur, user.getId(), type, id);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user.getId(), multiReward.build());
		
		ResponseRichangListCommand.Builder builder = ResponseRichangListCommand.newBuilder();
		
		List<UserRichang> uaList = userActivityService.selectUserRichangList(user.getId());
		builder.addAllUserRichang(uaList);
		
		responseBuilder.setRichangListCommand(builder.build());
		pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
	}
	
	public void richangList(RequestRichangListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseRichangListCommand.Builder builder = ResponseRichangListCommand.newBuilder();
		
		List<UserRichang> uaList = userActivityService.selectUserRichangList(user.getId());
		builder.addAllUserRichang(uaList);
		
		responseBuilder.setRichangListCommand(builder.build());
	}
	
	public void kaifu2Activity(RequestKaifu2ActivityCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifu2ActivityCommand.Builder builder = ResponseKaifu2ActivityCommand.newBuilder();
		
		builder.setAccRcPs(activityService.getKaifu2AccRcPs(user.getServerId(), ActivityConst.KAIFU2_RECHARGE));
		builder.addAllRank(activityService.getKaifu2RankList(user));
		builder.setAccRcPsRwRc(activityService.getKaifu2RwRc(user, ActivityConst.KAIFU2_LEIJI_RECHARGE_PERSON_COUNT));
		
		responseBuilder.setKaifu2ActivityCommand(builder.build());
	}
	
	public void kaifu2Reward(RequestKaifu2RewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifu2RewardCommand.Builder builder = ResponseKaifu2RewardCommand.newBuilder();
		int type = cmd.getType();
		int id = cmd.getId();
		
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		ResultConst result = activityService.doKaifu2Reward(multiReward, user, type, id);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user.getId(), multiReward.build());
		builder.setAccRcPsRwRc(activityService.getKaifu2RwRc(user, ActivityConst.KAIFU2_LEIJI_RECHARGE_PERSON_COUNT));
		
		responseBuilder.setKaifu2RewardCommand(builder.build());
	}
	
	public void kaifuReward(RequestKaifuRewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifuRewardCommand.Builder builder = ResponseKaifuRewardCommand.newBuilder();
		int type = cmd.getType();
		int id = cmd.getId();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserKaifu.Builder uk = UserKaifu.newBuilder(userActivityService.selectUserKaifu(user.getId(), type));
		ResultConst result = activityService.handleKaifuReward(multiReward, uk, user.getId(), type, id);
		
		if (result instanceof ErrorConst) {
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
            return;
		}
		
		rewardService.doRewards(user.getId(), multiReward.build());
		builder.setUserKaifu(uk.build());
		responseBuilder.setKaifuRewardCommand(builder.build());
		pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
	}
	
	public void kaifuList(RequestKaifuListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifuListCommand.Builder builder = ResponseKaifuListCommand.newBuilder();
		
		List<UserKaifu> ukList = userActivityService.selectUserKaifuList(user.getId());
		builder.addAllUserKaifu(ukList);
		
		responseBuilder.setKaifuListCommand(builder.build());
	}
}
