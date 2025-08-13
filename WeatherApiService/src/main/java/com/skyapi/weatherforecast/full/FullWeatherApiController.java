package com.skyapi.weatherforecast.full;

import java.util.concurrent.TimeUnit;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
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
	private final FullWeatherModelAssembler fullWeatherModelAssembler;

	@GetMapping
	public ResponseEntity<?> getFullWeatherByIPAddress(HttpServletRequest request) {
		String ipAddress = CommonUtility.getIpAddress(request);
		Location locationFromIp = this.geolocationService.getLocationByIp2Location(ipAddress);

		Location location = this.fullWeatherService.getLocationByIpAddress(locationFromIp);

		FullWeatherDTO fullWeatherDTO = this.convertLocationEntityToFullWeatherDTO(location);

		EntityModel<FullWeatherDTO> entity = this.fullWeatherModelAssembler.toModel(fullWeatherDTO);

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic()).body(entity);
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getFullWeatherByLocationCode(@PathVariable("locationCode") String locationCode) {
		Location location = this.fullWeatherService.getLocationByCode(locationCode);

		FullWeatherDTO fullWeatherDTO = this.convertLocationEntityToFullWeatherDTO(location);

		EntityModel<FullWeatherDTO> entity = this.fullWeatherModelAssembler.addLinksByLocation(locationCode,
				fullWeatherDTO);

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic()).body(entity);
	}

	@PutMapping("/{locationCode}")
	public ResponseEntity<?> updateFullWeather(@PathVariable("locationCode") String locationCode,
			@RequestBody @Valid FullWeatherDTO fullWeatherDTOInRequest) throws BadRequestException {
		if (fullWeatherDTOInRequest.getListHourlyWeather().isEmpty()) {
			throw new BadRequestException("Hourly weather data cannot be empty.");
		}

		if (fullWeatherDTOInRequest.getListDailyWeathers().isEmpty()) {
			throw new BadRequestException("Daily weather data cannot be empty.");
		}

		Location location = this.convertFullWeatherDTOToLocationEntity(fullWeatherDTOInRequest);
		Location updatedLocation = this.fullWeatherService.updateFullWeather(locationCode, location);

		FullWeatherDTO fullWeatherDTO = this.convertLocationEntityToFullWeatherDTO(updatedLocation);

		return ResponseEntity.ok(this.fullWeatherModelAssembler.addLinksByLocation(locationCode, fullWeatherDTO));
	}

	private FullWeatherDTO convertLocationEntityToFullWeatherDTO(Location location) {
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
