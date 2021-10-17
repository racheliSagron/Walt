package com.walt.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.DriverDistanceIml;

@Repository
public interface DriverDistanceRepository extends CrudRepository<DriverDistanceIml, Long> {
	
	List <DriverDistanceIml> findAll();
	List <DriverDistanceIml> findByOrderByTotalDistanceDesc();
	DriverDistanceIml findByDriver(Driver driver);

}
