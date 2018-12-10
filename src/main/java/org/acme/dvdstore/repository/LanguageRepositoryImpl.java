package org.acme.dvdstore.repository;

import java.util.concurrent.atomic.AtomicLong;

import org.acme.dvdstore.model.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageRepositoryImpl extends AbstractRepository<Language> implements LanguageRepository {
	private final AtomicLong SEQUENCE = new AtomicLong(1);

	@Override
	public AtomicLong getSequence() {
		return SEQUENCE;
	}
}
