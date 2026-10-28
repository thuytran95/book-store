package com.example.quan_ly_cua_hang_sach_cu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quan_ly_cua_hang_sach_cu.models.Book;
import com.example.quan_ly_cua_hang_sach_cu.models.Category;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "bookstore.db";
    private static final int DATABASE_VERSION = 2; // Incremented version for image_uri column

    // Table Category
    private static final String TABLE_CATEGORY = "category";
    private static final String CAT_ID = "id";
    private static final String CAT_NAME = "name";

    // Table Book
    private static final String TABLE_BOOK = "book";
    private static final String BOOK_ID = "id";
    private static final String BOOK_CODE = "book_code";
    private static final String BOOK_TITLE = "title";
    private static final String BOOK_AUTHOR = "author";
    private static final String BOOK_CATEGORY = "category";
    private static final String BOOK_CONDITION = "condition";
    private static final String BOOK_CONDITION_PERCENT = "condition_percent";
    private static final String BOOK_PRICE = "price";
    private static final String BOOK_ORIGINAL_PRICE = "original_price";
    private static final String BOOK_IMAGE_URI = "image_uri";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORY + " (" +
                CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CAT_NAME + " TEXT)";

        String createBookTable = "CREATE TABLE " + TABLE_BOOK + " (" +
                BOOK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BOOK_CODE + " TEXT, " +
                BOOK_TITLE + " TEXT, " +
                BOOK_AUTHOR + " TEXT, " +
                BOOK_CATEGORY + " TEXT, " +
                BOOK_CONDITION + " TEXT, " +
                BOOK_CONDITION_PERCENT + " INTEGER, " +
                BOOK_PRICE + " TEXT, " +
                BOOK_ORIGINAL_PRICE + " TEXT, " +
                BOOK_IMAGE_URI + " TEXT)";

        db.execSQL(createCategoryTable);
        db.execSQL(createBookTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOK);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        onCreate(db);
    }

    // --- CATEGORY CRUD ---
    public void addCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CAT_NAME, category.getName());
        db.insert(TABLE_CATEGORY, null, values);
        db.close();
    }

    public int updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CAT_NAME, category.getName());
        return db.update(TABLE_CATEGORY, values, CAT_ID + "=?", new String[]{String.valueOf(category.getId())});
    }

    public int deleteCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORY, CAT_ID + "=?", new String[]{String.valueOf(category.getId())});
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CATEGORY;
        SQLiteDatabase db = this.getReadableDatabase();
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

    // --- BOOK CRUD ---
    public void addBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_CODE, book.getBookCode());
        values.put(BOOK_TITLE, book.getTitle());
        values.put(BOOK_AUTHOR, book.getAuthor());
        values.put(BOOK_CATEGORY, book.getCategory());
        values.put(BOOK_CONDITION, book.getCondition());
        values.put(BOOK_CONDITION_PERCENT, book.getConditionPercent());
        values.put(BOOK_PRICE, book.getPrice());
        values.put(BOOK_ORIGINAL_PRICE, book.getOriginalPrice());
        values.put(BOOK_IMAGE_URI, book.getImageUri());
        db.insert(TABLE_BOOK, null, values);
        db.close();
    }

    public int updateBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BOOK_CODE, book.getBookCode());
        values.put(BOOK_TITLE, book.getTitle());
        values.put(BOOK_AUTHOR, book.getAuthor());
        values.put(BOOK_CATEGORY, book.getCategory());
        values.put(BOOK_CONDITION, book.getCondition());
        values.put(BOOK_CONDITION_PERCENT, book.getConditionPercent());
        values.put(BOOK_PRICE, book.getPrice());
        values.put(BOOK_ORIGINAL_PRICE, book.getOriginalPrice());
        values.put(BOOK_IMAGE_URI, book.getImageUri());
        return db.update(TABLE_BOOK, values, BOOK_ID + "=?", new String[]{String.valueOf(book.getId())});
    }

    public int deleteBook(Book book) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_BOOK, BOOK_ID + "=?", new String[]{String.valueOf(book.getId())});
    }

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_BOOK;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Book book = new Book();
                book.setId(cursor.getInt(0));
                book.setBookCode(cursor.getString(1));
                book.setTitle(cursor.getString(2));
                book.setAuthor(cursor.getString(3));
                book.setCategory(cursor.getString(4));
                book.setCondition(cursor.getString(5));
                book.setConditionPercent(cursor.getInt(6));
                book.setPrice(cursor.getString(7));
                book.setOriginalPrice(cursor.getString(8));
                book.setImageUri(cursor.getString(9));
                list.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
