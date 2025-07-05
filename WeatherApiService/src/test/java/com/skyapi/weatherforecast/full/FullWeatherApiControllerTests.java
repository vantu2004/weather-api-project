package com.skyapi.weatherforecast.full;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.DailyWeatherId;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.HourlyWeatherId;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.daily.DailyWeatherDTO;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;

@WebMvcTest(FullWeatherApiController.class)
public class FullWeatherApiControllerTests {
	private static final String END_POINT_PATH = "/v1/full";
	private static final String REQUEST_CONTENT_TYPE = "application/json";

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private GeolocationService geolocationService;
	@MockBean
	private FullWeatherService fullWeatherService;

	@Test
	public void testGetFullWeatherByIpShouldReturn400BadRequest() throws Exception {
		GeolocationException geolocationException = new GeolocationException("Geolocation error.");
		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString()))
				.thenThrow(geolocationException);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", is(geolocationException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetFullWeatherByIpShouldReturn404NotFound() throws Exception {
		Location location = Location.builder().code("HCM_VNN").build();

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(location.getCode());

		Mockito.when(this.fullWeatherService.getLocationByIpAddress(location)).thenThrow(locationNotFoundException);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetByIPShouldReturn200OK() throws Exception {
		Location location = new Location();
		location.setCode("NYC_USA");
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setTemperature(12);
		realtimeWeather.setHumidity(32);
		realtimeWeather.setLastUpdated(new Date());
		realtimeWeather.setPrecipitation(88);
		realtimeWeather.setStatus("Cloudy");
		realtimeWeather.setWindSpeed(5);
		/*
		 * mặc dù đã set location giúp field location trong RealtimeWeatherDTO có giá
		 * trị nhưng nhờ hàm convertLocationEntityToDTO trong FullWeatherApiController
		 * đã set lại null giúp @JsonInclude(JsonInclude.Include.NON_NULL) có ý nghĩa
		 */
		realtimeWeather.setLocation(location);

		location.setRealtimeWeather(realtimeWeather);

		DailyWeather dailyForecast1 = DailyWeather.builder().id(new DailyWeatherId(16, 7, location)).minTemp(23)
				.maxTemp(32).precipitation(40).status("Cloudy").build();
		DailyWeather dailyForecast2 = DailyWeather.builder().id(new DailyWeatherId(17, 7, location)).minTemp(25)
				.maxTemp(34).precipitation(30).status("Sunny").build();

		location.setListDailyWeathers(List.of(dailyForecast1, dailyForecast2));

		HourlyWeather hourlyForecast1 = HourlyWeather.builder().id(new HourlyWeatherId(10, location)).temperature(13)
				.precipitation(70).status("Cloudy").build();
		HourlyWeather hourlyForecast2 = HourlyWeather.builder().id(new HourlyWeatherId(11, location)).temperature(15)
				.precipitation(60).status("Sunny").build();

		location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);
		Mockito.when(this.fullWeatherService.getLocationByIpAddress(location)).thenReturn(location);

		String expectedLocation = location.toString();

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isOk())
				.andExpect(jsonPath("$.location", is(expectedLocation))).andDo(print());
	}

	@Test
	public void testGetFullWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(locationCode);

		Mockito.when(this.fullWeatherService.getLocationByCode(locationCode)).thenThrow(locationNotFoundException);

		mockMvc.perform(get(requestURI)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetByLocationCodeShouldReturn200OK() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		Location location = new Location();
		location.setCode(locationCode);
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setTemperature(12);
		realtimeWeather.setHumidity(32);
		realtimeWeather.setLastUpdated(new Date());
		realtimeWeather.setPrecipitation(88);
		realtimeWeather.setStatus("Cloudy");
		realtimeWeather.setWindSpeed(5);
		/*
		 * mặc dù đã set location giúp field location trong RealtimeWeatherDTO có giá
		 * trị nhưng nhờ hàm convertLocationEntityToDTO trong FullWeatherApiController
		 * đã set lại null giúp @JsonInclude(JsonInclude.Include.NON_NULL) có ý nghĩa
		 */
		realtimeWeather.setLocation(location);

		location.setRealtimeWeather(realtimeWeather);

		DailyWeather dailyForecast1 = DailyWeather.builder().id(new DailyWeatherId(16, 7, location)).minTemp(23)
				.maxTemp(32).precipitation(40).status("Cloudy").build();
		DailyWeather dailyForecast2 = DailyWeather.builder().id(new DailyWeatherId(17, 7, location)).minTemp(25)
				.maxTemp(34).precipitation(30).status("Sunny").build();

		location.setListDailyWeathers(List.of(dailyForecast1, dailyForecast2));

		HourlyWeather hourlyForecast1 = HourlyWeather.builder().id(new HourlyWeatherId(10, location)).temperature(13)
				.precipitation(70).status("Cloudy").build();
		HourlyWeather hourlyForecast2 = HourlyWeather.builder().id(new HourlyWeatherId(11, location)).temperature(15)
				.precipitation(60).status("Sunny").build();

		location.setListHourlyWeather(List.of(hourlyForecast1, hourlyForecast2));

		Mockito.when(this.fullWeatherService.getLocationByCode(locationCode)).thenReturn(location);

		String expectedLocation = location.toString();

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(jsonPath("$.location", is(expectedLocation))).andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn400BadRequestBecauseNoHourlyWeather() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", is("Hourly weather data cannot be empty."))).andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn400BadRequestBecauseNoDailyWeather() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
		hourlyWeatherDTO.setHourOfDay(5);
		hourlyWeatherDTO.setTemperature(5);
		hourlyWeatherDTO.setPrecipitation(5);
		hourlyWeatherDTO.setStatus("Cloudy");

		fullWeatherDTO.getListHourlyWeather().add(hourlyWeatherDTO);

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", is("Daily weather data cannot be empty."))).andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn400BadRequestBecauseInvalidRealtimeWeather() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		// test field này
		realtimeWeatherDTO.setTemperature(128);
		realtimeWeatherDTO.setHumidity(32);
		realtimeWeatherDTO.setPrecipitation(88);
		realtimeWeatherDTO.setWindSpeed(5);
		realtimeWeatherDTO.setStatus("Cloudy");
		realtimeWeatherDTO.setLastUpdated(new Date());

		HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
		hourlyWeatherDTO.setHourOfDay(5);
		hourlyWeatherDTO.setTemperature(5);
		hourlyWeatherDTO.setPrecipitation(5);
		hourlyWeatherDTO.setStatus("Cloudy");

		DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO();
		dailyWeatherDTO.setDayOfMonth(5);
		dailyWeatherDTO.setMonth(5);
		dailyWeatherDTO.setMaxTemp(5);
		dailyWeatherDTO.setMinTemp(5);
		dailyWeatherDTO.setPrecipitation(5);
		dailyWeatherDTO.setStatus("Cloudy");

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
		fullWeatherDTO.getListHourlyWeather().add(hourlyWeatherDTO);
		fullWeatherDTO.getListDailyWeathers().add(dailyWeatherDTO);

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0]",
						containsString("Temperature must be in the range of -50 to 50 Celsius degree")))
				.andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn400BadRequestBecauseInvalidHourlyWeather() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		realtimeWeatherDTO.setTemperature(18);
		realtimeWeatherDTO.setHumidity(32);
		realtimeWeatherDTO.setPrecipitation(88);
		realtimeWeatherDTO.setWindSpeed(5);
		realtimeWeatherDTO.setStatus("Cloudy");
		realtimeWeatherDTO.setLastUpdated(new Date());

		HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
		hourlyWeatherDTO.setHourOfDay(5);
		// test field này
		hourlyWeatherDTO.setTemperature(57);
		hourlyWeatherDTO.setPrecipitation(5);
		hourlyWeatherDTO.setStatus("Cloudy");

		DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO();
		dailyWeatherDTO.setDayOfMonth(5);
		dailyWeatherDTO.setMonth(5);
		dailyWeatherDTO.setMaxTemp(5);
		dailyWeatherDTO.setMinTemp(5);
		dailyWeatherDTO.setPrecipitation(5);
		dailyWeatherDTO.setStatus("Cloudy");

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
		fullWeatherDTO.getListHourlyWeather().add(hourlyWeatherDTO);
		fullWeatherDTO.getListDailyWeathers().add(dailyWeatherDTO);

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors[0]",
						containsString("Temperature must be in the range of -50 to 50 Celsius degree")))
				.andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn400BadRequestBecauseInvalidDailyWeather() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		realtimeWeatherDTO.setTemperature(18);
		realtimeWeatherDTO.setHumidity(32);
		realtimeWeatherDTO.setPrecipitation(88);
		realtimeWeatherDTO.setWindSpeed(5);
		realtimeWeatherDTO.setStatus("Cloudy");
		realtimeWeatherDTO.setLastUpdated(new Date());

		HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
		hourlyWeatherDTO.setHourOfDay(5);
		hourlyWeatherDTO.setTemperature(5);
		hourlyWeatherDTO.setPrecipitation(5);
		hourlyWeatherDTO.setStatus("Cloudy");

		DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO();
		// test field này
		dailyWeatherDTO.setDayOfMonth(57);
		dailyWeatherDTO.setMonth(5);
		dailyWeatherDTO.setMaxTemp(5);
		dailyWeatherDTO.setMinTemp(5);
		dailyWeatherDTO.setPrecipitation(5);
		dailyWeatherDTO.setStatus("Cloudy");

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
		fullWeatherDTO.getListHourlyWeather().add(hourlyWeatherDTO);
		fullWeatherDTO.getListDailyWeathers().add(dailyWeatherDTO);

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", containsString("Day of month must be between 1-31"))).andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn404NotFound() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		realtimeWeatherDTO.setTemperature(18);
		realtimeWeatherDTO.setHumidity(32);
		realtimeWeatherDTO.setPrecipitation(88);
		realtimeWeatherDTO.setWindSpeed(5);
		realtimeWeatherDTO.setStatus("Cloudy");
		realtimeWeatherDTO.setLastUpdated(new Date());

		HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
		hourlyWeatherDTO.setHourOfDay(5);
		hourlyWeatherDTO.setTemperature(5);
		hourlyWeatherDTO.setPrecipitation(5);
		hourlyWeatherDTO.setStatus("Cloudy");

		DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO();
		dailyWeatherDTO.setDayOfMonth(5);
		dailyWeatherDTO.setMonth(5);
		dailyWeatherDTO.setMaxTemp(5);
		dailyWeatherDTO.setMinTemp(5);
		dailyWeatherDTO.setPrecipitation(5);
		dailyWeatherDTO.setStatus("Cloudy");

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
		fullWeatherDTO.getListHourlyWeather().add(hourlyWeatherDTO);
		fullWeatherDTO.getListDailyWeathers().add(dailyWeatherDTO);

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(locationCode);

		Mockito.when(this.fullWeatherService.updateFullWeather(Mockito.eq(locationCode), Mockito.any(Location.class)))
				.thenThrow(locationNotFoundException);

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testUpdateFullWeatherShouldReturn200Ok() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		Location location = new Location();
		location.setCode(locationCode);
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryCode("US");
		location.setCountryName("United States of America");

		FullWeatherDTO fullWeatherDTO = new FullWeatherDTO();

		RealtimeWeatherDTO realtimeWeatherDTO = new RealtimeWeatherDTO();
		realtimeWeatherDTO.setTemperature(18);
		realtimeWeatherDTO.setHumidity(32);
		realtimeWeatherDTO.setPrecipitation(88);
		realtimeWeatherDTO.setWindSpeed(5);
		realtimeWeatherDTO.setStatus("Cloudy");
		realtimeWeatherDTO.setLastUpdated(new Date());

		HourlyWeatherDTO hourlyWeatherDTO = new HourlyWeatherDTO();
		hourlyWeatherDTO.setHourOfDay(5);
		hourlyWeatherDTO.setTemperature(5);
		hourlyWeatherDTO.setPrecipitation(5);
		hourlyWeatherDTO.setStatus("Cloudy");

		DailyWeatherDTO dailyWeatherDTO = new DailyWeatherDTO();
		dailyWeatherDTO.setDayOfMonth(5);
		dailyWeatherDTO.setMonth(5);
		dailyWeatherDTO.setMaxTemp(5);
		dailyWeatherDTO.setMinTemp(5);
		dailyWeatherDTO.setPrecipitation(5);
		dailyWeatherDTO.setStatus("Cloudy");

		fullWeatherDTO.setRealtimeWeather(realtimeWeatherDTO);
		fullWeatherDTO.getListHourlyWeather().add(hourlyWeatherDTO);
		fullWeatherDTO.getListDailyWeathers().add(dailyWeatherDTO);

		String json = objectMapper.writeValueAsString(fullWeatherDTO);

		Mockito.when(this.fullWeatherService.updateFullWeather(Mockito.eq(locationCode), Mockito.any(Location.class)))
				.thenReturn(location);

		String expectedLocation = location.toString();

		mockMvc.perform(put(requestURI).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isOk()).andExpect(jsonPath("$.location", is(expectedLocation))).andDo(print());
	}
}
