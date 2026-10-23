package com.rfo.model;

/**
 * Something that has to be serviced after a number of kilometres.
 */
public interface Serviceable {
    //check if vehicle is due for service.
    boolean isDueForService();
    
    //Marks service done. 
    void markServiced();
}
