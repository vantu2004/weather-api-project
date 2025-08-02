package com.weatherapi.clientmanager.admin.clientapp;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.skyapi.weatherforecast.common.ClientApp;

public interface AdminClientAppRepository extends JpaRepository<ClientApp, Integer> {
	
	@Query("SELECT c FROM ClientApp c WHERE c.trashed = false AND (c.name LIKE %?1% OR CONCAT(c.role, '') LIKE %?1% OR c.user.name LIKE %?1%)")
	public Page<ClientApp> findAll(String keyword, Pageable pageable);
	
	@Query("SELECT c FROM ClientApp c WHERE c.trashed = false")
	public Page<ClientApp> findAll(Pageable pageable);	
	
	@Query("UPDATE ClientApp c SET c.enabled = ?2 WHERE c.id = ?1")
	@Modifying
	public void updateEnabledStatus(Integer id, boolean enabled);
	
	@Modifying
	@Query("UPDATE ClientApp c SET c.trashed = true, c.name = CONCAT('trashed-', c.name) WHERE c.id = ?1")
	public void trashById(Integer id);	
	
	@Query("SELECT c FROM ClientApp c WHERE c.id = ?1 AND c.trashed = false")
	public Optional<ClientApp> findByIdNotTrashed(Integer id);
	
}
