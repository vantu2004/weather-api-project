package com.skyapi.weatherforecast;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;
import com.skyapi.weatherforecast.common.Location;

@Service
public class GeolocationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeolocationService.class);
	private String dbPath = "ip2_location_db/IP2LOCATION-LITE-DB3.BIN";
	private IP2Location ip2Location;

	public GeolocationService() {
		try {
			ip2Location = new IP2Location();
			ip2Location.Open(dbPath);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	public Location getLocationByIp2Location(String ipAdress) throws GeolocationException {
		try {
			IPResult ipResult = this.ip2Location.IPQuery(ipAdress);

			// chuyển OK ra trước vì nếu getStatus null thì ko ném lỗi
			if (!"OK".equals(ipResult.getStatus())) {
				throw new GeolocationException("Geolocation Failed with Status Code " + ipResult.getStatus());
			}

			LOGGER.info(ipResult.toString());

			Location location = Location.builder().cityName(ipResult.getCity()).regionName(ipResult.getRegion())
					.countryName(ipResult.getCountryLong()).countryCode(ipResult.getCountryShort()).build();

			return location;
		} catch (IOException e) {
			throw new GeolocationException("Error Querying IP Database!", e);
		}

	}
}
