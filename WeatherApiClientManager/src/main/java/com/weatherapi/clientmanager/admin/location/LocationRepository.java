package com.weatherapi.clientmanager.admin.location;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.skyapi.weatherforecast.common.Location;

public interface LocationRepository extends CrudRepository<Location, String> {

	@Query("""
			SELECT NEW Location(l.code, l.countryCode, l.countryName, l.cityName, l.regionName)
			FROM Location l WHERE l.enabled = true
			AND l.trashed = false AND (l.cityName LIKE %?1% OR l.countryName LIKE %?1%
			OR l.countryCode LIKE %?1% OR l.regionName LIKE %?1%)			
			""")
	public List<Location> search(String keyword);
	
	@Query("SELECT l FROM Location l WHERE l.enabled=true AND l.trashed=false AND l.code=?1")
	public Location findByCodeEnabledUntrashed(String code);
}
