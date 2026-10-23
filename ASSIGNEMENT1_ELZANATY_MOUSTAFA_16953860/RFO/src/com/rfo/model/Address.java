package com.rfo.model;

/**
 * A postal address: street, city, country and postal code. Used by locations and drivers.
 */
public class Address {

    private String street;
    private String city;
    private CountryCode country;
    private String postalCode;

    public Address(String street, String city, CountryCode country, String postalCode) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
    }

    //returns full address = street, city potalcode, country
    public String getFullAddress() {
        return getStreet()
                + ", "
                + getCity() + " "
                + getPostalCode()
                + ", "
                + getCountry();
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public CountryCode getCountry() {
        return country;
    }

    public void setCountry(CountryCode country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

}
