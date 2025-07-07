package com.skyapi.weatherforecast.hourly;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.daily.DailyWeatherApiController;
import com.skyapi.weatherforecast.full.FullWeatherApiController;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherApiController;
import com.skyapi.weatherforecast.util.CommonUtility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/hourly")
@RequiredArgsConstructor
/*
 * trường hợp muốn validate với @RequestParam/@PathVariable/@ModelAttribute thì
 * mới cần, còn với @RequestBody đã có @Valid/@Validated phía trc thì ko cần
 */
@Validated
public class HourlyWeatherApiController {
	private final static Logger LOGGER = LoggerFactory.getLogger(HourlyWeatherApiController.class);

	private final GeolocationService geolocationService;
	private final HourlyWeatherService hourlyWeatherService;
	private final ModelMapper modelMapper;

	@GetMapping
	public ResponseEntity<?> listHourlyForecastByIPAddress(HttpServletRequest request) {
		try {
			String ipAddress = CommonUtility.getIpAddress(request);
			Location location = this.geolocationService.getLocationByIp2Location(ipAddress);

			// X-Current-Hour không phải header mặc định mà tự định nghĩa
			int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));

			List<HourlyWeather> hourlyWeathers = this.hourlyWeatherService.getListHourlyWeather(location, currentHour);

			if (hourlyWeathers.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			HourlyWeatherListDTO hourlyWeatherListDTO = this.convertListHourlyWeatherToDTO(hourlyWeathers);
			return ResponseEntity.ok().body(this.addLinksByIp(hourlyWeatherListDTO));
		} catch (NumberFormatException e) {
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> listHourlyForecastByLocationCode(@PathVariable("locationCode") String locationCode,
			HttpServletRequest request) {
		try {
			// X-Current-Hour không phải header mặc định mà tự định nghĩa
			int currentHour = Integer.parseInt(request.getHeader("X-Current-Hour"));

			List<HourlyWeather> hourlyWeathers = this.hourlyWeatherService
					.getHourlyWeatherByLocationCodeAndCurrentHour(locationCode, currentHour);

			if (hourlyWeathers.isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			HourlyWeatherListDTO hourlyWeatherListDTO = this.convertListHourlyWeatherToDTO(hourlyWeathers);

			return ResponseEntity.ok().body(this.addLinksByLocation(locationCode, hourlyWeatherListDTO));
		} catch (NumberFormatException e) {
			LOGGER.error(e.getMessage(), e);
			return ResponseEntity.badRequest().build();
		}
	}

	@PutMapping("/{locationCode}")
	public ResponseEntity<?> updateHourlyForecast(@PathVariable("locationCode") String locationCode,
			@Valid @RequestBody List<HourlyWeatherDTO> hourlyWeatherDTOs) throws BadRequestException {

		if (hourlyWeatherDTOs.isEmpty()) {
			throw new BadRequestException("Hourly forecast data cannot be empty.");
		}

		List<HourlyWeather> hourlyWeathers = this.convertListHourlyWeatherDTOToEntity(hourlyWeatherDTOs);

		List<HourlyWeather> updatedHourlyWeathers = this.hourlyWeatherService.updateHourlyWeather(locationCode,
				hourlyWeathers);

		HourlyWeatherListDTO hourlyWeatherListDTO = this.convertListHourlyWeatherToDTO(updatedHourlyWeathers);

		return ResponseEntity.ok().body(this.addLinksByLocation(locationCode, hourlyWeatherListDTO));
	}

	private HourlyWeatherListDTO convertListHourlyWeatherToDTO(List<HourlyWeather> hourlyWeathers) {
		HourlyWeatherListDTO hourlyWeatherListDTO = new HourlyWeatherListDTO();

		Location location = hourlyWeathers.get(0).getId().getLocation();
		hourlyWeatherListDTO.setLocation(location.toString());

		for (HourlyWeather hourlyWeather : hourlyWeathers) {
			// dùng cấu hình modelMapper bên class Main để ánh xạ được hourOfDay
			HourlyWeatherDTO hourlyWeatherDTO = this.modelMapper.map(hourlyWeather, HourlyWeatherDTO.class);

			hourlyWeatherListDTO.addHourlyWeatherDTO(hourlyWeatherDTO);
		}

		return hourlyWeatherListDTO;
	}

	private List<HourlyWeather> convertListHourlyWeatherDTOToEntity(List<HourlyWeatherDTO> hourlyWeatherDTOs) {
		List<HourlyWeather> hourlyWeathers = new ArrayList<HourlyWeather>();

		hourlyWeatherDTOs.forEach(hourlyWeatherDTO -> {
			/*
			 * dùng cấu hình modelMapper bên class Main để ánh xạ ngược field hourOfDay từ
			 * DTO sang entity
			 */
			hourlyWeathers.add(modelMapper.map(hourlyWeatherDTO, HourlyWeather.class));
		});

		return hourlyWeathers;
	}

	private HourlyWeatherListDTO addLinksByIp(HourlyWeatherListDTO hourlyWeatherListDTO) {
		hourlyWeatherListDTO.add(
				linkTo(methodOn(HourlyWeatherApiController.class).listHourlyForecastByIPAddress(null)).withSelfRel());
		hourlyWeatherListDTO.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealtimeByIPAddress(null))
				.withRel("realtime_weather"));
		hourlyWeatherListDTO.add(linkTo(methodOn(DailyWeatherApiController.class).listDailyForecastByIPAddress(null))
				.withRel("daily_forecast"));
		hourlyWeatherListDTO.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByIPAddress(null))
				.withRel("full_forecast"));

		return hourlyWeatherListDTO;
	}

	private HourlyWeatherListDTO addLinksByLocation(String locationCode, HourlyWeatherListDTO hourlyWeatherListDTO) {
		hourlyWeatherListDTO.add(
				linkTo(methodOn(HourlyWeatherApiController.class).listHourlyForecastByLocationCode(locationCode, null))
						.withSelfRel());
		hourlyWeatherListDTO
				.add(linkTo(methodOn(RealtimeWeatherApiController.class).getRealtimeByLocationCode(locationCode))
						.withRel("realtime_weather"));
		hourlyWeatherListDTO
				.add(linkTo(methodOn(DailyWeatherApiController.class).getDailyForecastByLocationCode(locationCode))
						.withRel("daily_forecast"));
		hourlyWeatherListDTO
				.add(linkTo(methodOn(FullWeatherApiController.class).getFullWeatherByLocationCode(locationCode))
						.withRel("full_forecast"));

		return hourlyWeatherListDTO;
	}
}
