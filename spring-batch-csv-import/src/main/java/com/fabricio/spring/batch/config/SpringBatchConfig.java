package com.fabricio.spring.batch.config;

import com.fabricio.spring.batch.entity.CustomerEntity;
import com.fabricio.spring.batch.repository.CustomerJdbcRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SpringBatchConfig {

	@Bean
	public Job customersJob(JobRepository jobRepository,
													PlatformTransactionManager transactionManager,
													CustomerJdbcRepository customerJdbcRepository) {
		return new JobBuilder("customersJob", jobRepository)
						.start(customerFirstStep(jobRepository, transactionManager, customerJdbcRepository))
						.build();
	}

	@Bean
	public JobOperatorFactoryBean jobOperator(JobRepository jobRepository) {
		JobOperatorFactoryBean jobOperatorFactoryBean = new JobOperatorFactoryBean();
		jobOperatorFactoryBean.setJobRepository(jobRepository);
		jobOperatorFactoryBean.setTaskExecutor(new SimpleAsyncTaskExecutor());
		return jobOperatorFactoryBean;
	}

	@Bean
	public FlatFileItemReader<CustomerEntity> reader() {
		var itemReader = new FlatFileItemReader<>(lineMapper());
		itemReader.setResource(new FileSystemResource("src/main/resources/customers.csv"));
		itemReader.setName("customersReader");
		itemReader.setLinesToSkip(1);

		return itemReader;
	}

	private LineMapper<CustomerEntity> lineMapper() {
		DefaultLineMapper<CustomerEntity> lineMapper = new DefaultLineMapper<>();

		DelimitedLineTokenizer lineTokenizer = new  DelimitedLineTokenizer();
		lineTokenizer.setDelimiter(",");
		lineTokenizer.setStrict(false);
		lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

		BeanWrapperFieldSetMapper<CustomerEntity> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
		fieldSetMapper.setTargetType(CustomerEntity.class);

		lineMapper.setLineTokenizer(lineTokenizer);
		lineMapper.setFieldSetMapper(fieldSetMapper);

		return lineMapper;
	}

	@Bean
	public CustomerProcessor processor() {
		return new CustomerProcessor();
	}

	@Bean
	public RepositoryItemWriter<CustomerEntity> writer(CustomerJdbcRepository customerJdbcRepository) {
		var writer = new RepositoryItemWriter<>(customerJdbcRepository);
		writer.setMethodName("save");

		return writer;
	}

	@Bean
	public Step customerFirstStep(JobRepository jobRepository,
																PlatformTransactionManager transactionManager,
																CustomerJdbcRepository customerJdbcRepository) {
		return new StepBuilder(jobRepository)
						.<CustomerEntity, CustomerEntity>chunk(10).transactionManager(transactionManager)
						.reader(reader())
						.processor(processor())
						.writer(writer(customerJdbcRepository))
						.build();
	}

}
