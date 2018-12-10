package org.acme.dvdstore.repository;

import java.util.concurrent.atomic.AtomicLong;

import org.acme.dvdstore.model.Film;
import org.springframework.stereotype.Component;

@Component
public class FilmRepositoryImpl extends AbstractRepository<Film> implements FilmRepository {
	private final AtomicLong SEQUENCE = new AtomicLong(1);

	@Override
	public AtomicLong getSequence() {
		return SEQUENCE;
	}
}
