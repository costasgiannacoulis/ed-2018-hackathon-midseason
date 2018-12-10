package gr.codehub.guide.filmrepository.model;

import lombok.Data;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Actor extends BaseEntity {
	private String firstName;
	private String lastName;
}
