package com.example.quan_ly_cua_hang_sach_cu.models;

public class BookModel {
    private String id;
    private String title;
    private String author;
    private String category;
    private String condition;
    private int conditionPercent;
    private String price;
    private String originalPrice;

    public BookModel(String id, String title, String author, String category, String condition, int conditionPercent, String price, String originalPrice) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.condition = condition;
        this.conditionPercent = conditionPercent;
        this.price = price;
        this.originalPrice = originalPrice;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public String getCondition() { return condition; }
    public int getConditionPercent() { return conditionPercent; }
    public String getPrice() { return price; }
    public String getOriginalPrice() { return originalPrice; }
}
