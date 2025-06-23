package com.skyapi.weatherforecast;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WeatherApiServiceApplication {

	/*
	 * để spring quản lý vòng đời mà ko cần quản lý thủ công vì modelMapper sau khi
	 * được tải về vẫn chưa được uan rlys bởi spring, và khi dùng chỉ cần inject là
	 * được
	 */
	@Bean
	public ModelMapper getModelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		return modelMapper;
	}

	public static void main(String[] args) {
		SpringApplication.run(WeatherApiServiceApplication.class, args);
	}

}
