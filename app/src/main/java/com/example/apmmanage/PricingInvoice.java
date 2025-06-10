package com.example.apmmanage;
import  com.example.apmmanage.Customer;
import com.example.apmmanage.Product;
import  com.example.apmmanage.PricingInvoiceItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PricingInvoice {
    private String id;
    private Date date;
    private com.example.apmmanage.Customer customer;
    private List<com.example.apmmanage.PricingInvoiceItem> items;
    private double discount;
    private double discountPercentage;
    private double paid;
    private String notes;
    private String representative;
    private String branch;

    public PricingInvoice() {
        this.items = new ArrayList<>();
        this.date = new Date();
    }

    public void addItem(Product product, int quantity,double bee3,String stock) {
        com.example.apmmanage.PricingInvoiceItem item = new com.example.apmmanage.PricingInvoiceItem(product, quantity,bee3,stock);
        items.add(item);
    }

    public double getTotal() {
        double total = 0;
        for (com.example.apmmanage.PricingInvoiceItem item : items) {
            total += item.getTotal();
        }
        return total;
    }

    public double getTotalAfterDiscount() {
        return getTotal() - discount;
    }

    public double getBalance() {
        return getTotalAfterDiscount() - paid;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public com.example.apmmanage.Customer getCustomer() { return customer; }
    public void setCustomer(com.example.apmmanage.Customer customer) { this.customer = customer; }

    public List<PricingInvoiceItem> getItems() { return items; }
    public void setItems(List<PricingInvoiceItem> items) { this.items = items; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
        this.discount = getTotal() * (discountPercentage / 100);
    }

    public double getPaid() { return paid; }
    public void setPaid(double paid) { this.paid = paid; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRepresentative() { return representative; }
    public void setRepresentative(String representative) { this.representative = representative; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
}