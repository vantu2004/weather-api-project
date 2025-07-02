package com.skyapi.weatherforecast.daily;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.DailyWeatherId;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;

@WebMvcTest(DailyWeatherApiController.class)
public class DailyWeatherApiControllerTests {
	private static final String END_POINT_PATH = "/v1/daily";
	private static final String REQUEST_CONTENT_TYPE = "application/json";

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private GeolocationService geolocationService;
	@MockBean
	private DailyWeatherService dailyWeatherService;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testGetDailyWeatherByIpShouldReturn400BadRequest() throws Exception {
		GeolocationException geolocationException = new GeolocationException("Geolocation error.");
		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString()))
				.thenThrow(geolocationException);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", is(geolocationException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetDailyWeatherByIpShouldReturn404NotFound() throws Exception {
		Location location = Location.builder().code("HCM_VNN").build();

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);

		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(location.getCode());

		Mockito.when(this.dailyWeatherService.getDailyWeatherByLocation(Mockito.any()))
				.thenThrow(locationNotFoundException);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetDailyWeatherByIpShouldReturn204NoContent() throws Exception {
		Location location = Location.builder().code("HCM_VN").build();

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);

		List<DailyWeather> dailyWeathers = new ArrayList<DailyWeather>();

		Mockito.when(this.dailyWeatherService.getDailyWeatherByLocation(Mockito.any())).thenReturn(dailyWeathers);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	public void testGetDailyWeatherByIpShouldReturn200Ok() throws Exception {
		Location location = Location.builder().code("HCM_VN").cityName("Ho Chi Minh City").regionName("Ho Chi Minh")
				.countryName("Viet Nam").countryCode("VN").build();

		DailyWeather dailyWeather01 = DailyWeather.builder().id(new DailyWeatherId(2, 6, location)).maxTemp(30)
				.minTemp(20).precipitation(25).status("Sunny").build();
		DailyWeather dailyWeather02 = DailyWeather.builder().id(new DailyWeatherId(4, 7, location)).maxTemp(40)
				.minTemp(30).precipitation(35).status("Rainy").build();
		List<DailyWeather> dailyWeathers = List.of(dailyWeather01, dailyWeather02);

		location.setListDailyWeathers(dailyWeathers);

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);
		Mockito.when(this.dailyWeatherService.getDailyWeatherByLocation(Mockito.any())).thenReturn(dailyWeathers);

		String expectedLocation = location.toString();

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isOk())
				.andExpect(jsonPath("$.location", is(expectedLocation)))
				.andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(2))).andDo(print());
	}

	@Test
	public void testGetDailyWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		Location location = Location.builder().code(locationCode).build();
		LocationNotFoundException locationNotFoundException = new LocationNotFoundException(location.getCode());

		Mockito.when(this.dailyWeatherService.getDailyWeatherByLocationCode(locationCode))
				.thenThrow(locationNotFoundException);

		mockMvc.perform(get(requestURI)).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errors[0]", is(locationNotFoundException.getMessage()))).andDo(print());
	}

	@Test
	public void testGetDailyWeatherByLocationCodeShouldReturn204NoContent() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		List<DailyWeather> dailyWeathers = new ArrayList<DailyWeather>();

		Mockito.when(this.dailyWeatherService.getDailyWeatherByLocationCode(locationCode)).thenReturn(dailyWeathers);

		mockMvc.perform(get(requestURI)).andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	public void testGetDailyWeatherByLocationCodeShouldReturn200Ok() throws Exception {
		String locationCode = "HCM_VN";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		Location location = Location.builder().code(locationCode).cityName("Ho Chi Minh City").regionName("Ho Chi Minh")
				.countryName("Viet Nam").countryCode("VN").build();

		DailyWeather dailyWeather01 = DailyWeather.builder().id(new DailyWeatherId(2, 6, location)).maxTemp(30)
				.minTemp(20).precipitation(25).status("Sunny").build();
		DailyWeather dailyWeather02 = DailyWeather.builder().id(new DailyWeatherId(4, 7, location)).maxTemp(40)
				.minTemp(30).precipitation(35).status("Rainy").build();
		List<DailyWeather> dailyWeathers = List.of(dailyWeather01, dailyWeather02);

		location.setListDailyWeathers(dailyWeathers);

		Mockito.when(this.dailyWeatherService.getDailyWeatherByLocationCode(locationCode)).thenReturn(dailyWeathers);

		String expectedLocation = location.toString();

		mockMvc.perform(get(requestURI)).andExpect(status().isOk())
				.andExpect(jsonPath("$.location", is(expectedLocation)))
				.andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(2))).andDo(print());
	}

	@Test
	public void testUpdateShouldReturn400BadRequestBecauseNoData() throws Exception {
		String requestURI = END_POINT_PATH + "/NYC_USA";

		List<DailyWeatherDTO> listDTO = Collections.emptyList();

		String requestBody = this.objectMapper.writeValueAsString(listDTO);

		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", is("Daily forecast data cannot be empty."))).andDo(print());
	}

	@Test
	public void testUpdateShouldReturn400BadRequestBecauseInvalidData() throws Exception {
		String requestURI = END_POINT_PATH + "/NYC_USA";

		DailyWeatherDTO dto1 = DailyWeatherDTO.builder().dayOfMonth(40).month(7).minTemp(23).maxTemp(30)
				.precipitation(20).status("Clear").build();

		DailyWeatherDTO dto2 = DailyWeatherDTO.builder().dayOfMonth(20).month(71).minTemp(23).maxTemp(30)
				.precipitation(20).status("Clear").build();

		List<DailyWeatherDTO> listDTO = List.of(dto1, dto2);

		String requestBody = objectMapper.writeValueAsString(listDTO);

		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0]", containsString("Day of month must be between 1-31"))).andDo(print());
	}

	@Test
	public void testUpdateShouldReturn404NotFound() throws Exception {
		String locationCode = "NYC_USA";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		DailyWeatherDTO dto = DailyWeatherDTO.builder().dayOfMonth(20).month(1).minTemp(23).maxTemp(30)
				.precipitation(20).status("Clear").build();

		List<DailyWeatherDTO> listDTO = List.of(dto);

		String requestBody = objectMapper.writeValueAsString(listDTO);

		LocationNotFoundException ex = new LocationNotFoundException(locationCode);
		Mockito.when(this.dailyWeatherService.updateDailyWeather(Mockito.eq(locationCode), Mockito.anyList()))
				.thenThrow(ex);

		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.errors[0]", is(ex.getMessage())))
				.andDo(print());
	}

	@Test
	public void testUpdateShouldReturn200Ok() throws Exception {
		String locationCode = "NYC_USA";
		String requestURI = END_POINT_PATH + "/" + locationCode;

		DailyWeatherDTO dailyWeatherDTO1 = DailyWeatherDTO.builder().dayOfMonth(17).month(7).minTemp(25).maxTemp(35)
				.precipitation(40).status("Sunny").build();
		DailyWeatherDTO dailyWeatherDTO2 = DailyWeatherDTO.builder().dayOfMonth(18).month(7).minTemp(26).maxTemp(34)
				.precipitation(50).status("Clear").build();

		Location location = Location.builder().code("HCM_VN").cityName("Ho Chi Minh City").regionName("Ho Chi Minh")
				.countryName("Viet Nam").countryCode("VN").build();

		DailyWeather dailyWeather1 = DailyWeather.builder().id(new DailyWeatherId(17, 7, location)).minTemp(25)
				.maxTemp(35).precipitation(40).status("Sunny").build();
		DailyWeather dailyWeather2 = DailyWeather.builder().id(new DailyWeatherId(18, 7, location)).minTemp(26)
				.maxTemp(34).precipitation(50).status("Clear").build();

		List<DailyWeatherDTO> dailyWeatherDTOs = List.of(dailyWeatherDTO1, dailyWeatherDTO2);
		List<DailyWeather> dailyWeathers = List.of(dailyWeather1, dailyWeather2);

		String requestBody = this.objectMapper.writeValueAsString(dailyWeatherDTOs);

		Mockito.when(this.dailyWeatherService.updateDailyWeather(Mockito.eq(locationCode), Mockito.anyList()))
				.thenReturn(dailyWeathers);

		mockMvc.perform(put(requestURI).contentType(REQUEST_CONTENT_TYPE).content(requestBody))
				.andExpect(status().isOk()).andExpect(jsonPath("$.location", is(location.toString())))
				.andExpect(jsonPath("$.daily_forecast[0].day_of_month", is(17)))
				.andExpect(jsonPath("$.daily_forecast[1].day_of_month", is(18))).andDo(print());

	}
}
