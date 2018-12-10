package org.acme.dvdstore.service;

import org.acme.dvdstore.model.Actor;
import org.acme.dvdstore.repository.ActorRepository;
import org.acme.dvdstore.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActorServiceImpl extends AbstractService<Actor> implements ActorService {
	@Autowired
	private ActorRepository actorRepository;

	@Override
	public BaseRepository<Actor, Long> getRepository() {
		return actorRepository;
	}
}
