package com.skyapi.weatherforecast.common;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
@Table(name = "weather_hourly")
public class HourlyWeather {
	@EmbeddedId
	@Builder.Default
	private HourlyWeatherId id = new HourlyWeatherId();

	private Integer temperature;
	private Integer precipitation;

	@Column(length = 50)
	private String status;
}
