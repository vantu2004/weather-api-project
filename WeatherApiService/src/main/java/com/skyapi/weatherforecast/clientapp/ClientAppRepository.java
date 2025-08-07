package com.skyapi.weatherforecast.clientapp;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.skyapi.weatherforecast.common.ClientApp;

@Repository
public interface ClientAppRepository extends JpaRepository<ClientApp, Integer> {
	@Query("SELECT c FROM ClientApp c WHERE c.clientId = ?1 AND c.enabled=true AND c.trashed=false")
	public Optional<ClientApp> findByClientId(String clientId);
}
