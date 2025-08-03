package com.skyapi.weatherforecast.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "locations")
public class Location {
	@Id
	@Column(length = 12, nullable = false, unique = true)
	// null kết hợp length để đảm bảo chỉ xuất 1 message cùng lúc
	@NotNull(message = "Location code cannot be null")
	@Length(min = 3, max = 12, message = "Location code must have 3-12 characters")
	private String code;

	@Column(length = 128, nullable = false)
	@JsonProperty("city_name")
	@NotNull(message = "City name cannot be null")
	@Length(min = 3, max = 128, message = "City name must have 3-128 characters")
	private String cityName;

	@Column(length = 128, nullable = false)
	@JsonProperty("region_name")
	// region name có thể null nên chỉ xét length
	@Length(min = 3, max = 128, message = "Region name must have 3-128 characters")
	private String regionName;

	@Column(length = 64, nullable = false)
	@JsonProperty("country_name")
	@NotNull(message = "Country name cannot be null")
	@Length(min = 3, max = 64, message = "Country name must have 3-64 characters")
	private String countryName;

	@Column(length = 2, nullable = false)
	@JsonProperty("country_code")
	@NotNull(message = "Country code cannot be null")
	@Length(min = 2, max = 2, message = "Country code must have 2 characters")
	private String countryCode;

	private boolean enabled;

	@JsonIgnore
	private boolean trashed;

	@OneToOne(mappedBy = "location", cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	@JsonIgnore
	private RealtimeWeather realtimeWeather;

	@OneToMany(mappedBy = "id.location", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<HourlyWeather> listHourlyWeather = new ArrayList<HourlyWeather>();

	@OneToMany(mappedBy = "id.location", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<DailyWeather> listDailyWeathers = new ArrayList<DailyWeather>();

	public Location(
			@NotNull(message = "Location code cannot be null") @Length(min = 3, max = 12, message = "Location code must have 3-12 characters") String code,
			@NotNull(message = "City name cannot be null") @Length(min = 3, max = 128, message = "City name must have 3-128 characters") String cityName,
			@Length(min = 3, max = 128, message = "Region name must have 3-128 characters") String regionName,
			@NotNull(message = "Country name cannot be null") @Length(min = 3, max = 64, message = "Country name must have 3-64 characters") String countryName,
			@NotNull(message = "Country code cannot be null") @Length(min = 2, max = 2, message = "Country code must have 2 characters") String countryCode) {
		super();
		this.code = code;
		this.cityName = cityName;
		this.regionName = regionName;
		this.countryName = countryName;
		this.countryCode = countryCode;
	}

	// phục vụ test với mockito
	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	// phục vụ test với mockito
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		return Objects.equals(code, other.code);
	}

	@Override
	public String toString() {
		return this.cityName + ", " + (this.regionName == null ? "" : this.regionName + ", ") + this.countryName;
	}

	// dùng cho updateLocation
	public void copyFieldsFrom(Location locationInRequest) {
		setCityName(locationInRequest.getCityName());
		setRegionName(locationInRequest.getRegionName());
		setCountryName(locationInRequest.getCountryName());
		setCountryCode(locationInRequest.getCountryCode());
		setEnabled(locationInRequest.isEnabled());
	}

	// dùng cho updateFullWeather
	public void copyAllFieldsFrom(Location locationInRequest) {
		this.copyFieldsFrom(locationInRequest);
		setCode(locationInRequest.getCode());
		setTrashed(locationInRequest.isTrashed());
	}

}
