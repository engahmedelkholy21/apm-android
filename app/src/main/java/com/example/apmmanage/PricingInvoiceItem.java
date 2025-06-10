package com.example.apmmanage;

import com.example.apmmanage.Product;

public class PricingInvoiceItem {
    private Product product;
    private int quantity;
    private double unitPrice;

    private String stock;
    public PricingInvoiceItem(Product product, int quantity,double bee3,String stock) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getSellingPrice();
        this.stock = product.getStock();
    }

    public double getTotal() {
        return quantity * unitPrice;
    }

    // Getters and Setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

//    public Product get_pro_int_code() { return pro_int_code; }
//    public void set_pro_int_code(pro_int_code product) { this.pro_int_code = pro_int_code; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getstock() { return stock; }
    public void setstock(String stock) { this.stock = stock; }
}