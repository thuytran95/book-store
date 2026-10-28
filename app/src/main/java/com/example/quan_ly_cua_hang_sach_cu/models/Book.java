package com.example.quan_ly_cua_hang_sach_cu.models;

public class Book {
    private int id;
    private String bookCode;
    private String title;
    private String author;
    private String category;
    private String condition;
    private int conditionPercent;
    private String price;
    private String originalPrice;
    private String imageUri;

    public Book() {}

    public Book(String bookCode, String title, String author, String category, String condition, int conditionPercent, String price, String originalPrice, String imageUri) {
        this.bookCode = bookCode;
        this.title = title;
        this.author = author;
        this.category = category;
        this.condition = condition;
        this.conditionPercent = conditionPercent;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageUri = imageUri;
    }

    public Book(int id, String bookCode, String title, String author, String category, String condition, int conditionPercent, String price, String originalPrice, String imageUri) {
        this.id = id;
        this.bookCode = bookCode;
        this.title = title;
        this.author = author;
        this.category = category;
        this.condition = condition;
        this.conditionPercent = conditionPercent;
        this.price = price;
        this.originalPrice = originalPrice;
        this.imageUri = imageUri;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getBookCode() { return bookCode; }
    public void setBookCode(String bookCode) { this.bookCode = bookCode; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public int getConditionPercent() { return conditionPercent; }
    public void setConditionPercent(int conditionPercent) { this.conditionPercent = conditionPercent; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(String originalPrice) { this.originalPrice = originalPrice; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
}
