package com.skyapi.weatherforecast.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

public class CommonUtility {
	private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtility.class);

	public static String getIpAddress(HttpServletRequest request) {
		/*
		 * Lấy IP từ header X-FORWARDED-FOR, header này thường được proxy hoặc load
		 * balancer (như Nginx) thêm vào để chỉ ra IP gốc của client.
		 */
		String ip = request.getHeader("X-FORWARED-FOR");
		if (StringUtils.isBlank(ip)) {
			/*
			 * Trường hợp IP ko có trog header thì Lấy IP người gửi request trực tiếp đến
			 * server. Nếu ko có proxy/nginx thì đây chính là IP thật nhưng nếu có proxy,
			 * thì IP này là IP của proxy, không phải client thật.
			 */
			ip = request.getRemoteAddr();
		}

		LOGGER.info(ip);

		return ip;
	}
}
