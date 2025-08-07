package com.skyapi.weatherforecast;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
/*
 * @Profile nghĩa là chỉ định đoạn code nào (bean/class nào) sẽ được Spring
 * khởi tạo (tức là “chạy”) dựa theo profile đang được kích hoạt. "Nếu bạn đang
 * chạy môi trường dev, thì chỉ khởi tạo các bean/class có @Profile("dev")."
 */
@Profile("test")
public class SecurityConfigForControllerTests {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.authorizeHttpRequests(auth -> auth.anyRequest().permitAll()).csrf(csrf -> csrf.disable());

		return httpSecurity.build();
	}
}
