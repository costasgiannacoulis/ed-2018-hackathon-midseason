package org.acme.dvdstore.service;

import org.acme.dvdstore.model.Rental;
import org.acme.dvdstore.repository.BaseRepository;
import org.acme.dvdstore.repository.RentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RentalServiceImpl extends AbstractService<Rental> implements RentalService {
	@Autowired
	private RentalRepository rentalRepository;

	@Override
	public BaseRepository<Rental, Long> getRepository() {
		return rentalRepository;
	}
}
