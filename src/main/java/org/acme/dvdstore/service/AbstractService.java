package org.acme.dvdstore.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.acme.dvdstore.base.AbstractLogEntity;
import org.acme.dvdstore.model.BaseEntity;
import org.acme.dvdstore.repository.BaseRepository;

public abstract class AbstractService<T extends BaseEntity> extends AbstractLogEntity implements BaseService<T, Long> {
	public abstract BaseRepository<T, Long> getRepository();

	@PostConstruct
	private void init() {
		log.debug("Starting {}.", getClass().getName());
	}

	@Override
	public T create(final T entity) {
		return getRepository().create(entity);
	}

	@Override
	public List<T> createAll(final T... entities) {
		final List<T> updatedEntities = new ArrayList<>();
		for (final T entity : entities) {
			updatedEntities.add(create(entity));
		}
		return updatedEntities;
	}

	@Override
	public void update(final T entity) {
		getRepository().update(entity);
	}

	@Override
	public void delete(final T entity) {
		getRepository().delete(entity);
	}

	@Override
	public boolean exists(final T entity) {
		return getRepository().exists(entity);
	}

	@Override
	public T get(final Long id) {
		return getRepository().get(id);
	}

	@Override
	public List<T> findAll() {
		return getRepository().findAll();
	}
}
