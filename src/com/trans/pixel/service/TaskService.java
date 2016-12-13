package com.trans.pixel.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.trans.pixel.constants.ErrorConst;
import com.trans.pixel.constants.NoticeConst;
import com.trans.pixel.constants.ResultConst;
import com.trans.pixel.constants.SuccessConst;
import com.trans.pixel.constants.TaskConst;
import com.trans.pixel.model.userinfo.UserBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.TaskOrder;
import com.trans.pixel.protoc.Commands.TaskTarget;
import com.trans.pixel.protoc.Commands.UserTask;
import com.trans.pixel.service.redis.LadderRedisService;
import com.trans.pixel.service.redis.TaskRedisService;
import com.trans.pixel.service.redis.UserActivityRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class TaskService {
	
	private static final Logger log = LoggerFactory.getLogger(TaskService.class);
	
	@Resource
	private UserTaskService userTaskService;
	@Resource
	private TaskRedisService taskRedisService;
	@Resource
	private AchieveService achieveService;
	@Resource
	private ServerService serverService;
	@Resource
	private UserService userService;
	@Resource
	private MailService mailService;
	@Resource
	private LogService logService;
	@Resource
	private LadderRedisService ladderRedisService;
	@Resource
	private NoticeService noticeService;
	@Resource
	private UserActivityRedisService userActivityRedisService;
	@Resource
	private CostService costService;
	
	/****************************************************************************************/
	/** task1*/
	/****************************************************************************************/
	
	public void sendTask1Score(UserBean user, TaskConst taskConst, boolean isAdded) {
		sendTask1Score(user, taskConst.getTargetid(), isAdded);
	}
	
	public void sendTask1Score(UserBean user, TaskConst taskConst) {
		sendTask1Score(user, taskConst.getTargetid());
	}
	
	public void sendTask1Score(UserBean user, TaskConst taskConst, int count) {
		sendTask1Score(user, taskConst.getTargetid(), count);
	}
	
	public void sendTask1Score(UserBean user, int targetId) {
		sendTask1Score(user, targetId, 1, true);
	}
	
	public void sendTask1Score(UserBean user, int targetId, int count) {
		sendTask1Score(user, targetId, count, true);
	}
	
	public void sendTask1Score(UserBean user, int targetId, boolean isAdded) {
		sendTask1Score(user, targetId, 1, isAdded);
	}
	
	public void sendTask1Score(UserBean user, TaskConst taskConst, int count, boolean isAdded) {
		sendTask1Score(user, taskConst.getTargetid(), count, isAdded);
	}
	
	public void sendTask1Score(UserBean user, int targetId, int count, boolean isAdded) {
		long userId = user.getId();
		Map<String, TaskTarget> map = taskRedisService.getTask1TargetConfig();
		Iterator<Entry<String, TaskTarget>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TaskTarget> entry = it.next();
			TaskTarget task = entry.getValue();
			
			if (task.getTargetid() == targetId) {
				if (isAllFinished(task, user))
					return;
				UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask(userId, task.getTargetid()));
				if (isAdded)
					ut.setProcess(ut.getProcess() + count);
				else 
					ut.setProcess(Math.max(ut.getProcess(), count));
				
				userTaskService.updateUserTask(userId, ut.build());
				
				if (isCompleteNewTask(ut.build(), user))
					noticeService.pushNotice(userId, NoticeConst.TYPE_TASK);
			}
		}
	}
	
	private boolean isAllFinished(TaskTarget task, UserBean user) {
		List<TaskOrder> taskOrderList = task.getOrderList();
		for (TaskOrder taskOrder : taskOrderList) {
			if (taskOrder.getOrder() > user.getTask1Order())
				return false;
		}
		
		return true;
	}
	
	public ResultConst handleTaskReward(MultiReward.Builder rewards, UserBean user, int type, int order) {
		TaskOrder taskOrder = getTaskOrder(user, type, order);
		if (taskOrder == null)
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		UserTask userTask = getUserTask(user, type, taskOrder.getTargetid());
		if (userTask.getProcess() < taskOrder.getTargetcount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		rewards.addAllLoot(getRewardList(taskOrder));
		updateUserTaskProcess(userTask, user, type);
		
		isDeleteNotice(user);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private TaskOrder getTaskOrder(UserBean user, int type, int order) {
		if (type == 1) {
			if (user.getTask1Order() + 1 != order)
				return null;
			return taskRedisService.getTask1Order(order);
		}
		
		return null;
	}
	
	private UserTask getUserTask(UserBean user, int type, int targetId) {
		if (type == 1) {
			return userTaskService.selectUserTask(user.getId(), targetId);
		}
		
		return null;
	}
	
	private void updateUserTaskProcess(UserTask userTask, UserBean user, int type) {
		if (type == 1) {
			user.setTask1Order(user.getTask1Order() + 1);
		}
	}
	
	private boolean isCompleteNewTask(UserTask ut, UserBean user) {
		TaskOrder taskOrder = taskRedisService.getTask1Order(user.getTask1Order() + 1);
		if (taskOrder == null)
			return false;
		
		if (ut.getTargetid() != taskOrder.getTargetid())
			return false;
		
		if (taskOrder.getTargetcount() <= ut.getProcess())
			return true;
		
		return false;
	}
	
	private List<RewardInfo> getRewardList(TaskOrder order) {
		List<RewardInfo> rewardList = new ArrayList<RewardInfo>();
		RewardInfo.Builder reward = RewardInfo.newBuilder();
		
		if (order.getRewardid1() > 0) {
			reward.setItemid(order.getRewardid1());
			reward.setCount(order.getRewardcount1());
			rewardList.add(reward.build());
		}
		
		if (order.getRewardid2() > 0) {
			reward.setItemid(order.getRewardid2());
			reward.setCount(order.getRewardcount2());
			rewardList.add(reward.build());
		}
		
		return rewardList;
	}
	
	public void isDeleteNotice(UserBean user) {
		long userId = user.getId();
		
		List<UserTask> ut1List = userTaskService.selectUserTaskList(userId);
		for (UserTask ut : ut1List) {
			if (isCompleteNewTask(ut, user))
				return;
		}
		
		noticeService.deleteNotice(userId, NoticeConst.TYPE_ACTIVITY);
	}
	
	private List<Integer> convertServerIdList(String serverid) {
		String[] serverids = serverid.split(",");
		List<Integer> serverIdList = new ArrayList<Integer>();
		for (String serveridStr : serverids) {
			serverIdList.add(TypeTranslatedUtil.stringToInt(serveridStr));
		}
		
		return serverIdList;
	}
}
