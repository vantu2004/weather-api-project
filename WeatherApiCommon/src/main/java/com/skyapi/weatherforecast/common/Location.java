package com.skyapi.weatherforecast.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "locations")
public class Location {
	@Id
	@Column(length = 10, nullable = false, unique = true)
	private String code;

	@Column(length = 128, nullable = false, name = "city_name")
	private String cityName;

	@Column(length = 128, nullable = false, name = "region_name")
	private String regionName;

	@Column(length = 64, nullable = false, name = "country_name")
	private String countryName;

	@Column(length = 2, nullable = false, name = "country_code")
	private String countryCode;

	private boolean enabled;
	private boolean trashed;
}
