package com.skyapi.weatherforecast.location;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.skyapi.weatherforecast.common.Location;

@DataJpaTest
//dùng chính db đã cấu hình trong file properties thay vì dùng db mặc định như H2
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class FilterableLocationRepositoryTests {
	@Autowired
	private LocationRepository locationRepository;

	@Test
	public void testListLocationsWithDefaults() {
		int pageNum = 0;
		int pageSize = 5;
		String sortField = "code";

		Sort sort = Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

		Page<Location> page = this.locationRepository.listWithFilter(pageable, Collections.emptyMap());

		List<Location> locations = page.getContent();

		/*
		 * đảm bảo tổng số row (ko phân trang) > tổng số row (tính đến hết trang hiện
		 * tại)
		 */
		System.out.println("OFFSET " + pageable.getOffset());
		System.out.println("ELEMENTS_IN_PAGE " + (pageable.getOffset() + locations.size()));
		System.out.println("TOTAL_ELEMENTS " + page.getTotalElements());
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(pageable.getOffset() + locations.size());

		assertThat(locations).isNotEmpty();
		assertThat(locations.size()).isLessThanOrEqualTo(pageSize);
		assertThat(locations).isSortedAccordingTo(new Comparator<Location>() {

			@Override
			public int compare(Location l1, Location l2) {
				return l1.getCode().compareTo(l2.getCode());
			}
		});

		locations.forEach(System.out::println);
	}

	@Test
	public void testListNoFilterSortedByCityName() {
		int pageNum = 0;
		int pageSize = 5;
		String sortField = "cityName";

		Sort sort = Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

		Page<Location> page = this.locationRepository.listWithFilter(pageable, Collections.emptyMap());

		List<Location> locations = page.getContent();

		System.out.println("OFFSET " + pageable.getOffset());
		System.out.println("ELEMENTS_IN_PAGE " + (pageable.getOffset() + locations.size()));
		System.out.println("TOTAL_ELEMENTS " + page.getTotalElements());
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(pageable.getOffset() + locations.size());

		assertThat(locations).isNotEmpty();
		assertThat(locations.size()).isLessThanOrEqualTo(pageSize);
		assertThat(locations).isSortedAccordingTo(new Comparator<Location>() {

			@Override
			public int compare(Location l1, Location l2) {
				return l1.getCityName().compareTo(l2.getCityName());
			}
		});

		locations.forEach(System.out::println);
	}

	@Test
	public void testListFilteredRegionNameSortedByCityName() {
		int pageNum = 0;
		int pageSize = 5;
		String sortField = "cityName";
		String regionName = "Ho Chi Minh";

		Sort sort = Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

		Map<String, Object> filterFields = new HashMap<String, Object>();
		filterFields.put("regionName", regionName);

		Page<Location> page = this.locationRepository.listWithFilter(pageable, filterFields);

		List<Location> locations = page.getContent();

		System.out.println("OFFSET " + pageable.getOffset());
		System.out.println("ELEMENTS_IN_PAGE " + (pageable.getOffset() + locations.size()));
		System.out.println("TOTAL_ELEMENTS " + page.getTotalElements());
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(pageable.getOffset() + locations.size());

		assertThat(locations).isNotEmpty();
		assertThat(locations.size()).isLessThanOrEqualTo(pageSize);
		assertThat(locations).isSortedAccordingTo(new Comparator<Location>() {

			@Override
			public int compare(Location l1, Location l2) {
				return l1.getCityName().compareTo(l2.getCityName());
			}
		});
		locations.forEach(location -> assertThat(location.getRegionName()).isEqualTo(regionName));

		locations.forEach(System.out::println);
	}

	@Test
	public void testListFilteredCountryCodeAndEnabledSortedByCityName() {
		int pageNum = 0;
		int pageSize = 5;
		String sortField = "cityName";
		String countryCode = "VN";
		boolean enabled = true;

		Sort sort = Sort.by(sortField).ascending();
		Pageable pageable = PageRequest.of(pageNum, pageSize, sort);

		Map<String, Object> filterFields = new HashMap<String, Object>();
		filterFields.put("countryCode", countryCode);
		filterFields.put("enabled", enabled);

		Page<Location> page = this.locationRepository.listWithFilter(pageable, filterFields);

		List<Location> locations = page.getContent();

		System.out.println("OFFSET " + pageable.getOffset());
		System.out.println("ELEMENTS_IN_PAGE " + (pageable.getOffset() + locations.size()));
		System.out.println("TOTAL_ELEMENTS " + page.getTotalElements());
		assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(pageable.getOffset() + locations.size());
		
		assertThat(locations).isNotEmpty();
		assertThat(locations.size()).isLessThanOrEqualTo(pageSize);
		assertThat(locations).isSortedAccordingTo(new Comparator<Location>() {

			@Override
			public int compare(Location l1, Location l2) {
				return l1.getCityName().compareTo(l2.getCityName());
			}
		});
		locations.forEach(location -> {
			assertThat(location.getCountryCode()).isEqualTo(countryCode);
			assertThat(location.isEnabled()).isTrue();
		});

		locations.forEach(System.out::println);

	}
}
