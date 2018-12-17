package org.acme.dvdstore.base;

public enum StructurePattern {
	ENTITIES("entities"),
	REPORT("report"),
	ARCHIVE("archive"),
	BACKUP("backup");

	private final String name;

	StructurePattern(final String suffix) {
		name = suffix;
	}

	public String getName() {
		return name;
	}

	public static StructurePattern get(final int token) {
		for (final StructurePattern entity : StructurePattern.values()) {
			if (entity.getName().equals(token)) {
				return entity;
			}
		}
		return null;
	}
}
