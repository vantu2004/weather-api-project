package com.skyapi.weatherforecast.realtime;

import org.modelmapper.ModelMapper;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.daily.DailyWeatherApiController;
import com.skyapi.weatherforecast.full.FullWeatherApiController;
import com.skyapi.weatherforecast.hourly.HourlyWeatherApiController;
import com.skyapi.weatherforecast.util.CommonUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/realtime")
@RequiredArgsConstructor
public class RealtimeWeatherApiController {
	private final GeolocationService geolocationService;
	private final RealtimeWeatherService realtimeWeatherService;
	private final ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<?> getRealtimeByIPAddress(HttpServletRequest request) {
		String ipAddress = CommonUtility.getIpAddress(request);

		Location location = this.geolocationService.getLocationByIp2Location(ipAddress);

		RealtimeWeather realtimeWeather = this.realtimeWeatherService
				.getRealtimeWeatherByCountryCodeAndCityName(location);

		RealtimeWeatherDTO realtimeWeatherDTO = this.modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);

		return ResponseEntity.ok(addLinksByIp(realtimeWeatherDTO));
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getRealtimeByLocationCode(@PathVariable("locationCode") String locationCode) {
		RealtimeWeather realtimeWeather = this.realtimeWeatherService.getRealtimeWeatherByLocationCode(locationCode);
		RealtimeWeatherDTO realtimeWeatherDTO = this.convertEntityToDTO(realtimeWeather);

		return ResponseEntity.ok(addLinksByLocation(locationCode, realtimeWeatherDTO));
	}

	@PutMapping("{locationCode}")
	public ResponseEntity<?> updateRealtimeWeather(@PathVariable("locationCode") String locationCode,
			@Valid @RequestBody RealtimeWeatherDTO realtimeWeatherDTOInRequest) {
		RealtimeWeather realtimeWeatherInRequest = this.convertDTOToEntity(realtimeWeatherDTOInRequest);
		realtimeWeatherInRequest.setLocationCode(locationCode);

		RealtimeWeather realtimeWeather = this.realtimeWeatherService.updateRealtimeWeather(locationCode,
				realtimeWeatherInRequest);
		RealtimeWeatherDTO realtimeWeatherDTO = this.convertEntityToDTO(realtimeWeather);

		return ResponseEntity.ok(this.addLinksByLocation(locationCode, realtimeWeatherDTO));
	}

	private RealtimeWeatherDTO convertEntityToDTO(RealtimeWeather realtimeWeather) {
		return this.modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);
	}

	private RealtimeWeather convertDTOToEntity(RealtimeWeatherDTO realtimeWeatherDTO) {
		return this.modelMapper.map(realtimeWeatherDTO, RealtimeWeather.class);
	}

	/*
	 * thay vì cứ gọi WebMvcLinkBuilder nhiều lần thì import thẳng luôn
	 * linkTo()/methodOn() dạng static
	 */
	private RealtimeWeatherDTO addLinksByIp(RealtimeWeatherDTO realtimeWeatherDTO) {
		// Thêm link self vào realtimeWeatherDTO
		realtimeWeatherDTO
				.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealtimeByIPAddress(null)).withSelfRel());
		realtimeWeatherDTO.add(linkTo(methodOn(HourlyWeatherApiController.class).listHourlyForecastByIPAddress(null))
				.withRel("hourly_forecast"));
		realtimeWeatherDTO.add(linkTo(methodOn(DailyWeatherApiController.class).listDailyForecastByIPAddress(null))
				.withRel("daily_forecast"));
		realtimeWeatherDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIPAddress(null))
				.withRel("full_forecast"));

		return realtimeWeatherDTO;
	}

	// getRealtimeByLocationCode() và updateRealtimeWeather() dùng chung
	private RealtimeWeatherDTO addLinksByLocation(String locationCode, RealtimeWeatherDTO realtimeWeatherDTO) {
		realtimeWeatherDTO
				.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealtimeByLocationCode(locationCode))
						.withSelfRel());
		realtimeWeatherDTO.add(
				linkTo(methodOn(HourlyWeatherApiController.class).listHourlyForecastByLocationCode(locationCode, null))
						.withRel("hourly_forecast"));
		realtimeWeatherDTO
				.add(linkTo(methodOn(DailyWeatherApiController.class).getDailyForecastByLocationCode(locationCode))
						.withRel("daily_forecast"));
		realtimeWeatherDTO
				.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode))
						.withRel("full_forecast"));

		return realtimeWeatherDTO;
	}
}
