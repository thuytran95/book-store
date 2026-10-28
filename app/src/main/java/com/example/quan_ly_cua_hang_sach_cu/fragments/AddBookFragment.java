package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.quan_ly_cua_hang_sach_cu.DatabaseHelper;
import com.example.quan_ly_cua_hang_sach_cu.MainActivity;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.models.Book;
import com.example.quan_ly_cua_hang_sach_cu.models.Category;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class AddBookFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private ImageView ivBookImage;
    private String selectedImageUri = null;
    private boolean isEditing = false;
    private int editBookId = -1;
    private String editBookCode = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        requireContext().getContentResolver().takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    selectedImageUri = uri.toString();
                    android.graphics.Bitmap bmp = com.example.quan_ly_cua_hang_sach_cu.utils.ImageUtils.decodeSampledBitmapFromUri(getContext(), uri, 300, 400);
                    if (bmp != null) {
                        ivBookImage.setImageBitmap(bmp);
                    } else {
                        ivBookImage.setImageURI(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_book, container, false);

        dbHelper = new DatabaseHelper(getContext());

        ivBookImage = view.findViewById(R.id.ivBookImage);
        Button btnSelectImage = view.findViewById(R.id.btnSelectImage);
        EditText edtTitle = view.findViewById(R.id.edtAddTitle);
        EditText edtAuthor = view.findViewById(R.id.edtAddAuthor);
        EditText edtIsbn = view.findViewById(R.id.edtAddIsbn);
        EditText edtPrice = view.findViewById(R.id.edtAddPrice);
        EditText edtOriginalPrice = view.findViewById(R.id.edtAddOriginalPrice);
        AutoCompleteTextView autoCompleteCategory = view.findViewById(R.id.autoCompleteCategory);
        AutoCompleteTextView autoCompleteCondition = view.findViewById(R.id.autoCompleteCondition);
        Button btnSave = view.findViewById(R.id.btnSaveBook);

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        ivBookImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // 1. Load categories
        List<Category> categoryModels = dbHelper.getAllCategories();
        if (categoryModels.isEmpty()) {
            dbHelper.addCategory(new Category("Văn học cổ điển & Hiện đại"));
            dbHelper.addCategory(new Category("Khoa học & Vũ trụ"));
            dbHelper.addCategory(new Category("Lịch sử & Triết học"));
            dbHelper.addCategory(new Category("Tiểu sử & Danh nhân"));
            categoryModels = dbHelper.getAllCategories();
        }

        List<String> categoryNames = new ArrayList<>();
        for (Category cat : categoryModels) {
            categoryNames.add(cat.getName());
        }

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
        autoCompleteCategory.setAdapter(categoryAdapter);
        autoCompleteCategory.setOnClickListener(v -> autoCompleteCategory.showDropDown());
        autoCompleteCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteCategory.showDropDown();
        });

        // 2. Load conditions
        List<String> conditionOptions = new ArrayList<>();
        conditionOptions.add("Tốt");
        conditionOptions.add("Khá");
        conditionOptions.add("Trung bình");

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, conditionOptions);
        autoCompleteCondition.setAdapter(conditionAdapter);
        autoCompleteCondition.setOnClickListener(v -> autoCompleteCondition.showDropDown());
        autoCompleteCondition.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteCondition.showDropDown();
        });

        // Check if editing
        if (getArguments() != null && getArguments().containsKey("book_id")) {
            isEditing = true;
            editBookId = getArguments().getInt("book_id");
            editBookCode = getArguments().getString("book_code");
            
            String title = getArguments().getString("title");
            if (title != null) edtTitle.setText(title);

            String author = getArguments().getString("author");
            if (author != null) edtAuthor.setText(author);

            if (editBookCode != null) edtIsbn.setText(editBookCode);

            String price = getArguments().getString("price");
            if (price != null) edtPrice.setText(price);

            String origPrice = getArguments().getString("original_price");
            if (origPrice != null) edtOriginalPrice.setText(origPrice);

            String cat = getArguments().getString("category");
            if (cat != null) autoCompleteCategory.setText(cat, false);

            String cond = getArguments().getString("condition");
            if (cond != null) autoCompleteCondition.setText(cond, false);

            selectedImageUri = getArguments().getString("image_uri");
            if (selectedImageUri != null && !selectedImageUri.isEmpty()) {
                try {
                    Uri uri = Uri.parse(selectedImageUri);
                    android.graphics.Bitmap bmp = com.example.quan_ly_cua_hang_sach_cu.utils.ImageUtils.decodeSampledBitmapFromUri(getContext(), uri, 300, 400);
                    if (bmp != null) {
                        ivBookImage.setImageBitmap(bmp);
                    } else {
                        ivBookImage.setImageURI(uri);
                    }
                } catch (Exception ignored) {
                    ivBookImage.setImageResource(R.drawable.ic_book_open);
                }
            }

            btnSave.setText("Cập nhật sách");
        }

        btnSave.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String author = edtAuthor.getText().toString().trim();
            String isbn = edtIsbn.getText().toString().trim();
            String price = edtPrice.getText().toString().trim();
            String originalPrice = edtOriginalPrice.getText().toString().trim();
            String selectedCategory = autoCompleteCategory.getText().toString().trim();
            String selectedCondition = autoCompleteCondition.getText().toString().trim();

            if (title.isEmpty() || author.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên sách và tác giả!", Toast.LENGTH_SHORT).show();
            } else if (selectedCategory.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn thể loại!", Toast.LENGTH_SHORT).show();
            } else if (selectedCondition.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn tình trạng sách!", Toast.LENGTH_SHORT).show();
            } else {
                String bookCode = isbn.isEmpty() ? (editBookCode != null ? editBookCode : "MS-" + (int)(Math.random() * 90000 + 10000)) : isbn;
                String finalPrice = price.isEmpty() ? "50.000 đ" : (price.contains("đ") ? price : price + " đ");
                String finalOrigPrice = originalPrice.isEmpty() ? null : (originalPrice.contains("đ") ? originalPrice : originalPrice + " đ");

                int conditionPercent = selectedCondition.equals("Tốt") ? 90 : (selectedCondition.equals("Khá") ? 75 : 60);

                if (isEditing) {
                    Book updatedBook = new Book(editBookId, bookCode, title, author, selectedCategory, selectedCondition, conditionPercent, finalPrice, finalOrigPrice, selectedImageUri);
                    dbHelper.updateBook(updatedBook);
                    Toast.makeText(getContext(), "Đã cập nhật sách thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Book newBook = new Book(bookCode, title, author, selectedCategory, selectedCondition, conditionPercent, finalPrice, finalOrigPrice, selectedImageUri);
                    dbHelper.addBook(newBook);
                    Toast.makeText(getContext(), "Đã lưu sách thành công vào kho!", Toast.LENGTH_SHORT).show();
                }

                if (getActivity() != null) {
                    if (isEditing) {
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setScreenTitle("Kho sách");
                        }
                        requireActivity().getSupportFragmentManager().popBackStack();
                    } else {
                        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                        if (bottomNav != null) {
                            bottomNav.setSelectedItemId(R.id.nav_inventory);
                        }
                    }
                }
            }
        });

        return view;
    }
}
