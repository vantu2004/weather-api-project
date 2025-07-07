package com.skyapi.weatherforecast.realtime;

import java.util.Date;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*
 * việc RealtimeWeatherDTO extends RepresentationModel đã kế thừa luôn add() và
 * List<Link> links của class này, vì thế ta có thể call add() từ
 * realtimeWeather object và in ra được links
 */
public class RealtimeWeatherDTO extends RepresentationModel<RealtimeWeatherDTO> {
	/*
	 * dùng hàm toString() của Location để map qua ko show field này khi null (dùng
	 * trong api của fullWeather)
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String location;

	@Range(min = -50, max = 50, message = "Temperature must be in the range of -50 to 50 Celsius degree")
	private int temperature;

	@Range(min = 0, max = 100, message = "Humidity must be in the range of 0 to 100 percentage")
	private int humidity;

	@Range(min = 0, max = 100, message = "Precipitation must be in the range of 0 to 100 percentage")
	private int precipitation;

	@Range(min = 0, max = 200, message = "Wind speed must be in the range of 0 to 200 km/h")
	private int windSpeed;

	@NotNull(message = "Status must not be empty")
	@Length(min = 3, max = 50, message = "Status must be in between 3-50 characters")
	private String status;

	// xuất theo chuẩn RFC 3339 và ISO 8601
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private Date lastUpdated;
}
