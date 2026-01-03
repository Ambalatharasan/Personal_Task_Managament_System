package Project.config;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import Project.scheduler.TaskExecutionJob;

@Configuration
public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiredSpringBeanJobFactory jobFactory = new AutowiredSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public JobDetail taskExecutionJobDetail() {
        return JobBuilder.newJob(TaskExecutionJob.class).withIdentity("taskExecutionJob").storeDurably().build();
    }

    @Bean
    public Trigger taskExecutionTrigger() {
        return TriggerBuilder.newTrigger().forJob(taskExecutionJobDetail()).withIdentity("taskExecutionTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(1).repeatForever()).build();
    }

    @Bean
    public Scheduler scheduler(SpringBeanJobFactory springBeanJobFactory)
            throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.setJobFactory(springBeanJobFactory);
        // Do not schedule here, schedule in MainApplication
        return scheduler;
    }

    private static class AutowiredSpringBeanJobFactory extends SpringBeanJobFactory {

        private ApplicationContext applicationContext;

        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
            return job;
        }
    }
}
