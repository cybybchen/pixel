package com.trans.pixel.service;

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
import com.trans.pixel.protoc.ActivityProto.ACTIVITY_TYPE;
import com.trans.pixel.protoc.Base.MultiReward;
import com.trans.pixel.protoc.RewardTaskProto.Task2TargetHero;
import com.trans.pixel.protoc.RewardTaskProto.TaskOrder;
import com.trans.pixel.protoc.RewardTaskProto.TaskTarget;
import com.trans.pixel.protoc.RewardTaskProto.UserTask;
import com.trans.pixel.service.redis.LevelRedisService;
import com.trans.pixel.service.redis.TaskRedisService;

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
	private LevelRedisService userLevelService;
	
	/****************************************************************************************/
	/** task1*/
	/****************************************************************************************/
	
	public void sendTask1Score(UserBean user, TaskConst task) {
		
	}
	
	public void sendTask1Score(UserBean user, TaskConst task, int count, boolean isAdded) {
		
	}
	
	public void sendTask1Score(UserBean user, int targetId, boolean isAdded) {
		sendTask1Score(user, targetId, 1, isAdded);
	}
	
	public void sendTask1Score(UserBean user, int targetId) {
		sendTask1Score(user, targetId, true);
	}
	
	public void sendTask1Score(UserBean user, int targetId, int count) {
		sendTask1Score(user, targetId, count, true);
	}
	
	public void sendTask1Score(UserBean user, int targetId, int count, boolean isAdded) {
		long userId = user.getId();
		Map<Integer, TaskTarget> map = taskRedisService.getTask1TargetConfig();
		Iterator<Entry<Integer, TaskTarget>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, TaskTarget> entry = it.next();
			TaskTarget task = entry.getValue();
			
			if (task.getTargetid() == targetId) {
				if (isAllFinished(task, user))
					return;
				UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask(userId, task.getTargetid()));
				if (isAdded)
					ut.setProcess(ut.getProcess() + count);
				else if (targetId == ACTIVITY_TYPE.TYPE_PVP_BUFF_LEVELUP5_VALUE || targetId == ACTIVITY_TYPE.TYPE_ZHUJUE_LEVELUP5_VALUE) {
					boolean hasAdded = false;
					for (int heroId : ut.getHeroidList()) {
						if (heroId == count) {
							hasAdded = true;
							break;
						}
					}
					if (!hasAdded)
						ut.addHeroid(count);
				} /*else if (targetId == ACTIVITY_TYPE.TYPE_EVENT_COMPLETE_VALUE) {
					for (TaskOrder taskOrder : task.getOrderList()) {
						if (taskOrder.getTargetcount() == count) {
							ut.setProcess(Math.max(ut.getProcess(), count));
							break;
						}
					}
				} */else
					ut.setProcess(Math.max(ut.getProcess(), count));
				
				userTaskService.updateUserTask(userId, ut.build());
				
				if (isCompleteNewTaskByTask1(ut.build(), user))
					noticeService.pushNotice(userId, NoticeConst.TYPE_MAINTASK);
			}
		}
	}
	
	/**
	 * task 3
	 */
	
	public void sendTask3Score(UserBean user, TaskConst task) {
		
	}
	
	public void sendTask3Score(UserBean user, int targetId) {
		sendTask3Score(user, targetId, 1);
	}
	
	public void sendTask3Score(UserBean user, int targetId, int count) {
		sendTask3Score(user.getId(), targetId, count);
	}
	
	public void sendTask3Score(long userId, int targetId) {
		sendTask3Score(userId, targetId, 1);
	}
	
	public void sendTask3Score(long userId, int targetId, int count) {
		Map<Integer, TaskOrder> map = taskRedisService.getTask3OrderConfig();
		Iterator<Entry<Integer, TaskOrder>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, TaskOrder> entry = it.next();
			TaskOrder task = entry.getValue();
			
			if (task.getTargetid() == targetId) {
				UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask3(userId, task.getTargetid()));
				ut.setProcess(ut.getProcess() + count);
				
				userTaskService.updateUserTask3(userId, ut.build());
				
				if (isCompleteNewTaskByTask3(ut.build(), task))
					noticeService.pushNotice(userId, NoticeConst.TYPE_DAILYTASK);
			}
		}
	}
	
	/**
	 * task 2
	 */
	
	public void sendTask2Score(UserBean user, int targetId, int heroId) {
		sendTask2Score(user, targetId, heroId, 1);
	}
	
	public void sendTask2Score(UserBean user, int targetId, int heroId, int count) {
		long userId = user.getId();
		Map<Integer, Task2TargetHero> map = taskRedisService.getTask2TargetConfig();
		Iterator<Entry<Integer, Task2TargetHero>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Task2TargetHero> entry = it.next();
			Task2TargetHero taskHero = entry.getValue();
			for (TaskTarget task : taskHero.getTargetList()) {
				if (task.getTargetid() == targetId) {
					UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask2(userId, targetId));
					boolean hasModify = false;
					
					if (ut.getHeroidList().contains(heroId)) {
						continue;
					}
					
					for (TaskOrder order : task.getOrderList()) {
						if (order.getTargetcount() == heroId) {
							if (order.getTargetcount1() == 0 || order.getTargetcount1() <= count) {
	//							ut.addCompleteOrder(order.getOrder());
								ut.addHeroid(heroId);
								hasModify = true;
							}
						}
					}
					
					if (hasModify) {
						userTaskService.updateUserTask2(userId, ut.build());
						
						if (isCompleteNewTaskByTask2(ut.build(), user, taskHero))
							noticeService.pushNotice(userId, NoticeConst.TYPE_MAINTASK);
					}
				}
			}
		}
	}
	
	public void sendTask2Score(UserBean user, int targetId, List<Integer> heroIds) {
		long userId = user.getId();
		Map<Integer, Task2TargetHero> map = taskRedisService.getTask2TargetConfig();
		Iterator<Entry<Integer, Task2TargetHero>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Task2TargetHero> entry = it.next();
			Task2TargetHero taskHero = entry.getValue();
			for (TaskTarget task : taskHero.getTargetList()) {
				if (task.getTargetid() == targetId) {
					UserTask.Builder ut = UserTask.newBuilder(userTaskService.selectUserTask2(userId, targetId));
					boolean hasModify = false;
					
					for (int heroId : heroIds) {
						if (ut.getHeroidList().contains(heroId)) {
							continue;
						}
						
						for (TaskOrder order : task.getOrderList()) {
							if (order.getTargetcount() == heroId) {
//								if (order.getTargetcount1() == 0 || order.getTargetcount1() <= count) {
									ut.addHeroid(heroId);
									hasModify = true;
//								}
							}
						}
					}
					if (hasModify) {
						userTaskService.updateUserTask2(userId, ut.build());
						
						if (isCompleteNewTaskByTask2(ut.build(), user, taskHero))
							noticeService.pushNotice(userId, NoticeConst.TYPE_MAINTASK);
					}
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
		if (userTask.getStatus() == 1)
			return ErrorConst.ACTIVITY_REWARD_HAS_GET_ERROR;
		if (type == 2 || taskOrder.getTargetid() == ACTIVITY_TYPE.TYPE_PVP_BUFF_LEVELUP5_VALUE
				|| taskOrder.getTargetid() == ACTIVITY_TYPE.TYPE_ZHUJUE_LEVELUP5_VALUE) {
			if (!userTask.getHeroidList().contains(taskOrder.getTargetcount())) {
				return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
			}
		} else if (userTask.getProcess() < taskOrder.getTargetcount())
			return ErrorConst.ACTIVITY_HAS_NOT_COMPLETE_ERROR;
		
//		rewards.addAllLoot(getRewardList(taskOrder));
		rewards.addAllLoot(taskOrder.getRewardList());
		updateUserTaskProcess(userTask, user, type, heroId);
		
		if (type == 3) {
			isDeleteDailyTaskNotice(user);
		}else 
			isDeleteMainTaskNotice(user);
		
		return SuccessConst.ACTIVITY_REWARD_SUCCESS;
	}
	
	private TaskOrder getTaskOrder(UserBean user, int type, int order, int heroId) {
		if (type == 1) {
			return taskRedisService.getTask1Order(user.getTask1Order() + 1);
		} else if (type == 2) {
			int originalOrder = getTask2OriginalOrder(heroId, user.getTask2Record());
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
			logService.sendSidequestLog(user.getServerId(), user.getId(), heroId, userTask.getTargetid(), getTask2OriginalOrder(heroId, user.getTask2Record()));
		} else if (type == 3) {
			userTask.setStatus(1);
			userTaskService.updateUserTask3(user.getId(), userTask.build());
			sendTask3Score(user, ACTIVITY_TYPE.TYPE_DAILY_ALL_VALUE);
			
			UserLevelBean userLevel = userLevelService.getUserLevel(user);
			logService.sendDailyquestLog(user.getServerId(), user.getId(), userTask.getTargetid(), userLevel.getUnlockDaguan(), user.getZhanliMax(), user.getVip());
		}
	}
	
	private boolean isCompleteNewTaskByTask1(UserTask ut, UserBean user) {
		TaskOrder taskOrder = taskRedisService.getTask1Order(user.getTask1Order() + 1);
		if (taskOrder == null)
			return false;
		
		if (ut.getTargetid() != taskOrder.getTargetid())
			return false;
		
		if (taskOrder.getTargetid() == ACTIVITY_TYPE.TYPE_PVP_BUFF_LEVELUP5_VALUE
				|| taskOrder.getTargetid() == ACTIVITY_TYPE.TYPE_ZHUJUE_LEVELUP5_VALUE) {
			if (ut.getHeroidList().contains(taskOrder.getTargetcount())) {
				return true;
			}
		}
		
		if (taskOrder.getTargetcount() <= ut.getProcess())
			return true;
		
		return false;
	}
	
	private boolean isCompleteNewTaskByTask3(UserTask ut, TaskOrder task) {
		if (ut.hasStatus() && ut.getStatus() == 1)
			return false;
		if (task.getTargetcount() <= ut.getProcess())
			return true;
		
		return false;
	}
	
	private boolean isCompleteNewTaskByTask2(UserTask ut, UserBean user, Task2TargetHero taskHero) {
		int nextOrder = getTask2NextOrder(taskHero.getHeroid(), user.getTask2Record());
		for (TaskTarget task : taskHero.getTargetList()) {
			if (task.getTargetid() != ut.getTargetid())
				continue;
			for (TaskOrder taskOrder : task.getOrderList()) {
				if (taskOrder.getOrder() == nextOrder) {
	//				if (task.getTargetid() % 1000 == 700) {
						if (ut.getHeroidList().contains(taskOrder.getTargetcount()))
								return true;
	//				} else if (ut.getProcess() >= taskOrder.getTargetcount())
	//					return true;
				}
			}
		}
		
		return false;
	}
	
	public void isDeleteMainTaskNotice(UserBean user) {
		long userId = user.getId();
		
		//task 2
		Map<Integer, Task2TargetHero> map = taskRedisService.getTask2TargetConfig();
		Iterator<Entry<Integer, Task2TargetHero>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, Task2TargetHero> entry = it.next();
			Task2TargetHero taskHero = entry.getValue();
			int nextOrder = getTask2NextOrder(taskHero.getHeroid(), user.getTask2Record());
			for (TaskTarget task : taskHero.getTargetList()) {
				for (TaskOrder taskOrder : task.getOrderList()) {
					if (taskOrder.getOrder() == nextOrder) {
						UserTask ut = userTaskService.selectUserTask2(user.getId(), task.getTargetid());
						if (ut.getHeroidList().contains(taskOrder.getTargetcount()))
							return;
						
						break;
					}
				}
			}
		}
				
		//task 1
		TaskOrder task1Order = taskRedisService.getTask1Order(user.getTask1Order() + 1);
		if (task1Order != null) {
			UserTask ut1 = userTaskService.selectUserTask(userId, task1Order.getTargetid());
			if (isCompleteNewTaskByTask1(ut1, user))
				return;
		}
		
		noticeService.deleteNotice(userId, NoticeConst.TYPE_MAINTASK);
	}
	
	public void isDeleteDailyTaskNotice(UserBean user) {
		long userId = user.getId();
		
		//task 3
		Map<Integer, TaskOrder> map = taskRedisService.getTask3OrderConfig();
		Iterator<Entry<Integer, TaskOrder>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, TaskOrder> entry = it.next();
			TaskOrder taskOrder = entry.getValue();
			UserTask ut = userTaskService.selectUserTask3(userId, taskOrder.getTargetid());
			if (isCompleteNewTaskByTask3(ut, taskOrder))
				return;
		}
		noticeService.deleteNotice(userId, NoticeConst.TYPE_DAILYTASK);
	}
	
	private int getTask2OriginalOrder(int heroId, int task2Record) {
		return (task2Record >> (4 * (heroId - 1)) & 15);
	}
	
	private int getTask2NextOrder(int heroId, int task2Record) {
		return getTask2OriginalOrder(heroId, task2Record) + 1;
	}
}
