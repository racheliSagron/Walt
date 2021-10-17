package com.walt;

import com.walt.model.*;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.awlt.message.ErrorMessage;
import com.walt.dao.CityRepository;
import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverDistanceRepository;
import com.walt.dao.DriverRepository;
import com.walt.dao.RestaurantRepository;


@Service
public class WaltServiceImpl implements WaltService {

	@Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;
    
    @Resource
    DriverDistanceRepository driverDistanceRepository;
    
	
    //check if driver is available in deliveryTime - > There are no delivery record to this driver in delivery time
	private boolean checkIfDriverIsAvliable(Driver driver , Date deliveryTime) {
		List<Delivery> deliveryPerDriver = deliveryRepository.findAllDeliveryByDriver(driver);
		for (Delivery delivery: deliveryPerDriver) {
				//Each drive takes a full hour – it will start and end in a full hour (no need to calculate minutes)
				long difference = deliveryTime.getTime() - delivery.getDeliveryTime().getTime();
				long difference_In_Hours = (difference/ (1000 * 60 * 60))% 24;
				if(difference_In_Hours < 1) {
					return false;
				}
		}
		return true;
	}
	
	
	//get the least busy driver according to the driver history
	private Driver getDriverWithMinDistance(List<Driver> drivers, Date deliveryTime ) {
		Driver choosenDriver = drivers.get(0);
		DriverDistance choosenDriverDistance = driverDistanceRepository.findByDriver(choosenDriver);
		if(choosenDriverDistance == null) {
			choosenDriverDistance = new DriverDistanceIml(choosenDriver, 0L);
			driverDistanceRepository.save((DriverDistanceIml)choosenDriverDistance);
		}
		
		//iterate all drivers and find their records in driver distance repository
		for(Driver driver: drivers) {
			DriverDistance driverDistanceIml = driverDistanceRepository.findByDriver(driver);
			if(driverDistanceIml == null) {
				driverDistanceRepository.save(new DriverDistanceIml(driver, 0L));
				driverDistanceIml = driverDistanceRepository.findByDriver(driver);
			}
			if((driverDistanceIml.getTotalDistance() < choosenDriverDistance.getTotalDistance()) && (checkIfDriverIsAvliable(driver, deliveryTime))) {
				choosenDriver = driver;
			}
		}
		return choosenDriver;	
	}

	
	//driver should be picked if he/she lives in the same city of the restaurant & customer
	private boolean checkSameCity(Customer customer, Restaurant restaurant) throws ErrorMessage  {
		int sameCity = (customer.getCity().equals(restaurant.getCity())) ? 1 : 0;
		switch(sameCity) {
		case 1:
			return true;
		case 0:
			throw new ErrorMessage();
		}
		return false;		
	}
	
	
	//he/she has no other delivery at the same time
	private boolean checkAvailability(List <Driver> availableDriversInCity) throws ErrorMessage {
		int listEmpty = availableDriversInCity.isEmpty() ? 1 : 0;
		switch(listEmpty) {
		case 0:
			return true;
		case 1:
			throw new ErrorMessage();
		}
		return false;	
	}
	
	
    //using stream
    private LinkedHashMap<Driver, Long> sortMap (List <DriverDistanceIml> driversReport){
    	//convert from list to map
    	Map<Driver, Long> map = driversReport.stream().collect
    		        (Collectors.toMap(DriverDistanceIml::getDriver, DriverDistanceIml::getTotalDistance));
    	
    	//sorted map to linked Hash Map
    	LinkedHashMap<Driver, Long> sortedMap = new LinkedHashMap<>();
    	map.entrySet()
    	    .stream()
    	    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
    	    .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));
    	return sortedMap;	
    }
    
    
    
    //provide a detailed report to display the drivers name and the total distance
    //of delivery order by total distance in descending order
    private void writeReport(Boolean withCity, List <DriverDistanceIml> driversReport) {
    	//find place to write the report
    	String path = System.getProperty("user.dir");
    	String fileName = "\\Driver Rank Report.txt";
    	if(withCity) {fileName = "\\Driver Rank Report By City.txt";}
    	
    	LinkedHashMap<Driver, Long> sortedMap = sortMap(driversReport);
    	
    	//write report to file
    	File file = new File(path + fileName);
    	BufferedWriter bf = null;
        try {
            bf = new BufferedWriter(new FileWriter(file));
            for (Map.Entry<Driver, Long> entry :
            	sortedMap.entrySet()) {
                bf.write(entry.getKey().getName() + " : "
                         + entry.getValue());
                bf.newLine();}
            bf.flush();}
        catch (IOException e) {
            e.printStackTrace();}
        
        finally {try {
                bf.close();}
            catch (Exception e) {}
        }
    } 	
    
    
    
    
    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime){
    	Driver choosenDriver;
    	//get all drivers from customer city
    	List <Driver> availableDriversInCity = driverRepository.findAllDriversByCity(customer.getCity());
    	
    	//driver should be picked if he/she lives in the same city of the restaurant & customer
    	//, he/she has no other delivery at the same time
    	boolean checkSameCityOrAvailability;
    	try{
    		checkSameCityOrAvailability = checkAvailability(availableDriversInCity);
    	}catch(ErrorMessage em) {
    		System.out.println("There are no Available driver");
    		return null;
    	}
    	try{
    		checkSameCityOrAvailability = checkSameCity(customer,restaurant);
    	}catch(ErrorMessage em) {
    		System.out.println("There are no Available driver");
    		return null;
    	}
    	
    	
    	//If more than one driver is available assign it to the least busy driver according to the driver history
    	choosenDriver = getDriverWithMinDistance(availableDriversInCity, deliveryTime);
    	
    	
    	//When assigning a delivery, save the distance from the restaurant to the customer,
    	//for this purpose the distance will be random number between 0-20 Km
    	long distance = (long)(Math.random() * 20);
    	
    	//crate new delivery and save it in delivery repository
    	Delivery delivery = new Delivery(choosenDriver, restaurant, customer, deliveryTime);
    	deliveryRepository.save(delivery);
    	
    	//update driver distance for the next functions in driver distance repository
    	DriverDistanceIml driverDistance = driverDistanceRepository.findByDriver(choosenDriver);
    	driverDistance.setTotalDistance(driverDistance.getTotalDistance() + distance);
		driverDistanceRepository.save(driverDistance);
    	
        return delivery;
    }
    
    

    
    @Override
    public List<DriverDistance> getDriverRankReport() {
    	List <DriverDistanceIml> driversReport = driverDistanceRepository.findByOrderByTotalDistanceDesc(); 
    	//because this is an override method I required to cast all driverDistanceIml
    	//to their interface DriverDistance
    	List<DriverDistance> driversDistanceReport = new ArrayList<>();
    	for(DriverDistanceIml driverDistanceIml : driversReport) {
    		driversDistanceReport.add(driverDistanceIml);
    	}
    	//write report to root directory of the project
    	writeReport(false, driversReport);
    	
    	return driversDistanceReport;
    }
    

    
    
    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
    	List <DriverDistanceIml> driversReport = driverDistanceRepository.findAll();
    	//because this is an override method I required to cast all driverDistanceIml
    	//to their interface DriverDistance and get only drivers from the input city
    	List<DriverDistance> driversDistanceReport = new ArrayList<>();
    	for(DriverDistanceIml driverDistanceIml : driversReport) {
    		if((driverDistanceIml.getDriver().getCity()).equals(city)) {
    			driversDistanceReport.add(driverDistanceIml);
    		}
    	}
    	//write report to root directory of the project
    	writeReport(true, driversReport);
    	
    	return driversDistanceReport;
    }
}
