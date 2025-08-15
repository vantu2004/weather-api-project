package com.skyapi.weatherforecast.daily;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyWeatherService {
	private final DailyWeatherRepository dailyWeatherRepository;
	private final LocationRepository locationRepository;

	// location lấy bằng ipAddress nếu thiếu thông tin (thiếu locationCode)
	@Cacheable(cacheNames = "dailyWeatherCacheByCountryCodeAndCityName", key = "{#location.countryCode, #location.cityName}")
	public List<DailyWeather> getDailyWeatherByLocation(Location location) {
		String countryCode = location.getCountryCode();
		String cityName = location.getCityName();

		Location locationInDB = this.locationRepository.findByCountryCodeAndCityName(countryCode, cityName);
		if (locationInDB == null) {
			throw new LocationNotFoundException(countryCode, cityName);
		}

		return this.dailyWeatherRepository.findByLocationCode(locationInDB.getCode());
	}

	@Cacheable("dailyWeatherCacheByLocationCode")
	public List<DailyWeather> getDailyWeatherByLocationCode(String locationCode) {
		Location locationInDB = this.locationRepository.findByCode(locationCode);
		if (locationInDB == null) {
			throw new LocationNotFoundException(locationCode);
		}

		return this.dailyWeatherRepository.findByLocationCode(locationInDB.getCode());
	}

	@CachePut(cacheNames = "dailyWeatherCacheByLocationCode", key = "#locationCode")
	@CacheEvict(cacheNames = "dailyWeatherCacheByCountryCodeAndCityName", allEntries = true)
	public List<DailyWeather> updateDailyWeather(String locationCode, List<DailyWeather> dailyWeathers) {
		Location location = this.locationRepository.findByCode(locationCode);
		if (location == null) {
			throw new LocationNotFoundException(locationCode);
		}

		dailyWeathers.forEach(dailyWeather -> {
			dailyWeather.getId().setLocation(location);
		});

		/*
		 * Xoá danh sách cũ, thêm danh sách mới để orphanRemoval xóa những row có trog
		 * db nhưng ko có trong request
		 */
		location.getListDailyWeathers().clear();
		location.getListDailyWeathers().addAll(dailyWeathers);

		// Lưu location => tự động xóa các orphan
		Location updatedLocation = locationRepository.save(location);

		return updatedLocation.getListDailyWeathers();
	}
}
