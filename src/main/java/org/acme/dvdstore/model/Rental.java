package gr.codehub.guide.filmrepository.model;

import lombok.Data;

@Data
@Builder
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Rental extends BaseEntity {
	private Customer customer;
	private Film film;
	private Date rentalDate;
	private int rentalDays;
}
