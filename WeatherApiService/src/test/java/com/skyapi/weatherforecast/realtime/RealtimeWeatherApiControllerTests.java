package com.skyapi.weatherforecast.realtime;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.GeolocationException;
import com.skyapi.weatherforecast.GeolocationService;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;

@WebMvcTest(RealtimeWeatherApiController.class)
public class RealtimeWeatherApiControllerTests {
	private static final String END_POINT_PATH = "/v1/realtime";

	// giả lập HTTP request đến controller
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private RealtimeWeatherService realtimeWeatherService;
	@MockBean
	private GeolocationService geolocationService;

	@Test
	public void testGetRealtimeWeatherShouldReturn400BadRequest() throws Exception {
		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString()))
				.thenThrow(GeolocationException.class);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testGetRealtimeWeatherShouldReturn404NotFound() throws Exception {
		Location location = new Location();

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);
		Mockito.when(this.realtimeWeatherService.getRealtimeWeatherByCountryCodeAndCityName(location))
				.thenThrow(LocationNotFoundException.class);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testGetRealtimeWeatherShouldReturn200Ok() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setLocationCode(location.getCode());
		realtimeWeather.setTemperature(-100);
		realtimeWeather.setHumidity(60);
		realtimeWeather.setPrecipitation(50);
		realtimeWeather.setWindSpeed(5);
		realtimeWeather.setStatus("Snowy");
		realtimeWeather.setLastUpdated(new Date());

		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);

		Mockito.when(this.geolocationService.getLocationByIp2Location(Mockito.anyString())).thenReturn(location);
		Mockito.when(this.realtimeWeatherService.getRealtimeWeatherByCountryCodeAndCityName(location))
				.thenReturn(realtimeWeather);

		mockMvc.perform(get(END_POINT_PATH)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testGetRealtimeWeatherByLocationCodeShouldReturn404NotFound() throws Exception {
		String locationCode = "ABC";

		Mockito.when(this.realtimeWeatherService.getRealtimeWeatherByLocationCode(locationCode))
				.thenThrow(LocationNotFoundException.class);

		String requestUri = END_POINT_PATH + "/" + locationCode;

		mockMvc.perform(get(requestUri)).andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testGetRealtimeWeatherByLocationCodeShouldReturn200Ok() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setLocationCode(location.getCode());
		realtimeWeather.setTemperature(-100);
		realtimeWeather.setHumidity(60);
		realtimeWeather.setPrecipitation(50);
		realtimeWeather.setWindSpeed(5);
		realtimeWeather.setStatus("Snowy");
		realtimeWeather.setLastUpdated(new Date());

		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);

		Mockito.when(this.realtimeWeatherService.getRealtimeWeatherByLocationCode(location.getCode()))
				.thenReturn(realtimeWeather);

		String requestUri = END_POINT_PATH + "/" + location.getCode();

		mockMvc.perform(get(requestUri)).andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testUpdateRealtimeWeatherShouldReturn400BadRequest() throws Exception {
		String locationCode = "HCM_VN";
		String requestUri = END_POINT_PATH + "/" + locationCode;

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setTemperature(120);
		realtimeWeather.setHumidity(-2);
		realtimeWeather.setPrecipitation(-2);
		realtimeWeather.setWindSpeed(-2);
		realtimeWeather.setStatus("");

		String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

		mockMvc.perform(put(requestUri).contentType("application/json").content(bodyContent))
				.andExpect(status().isBadRequest()).andDo(print());
	}

	@Test
	public void testUpdateRealtimeWeatherShouldReturn404NotFound() throws Exception {
		String locationCode = "ABC";
		String requestUri = END_POINT_PATH + "/" + locationCode;

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setTemperature(-25);
		realtimeWeather.setHumidity(60);
		realtimeWeather.setPrecipitation(50);
		realtimeWeather.setWindSpeed(5);
		realtimeWeather.setStatus("Snowy");

		Mockito.when(this.realtimeWeatherService.updateRealtimeWeather(locationCode, realtimeWeather))
				.thenThrow(LocationNotFoundException.class);

		String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

		/*
		 * khi call đến api thì đag truyền bodyContent và được convert về
		 * realtimeWeather bởi @RequestBody (1 đối tượng mới có cùng nội dung), nhưng
		 * Mockito phía trên nhận vào tham số là realtimeWeather gốc, 2 realtimeWeather
		 * lúc này đã khác địa chỉ ô nhớ nên xảy ra lỗi vì thế cần hàm hashCode và equal
		 * để so sánh
		 */
		mockMvc.perform(put(requestUri).contentType("application/json").content(bodyContent))
				.andExpect(status().isNotFound()).andDo(print());
	}

	@Test
	public void testUpdateRealtimeWeatherShouldReturn200Ok() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Southern Vietnam");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		RealtimeWeather realtimeWeather = new RealtimeWeather();
		realtimeWeather.setTemperature(-25);
		realtimeWeather.setHumidity(60);
		realtimeWeather.setPrecipitation(50);
		realtimeWeather.setWindSpeed(5);
		realtimeWeather.setStatus("Snowy");
		realtimeWeather.setLastUpdated(new Date());

		realtimeWeather.setLocation(location);
		location.setRealtimeWeather(realtimeWeather);

		String requestUri = END_POINT_PATH + "/" + location.getCode();

		Mockito.when(this.realtimeWeatherService.updateRealtimeWeather(location.getCode(), realtimeWeather))
				.thenReturn(realtimeWeather);

		/*
		 * vì dung @JsonIgnore cho 3 field locationCode/lastUpdated/location nên thông
		 * tin 3 field này ko có trong bodyContent nhưng khi check với expectedLocation
		 * vẫn đúng vì Mockito đag set để trả về realtimeWeather với các field đã update
		 */
		String bodyContent = objectMapper.writeValueAsString(realtimeWeather);

		String expectedLocation = location.getCityName() + ", " + location.getRegionName() + ", "
				+ location.getCountryName();

		mockMvc.perform(put(requestUri).contentType("application/json").content(bodyContent)).andExpect(status().isOk())
				.andExpect(jsonPath("$.location", is(expectedLocation))).andDo(print());
	}
}
