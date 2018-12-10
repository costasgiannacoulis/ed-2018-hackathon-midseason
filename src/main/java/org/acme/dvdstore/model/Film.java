package gr.codehub.guide.filmrepository.model;

import java.util.Set;

import lombok.Data;

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
