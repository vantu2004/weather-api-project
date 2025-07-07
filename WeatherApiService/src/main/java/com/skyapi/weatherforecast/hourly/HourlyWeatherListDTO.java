package com.skyapi.weatherforecast.hourly;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "location", "hourly_forecast", "_links" })
public class HourlyWeatherListDTO extends RepresentationModel<HourlyWeatherListDTO> {
	private String location;

	@JsonProperty("hourly_forecast")
	private List<HourlyWeatherDTO> hourlyWeatherDTOs = new ArrayList<HourlyWeatherDTO>();

	public void addHourlyWeatherDTO(HourlyWeatherDTO hourlyWeatherDTO) {
		this.hourlyWeatherDTOs.add(hourlyWeatherDTO);
	}
}
