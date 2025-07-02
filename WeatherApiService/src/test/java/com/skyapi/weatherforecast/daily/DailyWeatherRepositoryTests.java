package com.skyapi.weatherforecast.daily;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.DailyWeatherId;
import com.skyapi.weatherforecast.common.Location;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class DailyWeatherRepositoryTests {
	@Autowired
	private DailyWeatherRepository dailyWeatherRepository;

	@Test
	public void testAddDailyWeather() {
		String locationCode = "DN_VN";
		Location location = Location.builder().code(locationCode).build();

		DailyWeather dailyWeather = DailyWeather.builder().id(new DailyWeatherId(1, 2, location)).maxTemp(35)
				.minTemp(10).precipitation(22).status("Windy").build();

		DailyWeather addedDailyWeather = this.dailyWeatherRepository.save(dailyWeather);

		assertThat(addedDailyWeather.getId().getLocation().getCode()).isEqualTo(locationCode);
	}

	@Test
	public void testDeleteDailyWeather() {
		String locationCode = "HCM_VN";
		Location location = Location.builder().code(locationCode).build();

		DailyWeatherId dailyWeatherId = new DailyWeatherId(4, 5, location);
		this.dailyWeatherRepository.deleteById(dailyWeatherId);

		Optional<DailyWeather> dailyWeather = this.dailyWeatherRepository.findById(dailyWeatherId);

		assertThat(dailyWeather).isNotPresent();
	}
}
