package com.skyapi.weatherforecast.common;

import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
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
@Table(name = "realtime_weather")
public class RealtimeWeather {
	@Id
	@Column(name = "location_code")
	@JsonIgnore
	private String locationCode;

	private int temperature;
	private int humidity;
	private int precipitation;

	@JsonProperty("wind_speed")
	private int windSpeed;

	@Column(length = 50)
	private String status;

	@JsonProperty("last_updated")
	@JsonIgnore
	private Date lastUpdated;

	@OneToOne
	@JoinColumn(name = "location_code")
	@MapsId
	@JsonIgnore
	private Location location;

	// 2 hàm hashCode và equals dùng để test với hàm update
	@Override
	public int hashCode() {
		return Objects.hash(locationCode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RealtimeWeather other = (RealtimeWeather) obj;
		return Objects.equals(locationCode, other.locationCode);
	}
}
