package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.adapters.BookAdapter;
import com.example.quan_ly_cua_hang_sach_cu.models.BookModel;
import java.util.ArrayList;
import java.util.List;

public class BookInventoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_inventory, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<BookModel> bookList = new ArrayList<>();
        bookList.add(new BookModel("MS-00124", "Nỗi Buồn Chiến Tranh", "Bảo Ninh", "Văn học", "Tốt", 85, "65.000 đ", "120.000 đ"));
        bookList.add(new BookModel("MS-00208", "Vũ Trụ Trong Vỏ Hạt Dẻ", "Stephen Hawking", "Khoa học", "Rất mới", 95, "88.000 đ", null));
        bookList.add(new BookModel("MS-00315", "Steve Jobs - Tiểu Sử Chính Thức", "Walter Isaacson", "Tiểu sử", "Khá - gáy sờn", 70, "95.000 đ", "190.000 đ"));
        bookList.add(new BookModel("MS-00412", "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Văn học", "Tốt", 80, "45.000 đ", "85.000 đ"));

        BookAdapter adapter = new BookAdapter(bookList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
