package com.skyapi.weatherforecast.daily;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyWeatherListDTO {
	private String location;

	private List<DailyWeatherDTO> dailyForecast = new ArrayList<DailyWeatherDTO>();

	public void addDailyWeatherDTO(DailyWeatherDTO dailyWeatherDTO) {
		this.dailyForecast.add(dailyWeatherDTO);
	}
}
