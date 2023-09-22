package it.fincons.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobInsertNotifications implements Job {

	@Value("${osp.scheduler.api.baseurl}")
	private String apiBaseUrl;

	@Autowired
	RestTemplate restTemplateOsp;

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("Execute job Insert Notifications - START");

		ResponseEntity<Void> response = restTemplateOsp.exchange(
				apiBaseUrl + "/notifica-scadenzario-management/inserimento", HttpMethod.POST, null, Void.class);

		log.info("Response: " + response);

		log.info("Execute job Insert Notifications - END");
	}

}
