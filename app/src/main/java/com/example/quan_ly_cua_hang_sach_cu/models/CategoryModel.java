package com.example.quan_ly_cua_hang_sach_cu.models;

public class CategoryModel {
    private String id;
    private String name;
    private String code;
    private int count;
    private float percentage;

    public CategoryModel(String id, String name, String code, int count, float percentage) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.count = count;
        this.percentage = percentage;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public int getCount() { return count; }
    public float getPercentage() { return percentage; }
}
