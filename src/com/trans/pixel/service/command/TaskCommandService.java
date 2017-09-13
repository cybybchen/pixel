package com.trans.pixel.service.command;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.Commands.ErrorCommand;
import com.trans.pixel.protoc.Commands.ResponseCommand.Builder;
import com.trans.pixel.protoc.RewardTaskProto.RequestGetTaskRewardCommand;
import com.trans.pixel.protoc.RewardTaskProto.RequestUserTaskCommand;
import com.trans.pixel.protoc.RewardTaskProto.ResponseUserTaskCommand;
import com.trans.pixel.service.LogService;
import com.trans.pixel.service.NoticeService;
import com.trans.pixel.service.TaskService;
import com.trans.pixel.service.UserTaskService;
import com.trans.pixel.service.redis.RedisService;

@Service
public class TaskCommandService extends BaseCommandService {

	@Resource
	private TaskService taskService;
	@Resource
	private PushCommandService pusher;
	@Resource
	private UserTaskService userTaskService;
	@Resource
	private NoticeService noticeService;
	@Resource
	private LogService logService;
	
	public void taskReward(RequestGetTaskRewardCommand cmd, Builder responseBuilder, UserBean user) {
		int type = cmd.getType();
		int order = cmd.getOrder();
		int heroId = cmd.getHeroid();
		MultiReward.Builder multiReward = MultiReward.newBuilder();
		ResultConst result = taskService.handleTaskReward(multiReward, user, type, order, heroId);
		
		if (result instanceof ErrorConst) {
			logService.sendErrorLog(user.getId(), user.getServerId(), cmd.getClass(), RedisService.formatJson(cmd), result);
			ErrorCommand errorCommand = buildErrorCommand((ErrorConst)result);
            responseBuilder.setErrorCommand(errorCommand);
		}else{
			handleRewards(responseBuilder, user, multiReward.build());
			pusher.pushUserInfoCommand(responseBuilder, user);
			/**
			 * send log
			 */
//			activityService.sendLog(user.getId(), user.getServerId(), ActivityConst.LOG_TYPE_ACTIVITY, id, order);
		}
		ResponseUserTaskCommand.Builder builder = ResponseUserTaskCommand.newBuilder();
		builder.addAllUserTask1(userTaskService.selectUserTaskList(user.getId()));
		builder.addAllUserTask2(userTaskService.selectUserTask2List(user.getId()));
		builder.addAllUserTask3(userTaskService.selectUserTask3List(user.getId()));
		responseBuilder.setUserTaskCommand(builder.build());
	}

	public void userTask(RequestUserTaskCommand cmd, Builder responseBuilder, UserBean user) {
		taskService.isDeleteMainTaskNotice(user);
		taskService.isDeleteDailyTaskNotice(user);
		ResponseUserTaskCommand.Builder builder = ResponseUserTaskCommand.newBuilder();
		builder.addAllUserTask1(userTaskService.selectUserTaskList(user.getId()));
		builder.addAllUserTask2(userTaskService.selectUserTask2List(user.getId()));
		builder.addAllUserTask3(userTaskService.selectUserTask3List(user.getId()));
		responseBuilder.setUserTaskCommand(builder.build());
	}
}
