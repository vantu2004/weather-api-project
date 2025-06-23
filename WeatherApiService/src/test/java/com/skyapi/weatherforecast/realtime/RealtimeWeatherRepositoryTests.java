package com.skyapi.weatherforecast.realtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class RealtimeWeatherRepositoryTests {
	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private RealtimeWeatherRepository realtimeWeatherRepository;

	@Test
	public void testUpdateRealtimeWeather() {
		String code = "NYC_USA";
		Location location = this.locationRepository.findByCode(code);
		RealtimeWeather realtimeWeather = location.getRealtimeWeather();

		realtimeWeather.setTemperature(-100);
		realtimeWeather.setHumidity(60);
		realtimeWeather.setPrecipitation(50);
		realtimeWeather.setWindSpeed(5);
		realtimeWeather.setStatus("Snowy");
		realtimeWeather.setLastUpdated(new Date());

		RealtimeWeather updatedRealtimeWeather = this.realtimeWeatherRepository.save(realtimeWeather);

		assertThat(updatedRealtimeWeather.getTemperature()).isEqualTo(-100);
	}

	@Test
	public void testGetRealtimeWeatherByCountryCodeAndCityNameShouldNotFound() {
		String countryCode = "A";
		String cityName = "B";
		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByCountryCodeAndCityName(countryCode,
				cityName);

		assertThat(realtimeWeather).isNull();
	}

	@Test
	public void testGetRealtimeWeatherByCountryCodeAndCityName() {
		String countryCode = "VN";
		String cityName = "Ho Chi Minh City";
		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByCountryCodeAndCityName(countryCode,
				cityName);

		assertThat(realtimeWeather).isNotNull();
		assertThat(realtimeWeather.getLocationCode()).isEqualTo("HCM_VN");
		// System.out.println(realtimeWeather);
	}

	@Test
	public void testFindByLocationNotFound() {
		String locationCode = "ABCXYZ";
		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByLocationCode(locationCode);

		assertThat(realtimeWeather).isNull();
	}

	@Test
	public void testFindByTrashedLocationNotFound() {
		String locationCode = "NYC_USA";
		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByLocationCode(locationCode);

		assertThat(realtimeWeather).isNull();
	}

	@Test
	public void testFindByLocationFound() {
		String locationCode = "HCM_VN";
		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByLocationCode(locationCode);

		assertThat(realtimeWeather).isNotNull();
		assertThat(realtimeWeather.getLocationCode()).isEqualTo(locationCode);
	}
}
