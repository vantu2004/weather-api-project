package com.skyapi.weatherforecast.realtime;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeWeatherDTO {
	// dùng hàm toString() của Location để map qua
	private String location;

	private int temperature;
	private int humidity;
	private int precipitation;
	@JsonProperty("wind_speed")
	private int windSpeed;

	private String status;

	@JsonProperty("last_updated")
	// xuất theo chuẩn RFC 3339 và ISO 8601
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private Date lastUpdated;
}
