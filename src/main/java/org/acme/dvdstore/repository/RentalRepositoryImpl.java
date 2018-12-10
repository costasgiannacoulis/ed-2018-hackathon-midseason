package org.acme.dvdstore.repository;

import java.util.concurrent.atomic.AtomicLong;

import org.acme.dvdstore.model.Rental;
import org.springframework.stereotype.Component;

@Component
public class RentalRepositoryImpl extends AbstractRepository<Rental> implements RentalRepository {
	private final AtomicLong SEQUENCE = new AtomicLong(1);

	@Override
	public AtomicLong getSequence() {
		return SEQUENCE;
	}
}
