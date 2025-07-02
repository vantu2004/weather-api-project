package com.skyapi.weatherforecast.hourly;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HourlyWeatherService {
	private final HourlyWeatherRepository hourlyWeatherRepository;
	private final LocationRepository locationRepository;

	/*
	 * controller truyền location lấy đc từ ipAddress nên không đủ thông tin (thiếu
	 * locationCode)
	 */
	public List<HourlyWeather> getListHourlyWeather(Location location, int currentHour)
			throws LocationNotFoundException {
		String countryCode = location.getCountryCode();
		String cityName = location.getCityName();
		Location locationInDB = this.locationRepository.findByCountryCodeAndCityName(countryCode, cityName);

		if (locationInDB == null) {
			throw new LocationNotFoundException(countryCode, cityName);
		}

		return this.hourlyWeatherRepository.findByLocationCodeAndHourOfDay(locationInDB.getCode(), currentHour);
	}

	public List<HourlyWeather> getHourlyWeatherByLocationCodeAndCurrentHour(String locationCode, int currentHour) {
		Location location = this.locationRepository.findByCode(locationCode);
		if (location == null) {
			throw new LocationNotFoundException("No location found with the given location code.");
		}

		return this.hourlyWeatherRepository.findByLocationCodeAndHourOfDay(locationCode, currentHour);
	}

	public List<HourlyWeather> updateHourlyWeather(String locationCode, List<HourlyWeather> hourlyWeathers) {
		Location location = this.locationRepository.findByCode(locationCode);
		if (location == null) {
			throw new LocationNotFoundException(locationCode);
		}

		hourlyWeathers.forEach(hourlyWeather -> {
			hourlyWeather.getId().setLocation(location);
		});

		/*
		 * Xoá danh sách cũ, thêm danh sách mới để orphanRemoval xóa những row có trog
		 * db nhưng ko có trong request
		 */
		location.getListHourlyWeather().clear();
		location.getListHourlyWeather().addAll(hourlyWeathers);

		// Lưu location => tự động xóa các orphan
		Location updatedLocation = locationRepository.save(location);

		return updatedLocation.getListHourlyWeather();
	}
}
