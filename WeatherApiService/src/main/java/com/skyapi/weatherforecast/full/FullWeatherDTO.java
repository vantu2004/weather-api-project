package com.skyapi.weatherforecast.full;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skyapi.weatherforecast.daily.DailyWeatherDTO;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullWeatherDTO {
	private String location;

	/*
	 * mặc định khi giá trị các field trong realtimeWeather null/0 thì ẩn luôn
	 * realtimeWeather này, thực hiện bằng cách custom lại JsonInclude, Jackson tự
	 * tạo 1 instance của RealtimeWeatherFieldFilter và gọi đến hàm equals, hàm này
	 * trả về true thì ẩn, false thì hiện
	 */
	@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = RealtimeWeatherFieldFilter.class)
	@JsonProperty("realtime_weather")
	/*
	 * @Valid bên controller dò các field trong này -> dùng tiếp @Valid để kích hoạt
	 * validate trong class này
	 */
	@Valid
	private RealtimeWeatherDTO realtimeWeather = new RealtimeWeatherDTO();

	@JsonProperty("hourly_forecast")
	@Valid
	private List<HourlyWeatherDTO> listHourlyWeather = new ArrayList<HourlyWeatherDTO>();

	@JsonProperty("daily_forecast")
	@Valid
	private List<DailyWeatherDTO> listDailyWeathers = new ArrayList<DailyWeatherDTO>();
}
