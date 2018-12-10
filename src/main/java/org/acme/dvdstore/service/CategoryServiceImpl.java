package org.acme.dvdstore.service;

import org.acme.dvdstore.model.Category;
import org.acme.dvdstore.repository.BaseRepository;
import org.acme.dvdstore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CategoryServiceImpl extends AbstractService<Category> implements CategoryService {
	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public BaseRepository<Category, Long> getRepository() {
		return categoryRepository;
	}
}
