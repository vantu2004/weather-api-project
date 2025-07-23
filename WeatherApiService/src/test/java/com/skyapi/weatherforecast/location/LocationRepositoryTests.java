package com.skyapi.weatherforecast.location;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.DailyWeatherId;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.HourlyWeatherId;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;

@DataJpaTest
// dùng chính db đã cấu hình trong file properties thay vì dùng db mặc định như H2
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class LocationRepositoryTests {
	@Autowired
	private LocationRepository locationRepository;

	@Test
	public void testAddLocation() {
		Location location = new Location();
		location.setCode("HCM_VN");
		location.setCityName("Ho Chi Minh City");
		location.setRegionName("Ho Chi Minh");
		location.setCountryName("Vietnam");
		location.setCountryCode("VN");

		Location savedLocation = this.locationRepository.save(location);

		assertThat(savedLocation.getCode()).isEqualTo("HCM_VN");
	}

	@Test
	@Disabled
	public void testGetAllLocationUnTrashed() {
		List<Location> locations = this.locationRepository.findAllUnTrashed();

		assertThat(locations).isNotEmpty();

		locations.forEach(System.out::println);
	}

	@Test
	public void testGetLocationFirstPage() {
		int pageNum = 0;
		int pageSize = 5;

		Pageable pageable = PageRequest.of(pageNum, pageSize);
		Page<Location> page = this.locationRepository.findAllUnTrashed(pageable);

		assertThat(page).size().isEqualTo(5);

		page.forEach(System.out::println);
	}

	@Test
	public void testGetLocationNoContent() {
		int pageNum = 10;
		int pageSize = 5;

		Pageable pageable = PageRequest.of(pageNum, pageSize);
		Page<Location> page = this.locationRepository.findAllUnTrashed(pageable);

		assertThat(page).isEmpty();

		page.forEach(System.out::println);
	}

	@Test
	public void testGetLocationFirstPageWithSort() {
		int pageNum = 0;
		int pageSize = 5;

		Sort sort = Sort.by("code").ascending();

		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);
		Page<Location> page = this.locationRepository.findAllUnTrashed(pageable);

		assertThat(page).size().isGreaterThanOrEqualTo(3).isLessThanOrEqualTo(5);

		page.forEach(System.out::println);
	}

	@Test
	public void testGetLocationByCode() {
		String code = "A";
		Location location = this.locationRepository.findByCode(code);

		assertThat(location).isNotNull();
		System.out.println(location.getCode());
	}

	@Test
	public void testDeleteLocation() {
		String code = "NYC_USA";
		this.locationRepository.trashByCode(code);

		Location location = this.locationRepository.findByCode(code);
		// trashed rồi thì null mới đúng
		assertThat(location).isNull();
	}

	@Test
	public void testAddRealtimeWeatherData() {
		try {
			String code = "HCM_VN";
			Location location = locationRepository.findByCode(code);
			assertThat(location).isNotNull();

			RealtimeWeather realtimeWeather = location.getRealtimeWeather();
			if (realtimeWeather == null) {
				realtimeWeather = new RealtimeWeather();
				/*
				 * Khi dùng @MapsId, khóa chính (locationCode) của RealtimeWeather phải lấy từ
				 * location.getCode() (đc gọi ngầm), không được tự gán thủ công vì Hibernate vẫn
				 * chưa xác định quan hệ giữa RealtimeWeather với Location, việc gọi
				 * setLocationCode(...) thủ công Hibernate hiểu sai là đang cập nhật một bản ghi
				 * cũ → gây lỗi StaleObjectStateException. Chỉ cần gọi setLocation(location),
				 * Hibernate sẽ tự gán đúng locationCode.
				 */
				realtimeWeather.setLocation(location);
				location.setRealtimeWeather(realtimeWeather);
			}

			realtimeWeather.setTemperature(31);
			realtimeWeather.setHumidity(45);
			realtimeWeather.setPrecipitation(34);
			realtimeWeather.setWindSpeed(25);
			realtimeWeather.setStatus("Sunny");
			realtimeWeather.setLastUpdated(new Date());

			// Không save realtimeWeather riêng
			Location updatedLocation = locationRepository.save(location); // sẽ cascade realtimeWeather luôn

			assertThat(updatedLocation.getRealtimeWeather().getLocationCode()).isEqualTo(code);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Test
	public void testAddHourlyWeatherData() {
		Location location = this.locationRepository.findByCode("HCM_VN");
		assertThat(location).isNotNull();

		List<HourlyWeather> hourlyWeathers = location.getListHourlyWeather();

		HourlyWeather forecast1 = HourlyWeather.builder().id(new HourlyWeatherId(8, location)).temperature(23)
				.precipitation(10).status("Rainy").build();

		HourlyWeather forecast2 = HourlyWeather.builder().id(new HourlyWeatherId(9, location)).temperature(32)
				.precipitation(8).status("Snowy").build();

		hourlyWeathers.add(forecast1);
		hourlyWeathers.add(forecast2);

		Location updatedLocation = this.locationRepository.save(location);

		assertThat(updatedLocation.getListHourlyWeather()).isNotEmpty();
	}

	@Test
	public void testGetLocationByCountryCodeAndCityName() {
		String countryCode = "VN";
		String cityName = "Ho Chi Minh City";
		Location location = this.locationRepository.findByCountryCodeAndCityName(countryCode, cityName);

		assertThat(location).isNotNull();
		assertThat(location.getCountryCode()).isEqualTo(countryCode);
		assertThat(location.getCityName()).isEqualTo(cityName);
	}

	@Test
	public void testAddDailyWeatherData() {
		Location location = this.locationRepository.findByCode("HCM_VN");
		assertThat(location).isNotNull();

		List<DailyWeather> dailyWeathers = location.getListDailyWeathers();

		DailyWeather dailyWeather01 = DailyWeather.builder().id(new DailyWeatherId(2, 6, location)).maxTemp(30)
				.minTemp(20).precipitation(25).status("Sunny").build();
		DailyWeather dailyWeather02 = DailyWeather.builder().id(new DailyWeatherId(4, 7, location)).maxTemp(40)
				.minTemp(30).precipitation(35).status("Rainy").build();

		dailyWeathers.add(dailyWeather01);
		dailyWeathers.add(dailyWeather02);

		Location updatedLocation = this.locationRepository.save(location);

		assertThat(updatedLocation.getListDailyWeathers()).isNotEmpty();
	}

}
