package com.example.quan_ly_cua_hang_sach_cu;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quan_ly_cua_hang_sach_cu.models.Book;

import java.util.ArrayList;
import java.util.List;

public class BookStore {

    static final String TABLE = "book";
    static final String COLUMN_ID = "id";
    static final String COLUMN_CODE = "book_code";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_AUTHOR = "author";
    static final String COLUMN_CATEGORY = "category";
    static final String COLUMN_CONDITION = "condition";
    static final String COLUMN_CONDITION_PERCENT = "condition_percent";
    static final String COLUMN_PRICE = "price";
    static final String COLUMN_ORIGINAL_PRICE = "original_price";
    static final String COLUMN_IMAGE_URI = "image_uri";

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CODE + " TEXT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_AUTHOR + " TEXT, " +
            COLUMN_CATEGORY + " TEXT, " +
            COLUMN_CONDITION + " TEXT, " +
            COLUMN_CONDITION_PERCENT + " INTEGER, " +
            COLUMN_PRICE + " TEXT, " +
            COLUMN_ORIGINAL_PRICE + " TEXT, " +
            COLUMN_IMAGE_URI + " TEXT)";

    private final SQLiteOpenHelper helper;

    public BookStore(SQLiteOpenHelper helper) {
        this.helper = helper;
    }

    public void addBook(Book book) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insert(TABLE, null, bookValues(book));
        db.close();
    }

    public int updateBook(Book book) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.update(TABLE, bookValues(book), COLUMN_ID + "=?", new String[]{String.valueOf(book.getId())});
    }

    public int deleteBook(Book book) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABLE, COLUMN_ID + "=?", new String[]{String.valueOf(book.getId())});
    }

    public List<Book> getAllBooks() {
        List<Book> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE;
        SQLiteDatabase db = helper.getReadableDatabase();
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

    public List<Book> getAllOrSeed() {
        List<Book> list = getAllBooks();
        if (list.isEmpty()) {
            addBook(new Book("MS-00124", "Nỗi Buồn Chiến Tranh", "Bảo Ninh", "Văn học", "Tốt", 85, "65.000 đ", "120.000 đ", null));
            addBook(new Book("MS-00208", "Vũ Trụ Trong Vỏ Hạt Dẻ", "Stephen Hawking", "Khoa học", "Rất mới", 95, "88.000 đ", null, null));
            addBook(new Book("MS-00315", "Steve Jobs - Tiểu Sử Chính Thức", "Walter Isaacson", "Tiểu sử", "Khá", 70, "95.000 đ", "190.000 đ", null));
            addBook(new Book("MS-00412", "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Văn học", "Trung bình", 80, "45.000 đ", "85.000 đ", null));
            list = getAllBooks();
        }
        return list;
    }

    private ContentValues bookValues(Book book) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_CODE, book.getBookCode());
        values.put(COLUMN_TITLE, book.getTitle());
        values.put(COLUMN_AUTHOR, book.getAuthor());
        values.put(COLUMN_CATEGORY, book.getCategory());
        values.put(COLUMN_CONDITION, book.getCondition());
        values.put(COLUMN_CONDITION_PERCENT, book.getConditionPercent());
        values.put(COLUMN_PRICE, book.getPrice());
        values.put(COLUMN_ORIGINAL_PRICE, book.getOriginalPrice());
        values.put(COLUMN_IMAGE_URI, book.getImageUri());
        return values;
    }
}
