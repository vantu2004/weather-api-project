package com.skyapi.weatherforecast.location;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

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
	public void testGetAllLocationUnTrashed() {
		List<Location> locations = this.locationRepository.findAllUnTrashed();

		assertThat(locations).isNotEmpty();

		locations.forEach(System.out::println);
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

}
