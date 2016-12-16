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
import com.trans.pixel.model.userinfo.UserLevelBean;
import com.trans.pixel.protoc.Commands.MultiReward;
import com.trans.pixel.protoc.Commands.RewardInfo;
import com.trans.pixel.protoc.Commands.TaskOrder;
import com.trans.pixel.protoc.Commands.TaskTarget;
import com.trans.pixel.protoc.Commands.UserTask;
import com.trans.pixel.service.redis.TaskRedisService;
import com.trans.pixel.utils.TypeTranslatedUtil;

@Service
public class TaskService {
	
	private static final Logger log = LoggerFactory.getLogger(TaskService.class);
	
	@Resource
	private UserTaskService userTaskService;
	@Resource
	private TaskRedisService taskRedisService;
	@Resource
	private UserService userService;
	@Resource
	private LogService logService;
	@Resource
	private NoticeService noticeService;
	@Resource
	private UserLevelService userLevelService;
	
	/****************************************************************************************/
	/** task1*/
	/****************************************************************************************/
	
	public void sendTask1Score(UserBean user, TaskConst taskConst, boolean isAdded) {
		sendTask1Score(user, taskConst, 1, isAdded);
	}
	
	public void sendTask1Score(UserBean user, TaskConst taskConst) {
		sendTask1Score(user, taskConst, true);
	}
	
	public void sendTask1Score(UserBean user, TaskConst taskConst, int count) {
		sendTask1Score(user, taskConst, count, true);
	}
	
