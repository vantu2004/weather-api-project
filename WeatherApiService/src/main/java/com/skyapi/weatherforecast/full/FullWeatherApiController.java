package com.skyapi.weatherforecast.full;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.util.CommonUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/full")
public class FullWeatherApiController {
	private final GeolocationService geolocationService;
	private final FullWeatherService fullWeatherService;
	private final ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<?> getFullWeatherByIPAddress(HttpServletRequest request) {
		String ipAddress = CommonUtility.getIpAddress(request);
		Location locationFromIp = this.geolocationService.getLocationByIp2Location(ipAddress);

		Location location = this.fullWeatherService.getLocationByIpAddress(locationFromIp);

		return ResponseEntity.ok(this.convertLocationEntityToFulWeatherDTO(location));
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getFullWeatherByLocationCode(@PathVariable("locationCode") String locationCode) {
		Location location = this.fullWeatherService.getLocationByLocationCode(locationCode);

		return ResponseEntity.ok(this.convertLocationEntityToFulWeatherDTO(location));
	}

	@PutMapping("/{locationCode}")
	public ResponseEntity<?> updateFullWeather(@PathVariable("locationCode") String locationCode,
			@RequestBody @Valid FullWeatherDTO fullWeatherDTO) throws BadRequestException {
		if (fullWeatherDTO.getListHourlyWeather().isEmpty()) {
			throw new BadRequestException("Hourly weather data cannot be empty.");
		}

		if (fullWeatherDTO.getListDailyWeathers().isEmpty()) {
			throw new BadRequestException("Daily weather data cannot be empty.");
		}

		Location location = this.convertFullWeatherDTOToLocationEntity(fullWeatherDTO);
		Location updatedLocation = this.fullWeatherService.updateFullWeather(locationCode, location);

		return ResponseEntity.ok(this.convertLocationEntityToFulWeatherDTO(updatedLocation));
	}

	private FullWeatherDTO convertLocationEntityToFulWeatherDTO(Location location) {
		FullWeatherDTO fullWeatherDTO = this.modelMapper.map(location, FullWeatherDTO.class);

		/*
		 * theo doc thì ko show field này, dùng
		 * 
		 * @JsonInclude(JsonInclude.Include.NON_NULL) để thêm điều kiện chỉ show khi
		 * khác null
		 */
		fullWeatherDTO.getRealtimeWeather().setLocation(null);
		return fullWeatherDTO;
	}

	private Location convertFullWeatherDTOToLocationEntity(FullWeatherDTO fullWeatherDTO) {
		return this.modelMapper.map(fullWeatherDTO, Location.class);
	}
}
