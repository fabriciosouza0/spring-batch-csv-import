package com.fabricio.spring.batch.config;

import com.fabricio.spring.batch.entity.CustomerEntity;
import com.fabricio.spring.batch.repository.CustomerJdbcRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileParseException;
import org.springframework.batch.infrastructure.item.file.LineMapper;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.infrastructure.item.support.SynchronizedItemStreamReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
	public AsyncTaskExecutor asyncTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(8);
		executor.setThreadNamePrefix("batch-thread-");
		executor.afterPropertiesSet();
		return executor;
	}

	@Bean
	public SynchronizedItemStreamReader<CustomerEntity> synchronizedReader() {
		return new SynchronizedItemStreamReader<CustomerEntity>(reader());
	}

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

		return new StepBuilder("customerFirstStep", jobRepository)
			.<CustomerEntity, CustomerEntity>chunk(10)
			.transactionManager(transactionManager)

			.reader(synchronizedReader())
			.processor(processor())
			.writer(writer(customerJdbcRepository))

			.faultTolerant()
			.skip(FlatFileParseException.class)
			.skip(Exception.class)
			.skipLimit(50)
			.taskExecutor(asyncTaskExecutor())
			.build();
	}

}
