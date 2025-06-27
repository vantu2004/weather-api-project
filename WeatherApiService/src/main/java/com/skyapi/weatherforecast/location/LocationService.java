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

	public Location getLocationByCode(String code){
		Location location = this.locationRepository.findByCode(code);
		if (location == null) {
			throw new LocationNotFoundException(code);
		}
		return location;
	}

	public Location updateLocation(Location locationInRequest){
		String code = locationInRequest.getCode();
		Location locationInDb = this.getLocationByCode(code);

		locationInDb.setCityName(locationInRequest.getCityName());
		locationInDb.setRegionName(locationInRequest.getRegionName());
		locationInDb.setCountryName(locationInRequest.getCountryName());
		locationInDb.setCountryCode(locationInRequest.getCountryCode());
		locationInDb.setEnabled(locationInRequest.isEnabled());

		return this.locationRepository.save(locationInDb);
	}

	public void deleteLocation(String code){
		this.getLocationByCode(code);
		this.locationRepository.trashByCode(code);
	}
}
