package com.fabricio.spring.batch.repository;

import com.fabricio.spring.batch.entity.CustomerEntity;
import org.springframework.data.repository.CrudRepository;

public interface CustomerJdbcRepository extends CrudRepository<CustomerEntity, Long> {
}
