package com.skyapi.weatherforecast.hourly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.HourlyWeatherId;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.hourly.HourlyWeatherRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class HourlyWeatherRepositoryTests {
	@Autowired
	private HourlyWeatherRepository hourlyWeatherRepository;

	@Test
	public void testAddHourlyWeather() {
		String locationCode = "HCM_VN";
		Location location = Location.builder().code(locationCode).build();

		HourlyWeather hourlyWeather = HourlyWeather.builder().id(new HourlyWeatherId(10, location)).temperature(7)
				.precipitation(15).status("Rainy").build();

		HourlyWeather addedHourlyWeather = this.hourlyWeatherRepository.save(hourlyWeather);

		assertThat(addedHourlyWeather).isNotNull();
		assertThat(addedHourlyWeather.getId().getLocation().getCode()).isEqualTo(locationCode);
	}

	@Test
	public void testDeleteHourlyWeather() {
		Location location = Location.builder().code("HCM_VN").build();
		HourlyWeatherId hourlyWeatherId = new HourlyWeatherId(10, location);

		this.hourlyWeatherRepository.deleteById(hourlyWeatherId);

		Optional<HourlyWeather> hourlyWeather = this.hourlyWeatherRepository.findById(hourlyWeatherId);

		assertThat(hourlyWeather).isNotPresent();
	}

	@Test
	public void testGetHourlyWeatherByLocationCodeAndHourOfDay() {
		String locationCode = "HCM_VN";
		int hourOfDay = 9;
		List<HourlyWeather> hourlyWeathers = this.hourlyWeatherRepository.findByLocationCodeAndHourOfDay(locationCode,
				hourOfDay);

		assertThat(hourlyWeathers).isNotEmpty();
		
		System.out.println(hourlyWeathers);
	}
}
