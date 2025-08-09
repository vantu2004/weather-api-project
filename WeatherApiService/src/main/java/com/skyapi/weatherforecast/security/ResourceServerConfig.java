package com.skyapi.weatherforecast.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {
	private static final String LOCATION_ENDPOINT_PATTERN = "/v1/locations/**";
	private static final String REALTIME_WEATHER_ENDPOINT_PATTERN = "/v1/realtime/**";
	private static final String HOURLY_WEATHER_ENDPOINT_PATTERN = "/v1/hourly/**";
	private static final String DAILY_WEATHER_ENDPOINT_PATTERN = "/v1/daily/**";
	private static final String FULL_WEATHER_ENDPOINT_PATTERN = "/v1/full/**";

	private static final String SCOPE_READER = "SCOPE_READER";
	private static final String SCOPE_UPDATER = "SCOPE_UPDATER";
	private static final String SCOPE_SYSTEM = "SCOPE_SYSTEM";

	@Bean
	public SecurityFilterChain securityFilterChainOAuth2ResourceServer(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/").permitAll()
						// SYSTEM full quyền, UPDATER ko có quyền j, READER chỉ get
						.requestMatchers(HttpMethod.GET, LOCATION_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_READER, SCOPE_SYSTEM)
						.requestMatchers(HttpMethod.POST, LOCATION_ENDPOINT_PATTERN).hasAuthority(SCOPE_SYSTEM)
						.requestMatchers(HttpMethod.PUT, LOCATION_ENDPOINT_PATTERN).hasAuthority(SCOPE_SYSTEM)
						.requestMatchers(HttpMethod.DELETE, LOCATION_ENDPOINT_PATTERN).hasAuthority(SCOPE_SYSTEM)

						// 4 ENDPOINT còn lại: GET (SYSTEM, UPDATER, READER), PUT (SYSTEM, UPDATER)

						.requestMatchers(HttpMethod.GET, REALTIME_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER, SCOPE_READER)
						.requestMatchers(HttpMethod.PUT, REALTIME_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER)

						.requestMatchers(HttpMethod.GET, HOURLY_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER, SCOPE_READER)
						.requestMatchers(HttpMethod.PUT, HOURLY_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER)

						.requestMatchers(HttpMethod.GET, DAILY_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER, SCOPE_READER)
						.requestMatchers(HttpMethod.PUT, DAILY_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER)

						.requestMatchers(HttpMethod.GET, FULL_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER, SCOPE_READER)
						.requestMatchers(HttpMethod.PUT, FULL_WEATHER_ENDPOINT_PATTERN)
						.hasAnyAuthority(SCOPE_SYSTEM, SCOPE_UPDATER)

						.anyRequest().authenticated());

		return http.build();
	}
}