	public void sendTask1Score(UserBean user, TaskConst taskConst, int count, boolean isAdded) {
		long userId = user.getId();
		Map<String, TaskTarget> map = taskRedisService.getTask1TargetConfig();
		Iterator<Entry<String, TaskTarget>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TaskTarget> entry = it.next();
			TaskTarget task = entry.getValue();
			
			if (task.getTargetid() == taskConst.getTargetid()) {
				if (isAllFinished(task, user))
					return;
				UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask(userId, task.getTargetid()));
				if (isAdded)
					ut.setProcess(ut.getProcess() + count);
				else 
					ut.setProcess(Math.max(ut.getProcess(), count));
				
				userTaskService.updateUserTask(userId, ut.build());
				
				if (isCompleteNewTaskByTask1(ut.build(), user))
					noticeService.pushNotice(userId, NoticeConst.TYPE_TASK);
			}
		}
	}
	
	/**
	 * task 3
	 */
	
	public void sendTask3Score(UserBean user, TaskConst taskConst) {
		sendTask3Score(user, taskConst, 1);
	}
	
	public void sendTask3Score(UserBean user, TaskConst taskConst, int count) {
		long userId = user.getId();
		Map<String, TaskOrder> map = taskRedisService.getTask3OrderConfig();
		Iterator<Entry<String, TaskOrder>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TaskOrder> entry = it.next();
			TaskOrder task = entry.getValue();
			
			if (task.getTargetid() == taskConst.getTargetid()) {
				UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask3(userId, task.getTargetid()));
				ut.setProcess(ut.getProcess() + count);
				
				userTaskService.updateUserTask3(userId, ut.build());
				
				if (isCompleteNewTaskByTask3(ut.build(), user, task))
					noticeService.pushNotice(userId, NoticeConst.TYPE_TASK);
			}
		}
	}
	
	/**
	 * task 2
	 */
	
	public void sendTask2Score(UserBean user, TaskConst taskConst, int heroId) {
		sendTask2Score(user, taskConst, heroId, 1);
	}
	
	public void sendTask2Score(UserBean user, TaskConst taskConst, int heroId, int count) {
		long userId = user.getId();
		Map<String, TaskTarget> map = taskRedisService.getTask2TargetConfig();
		Iterator<Entry<String, TaskTarget>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TaskTarget> entry = it.next();
			TaskTarget task = entry.getValue();
			
			if (task.getTargetid() == taskConst.getTargetid()) {
				UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask2(userId, task.getTargetid()));
				boolean hasModify = false;
				if (task.getTargetid() % 1000 == 700 && !ut.getHeroidList().contains(heroId)) {
					for (TaskOrder order : task.getOrderList()) {
						if (order.getTargetcount() == heroId) {
							ut.addHeroid(heroId);
							hasModify = true;
							break;
						}
					}
				}
				for (TaskOrder order : task.getOrderList()) {
					if (order.getTargetcount() == heroId && order.getTaskcount1() <= count) {
						ut.setProcess(heroId);
						hasModify = true;
					}
				}
				
				if (hasModify) {
					userTaskService.updateUserTask2(userId, ut.build());
				
					if (isCompleteNewTaskByTask2(ut.build(), user, task))
						noticeService.pushNotice(userId, NoticeConst.TYPE_TASK);
				}
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
	
	public ResultConst handleTaskReward(MultiReward.Builder rewards, UserBean user, int type, int order, int heroId) {
		TaskOrder taskOrder = getTaskOrder(user, type, order, heroId);
		if (taskOrder == null)
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		
		UserTask.Builder userTask = UserTask.newBuilder(getUserTask(user, type, taskOrder.getTargetid()));
		if (type == 2 && taskOrder.getTargetid() % 1000 == 700) {
			if (!userTask.getHeroidList().contains(taskOrder.getTargetcount())) {
				return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
			}
		} else if (userTask.getProcess() < taskOrder.getTargetcount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
		rewards.addAllLoot(getRewardList(taskOrder));
		updateUserTaskProcess(userTask, user, type, heroId);
		
		isDeleteNotice(user);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private TaskOrder getTaskOrder(UserBean user, int type, int order, int heroId) {
		if (type == 1) {
			return taskRedisService.getTask1Order(user.getTask1Order() + 1);
		} else if (type == 2) {
			int originalOrder = user.getTask2Record() >> (4 * (heroId - 1)) & 15;
		log.debug("originalOrder is:" + originalOrder);
			return taskRedisService.getTask2Order(originalOrder + 1, heroId);
		} else if (type == 3) {
			return taskRedisService.getTask3Order(order);
		}
		
		return null;
	}
	
	private UserTask getUserTask(UserBean user, int type, int targetId) {
		if (type == 1) {
			return userTaskService.selectUserTask(user.getId(), targetId);
		} else if (type == 2) {
			return userTaskService.selectUserTask2(user.getId(), targetId);
		} else if (type == 3) {
			return userTaskService.selectUserTask3(user.getId(), targetId);
		}
		
		return null;
	}
	
	private void updateUserTaskProcess(UserTask.Builder userTask, UserBean user, int type, int heroId) {
		if (type == 1) {
			user.setTask1Order(user.getTask1Order() + 1);
			userService.updateUser(user);
			logService.sendMainquestLog(user.getServerId(), user.getId(), userTask.getTargetid(), user.getTask1Order());
		} else if (type == 2) {
			user.setTask2Record((1 << (4 * (heroId - 1))) + user.getTask2Record());
			userService.updateUser(user);
			logService.sendSidequestLog(user.getServerId(), user.getId(), heroId, userTask.getTargetid(), (user.getTask2Record() >> (4 * (heroId - 1))) & 15);
		} else if (type == 3) {
			userTask.setStatus(1);
			userTaskService.updateUserTask3(user.getId(), userTask.build());
			sendTask3Score(user, TaskConst.TARGET_COMPLETE_ALL_DAILY);
			
			UserLevelBean userLevel = userLevelService.selectUserLevelRecord(user.getId());
			logService.sendDailyquestLog(user.getServerId(), user.getId(), userTask.getTargetid(), userLevel.getPutongLevel(), user.getZhanliMax(), user.getVip());
		}
	}
	
	private boolean isCompleteNewTaskByTask1(UserTask ut, UserBean user) {
		TaskOrder taskOrder = taskRedisService.getTask1Order(user.getTask1Order() + 1);
		if (taskOrder == null)
			return false;
		
		if (ut.getTargetid() != taskOrder.getTargetid())
			return false;
		
		if (taskOrder.getTargetcount() <= ut.getProcess())
			return true;
		
		return false;
	}
	
	private boolean isCompleteNewTaskByTask3(UserTask ut, UserBean user, TaskOrder task) {
		if (task.getTargetcount() <= ut.getProcess())
			return true;
		
		return false;
	}
	
	private boolean isCompleteNewTaskByTask2(UserTask ut, UserBean user, TaskTarget task) {
		int nextOrder = (user.getTask2Record() >> 4 & 15) + 1;
		for (TaskOrder taskOrder : task.getOrderList()) {
			if (taskOrder.getOrder() == nextOrder) {
				if (task.getTargetid() % 1000 == 700 && ut.getHeroidList().contains(taskOrder.getTargetcount()))
					return true;
				
				if (ut.getProcess() >= taskOrder.getTargetcount())
					return true;
			}
		}
		
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
		
		//task 1
		List<UserTask> ut1List = userTaskService.selectUserTaskList(userId);
		for (UserTask ut : ut1List) {
			if (isCompleteNewTaskByTask1(ut, user))
				return;
		}
		
		//task 3
		Map<String, TaskOrder> map = taskRedisService.getTask3OrderConfig();
		Iterator<Entry<String, TaskOrder>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, TaskOrder> entry = it.next();
			TaskOrder taskOrder = entry.getValue();
			UserTask ut = userTaskService.selectUserTask3(userId, taskOrder.getTargetid());
			if (isCompleteNewTaskByTask3(ut, user, taskOrder))
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
