package com.walt.model;

import javax.persistence.Entity;

@Entity
public class City extends NamedEntity{

    public City(){}

    public City(String name){
        super(name);
    }
    
    @Override public boolean equals(Object obj) {
    	City city1 = (City)obj;
    	if(this.getName() == city1.getName()) {
    		return true;
    	}
    	return false;
    }
}
