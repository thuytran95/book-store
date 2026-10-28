package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.DatabaseHelper;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.adapters.BookAdapter;
import com.example.quan_ly_cua_hang_sach_cu.models.BookModel;
import java.util.List;

public class BookInventoryFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private List<BookModel> bookList;
    private BookAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_inventory, container, false);

        dbHelper = new DatabaseHelper(getContext());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadBooks();

        adapter = new BookAdapter(bookList, new BookAdapter.OnBookActionListener() {
            @Override
            public void onEditClick(int position) {
                BookModel book = bookList.get(position);
                Bundle bundle = new Bundle();
                bundle.putInt("book_id", book.getId());
                bundle.putString("book_code", book.getBookCode());
                bundle.putString("title", book.getTitle());
                bundle.putString("author", book.getAuthor());
                bundle.putString("category", book.getCategory());
                bundle.putString("condition", book.getCondition());
                bundle.putString("price", book.getPrice());
                bundle.putString("original_price", book.getOriginalPrice());
                bundle.putString("image_uri", book.getImageUri());

                AddBookFragment editFragment = new AddBookFragment();
                editFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .commit();
            }

            @Override
            public void onDeleteClick(int position) {
                BookModel book = bookList.get(position);
                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa sách")
                        .setMessage("Bạn có chắc chắn muốn xóa sách \"" + book.getTitle() + "\" không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            dbHelper.deleteBook(book);
                            bookList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Đã xóa sách thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void loadBooks() {
        bookList = dbHelper.getAllBooks();
        if (bookList.isEmpty()) {
            dbHelper.addBook(new BookModel("MS-00124", "Nỗi Buồn Chiến Tranh", "Bảo Ninh", "Văn học", "Tốt", 85, "65.000 đ", "120.000 đ", null));
            dbHelper.addBook(new BookModel("MS-00208", "Vũ Trụ Trong Vỏ Hạt Dẻ", "Stephen Hawking", "Khoa học", "Rất mới", 95, "88.000 đ", null, null));
            dbHelper.addBook(new BookModel("MS-00315", "Steve Jobs - Tiểu Sử Chính Thức", "Walter Isaacson", "Tiểu sử", "Khá - gáy sờn", 70, "95.000 đ", "190.000 đ", null));
            dbHelper.addBook(new BookModel("MS-00412", "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Văn học", "Tốt", 80, "45.000 đ", "85.000 đ", null));
            bookList = dbHelper.getAllBooks();
        }
    }
}
