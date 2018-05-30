package com.pancake.entity.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InfectiousDisease {
    private int id;
    private String name;
    private InfectiousDiseaseClassification infectiousDiseaseClassification;

    public InfectiousDisease() {
    }

    public InfectiousDisease(int id, String name, InfectiousDiseaseClassification infectiousDiseaseClassification) {
        this.id = id;
        this.name = name;
        this.infectiousDiseaseClassification = infectiousDiseaseClassification;
    }

    @Override
    public String toString() {
        String rtn = null;
        try {
            rtn = (new ObjectMapper()).writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InfectiousDiseaseClassification getInfectiousDiseaseClassification() {
        return infectiousDiseaseClassification;
    }

    public void setInfectiousDiseaseClassification(InfectiousDiseaseClassification infectiousDiseaseClassification) {
        this.infectiousDiseaseClassification = infectiousDiseaseClassification;
    }
}
