package com.fabricio.spring.batch.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customer")
public class CustomerEntity implements Persistable<Long> {

	@Transient
	private Boolean isNew = true;

	@Id
	private Long id;

	@Column("first_name")
	private String firstName;

	@Column("last_name")
	private String lastName;

	@Column("email")
	private String email;

	@Column("gender")
	private String gender;

	@Column("contact_no")
	private String contactNo;

	@Column("country")
	private String country;

	@Column("dob")
	private String dob;

	@Override
	public boolean isNew() {
		return isNew;
	}

}
