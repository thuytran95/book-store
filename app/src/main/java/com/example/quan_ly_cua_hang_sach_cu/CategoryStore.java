package com.example.quan_ly_cua_hang_sach_cu;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quan_ly_cua_hang_sach_cu.models.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryStore {

    static final String TABLE = "category";
    static final String COLUMN_ID = "id";
    static final String COLUMN_NAME = "name";

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT)";

    private final SQLiteOpenHelper helper;

    public CategoryStore(SQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public void addCategory(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, category.getName());
        db.insert(TABLE, null, values);
        db.close();
    }

    public int updateCategory(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String oldName = null;
            Cursor cursor = db.rawQuery(
                    "SELECT " + COLUMN_NAME + " FROM " + TABLE + " WHERE " + COLUMN_ID + "=?",
                    new String[]{String.valueOf(category.getId())});
            if (cursor.moveToFirst()) {
                oldName = cursor.getString(0);
            }
            cursor.close();

            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, category.getName());
            int rows = db.update(TABLE, values, COLUMN_ID + "=?", new String[]{String.valueOf(category.getId())});

            if (oldName != null && category.getName() != null && !oldName.equals(category.getName())) {
                ContentValues bookValues = new ContentValues();
                bookValues.put(BookStore.COLUMN_CATEGORY, category.getName());
                db.update(BookStore.TABLE, bookValues, BookStore.COLUMN_CATEGORY + "=?", new String[]{oldName});
            }
            db.setTransactionSuccessful();
            return rows;
        } finally {
            db.endTransaction();
        }
    }

    public int deleteCategory(Category category) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABLE, COLUMN_ID + "=?", new String[]{String.valueOf(category.getId())});
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Category cat = new Category();
                cat.setId(cursor.getInt(0));
                cat.setName(cursor.getString(1));
                list.add(cat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List<Category> getAllOrSeed() {
        List<Category> list = getAllCategories();
        if (list.isEmpty()) {
            addCategory(new Category("Văn học cổ điển & Hiện đại"));
            addCategory(new Category("Khoa học & Vũ trụ"));
            addCategory(new Category("Lịch sử & Triết học"));
            addCategory(new Category("Tiểu sử & Danh nhân"));
            list = getAllCategories();
        }
        return list;
    }
}
