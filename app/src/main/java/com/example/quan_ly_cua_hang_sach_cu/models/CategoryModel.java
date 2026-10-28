package com.example.quan_ly_cua_hang_sach_cu.models;

public class CategoryModel {
    private String id;
    private String name;

    public CategoryModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }
}
