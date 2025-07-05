package com.skyapi.weatherforecast.realtime;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RealtimeWeatherService {
	private final RealtimeWeatherRepository realtimeWeatherRepository;
	private final LocationRepository locationRepository;

	public RealtimeWeather getRealtimeWeatherByCountryCodeAndCityName(Location location) {
		String countryCode = location.getCountryCode();
		String cityName = location.getCityName();

		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByCountryCodeAndCityName(countryCode,
				cityName);
		if (realtimeWeather == null) {
			throw new LocationNotFoundException(countryCode, cityName);
		}

		return realtimeWeather;
	}

	public RealtimeWeather getRealtimeWeatherByLocationCode(String locationCode) {
		RealtimeWeather realtimeWeather = this.realtimeWeatherRepository.findByLocationCode(locationCode);
		if (realtimeWeather == null) {
			throw new LocationNotFoundException(locationCode);
		}

		return realtimeWeather;
	}

	public RealtimeWeather updateRealtimeWeather(String locationCode, RealtimeWeather realtimeWeather) {
		Location location = this.locationRepository.findByCode(locationCode);
		if (location == null) {
			throw new LocationNotFoundException(locationCode);
		}

		realtimeWeather.setLocation(location);
		realtimeWeather.setLastUpdated(new Date());

		if (location.getRealtimeWeather() != null) {
			/*
			 * khi test chỉ cần setLocation là đủ vì @MapsId đã tự ánh xạ location.getCode()
			 * sang locationCode của realtimeWeather, việc set thủ công khiến hibernate nghĩ
			 * là cần cập nhật entity
			 */
			realtimeWeather.setLocationCode(locationCode);
		}
		/*
		 * trường hợp realtimeWeather chưa tồn tại mà lưu trực tiếp realtimeWeather thì
		 * Hibernate sẽ ko hiểu quan hệ One-To-One -> lỗi -> setRealtimeWeather() và lưu
		 * location để tạo quan hệ cho Hibernate hiểu
		 */
		else {
			location.setRealtimeWeather(realtimeWeather);
			Location updatedLocation = this.locationRepository.save(location);

			return updatedLocation.getRealtimeWeather();
		}

		return this.realtimeWeatherRepository.save(realtimeWeather);
	}
}
