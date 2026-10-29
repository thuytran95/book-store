package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.DatabaseHelper;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.adapters.CategoryAdapter;
import com.example.quan_ly_cua_hang_sach_cu.models.Category;
import java.util.List;

public class CategoryFragment extends Fragment {

    private List<Category> categoryList;
    private CategoryAdapter adapter;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        dbHelper = new DatabaseHelper(getContext());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoryList = dbHelper.getAllCategories();
        if (categoryList.isEmpty()) {
            dbHelper.addCategory(new Category("Văn học cổ điển & Hiện đại"));
            dbHelper.addCategory(new Category("Khoa học & Vũ trụ"));
            dbHelper.addCategory(new Category("Lịch sử & Triết học"));
            dbHelper.addCategory(new Category("Tiểu sử & Danh nhân"));
            categoryList = dbHelper.getAllCategories();
        }

        adapter = new CategoryAdapter(categoryList, new CategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onDeleteClick(int position) {
                if (position == RecyclerView.NO_POSITION || position >= categoryList.size()) return;
                Category cat = categoryList.get(position);
                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa thể loại")
                        .setMessage("Bạn có chắc chắn muốn xóa thể loại \"" + cat.getName() + "\" không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            if (position >= categoryList.size()) return;
                            dbHelper.deleteCategory(cat);
                            categoryList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Đã xóa thể loại thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }

            @Override
            public void onEditClick(int position) {
                if (position == RecyclerView.NO_POSITION || position >= categoryList.size()) return;
                showCategoryDialog(categoryList.get(position), position);
            }
        });
        recyclerView.setAdapter(adapter);

        Button btnAddCategory = view.findViewById(R.id.btnAddCategory);
        btnAddCategory.setOnClickListener(v -> showCategoryDialog(null, -1));

        return view;
    }

    private void showCategoryDialog(@Nullable Category existingCategory, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        builder.setView(dialogView);

        TextView txtDialogTitle = dialogView.findViewById(R.id.txtDialogTitle);
        EditText edtName = dialogView.findViewById(R.id.edtDialogCatName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelCat);
        Button btnSave = dialogView.findViewById(R.id.btnSaveCat);

        if (existingCategory != null) {
            txtDialogTitle.setText("Chỉnh Sửa Thể Loại");
            edtName.setText(existingCategory.getName());
            btnSave.setText("Cập nhật");
        } else {
            txtDialogTitle.setText("Thêm Thể Loại Mới");
            btnSave.setText("Thêm mới");
        }

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên thể loại", Toast.LENGTH_SHORT).show();
            } else {
                if (existingCategory != null) {
                    existingCategory.setName(name);
                    dbHelper.updateCategory(existingCategory);
                    if (position >= 0 && position < categoryList.size()) {
                        categoryList.set(position, existingCategory);
                        adapter.notifyItemChanged(position);
                    }
                    Toast.makeText(getContext(), "Đã cập nhật thể loại thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Category newCat = new Category(name);
                    dbHelper.addCategory(newCat);
                    categoryList.clear();
                    categoryList.addAll(dbHelper.getAllCategories());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Đã thêm thể loại mới thành công!", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
