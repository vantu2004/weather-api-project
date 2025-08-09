package com.skyapi.weatherforecast.security;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.daily.DailyWeatherDTO;
import com.skyapi.weatherforecast.full.FullWeatherDTO;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;
import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;

@SpringBootTest
//@SpringBootTest ko bao gồm @AutoConfigureMockMvc nên phải khai báo, còn @WebMvcTest đã bao gồm nên ko cần
@AutoConfigureMockMvc
public class SecurityTests {
	private static final String GET_ACCESS_TOKEN_ENDPOINT = "/oauth2/token";

	private static final String LOCATION_API_ENDPOINT = "/v1/locations";
	private static final String REALTIME_WEATHER_API_ENDPOINT = "/v1/realtime";
	private static final String HOURLY_WEATHER_API_ENDPOINT = "/v1/hourly";
	private static final String DAILY_WEATHER_API_ENDPOINT = "/v1/daily";
	private static final String FULL_WEATHER_API_ENDPOINT = "/v1/full";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void testGetAccessTokenFail() throws Exception {
		this.mockMvc
				.perform(post(GET_ACCESS_TOKEN_ENDPOINT).param("client_id", "xxxx").param("client_secret", "xxxx")
						.param("grant_type", "client_credentials"))
				.andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error", is("invalid_client")))
				.andDo(print());
	}

	@Test
	public void testGetAccessTokenSuccess() throws Exception {
		this.mockMvc
				.perform(post(GET_ACCESS_TOKEN_ENDPOINT).param("client_id", "Qmr5U18wjVIP935dxLcE")
						.param("client_secret", "RcreuWUdNnLK9h3SlKsKPSQb0zfLuBIQfpGvIWhv")
						.param("grant_type", "client_credentials"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.access_token").isString())
				.andExpect(jsonPath("$.expires_in").isNumber()).andExpect(jsonPath("$.token_type", is("Bearer")))
				.andDo(print());
	}

	@Test
	public void testGetBaseURI() throws Exception {
		this.mockMvc.perform(get("/")).andExpect(status().isOk()).andDo(print());
	}

//	@Test
//	public void testListLocationsWithScopeReader() throws Exception {
//		this.mockMvc.perform(get(LOCATION_API_ENDPOINT)
//				/*
//				 * bỏ qua việc xác thực JWT thật sự (ko xác thực jwt claims hay token). Giả định
//				 * đã đăng nhập với role READER
//				 */
//				.with(jwt().authorities(new SimpleGrantedAuthority("READER")))).andExpect(status().isOk())
//				.andDo(print());
//	}

	@Test
	public void testListLocationsWithScopeReader() throws Exception {
		this.mockMvc.perform(get(LOCATION_API_ENDPOINT).with(jwt().jwt(jwt -> jwt.claim("scope", "READER"))))
				.andExpect(status().isOk()).andDo(print());
	}

	@Test
	public void testListLocationsWithUnknowScope() throws Exception {
		this.mockMvc.perform(get(LOCATION_API_ENDPOINT).with(jwt().jwt(jwt -> jwt.claim("scope", "USER"))))
				.andExpect(status().isForbidden()).andDo(print());
	}

	@Test
	public void testAddLocationWithScopeReader() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Ho Chi Minh");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");
		location.setEnabled(true);

		String bodyContent = this.objectMapper.writeValueAsString(location);

		this.mockMvc
				.perform(post(LOCATION_API_ENDPOINT).with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))
						.contentType(MediaType.APPLICATION_JSON).content(bodyContent))
				.andExpect(status().isForbidden()).andDo(print());
	}

	@Test
	public void testAddLocationWithScopeSystem() throws Exception {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Ho Chi Minh");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");
		location.setEnabled(true);

		String bodyContent = this.objectMapper.writeValueAsString(location);

		this.mockMvc
				.perform(post(LOCATION_API_ENDPOINT).with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM")))
						.contentType(MediaType.APPLICATION_JSON).content(bodyContent))
				.andExpect(status().isCreated()).andDo(print());
	}

	@Test
	public void testGetRealtimeWeatherWithScopeReader() throws Exception {
		String requestURL = REALTIME_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(get(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	public void testUpdateRealtimeWeatherWithScopeReader() throws Exception {
		RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = REALTIME_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	public void testUpdateRealtimeWeatherWithScopeSystem() throws Exception {
		RealtimeWeatherDTO dto = new RealtimeWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = REALTIME_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM")))).andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testGetHourlyWeatherWithScopeReader() throws Exception {
		String requestURL = HOURLY_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(
				get(requestURL).header("X-Current-Hour", "10").with(jwt().jwt(jwt -> jwt.claim("scope", "READER"))))
				.andDo(print()).andExpect(status().isNotFound());
	}

	@Test
	public void testUpdateHourlyWeatherWithScopeReader() throws Exception {
		HourlyWeatherDTO dto = new HourlyWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = HOURLY_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	public void testUpdateHourlyWeatherWithScopeSystem() throws Exception {
		HourlyWeatherDTO dto = new HourlyWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = HOURLY_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM")))).andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testGetDailyWeatherWithScopeReader() throws Exception {
		String requestURL = DAILY_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(get(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	public void testUpdateDailyWeatherWithScopeReader() throws Exception {
		DailyWeatherDTO dto = new DailyWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = DAILY_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	public void testUpdateDailyWeatherWithScopeSystem() throws Exception {
		DailyWeatherDTO dto = new DailyWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = DAILY_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM")))).andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testGetFullWeatherWithScopeReader() throws Exception {
		String requestURL = FULL_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(get(requestURL).with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	public void testUpdateFullWeatherWithWithScopeReader() throws Exception {
		FullWeatherDTO dto = new FullWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = FULL_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "READER")))).andDo(print()).andExpect(status().isForbidden());
	}

	@Test
	public void testUpdateFullWeatherWithScopeSystem() throws Exception {
		FullWeatherDTO dto = new FullWeatherDTO();
		String requestBody = objectMapper.writeValueAsString(dto);

		String requestURL = FULL_WEATHER_API_ENDPOINT + "/Location-Code";

		mockMvc.perform(put(requestURL).contentType(MediaType.APPLICATION_JSON).content(requestBody)
				.with(jwt().jwt(jwt -> jwt.claim("scope", "SYSTEM")))).andDo(print())
				.andExpect(status().isBadRequest());
	}

}
