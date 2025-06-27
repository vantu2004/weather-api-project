package com.skyapi.weatherforecast.common;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "location")
public class HourlyWeatherId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int hourOfDay;

	@ManyToOne
	@JoinColumn(name = "location_code")
	private Location location;

}
