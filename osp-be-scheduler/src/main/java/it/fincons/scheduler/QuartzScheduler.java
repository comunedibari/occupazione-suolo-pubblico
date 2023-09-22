package it.fincons.scheduler;


import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import it.fincons.scheduler.config.AutoWiringSpringBeanJobFactory;
import it.fincons.scheduler.job.JobInsertNotifications;

@Configuration
public class QuartzScheduler {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Value("${osp.scheduler.notifications.insert.cron.expression}")
	private String insertNotificationsCronExpression;

	@Autowired
	private ApplicationContext applicationContext;

	@PostConstruct
	public void init() {
		logger.info("Init Quartz...");
	}

	@Bean
	public SpringBeanJobFactory springBeanJobFactory() {
		AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
		logger.debug("Configuring Job factory");

		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	@Bean
	public Scheduler scheduler(Map<String, JobDetail> jobMap, Set<? extends Trigger> triggers,
			SchedulerFactoryBean factory) throws SchedulerException {

		logger.debug("Getting a handle to the Scheduler");
		Scheduler scheduler = factory.getScheduler();
		scheduler.setJobFactory(springBeanJobFactory());
		Map<JobDetail, Set<? extends Trigger>> triggersAndJobs = new HashMap<>();
		for (JobDetail jobDetail : jobMap.values()) {
			for (Trigger trigger : triggers) {
				if (trigger.getJobKey().equals(jobDetail.getKey())) {
					Set<Trigger> set = new HashSet<>();
					set.add(trigger);
					triggersAndJobs.put(jobDetail, set);
				}
			}
		}
		scheduler.scheduleJobs(triggersAndJobs, false);

		logger.debug("Starting Scheduler threads");
		scheduler.start();
		return scheduler;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean() throws IOException {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setJobFactory(springBeanJobFactory());
		// factory.setQuartzProperties(quartzProperties());
		return factory;
	}

//    public Properties quartzProperties() throws IOException {
//        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
//        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
//        propertiesFactoryBean.afterPropertiesSet();
//        return propertiesFactoryBean.getObject();
//    }

	@Bean(name = "jobInsertNotifications")
	public JobDetail jobDetailInsertNotifications() {

		return newJob().ofType(JobInsertNotifications.class).storeDurably()
				.withIdentity(JobKey.jobKey("Qrtz_Job_Detail_Insert_Notifications"))
				.withDescription("Invoke Job Insert Notifications service...").build();
	}

	@Bean
	public Trigger triggerInsertNotifications(@Qualifier("jobInsertNotifications") JobDetail job) {

		logger.info("Configuring trigger for job jobInsertNotifications to fire with this cron expression: {}",
				insertNotificationsCronExpression);

		return newTrigger().forJob(job).withIdentity(TriggerKey.triggerKey("Qrtz_Trigger_Insert_Notifications"))
				.withDescription("Insert Notifications trigger").withSchedule(cronSchedule(insertNotificationsCronExpression))
				.build();
	}

	// TODO
	// per schedulare nuovi job, bisogna creare un nuovo JobDetail e un nuovo
	// Trigger, facendo i due metodi corrispondenti, che chiamano la classe del
	// nuovo job. Di sotto un esempio:

//    @Bean(name="jobTwo")
//    public JobDetail jobDetailTwo() {
//
//    	 return newJob().ofType(JobTest2.class).storeDurably().withIdentity(JobKey.jobKey("Qrtz_Job_Test")).withDescription("Invoke Sample Job Test...").build();
//    }
//    
//    @Bean
//    public Trigger triggerTwo(@Qualifier("jobTwo") JobDetail job) {
//
//    	 int frequencyInSec = 30;
//         logger.info("Configuring trigger to fire every {} seconds", frequencyInSec);
//
//         return newTrigger().forJob(job).withIdentity(TriggerKey.triggerKey("Qrtz_Trigger_Test")).withDescription("Sample trigger test").withSchedule(simpleSchedule().withIntervalInSeconds(frequencyInSec).repeatForever()).build();
//    }

}
