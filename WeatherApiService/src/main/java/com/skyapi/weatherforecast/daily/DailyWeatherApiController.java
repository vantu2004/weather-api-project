package com.skyapi.weatherforecast.daily;

import java.util.ArrayList;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.util.CommonUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/daily")
@Validated
public class DailyWeatherApiController {
	private final DailyWeatherService dailyWeatherService;
	private final GeolocationService geolocationService;
	private final ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<?> listDailyForecastByIPAddress(HttpServletRequest request) {
		String ipAddress = CommonUtility.getIpAddress(request);
		Location location = this.geolocationService.getLocationByIp2Location(ipAddress);

		List<DailyWeather> dailyWeathers = this.dailyWeatherService.getDailyWeatherByLocation(location);

		if (dailyWeathers.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok().body(convertListDailyWeatherToDTO(dailyWeathers));
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getDailyForecastByLocationCode(@PathVariable("locationCode") String locationCode) {
		List<DailyWeather> dailyWeathers = this.dailyWeatherService.getDailyWeatherByLocationCode(locationCode);

		if (dailyWeathers.isEmpty()) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok().body(convertListDailyWeatherToDTO(dailyWeathers));
	}

	@PutMapping("/{locationCode}")
	public ResponseEntity<?> updateDailyForecast(@PathVariable("locationCode") String locationCode,
			@Valid @RequestBody List<DailyWeatherDTO> dailyWeatherDTOs) throws BadRequestException {

		if (dailyWeatherDTOs.isEmpty()) {
			throw new BadRequestException("Daily forecast data cannot be empty.");
		}

		List<DailyWeather> dailyWeathers = this.convertListDailyWeatherDTOToEntity(dailyWeatherDTOs);

		List<DailyWeather> updatedDailyWeathers = this.dailyWeatherService.updateDailyWeather(locationCode,
				dailyWeathers);

		return ResponseEntity.ok().body(this.convertListDailyWeatherToDTO(updatedDailyWeathers));
	}

	private DailyWeatherListDTO convertListDailyWeatherToDTO(List<DailyWeather> dailyWeathers) {
		Location location = dailyWeathers.get(0).getId().getLocation();

		DailyWeatherListDTO dailyWeatherListDTO = new DailyWeatherListDTO();
		dailyWeatherListDTO.setLocation(location.toString());

		for (DailyWeather dailyWeather : dailyWeathers) {
			// dùng cấu hình bên Main để ánh xạ 2 field dayOfMonth và month
			DailyWeatherDTO dailyWeatherDTO = this.modelMapper.map(dailyWeather, DailyWeatherDTO.class);
			dailyWeatherListDTO.getDailyForecast().add(dailyWeatherDTO);
		}

		return dailyWeatherListDTO;
	}

	private List<DailyWeather> convertListDailyWeatherDTOToEntity(@Valid List<DailyWeatherDTO> dailyWeatherDTOs) {
		List<DailyWeather> dailyWeathers = new ArrayList<DailyWeather>();

		dailyWeatherDTOs.forEach(dailyWeatherDTO -> {
			/*
			 * dùng cấu hình modelMapper bên class Main để ánh xạ ngược field
			 * dayOfMonth/month từ DTO sang entity
			 */
			dailyWeathers.add(modelMapper.map(dailyWeatherDTO, DailyWeather.class));
		});

		return dailyWeathers;
	}
}
