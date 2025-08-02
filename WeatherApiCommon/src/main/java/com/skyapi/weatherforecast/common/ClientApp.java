package com.skyapi.weatherforecast.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "client_apps")
public class ClientApp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, length = 50)
	private String name;
	
	@Column(nullable = false, length = 100, unique = true)
	private String clientId;
	
	@Column(nullable = false, length = 100, unique = true)
	private String clientSecret;
	
	@Transient
	private String rawClientSecret;
	
	private boolean enabled;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)	
	private AppRole role;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToOne(orphanRemoval = true)
	@JoinTable(
			name = "apps_locations",
			joinColumns = {@JoinColumn(name = "app_id", referencedColumnName = "id")},
			inverseJoinColumns = {@JoinColumn(name = "location_code", referencedColumnName = "code")}
	)	
	private Location location;
	
	private boolean trashed;
	
	public ClientApp() { }
	
	public static ClientApp newSystemApp() {
		ClientApp newApp = new ClientApp();
		newApp.setRole(AppRole.SYSTEM);
		
		return newApp;
	}

	public static ClientApp newReaderApp() {
		ClientApp newApp = new ClientApp();
		newApp.setRole(AppRole.READER);
		
		return newApp;
	}

	public static ClientApp newUpdaterApp() {
		ClientApp newApp = new ClientApp();
		newApp.setRole(AppRole.UPDATER);
		
		return newApp;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public AppRole getRole() {
		return role;
	}

	public void setRole(AppRole role) {
		this.role = role;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Transient
	public String getAssignedUser() {
		if (user == null) return null;
		return this.user.getName() + " - " + this.user.getEmail();
	}
	
	@Transient
	public String getAssignedLocation() {
		if (location == null) return null;
		return location.getCode() + " - " + location.getCityName() + ", " + location.getRegionName() + ", " + location.getCountryName();
	}
	
	@Transient
	public boolean isEditing() {
		return id != null && id > 0;		
	}
	
	public boolean isTrashed() {
		return trashed;
	}

	public void setTrashed(boolean trashed) {
		this.trashed = trashed;
	}
	
	public String getRawClientSecret() {
		return this.rawClientSecret;
	}

	public void setRawClientSecret(String rawClientSecret) {
		this.rawClientSecret = rawClientSecret;
	}
	
	
}
