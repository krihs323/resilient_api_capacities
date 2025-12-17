package com.example.resilient_api.domain.model;

import java.math.BigInteger;

public class CapacityList {

    String name;
    String cantTechnologies;


    public CapacityList() {
    }

    public CapacityList(String name, String cantTechnologies) {
        this.name = name;
        this.cantTechnologies = cantTechnologies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCantTechnologies() {
        return cantTechnologies;
    }

    public void setCantTechnologies(String cantTechnologies) {
        this.cantTechnologies = cantTechnologies;
    }
}
