package org.acme.dvdstore.repository;

import java.util.concurrent.atomic.AtomicLong;

import org.acme.dvdstore.model.Actor;
import org.springframework.stereotype.Component;

@Component
public class ActorRepositoryImpl extends AbstractRepository<Actor> implements ActorRepository {
	private final AtomicLong SEQUENCE = new AtomicLong(1);

	@Override
	public AtomicLong getSequence() {
		return SEQUENCE;
	}
}
