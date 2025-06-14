package com.skyapi.weatherforecast;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationRepository;

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
		location.setCode("NYC_USA");
		location.setCityName("New York City");
		location.setRegionName("New York");
		location.setCountryName("United State of America");
		location.setCountryCode("US");

		Location savedLocation = this.locationRepository.save(location);

		assertThat(savedLocation.getCode()).isEqualTo("NYC_USA");
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
}
