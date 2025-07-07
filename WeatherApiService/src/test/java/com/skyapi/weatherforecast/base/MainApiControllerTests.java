package com.skyapi.weatherforecast.base;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MainApiController.class)
public class MainApiControllerTests {
	private static final String BASE_URI = "/";

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testGetBaseUri() throws Exception {
		mockMvc.perform(get(BASE_URI)).andExpect(status().isOk()).andExpect(content().contentType("application/json"))
				.andExpect(jsonPath("$.locations_url", is("http://localhost/v1/locations")))
				.andExpect(jsonPath("$.location_by_code_url", is("http://localhost/v1/locations/{code}")))
				.andExpect(jsonPath("$.realtime_weather_by_ip_url", is("http://localhost/v1/realtime")))
				.andExpect(
						jsonPath("$.realtime_weather_by_code_url", is("http://localhost/v1/realtime/{locationCode}")))
				.andExpect(jsonPath("$.hourly_forecast_by_ip_url", is("http://localhost/v1/hourly")))
				.andExpect(jsonPath("$.hourly_forecast_by_code_url", is("http://localhost/v1/hourly/{locationCode}")))
				.andExpect(jsonPath("$.daily_forecast_by_ip_url", is("http://localhost/v1/daily")))
				.andExpect(jsonPath("$.daily_forecast_by_code_url", is("http://localhost/v1/daily/{locationCode}")))
				.andExpect(jsonPath("$.full_weather_by_ip_url", is("http://localhost/v1/full")))
				.andExpect(jsonPath("$.full_weather_by_code_url", is("http://localhost/v1/full/{locationCode}")))
				.andDo(print());
	}
}
