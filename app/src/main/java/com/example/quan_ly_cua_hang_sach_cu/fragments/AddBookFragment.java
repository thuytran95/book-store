package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quan_ly_cua_hang_sach_cu.R;

public class AddBookFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_book, container, false);

        EditText edtTitle = view.findViewById(R.id.edtAddTitle);
        EditText edtAuthor = view.findViewById(R.id.edtAddAuthor);
        EditText edtIsbn = view.findViewById(R.id.edtAddIsbn);
        EditText edtPrice = view.findViewById(R.id.edtAddPrice);
        EditText edtOriginalPrice = view.findViewById(R.id.edtAddOriginalPrice);
        Button btnSave = view.findViewById(R.id.btnSaveBook);

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String author = edtAuthor.getText().toString().trim();
            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên sách và tác giả!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Đã lưu sách thành công vào kho!", Toast.LENGTH_SHORT).show();
                edtTitle.setText("");
                edtAuthor.setText("");
                edtIsbn.setText("");
                edtPrice.setText("");
                edtOriginalPrice.setText("");
            }
        });

        return view;
    }
}
