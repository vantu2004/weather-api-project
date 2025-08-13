package com.skyapi.weatherforecast.realtime;

import org.modelmapper.ModelMapper;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

		return includeLastModified(this.addLinksByIp(realtimeWeatherDTO));
	}

	@GetMapping("/{locationCode}")
	public ResponseEntity<?> getRealtimeByLocationCode(@PathVariable("locationCode") String locationCode) {
		RealtimeWeather realtimeWeather = this.realtimeWeatherService.getRealtimeWeatherByLocationCode(locationCode);
		RealtimeWeatherDTO realtimeWeatherDTO = this.convertEntityToDTO(realtimeWeather);

		return includeLastModified(this.addLinksByLocation(locationCode, realtimeWeatherDTO));
	}

	private ResponseEntity<?> includeLastModified(RealtimeWeatherDTO realtimeWeatherDTO) {
		Instant lastUpdated = realtimeWeatherDTO.getLastUpdated().toInstant();

		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		Optional<String> ifModifiedSinceHeader = Optional.ofNullable(request.getHeader("If-Modified-Since"));

		if (ifModifiedSinceHeader.isPresent()) {
			// realtimeWeatherDTO.getLastUpdated() có kiểu Date nên chỉ cần ép Instant là đủ
			DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC);
			Instant ifModifiedSince = ZonedDateTime.parse(ifModifiedSinceHeader.get(), formatter).toInstant();

			if (!lastUpdated.isAfter(ifModifiedSince)) {
				return ResponseEntity.status(HttpStatusCode.valueOf(304)).build();
			}
		}

		/*
		 * khi dùng cache-control với max-age > 0, trình duyệt hoặc một proxy ở giữa đã
		 * lưu header/body nên brower sẽ ko gửi request đến server -> việc check
		 * last-modified ko diễn ra, việc check last-modified chỉ diễn ra khi cache hết
		 * hạn
		 * 
		 * check last-modified để đảm bảo là dữ liệu ko đổi và trả về 304 (Not Modified)
		 * hoặc 200 (OK) với body mới, việc check do spring tự thực hiện mà ko cần check
		 * thủ công như ETag
		 */
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic())
				.lastModified(lastUpdated).body(realtimeWeatherDTO);
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
