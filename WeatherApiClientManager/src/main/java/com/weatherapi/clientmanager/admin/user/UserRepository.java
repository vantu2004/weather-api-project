package com.weatherapi.clientmanager.admin.user;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.skyapi.weatherforecast.common.User;

public interface UserRepository extends CrudRepository<User, Integer>, PagingAndSortingRepository<User, Integer> {
	
	@Query("SELECT u FROM User u WHERE u.trashed = false AND (u.name LIKE %?1% OR u.email LIKE %?1% OR CONCAT(u.type, '') LIKE %?1%)")
	public Page<User> findAll(String keyword, Pageable pageable);
	
	@Query("SELECT u FROM User u WHERE u.trashed = false")
	public Page<User> findAll(Pageable pageable);
	
	@Query("UPDATE User u SET u.enabled = ?2 WHERE u.id = ?1")
	@Modifying
	public void updateEnabledStatus(Integer id, boolean enabled);
	
	public User findByEmail(String email);

	@Query("SELECT u FROM User u WHERE u.trashed = false AND u.enabled = true AND u.email = ?1")
	public User findByEmailEnabledAndNotTrashed(String email);
	
	@Modifying
	@Query("UPDATE User u SET u.trashed = true, u.email = CONCAT(u.id, '-', u.email, '-trashed') WHERE u.id = ?1")
	public void trashById(Integer id);
	
	@Query("SELECT u FROM User u WHERE u.enabled = true AND u.trashed = false AND (u.email LIKE %?1% OR u.name LIKE %?1%)")
	public List<User> search(String keyword);
}
