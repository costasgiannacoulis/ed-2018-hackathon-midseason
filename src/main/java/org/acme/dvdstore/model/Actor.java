package org.acme.dvdstore.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Actor extends BaseEntity {
	private String firstName;
	private String lastName;
}
