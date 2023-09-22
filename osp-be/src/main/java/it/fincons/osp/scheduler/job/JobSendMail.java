package it.fincons.osp.scheduler.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import it.fincons.osp.annotation.LogEntryExit;
import it.fincons.osp.services.ComunicazioneMailService;

public class JobSendMail implements Job {

	@Autowired
	ComunicazioneMailService comunicazioneMailService;

	@Override
	@LogEntryExit
	public void execute(JobExecutionContext context) throws JobExecutionException {
		comunicazioneMailService.sendComunicazioniMail();
	}

}
