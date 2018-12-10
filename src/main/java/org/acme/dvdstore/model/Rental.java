package org.acme.dvdstore.model;

import java.util.Date;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
