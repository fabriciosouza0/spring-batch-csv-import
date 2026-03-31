package com.fabricio.spring.batch.config;

import com.fabricio.spring.batch.entity.CustomerEntity;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<CustomerEntity, CustomerEntity> {

	@Override
	public CustomerEntity process(CustomerEntity customerEntity) throws Exception {
		return customerEntity;
	}

}
