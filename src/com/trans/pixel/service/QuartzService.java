package com.trans.pixel.service;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import com.trans.pixel.model.JobBean;
import com.trans.pixel.service.crontab.QuartzJobFactory;

/**
 * 定时任务
 */
@Service
public class QuartzService {
	Logger logger = Logger.getLogger(QuartzService.class);
	@Resource
	private Scheduler scheduler;

	public void addJob(JobBean job) {
		// schedulerFactoryBean 由spring创建注入
//		ApplicationContext context = new ClassPathXmlApplicationContext(
//				"config/applicationContext.xml");
//
//				Scheduler scheduler = (Scheduler) context.getBean("schedulerFactoryBean"); 
//		Scheduler scheduler = schedulerFactoryBean.getScheduler();
		// 这里获取任务信息数据
		TriggerKey triggerKey = TriggerKey.triggerKey(job.getName(), job.getGroup());

		try {
			// 获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
			CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
			// 不存在，创建一个
			if (null == trigger) {
				JobDetail jobDetail = JobBuilder.newJob(QuartzJobFactory.class).withIdentity(job.getName(), job.getGroup()).build();
				jobDetail.getJobDataMap().put("scheduleJob", job);
				// 表达式调度构建器
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
				// 按新的cronExpression表达式构建一个新的trigger
				trigger = TriggerBuilder.newTrigger().withIdentity(job.getName(), job.getGroup()).withSchedule(scheduleBuilder).build();
				scheduler.scheduleJob(jobDetail, trigger);
			} else {
				// Trigger已存在，那么更新相应的定时设置
				// 表达式调度构建器
				CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
				// 按新的cronExpression表达式重新构建trigger
				trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
				// 按新的trigger重新设置job执行
				scheduler.rescheduleJob(triggerKey, trigger);
			}

		} catch (SchedulerException e) {
			logger.error(e);
		}
	}
}
