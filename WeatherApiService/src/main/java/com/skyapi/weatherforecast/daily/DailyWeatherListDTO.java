package com.skyapi.weatherforecast.daily;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyWeatherListDTO {
	private String location;

	@JsonProperty("daily_forecast")
	private List<DailyWeatherDTO> dailyForecast = new ArrayList<DailyWeatherDTO>();

	public void addDailyWeatherDTO(DailyWeatherDTO dailyWeatherDTO) {
		this.dailyForecast.add(dailyWeatherDTO);
	}
}
