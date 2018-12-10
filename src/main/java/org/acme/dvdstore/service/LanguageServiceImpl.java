package org.acme.dvdstore.service;

import org.acme.dvdstore.model.Language;
import org.acme.dvdstore.repository.BaseRepository;
import org.acme.dvdstore.repository.LanguageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguageServiceImpl extends AbstractService<Language> implements LanguageService {
	@Autowired
	private LanguageRepository languageRepository;

	@Override
	public BaseRepository<Language, Long> getRepository() {
		return languageRepository;
	}
}
