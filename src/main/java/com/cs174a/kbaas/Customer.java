package com.cs174a.kbaas;

public class Customer
{
    private int tax_id;
    private int pin;
    private String name;
    private String address;

    public Customer(int tax_id, int pin, String name, String address)
    {
        this.tax_id = tax_id;
        this.pin = pin;
        this.name = name;
        this.address = address;
    }

    public int getTaxId()
    {
        return this.tax_id;
    }

    public int getPin()
    {
        return this.pin;
    }

    public String getName()
    {
        return this.name;
    }

    public String getAddress()
    {
        return this.address;
    }

    public void setTaxId(int new_id)
    {
        this.tax_id = new_id;
    }

    public void setPin(int new_pin)
    {
        this.pin = new_pin;
    }

    public void setName(String new_name)
    {
        this.name = new_name;
    }

    public void setAddress(String new_address)
    {
        this.address = new_address;
    }
}
