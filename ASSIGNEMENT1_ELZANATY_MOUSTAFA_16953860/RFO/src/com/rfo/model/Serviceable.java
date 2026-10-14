package com.rfo.model;

public interface Serviceable {
    //check if vehicle is due for service.
    boolean isDueForService();
    
    //Marks service done. 
    void markServiced();
}
