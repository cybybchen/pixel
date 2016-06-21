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
import com.trans.pixel.protoc.Commands.RequestKaifuListCommand;
import com.trans.pixel.protoc.Commands.RequestKaifuRewardCommand;
import com.trans.pixel.protoc.Commands.RequestRichangListCommand;
import com.trans.pixel.protoc.Commands.RequestRichangRewardCommand;
import com.trans.pixel.protoc.Commands.RequestShouchongRewardCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.Commands.ResponseKaifu2ActivityCommand;
import com.trans.pixel.protoc.Commands.ResponseKaifuListCommand;
import com.trans.pixel.protoc.Commands.ResponseRichangListCommand;
import com.trans.pixel.protoc.Commands.UserKaifu;
import com.trans.pixel.protoc.Commands.UserRichang;
import com.trans.pixel.service.ActivityService;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.NoticeService;
import com.trans.pixel.service.RewardService;
import com.trans.pixel.service.UserActivityService;
import com.trans.pixel.service.redis.RedisService;

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
	@Resource
	private NoticeService noticeService;
	@Resource
	private LogService logService;
	
	public void richangReward(RequestRichangRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int id = cmd.getId();
		int order = cmd.getOrder();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserRichang.Builder ur = UserRichang.newBuilder(userActivityService.selectUserRichang(user.getId(), id));
		ResultConst result = activityService.handleRichangReward(multiReward, ur, user, id, order);
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			rewardService.doRewards(user, multiReward.build());
			pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
			/**
			 * send log
			 */
			activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_ACTIVITY, id, order);
		}
		ResponseKaifuListCommand.Builder builder = ResponseKaifuListCommand.newBuilder();
		builder.addAllUserKaifu(userActivityService.selectUserKaifuList(user.getId()));
		builder.addAllRank(activityService.getKaifu2RankList(user));
		builder.addAllUserRichang(userActivityService.selectUserRichangList(user.getId()));
		responseBuilder.setKaifuListCommand(builder.build());
	}
	
	//not useful
	public void richangList(RequestRichangListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseRichangListCommand.Builder builder = ResponseRichangListCommand.newBuilder();
		
		List<UserRichang> uaList = userActivityService.selectUserRichangList(user.getId());
		builder.addAllUserRichang(uaList);
		
		responseBuilder.setRichangListCommand(builder.build());
	}
	
	//not useful
	public void kaifu2Activity(RequestKaifu2ActivityCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifu2ActivityCommand.Builder builder = ResponseKaifu2ActivityCommand.newBuilder();
		
		builder.setAccRcPs(activityService.getKaifu2AccRcPs(user.getServerId(), ActivityConst.KAIFU2_RECHARGE));
		builder.addAllRank(activityService.getKaifu2RankList(user));
		builder.setAccRcPsRwRc(activityService.getKaifu2RwRc(user, ActivityConst.KAIFU2_LEIJI_RECHARGE_PERSON_COUNT));
		
		responseBuilder.setKaifu2ActivityCommand(builder.build());
	}
	
	public void kaifuReward(RequestKaifuRewardCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifuListCommand.Builder builder = ResponseKaifuListCommand.newBuilder();
		int id = cmd.getId();
		int order = cmd.getOrder();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		UserKaifu.Builder uk = UserKaifu.newBuilder(userActivityService.selectUserKaifu(user.getId(), id));
		ResultConst result = activityService.handleKaifuReward(multiReward, uk, user, id, order);
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			rewardService.doRewards(user, multiReward.build());
			pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
			/**
			 * send log
			 */
			activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_ACTIVITY, id, order);
		}
		
		
		builder.addAllUserKaifu(userActivityService.selectUserKaifuList(user.getId()));
		builder.addAllRank(activityService.getKaifu2RankList(user));
		builder.addAllUserRichang(userActivityService.selectUserRichangList(user.getId()));
		responseBuilder.setKaifuListCommand(builder.build());
	}
	
	public void kaifuList(RequestKaifuListCommand cmd, Builder responseBuilder, UserBean user) {
		ResponseKaifuListCommand.Builder builder = ResponseKaifuListCommand.newBuilder();
		
		builder.addAllUserKaifu(userActivityService.selectUserKaifuList(user.getId()));
		builder.addAllRank(activityService.getKaifu2RankList(user));
		builder.addAllUserRichang(userActivityService.selectUserRichangList(user.getId()));
		activityService.isDeleteNotice(user);
		responseBuilder.setKaifuListCommand(builder.build());
	}
	
	public void shouchongReward(RequestShouchongRewardCommand cmd, Builder responseBuilder, UserBean user) {
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		ResultConst result = activityService.handleShouchongReward(multiReward, user);
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			rewardService.doRewards(user, multiReward.build());
			
			pusher.pushRewardCommand(responseBuilder, user, multiReward.build());
		}
		
		pusher.pushUserInfoCommand(responseBuilder, user);
	}
}
