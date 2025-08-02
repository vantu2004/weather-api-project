package com.weatherapi.clientmanager.user.clientapp;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.skyapi.weatherforecast.common.ClientApp;

public interface UserClientAppRepository extends JpaRepository<ClientApp, Integer> {
	
	@Query("SELECT c FROM ClientApp c WHERE c.trashed = false AND (c.user.id = ?1 AND c.name LIKE %?2%)")
	public Page<ClientApp> searchByUser(Integer userId, String keyword, Pageable pageable);
	
	@Query("SELECT c FROM ClientApp c WHERE c.trashed = false AND c.user.id = ?1")
	public Page<ClientApp> findAllByUser(Integer userId, Pageable pageable);	
	
	@Query("SELECT c FROM ClientApp c WHERE c.user.id = ?1 AND c.id = ?2 AND c.trashed = false")
	public ClientApp findByUserAndId(Integer userId, Integer appId);
	
	@Query("UPDATE ClientApp c SET c.enabled = ?3 WHERE c.id = ?2 AND c.user.id = ?1")
	@Modifying
	public void updateEnabledStatus(Integer userId, Integer appId, boolean enabled);
	
	@Modifying
	@Query("UPDATE ClientApp c SET c.trashed = true, c.name = CONCAT('trashed-', c.name) WHERE c.id = ?2 AND c.user.id = ?1")
	public void trashByUserAndId(Integer userId, Integer appId);
	
	@Query("SELECT c FROM ClientApp c WHERE c.user.id = ?1 AND c.id = ?2 AND c.trashed = false")
	public Optional<ClientApp> findByUserAndIdNotTrashed(Integer userId, Integer appId);	
}
