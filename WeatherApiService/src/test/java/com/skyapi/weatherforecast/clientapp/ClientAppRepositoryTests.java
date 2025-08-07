package com.skyapi.weatherforecast.clientapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import com.skyapi.weatherforecast.common.ClientApp;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Rollback(false)
public class ClientAppRepositoryTests {
	@Autowired
	private ClientAppRepository clientAppRepository;

	@Test
	public void testGetClientAppByClientIdNotFound() {
		String clientId = "xxxx";
		Optional<ClientApp> clientApp = this.clientAppRepository.findByClientId(clientId);

		assertThat(clientApp).isNotPresent();
	}

	@Test
	public void testGetClientAppByClientIdSuccess() {
		String clientId = "uwmAuaAvqy5y5WkLeyEP";
		Optional<ClientApp> clientApp = this.clientAppRepository.findByClientId(clientId);

		assertThat(clientApp).isPresent();

		System.out.println(clientApp.get());
	}
}
