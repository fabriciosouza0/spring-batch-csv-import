package com.fabricio.spring.batch.controller;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("JobCustomers")
@RequestMapping("/job-customers")
@AllArgsConstructor
public class CustomerJobController {

	private final JobOperator jobOperator;

	private final Job job;

	@PostMapping()
	public void importCostumersCsvToDBJob() throws JobInstanceAlreadyCompleteException, InvalidJobParametersException, JobExecutionAlreadyRunningException, JobRestartException {
		JobParameters jobParameters = new JobParametersBuilder()
						.addLong("startAt", System.currentTimeMillis()).toJobParameters();

		jobOperator.start(job, jobParameters);
	}

}
