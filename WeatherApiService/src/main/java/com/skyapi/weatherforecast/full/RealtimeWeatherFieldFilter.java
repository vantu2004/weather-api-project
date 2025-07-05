package com.skyapi.weatherforecast.full;

import com.skyapi.weatherforecast.realtime.RealtimeWeatherDTO;

public class RealtimeWeatherFieldFilter {
	public boolean equals(Object object) {
		if (object instanceof RealtimeWeatherDTO) {
			RealtimeWeatherDTO realtimeWeatherDTO = (RealtimeWeatherDTO) object;
			return realtimeWeatherDTO.getStatus() == null;
		}

		return false;
	}
}
