package org.acme.dvdstore.base;

public enum Report {
	DVD_RENTAL("dvd_rental");

	private final String prefix;

	Report(final String token) {
		prefix = token;
	}

	public String getPrefix() {
		return prefix;
	}

	public static Report get(final String token) {
		for (final Report entity : Report.values()) {
			if (entity.getPrefix().equals(token)) {
				return entity;
			}
		}
		return null;
	}
}
