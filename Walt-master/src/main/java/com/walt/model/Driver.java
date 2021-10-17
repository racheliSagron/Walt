package com.walt.model;

import java.io.Serializable;

import javax.persistence.*;
import com.walt.model.DriverDistance;

@Embeddable
@Entity
public class Driver extends NamedEntity implements Serializable{

    @ManyToOne
    City city;

    public Driver(){}

    public Driver(String name, City city){
        super(name);
        this.city = city;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }
    
    
}
