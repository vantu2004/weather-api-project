package com.skyapi.weatherforecast.full;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.DailyWeather;
import com.skyapi.weatherforecast.common.HourlyWeather;
import com.skyapi.weatherforecast.common.Location;
import com.skyapi.weatherforecast.common.RealtimeWeather;
import com.skyapi.weatherforecast.location.LocationNotFoundException;
import com.skyapi.weatherforecast.location.LocationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FullWeatherService {
	private final LocationRepository locationRepository;

	public Location getLocationByIpAddress(Location locationFromIp) {
		String countryCode = locationFromIp.getCountryCode();
		String cityName = locationFromIp.getCityName();

		Location location = this.locationRepository.findByCountryCodeAndCityName(countryCode, cityName);
		if (location == null) {
			throw new LocationNotFoundException(countryCode, cityName);
		}

		return location;
	}

	public Location getLocationByLocationCode(String locationCode) {
		Location location = this.locationRepository.findByCode(locationCode);
		if (location == null) {
			throw new LocationNotFoundException(locationCode);
		}

		return location;
	}

	public Location updateFullWeather(String locationCode, Location locationInRequest) {
		Location locationInDB = this.locationRepository.findByCode(locationCode);
		if (locationInDB == null) {
			throw new LocationNotFoundException(locationCode);
		}

		RealtimeWeather realtimeWeather = locationInRequest.getRealtimeWeather();
		realtimeWeather.setLocation(locationInDB);
		realtimeWeather.setLastUpdated(new Date());

		/*
		 * trường hợp realtimeWeather chưa tồn tại mà lưu luôn thì sẽ lỗi vì quan hệ 1-1
		 * chưa thiết lập và Hibernate ko hiểu -> setRealtimeWeather() -> save Location
		 * để tạo quan hệ
		 */
		if (locationInDB.getRealtimeWeather() != null) {
			/*
			 * khi test chỉ cần setLocation là đủ vì @MapsId đã tự ánh xạ location.getCode()
			 * sang locationCode của realtimeWeather, việc set thủ công khiến hibernate nghĩ
			 * là cần cập nhật entity
			 */
			realtimeWeather.setLocationCode(locationCode);
		} else {
			locationInDB.setRealtimeWeather(realtimeWeather);
			this.locationRepository.save(locationInDB);

			realtimeWeather.setLocationCode(locationCode);
		}

		List<HourlyWeather> hourlyWeathers = locationInRequest.getListHourlyWeather();
		hourlyWeathers.forEach(hourlyWeather -> hourlyWeather.getId().setLocation(locationInDB));

		List<DailyWeather> dailyWeathers = locationInRequest.getListDailyWeathers();
		dailyWeathers.forEach(dailyWeather -> dailyWeather.getId().setLocation(locationInDB));

		locationInRequest.setCode(locationInDB.getCode());
		locationInRequest.setCityName(locationInDB.getCityName());
		locationInRequest.setRegionName(locationInDB.getRegionName());
		locationInRequest.setCountryName(locationInDB.getCountryName());
		locationInRequest.setCountryCode(locationInDB.getCountryCode());
		locationInRequest.setEnabled(locationInDB.isEnabled());
		locationInRequest.setTrashed(locationInDB.isTrashed());

		return this.locationRepository.save(locationInRequest);
	}
}
