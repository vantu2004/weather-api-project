package com.skyapi.weatherforecast.hourly;

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.HourlyWeatherId;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;

@WebMvcTest(HourlyWeatherApiController.class)
public class HourlyWeatherApiControllerTests {
	private static final String END_POINT_PATH = "/v1/hourly";
	private static final String X_CURRENT_HOUR = "X-Current-Hour";

	@Autowired
	private MockMvc mockMvc;
	@MockBean
	private GeolocationService geolocationService;
	@MockBean
	private HourlyWeatherService hourlyWeatherService;
	@Autowired
	private ObjectMapper objectMapper;

	/*
	 * xử lý trường hợp header không chứa currentHour bằng cách thêm
	 * NumberFormatException, ch set header nên hiển nhiên là không có
	 */
	@Test
	public void testGetLocationByIpAddressShouldReturn400BadRequestBecauseNoHeaderXCurrent() throws Exception {
		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testGetLocationByIpAddressShouldReturn400BadRequestBecauseGeolocationException() throws Exception {
		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString()))
				.thenThrow(GeolocationException.class);

		mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, "9")).andExpect(status().isBadRequest())
				.andDo(print());
	}

	@Test
	public void testGetLocationByIpAddressShouldReturn204NoContent() throws Exception {
		int currentHour = 10;
		Location location = Location.builder().code("HCM_VN").build();

		List<HourlyWeather> hourlyWeathers = new ArrayList<HourlyWeather>();

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);
		Mockito.when(this.hourlyWeatherService.getListHourlyWeather(location, currentHour)).thenReturn(hourlyWeathers);

		mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
				.andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	public void testGetLocationByIpAddressShouldReturn200Ok() throws Exception {
		int currentHour = 10;
		Location location = new Location();
		location.setCode("HN_VN");
		location.setCityName("Hanoi");
		location.setRegionName("Northern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		HourlyWeather hourlyWeather1 = HourlyWeather.builder().id(new HourlyWeatherId(5, location)).temperature(20)
				.precipitation(20).status("Snowy").build();
		HourlyWeather hourlyWeather2 = HourlyWeather.builder().id(new HourlyWeatherId(6, location)).temperature(30)
				.precipitation(30).status("Rainy").build();

		List<HourlyWeather> hourlyWeathers = location.getListHourlyWeather();
		hourlyWeathers.add(hourlyWeather1);
		hourlyWeathers.add(hourlyWeather2);

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);
		Mockito.when(this.hourlyWeatherService.getListHourlyWeather(location, currentHour)).thenReturn(hourlyWeathers);

		String expectedLocation = location.toString();

		mockMvc.perform(get(END_POINT_PATH).header(X_CURRENT_HOUR, String.valueOf(currentHour)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.location", is(expectedLocation))).andDo(print());
	}

	@Test
	public void testListHourlyForecastByLocationCodeShouldReturn400BadRequestBecauseNoHeaderXCurrent()
			throws Exception {
		mockMvc.perform(get(END_POINT_PATH + "/HCM_VN")) // thiếu header
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testListHourlyForecastByLocationCodeShouldReturn404NotFoundBecauseLocationNotFound() throws Exception {
		String locationCode = "VN";
		int currentHour = 9;

		Mockito.when(hourlyWeatherService.getHourlyWeatherByLocationCodeAndCurrentHour(locationCode, currentHour))
				.thenThrow(LocationNotFoundException.class);

		mockMvc.perform(get(END_POINT_PATH + "/" + locationCode).header("X-Current-Hour", String.valueOf(currentHour)))
				.andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testListHourlyForecastByLocationCodeShouldReturn204NoContent() throws Exception {
		int currentHour = 9;
		List<HourlyWeather> emptyList = new ArrayList<>();

		Mockito.when(hourlyWeatherService.getHourlyWeatherByLocationCodeAndCurrentHour(Mockito.eq("HCM_VN"),
				Mockito.eq(currentHour))).thenReturn(emptyList);

		mockMvc.perform(get(END_POINT_PATH + "/HCM_VN").header("X-Current-Hour", String.valueOf(currentHour)))
				.andExpect(status().isNoContent()).andDo(print());
	}

	@Test
	public void testListHourlyForecastByLocationCodeShouldReturn200Ok() throws Exception {
		int currentHour = 10;
		Location location = Location.builder().code("HCM_VN").build();

		HourlyWeather hw1 = HourlyWeather.builder().id(new HourlyWeatherId(10, location)).status("Sunny")
				.temperature(32).build();
		HourlyWeather hw2 = HourlyWeather.builder().id(new HourlyWeatherId(11, location)).status("Rainy")
				.temperature(28).build();
		List<HourlyWeather> list = List.of(hw1, hw2);

		Mockito.when(hourlyWeatherService.getHourlyWeatherByLocationCodeAndCurrentHour(Mockito.eq("HCM_VN"),
				Mockito.eq(currentHour))).thenReturn(list);

		mockMvc.perform(get(END_POINT_PATH + "/HCM_VN").header("X-Current-Hour", String.valueOf(currentHour)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.hourly_forecast[0].status", is("Sunny")))
				.andExpect(jsonPath("$.hourly_forecast[1].status", is("Rainy"))).andDo(print());
	}

	@Test
	public void testUpdateHourlyWeatherShouldReturn400BadRequest() throws Exception {
		String uri = END_POINT_PATH + "/ABC";
		List<HourlyWeather> hourlyWeathers = Collections.emptyList();
		String requestBody = objectMapper.writeValueAsString(hourlyWeathers);

		mockMvc.perform(put(uri).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testUpdateHourlyWeatherShouldReturn400BadRequestBecauseInvalidData() throws Exception {
		String uri = END_POINT_PATH + "/ABC";

		HourlyWeatherDTO hourlyWeatherDTO1 = new HourlyWeatherDTO(1000, 1000, 1000, "Rainy");
		HourlyWeatherDTO hourlyWeatherDTO2 = new HourlyWeatherDTO(1200, 1200, 1200, "Rainy");

		List<HourlyWeatherDTO> hourlyWeatherDTOs = List.of(hourlyWeatherDTO1, hourlyWeatherDTO2);

		String requestBody = objectMapper.writeValueAsString(hourlyWeatherDTOs);

		mockMvc.perform(put(uri).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testUpdateHourlyWeatherShouldReturn404NotFound() throws Exception {
		String locationCode = "ABC";
		String uri = END_POINT_PATH + "/" + locationCode;

		HourlyWeatherDTO hourlyWeatherDTO1 = new HourlyWeatherDTO(10, 10, 10, "Rainy");
		HourlyWeatherDTO hourlyWeatherDTO2 = new HourlyWeatherDTO(12, 12, 12, "Rainy");

		List<HourlyWeatherDTO> hourlyWeatherDTOs = List.of(hourlyWeatherDTO1, hourlyWeatherDTO2);

		/*
		 * Mockito yêu cầu các tham số cùng kiểu (cùng là giá trị hoặc cùng là matcher
		 * nếu ko sẽ lỗi -> vì dùng anyList() -> buộc dùng eq)
		 */
		Mockito.when(this.hourlyWeatherService.updateHourlyWeather(Mockito.eq(locationCode), Mockito.anyList()))
				.thenThrow(LocationNotFoundException.class);

		String requestBody = objectMapper.writeValueAsString(hourlyWeatherDTOs);

		mockMvc.perform(put(uri).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testUpdateHourlyWeatherShouldReturn200Ok() throws Exception {
		String locationCode = "ABC";
		String uri = END_POINT_PATH + "/" + locationCode;

		Location location = new Location();
		location.setCode("HN_VN");
		location.setCityName("Hanoi");
		location.setRegionName("Northern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		HourlyWeather hourlyWeather1 = new HourlyWeather(new HourlyWeatherId(10, location), 10, 10, "Rainy");
		HourlyWeather hourlyWeather2 = new HourlyWeather(new HourlyWeatherId(12, location), 12, 12, "Rainy");

		List<HourlyWeather> hourlyWeathers = List.of(hourlyWeather1, hourlyWeather2);

		/*
		 * Mockito yêu cầu các tham số cùng kiểu (cùng là giá trị hoặc cùng là matcher
		 * nếu ko sẽ lỗi -> vì dùng anyList() -> buộc dùng eq)
		 */
		Mockito.when(this.hourlyWeatherService.updateHourlyWeather(Mockito.eq(locationCode), Mockito.anyList()))
				.thenReturn(hourlyWeathers);

		String requestBody = objectMapper.writeValueAsString(hourlyWeathers);

		mockMvc.perform(put(uri).contentType(MediaType.APPLICATION_JSON).content(requestBody))
				.andExpect(status().isOk()).andExpect(jsonPath("$.location", is(location.toString()))).andDo(print());
	}
}
