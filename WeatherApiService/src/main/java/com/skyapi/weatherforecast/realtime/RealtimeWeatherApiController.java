package com.skyapi.weatherforecast.realtime;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.util.CommonUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/realtime")
@RequiredArgsConstructor
public class RealtimeWeatherApiController {
	private static final Logger LOGGER = LoggerFactory.getLogger(RealtimeWeatherApiController.class);

	private final GeolocationService geolocationService;
	private final RealtimeWeatherService realtimeWeatherService;
	private final ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<?> getRealtimeByIPAddress(HttpServletRequest request) {
		String ipAddress = CommonUtility.getIpAddress(request);
		try {
			Location location = this.geolocationService.getLocationByIp2Location(ipAddress);

			RealtimeWeather realtimeWeather = this.realtimeWeatherService
					.getRealtimeWeatherByCountryCodeAndCityName(location);

			RealtimeWeatherDTO realtimeWeatherDTO = this.modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);

			return ResponseEntity.ok(realtimeWeatherDTO);
		} catch (GeolocationException e) {
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getRealtimeByLocationCode(@PathVariable("locationCode") String locationCode) {
		RealtimeWeather realtimeWeather = this.realtimeWeatherService.getRealtimeWeatherByLocationCode(locationCode);
		return ResponseEntity.ok(convertEntityToDTO(realtimeWeather));
	}

	@PutMapping("{locationCode}")
	public ResponseEntity<?> updateRealtimeWeather(@PathVariable("locationCode") String locationCode,
			@Valid @RequestBody RealtimeWeather realtimeWeatherInRequest) {
		RealtimeWeather realtimeWeather = this.realtimeWeatherService.updateRealtimeWeather(locationCode,
				realtimeWeatherInRequest);
		return ResponseEntity.ok(convertEntityToDTO(realtimeWeather));
	}

	private RealtimeWeatherDTO convertEntityToDTO(RealtimeWeather realtimeWeather) {
		return this.modelMapper.map(realtimeWeather, RealtimeWeatherDTO.class);
	}
}
