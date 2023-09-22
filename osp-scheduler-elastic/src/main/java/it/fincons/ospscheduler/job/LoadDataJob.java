package it.fincons.ospscheduler.job;

import it.fincons.ospscheduler.service.LoadDataService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoadDataJob implements Job {

    Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LoadDataService jobService;

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());

        try {
            jobService.executeSampleJob();
        } catch (IOException e) {
            e.printStackTrace();

            throw new JobExecutionException(e.getMessage());
        }

        logger.info("Next job scheduled @ {}", context.getNextFireTime());
    }
}
