package org.acme.dvdstore.service;

import org.acme.dvdstore.model.Film;
import org.acme.dvdstore.repository.BaseRepository;
import org.acme.dvdstore.repository.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FilmServiceImpl extends AbstractService<Film> implements FilmService {
	@Autowired
	private FilmRepository filmRepository;

	@Override
	public BaseRepository<Film, Long> getRepository() {
		return filmRepository;
	}
}
