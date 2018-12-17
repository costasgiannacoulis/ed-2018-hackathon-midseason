package org.acme.dvdstore.base;

public enum CommandPattern {
	CREATE("create"),
	READ("read"),
	UPDATE("update"),
	DELETE("delete");

	private final String prefix;

	CommandPattern(final String token) {
		prefix = token;
	}

	public String getPrefix() {
		return prefix;
	}

	public static CommandPattern get(final int token) {
		for (final CommandPattern entity : CommandPattern.values()) {
			if (entity.getPrefix().equals(token)) {
				return entity;
			}
		}
		return null;
	}
}
