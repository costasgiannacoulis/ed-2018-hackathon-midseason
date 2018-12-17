package org.acme.dvdstore.base;

public enum ActionPattern {
	CREATE("create"),
	READ("read"),
	UPDATE("update"),
	DELETE("delete");

	private final String prefix;

	ActionPattern(final String token) {
		prefix = token;
	}

	public String getPrefix() {
		return prefix;
	}

	public static ActionPattern get(final int token) {
		for (final ActionPattern entity : ActionPattern.values()) {
			if (entity.getPrefix().equals(token)) {
				return entity;
			}
		}
		return null;
	}
}
