package com.example.apmmanage;

import java.util.Date;

public class Product {
    private int id; // pro_ID
    private String name; // pro_name
    private String category; // pro_category
    private double costPrice; // pro_cost_price
    private double sellingPrice; // pro_bee3
    private double profit; // pro_profit (computed in DB)
    private double count; // pro_count
    private int limit; // pro_limit
    private Date expirationDate; // pro_expiration_date
    private String effectiveness; // pro_mada_fa3ala
    private String disease; // pro_marad
    private String supplier; // pro_mwared
    private String supplierPhone; // pro_mwared_phone
    private String addedBy; // pro_added_by
    private double pharmacyRatio; // nesba_saydaly
    private String internationalCode; // pro_int_code
    private String stock; // pro_stock
    private double piecesInPacket; // pro_pieces_in_packet
    private double countInPieces; // pro_count_in_pieces
    private double wholesalePrice; // pro_gomla_gomla
    private double alternativeSellingPrice; // pro_bee3_2
    private String inventoryStatus; // gard_status
    private Date lastInventoryDate; // last_gard_date

    // Default constructor
    public Product() {}

    // Getters and Setters
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(double costPrice) {
        this.costPrice = costPrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public double setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
        return sellingPrice;
    }

    public double getProfit() {
        return sellingPrice - costPrice; // Calculate profit dynamically
    }

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getEffectiveness() {
        return effectiveness;
    }

    public void setEffectiveness(String effectiveness) {
        this.effectiveness = effectiveness;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(String supplierPhone) {
        this.supplierPhone = supplierPhone;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public double getPharmacyRatio() {
        return pharmacyRatio;
    }

    public void setPharmacyRatio(double pharmacyRatio) {
        this.pharmacyRatio = pharmacyRatio;
    }

    public String getInternationalCode() {
        return internationalCode;
    }

    public void setInternationalCode(String internationalCode) {
        this.internationalCode = internationalCode;
    }

    public String getStock() {
        return stock;
    }

    public String setStock(String stock) {
        this.stock = stock;
        return stock;
    }

    public double getPiecesInPacket() {
        return piecesInPacket;
    }

    public void setPiecesInPacket(double piecesInPacket) {
        this.piecesInPacket = piecesInPacket;

    }

    public double getCountInPieces() {
        return countInPieces;
    }

    public void setCountInPieces(double countInPieces) {
        this.countInPieces = countInPieces;
    }

    public double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public double getAlternativeSellingPrice() {
        return alternativeSellingPrice;
    }

    public void setAlternativeSellingPrice(double alternativeSellingPrice) {
        this.alternativeSellingPrice = alternativeSellingPrice;
    }

    public String getInventoryStatus() {
        return inventoryStatus;
    }

    public void setInventoryStatus(String inventoryStatus) {
        this.inventoryStatus = inventoryStatus;
    }

    public Date getLastInventoryDate() {
        return lastInventoryDate;
    }

    public void setLastInventoryDate(Date lastInventoryDate) {
        this.lastInventoryDate = lastInventoryDate;
    }
}
