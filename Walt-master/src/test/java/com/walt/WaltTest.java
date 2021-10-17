package com.walt;

import com.walt.dao.*;
import com.walt.model.City;
import com.walt.model.Customer;
import com.walt.model.Delivery;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;
import com.walt.model.Restaurant;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }

    @Autowired
    WaltService waltService;

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
    
    @BeforeEach()
    public void prepareData(){

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);

        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);
        
        checkCreateDelivery();
        
        checkGetRank();
        
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", tlv, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");

        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff, bach));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);

        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }
    
    
    private void checkCreateDelivery() {
    	Customer customer = customerRepository.findByName("Mozart");
		Restaurant restaurant = restaurantRepository.findByName("meat");
		Restaurant restaurant2 = restaurantRepository.findByName("cafe");
		Date date = new GregorianCalendar(2014, Calendar.FEBRUARY, 11).getTime();
		waltService.createOrderAndAssignDriver(customer, restaurant, date);
		date = new Date();
		waltService.createOrderAndAssignDriver(customer, restaurant, date);
		date = new Date(2020, 4, 20, 17, 6, 6);
		waltService.createOrderAndAssignDriver(customer, restaurant, date);
		//check if assign difference drivers because delivery date is the same
		waltService.createOrderAndAssignDriver(customer, restaurant, date);
		//check if delivery not create because difference in city
		waltService.createOrderAndAssignDriver(customer, restaurant2, date);
		/*
		List <Delivery> delivery = deliveryRepository.findAll();
    	for (Delivery d: delivery) {
    		System.out.println("Name driver: " + d.getDriver().getName() + " Customer city: "+ d.getCustomer().getCity().getName() + " restaurant city: " + d.getRestaurant().getCity().getName());
    	}
    	*/	
		assertEquals(((List<Delivery>) deliveryRepository.findAll()).size(),4);
    }
    
    private void checkGetRank() {
    	City jerusalem = cityRepository.findByName("Jerusalem");
    	List <DriverDistance> driverDistance = waltService.getDriverRankReportByCity(jerusalem);
    	assertEquals(((List <DriverDistance>) driverDistance).size(),3);
    	/*
    	for (DriverDistance driver: driverDistance) {
    		System.out.println("Name driver: " + driver.getDriver().getName() + " City driver: "+ driver.getDriver().getCity().getName() + " is total distance: " + driver.getTotalDistance());
    	}
    	*/
    	City haifa = cityRepository.findByName("Haifa");
    	driverDistance = waltService.getDriverRankReportByCity(haifa);
    	assertEquals(((List <DriverDistance>) driverDistance).size(),0);
    }
    
   
    @Test
    public void testBasics(){
    	
        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }
}
