package gr.codehub.guide.filmrepository.model;

import lombok.Data;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Language extends BaseEntity {
	private String name;
}
