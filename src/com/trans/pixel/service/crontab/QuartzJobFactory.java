package com.trans.pixel.service.crontab;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.trans.pixel.model.JobBean;

//@DisallowConcurrentExecution
public class QuartzJobFactory implements Job {
	Logger logger = Logger.getLogger(QuartzJobFactory.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("任务成功运行");
        JobBean job = (JobBean)context.getMergedJobDataMap().get("scheduleJob");
        System.out.println("任务名称 = [" + job.getName() + "]");
    }
    private Scheduler getScheduler(){
		// schedulerFactoryBean 由spring创建注入
		ApplicationContext context = new ClassPathXmlApplicationContext("config/applicationContext.xml");
		return (Scheduler) context.getBean("schedulerFactoryBean"); 
    }
    
	public void addJob(JobBean job) {
		Scheduler scheduler = getScheduler();
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
	
	public void deleteJob(JobBean job) {
		Scheduler scheduler = getScheduler();
		JobKey jobKey = JobKey.jobKey(job.getName(), job.getGroup());
		try {
			scheduler.deleteJob(jobKey);
		} catch (SchedulerException e) {
			logger.error(e);
		}
	}
}
