package com.skyapi.weatherforecast;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.ip2location.IP2Location;
import com.ip2location.IPResult;

public class Ip2LocationTest {
	private String dbPath = "ip2_location_db/IP2LOCATION-LITE-DB3.BIN";

	@Test
	public void testInvalidIp() throws IOException {
		IP2Location ip2Location = new IP2Location();
		ip2Location.Open(dbPath);

		String ipAddress = "abc";
		IPResult ipResult = ip2Location.IPQuery(ipAddress);

		assertThat(ipResult.getStatus()).isEqualTo("INVALID_IP_ADDRESS");

		System.out.println(ipResult);
	}

	@Test
	public void testValidIp() throws IOException {
		IP2Location ip2Location = new IP2Location();
		ip2Location.Open(dbPath);

		// lấy IPv4 trong máy rồi tìm trên ipLookup là ra đc ip này
		String ipAddress = "123.21.220.121";
		IPResult ipResult = ip2Location.IPQuery(ipAddress);

		assertThat(ipResult.getStatus()).isEqualTo("OK");

		System.out.println(ipResult);
	}
}
