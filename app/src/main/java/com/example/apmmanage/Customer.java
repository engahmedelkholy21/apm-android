package com.example.apmmanage;

public class Customer {
    private String id;
    private String name;
    private String phone;
    private String address;
    private String notes;
    private double maxLimit;
    private double previousBalance;

    public Customer() {}

    public Customer(String id, String name, String phone, String address, String notes, double maxLimit) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.notes = notes;
        this.maxLimit = maxLimit;
        this.previousBalance = 0;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getMaxLimit() { return maxLimit; }
    public void setMaxLimit(double maxLimit) { this.maxLimit = maxLimit; }

    public double getPreviousBalance() { return previousBalance; }
    public void setPreviousBalance(double previousBalance) { this.previousBalance = previousBalance; }
}