package org.acme.dvdstore.base;

import org.acme.dvdstore.model.Actor;
import org.acme.dvdstore.model.Category;
import org.acme.dvdstore.model.Customer;
import org.acme.dvdstore.model.Film;
import org.acme.dvdstore.model.Language;
import org.acme.dvdstore.model.Rental;

public enum FilePattern {
	ACTOR(Actor.class.getSimpleName().toLowerCase()),
	CATEGORY(Category.class.getSimpleName().toLowerCase()),
	FILM(Film.class.getSimpleName().toLowerCase()),
	LANGUAGE(Language.class.getSimpleName().toLowerCase()),
	CUSTOMER(Customer.class.getSimpleName().toLowerCase()),
	RENTAL(Rental.class.getSimpleName().toLowerCase());

	private final String suffix;
	private final String dumpName;

	FilePattern(final String token) {
		suffix = token;
		dumpName = suffix + "-dump";
	}

	public String getSuffix() {
		return suffix;
	}

	public String getDumpName() {
		return dumpName;
	}

	public static FilePattern get(final int token) {
		for (final FilePattern entity : FilePattern.values()) {
			if (entity.getSuffix().equals(token)) {
				return entity;
			}
		}
		return null;
	}
}
