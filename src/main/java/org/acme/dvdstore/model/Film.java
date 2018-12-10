package org.acme.dvdstore.model;

import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Film extends BaseEntity {
	private String title;
	private String description;
	private int release;
	private Language language;
	private int length;
	private String rating;
	private Set<Actor> actors;
	private Set<Category> categories;
}
