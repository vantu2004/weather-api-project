package com.skyapi.weatherforecast.security;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
//@SpringBootTest ko bao gồm @AutoConfigureMockMvc nên phải khai báo, còn @WebMvcTest đã bao gồm nên ko cần
@AutoConfigureMockMvc
public class SecurityTests {
	private static final String GET_ACCESS_TOKEN_ENDPOINT = "/oauth2/token";

	@Autowired
	private MockMvc mockMvc;

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
				.perform(post(GET_ACCESS_TOKEN_ENDPOINT).param("client_id", "XwP6SoDhUanerD1uCRVU")
						.param("client_secret", "pRgtJ3NP2sytmGxo4z4cnuHPJIVNo9ZUkbMFQPlB")
						.param("grant_type", "client_credentials"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.access_token").isString())
				.andExpect(jsonPath("$.expires_in").isNumber()).andExpect(jsonPath("$.token_type", is("Bearer")))
				.andDo(print());
	}
}
