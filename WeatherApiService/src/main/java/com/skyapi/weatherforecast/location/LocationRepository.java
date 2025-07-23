package com.skyapi.weatherforecast.location;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.skyapi.weatherforecast.common.Location;

import jakarta.transaction.Transactional;

public interface LocationRepository extends JpaRepository<Location, String>,
		PagingAndSortingRepository<Location, String>, FilterableLocationRepository {
	@Deprecated
	@Query("SELECT l FROM Location l WHERE l.trashed = false")
	List<Location> findAllUnTrashed();

	@Query("SELECT l FROM Location l WHERE l.trashed = false")
	public Page<Location> findAllUnTrashed(Pageable pageable);

	@Query("SELECT l FROM Location l WHERE l.trashed = false AND l.code = ?1")
	public Location findByCode(String code);

	@Modifying
	@Transactional
	@Query("UPDATE Location SET trashed = true WHERE code = ?1")
	public void trashByCode(String code);

	@Query("SELECT l FROM Location l WHERE l.countryCode = ?1 AND l.cityName = ?2 AND l.trashed = false")
	public Location findByCountryCodeAndCityName(String countryCode, String cityName);
}
