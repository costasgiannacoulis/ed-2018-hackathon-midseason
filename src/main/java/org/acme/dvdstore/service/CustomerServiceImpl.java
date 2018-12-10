package org.acme.dvdstore.service;

import org.acme.dvdstore.model.Customer;
import org.acme.dvdstore.repository.BaseRepository;
import org.acme.dvdstore.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerServiceImpl extends AbstractService<Customer> implements CustomerService {
	@Autowired
	private CustomerRepository customerRepository;

	@Override
	public BaseRepository<Customer, Long> getRepository() {
		return customerRepository;
	}
}
