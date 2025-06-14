package com.skyapi.weatherforecast.location;

import java.util.List;

import org.springframework.stereotype.Service;

import com.skyapi.weatherforecast.common.Location;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {
	private final LocationRepository locationRepository;

	public Location add(Location location) {
		return this.locationRepository.save(location);
	}

	public List<Location> getAllLocationUnTrashed() {
		return this.locationRepository.findAllUnTrashed();
	}

	public Location getLocationByCode(String code) {
		return this.locationRepository.findByCode(code);
	}

	public Location updateLocation(Location locationInRequest) throws LocationNotFoundException {
		String code = locationInRequest.getCode();
		Location locationInDb = this.getLocationByCode(code);
		if (locationInDb == null) {
			throw new LocationNotFoundException("No location found with the given code:" + code);
		}

		locationInDb.setCityName(locationInRequest.getCityName());
		locationInDb.setRegionName(locationInRequest.getRegionName());
		locationInDb.setCountryName(locationInRequest.getCountryName());
		locationInDb.setCountryCode(locationInRequest.getCountryCode());
		locationInDb.setEnabled(locationInRequest.isEnabled());

		return this.locationRepository.save(locationInDb);
	}

	public void deleteLocation(String code) throws LocationNotFoundException {
		Location locationInDb = this.getLocationByCode(code);
		if (locationInDb == null) {
			throw new LocationNotFoundException("No location found with the given code:" + code);
		}

		this.locationRepository.trashByCode(code);
	}
}
