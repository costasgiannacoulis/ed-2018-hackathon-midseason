package org.acme.dvdstore.repository;

import java.util.concurrent.atomic.AtomicLong;

import org.acme.dvdstore.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryImpl extends AbstractRepository<Customer> implements CustomerRepository {
	private final AtomicLong SEQUENCE = new AtomicLong(1);

	@Override
	public AtomicLong getSequence() {
		return SEQUENCE;
	}
}
