package com.walt.model;

import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class DriverDistanceIml implements DriverDistance  {
	
	@Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
	
	@OneToOne
	private Driver driver;
	private Long totalDistance;
	
	public DriverDistanceIml() {
		
	}
	
	public DriverDistanceIml(Driver driver, Long totalDistance) {
		this.driver = driver;
		this.totalDistance = totalDistance;
	}
	
	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	@Override
	public Driver getDriver() {
		return driver;
	}

	@Override
	public Long getTotalDistance() {
		return totalDistance;
	}

	

	public void setTotalDistance(Long totalDistance) {
		this.totalDistance = totalDistance;
	}

}
