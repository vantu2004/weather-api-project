package com.skyapi.weatherforecast;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.daily.DailyWeatherDTO;
import com.skyapi.weatherforecast.full.FullWeatherDTO;
import com.skyapi.weatherforecast.hourly.HourlyWeatherDTO;

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

		/*
		 * typeMap() tạo 1 ánh xạ tùy chỉnh từ class nguồn -> đích, Mặc định,
		 * ModelMapper chỉ ánh xạ các thuộc tính cùng tên, cùng cấp. Vì hourOfDay nằm
		 * trong id (một field lồng) nên dùng addMapping để tùy chỉnh ánh xạ cụ thể
		 */
		modelMapper.typeMap(HourlyWeather.class, HourlyWeatherDTO.class)
				.addMapping(hourlyWeather -> hourlyWeather.getId().getHourOfDay(), HourlyWeatherDTO::setHourOfDay);

		// ánh xạ ngược lại từ hourOfDay của DTO sang hourOfDay của entity
		modelMapper.typeMap(HourlyWeatherDTO.class, HourlyWeather.class).addMapping(
				hourlyWeatherDTO -> hourlyWeatherDTO.getHourOfDay(),
				(hourlyWeather, value) -> hourlyWeather.getId().setHourOfDay(value == null ? 0 : (int) value));

		modelMapper.typeMap(DailyWeather.class, DailyWeatherDTO.class)
				.addMapping(dailyWeather -> dailyWeather.getId().getDayOfMonth(), DailyWeatherDTO::setDayOfMonth)
				.addMapping(dailyWeather -> dailyWeather.getId().getMonth(), DailyWeatherDTO::setMonth);

		modelMapper.typeMap(DailyWeatherDTO.class, DailyWeather.class)
				.addMapping(dailyWeatherDTO -> dailyWeatherDTO.getDayOfMonth(),
						(dailyWeather, value) -> dailyWeather.getId().setDayOfMonth(value == null ? 0 : (int) value))
				.addMapping(dailyWeatherDTO -> dailyWeatherDTO.getMonth(),
						(dailyWeather, value) -> dailyWeather.getId().setMonth(value == null ? 0 : (int) value));

		modelMapper.typeMap(Location.class, FullWeatherDTO.class).addMapping(location -> location.toString(),
				FullWeatherDTO::setLocation);

		return modelMapper;
	}

	public static void main(String[] args) {
		SpringApplication.run(WeatherApiServiceApplication.class, args);
	}

}
