package com.skyapi.weatherforecast.base;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.daily.DailyWeatherApiController;
import com.skyapi.weatherforecast.full.FullWeatherApiController;
import com.skyapi.weatherforecast.hourly.HourlyWeatherApiController;
import com.skyapi.weatherforecast.location.LocationApiController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherApiController;

@RestController
@RequestMapping("/")
public class MainApiController {
	@GetMapping
	public ResponseEntity<RootEntity> handleBaseUri() {
		return ResponseEntity.ok(createRootEntity());
	}

	private RootEntity createRootEntity() {
		RootEntity rootEntity = new RootEntity();

		/*
		 * methodOn() tạo 1 proxy để mock controller truyền vào, sau đó proxy này dựa
		 * vào method được gọi để ghi lại thông tin của method được gọi (tên method,
		 * class, tham số, annotation như @PathVariable, @RequestMapping, v.v.) chứ
		 * không thực sự gọi method đó, từ đó giúp linkTo(...) dựng URL tương ứng với
		 * handler đó.
		 */

		String locationsUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(LocationApiController.class).listAllLocations()).toString();
		String locationByCodeUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(LocationApiController.class).getLocation(null)).toString();
		String realtimeWeatherByIpUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(RealtimeWeatherApiController.class).getRealtimeByIPAddress(null))
				.toString();
		String realtimeWeatherByCodeUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(RealtimeWeatherApiController.class).getRealtimeByLocationCode(null))
				.toString();
		String hourlyForecastByIpUrl = WebMvcLinkBuilder.linkTo(
				WebMvcLinkBuilder.methodOn(HourlyWeatherApiController.class).listHourlyForecastByIPAddress(null))
				.toString();
		String hourlyForecastByCodeUrl = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder
				.methodOn(HourlyWeatherApiController.class).listHourlyForecastByLocationCode(null, null)).toString();
		String dailyForecastByIpUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(DailyWeatherApiController.class).listDailyForecastByIPAddress(null))
				.toString();
		String dailyForecastByCodeUrl = WebMvcLinkBuilder.linkTo(
				WebMvcLinkBuilder.methodOn(DailyWeatherApiController.class).getDailyForecastByLocationCode(null))
				.toString();
		String fullWeatherByIpUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(FullWeatherApiController.class).getFullWeatherByIPAddress(null))
				.toString();
		String fullWeatherByCodeUrl = WebMvcLinkBuilder
				.linkTo(WebMvcLinkBuilder.methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(null))
				.toString();

		rootEntity.setLocationsUrl(locationsUrl);
		rootEntity.setLocationByCodeUrl(locationByCodeUrl);
		rootEntity.setRealtimeWeatherByIpUrl(realtimeWeatherByIpUrl);
		rootEntity.setRealtimeWeatherByCodeUrl(realtimeWeatherByCodeUrl);
		rootEntity.setHourlyForecastByIpUrl(hourlyForecastByIpUrl);
		rootEntity.setHourlyForecastByCodeUrl(hourlyForecastByCodeUrl);
		rootEntity.setDailyForecastByIpUrl(dailyForecastByIpUrl);
		rootEntity.setDailyForecastByCodeUrl(dailyForecastByCodeUrl);
		rootEntity.setFullWeatherByIpUrl(fullWeatherByIpUrl);
		rootEntity.setFullWeatherByCodeUrl(fullWeatherByCodeUrl);

		return rootEntity;
	}
}
