package com.skyapi.weatherforecast.location;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skyapi.weatherforecast.common.Location;

public interface LocationRepository extends JpaRepository<Location, String> {

}
