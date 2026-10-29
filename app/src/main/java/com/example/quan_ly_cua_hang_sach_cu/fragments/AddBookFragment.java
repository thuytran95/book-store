package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        View view = inflater.inflate(R.layout.add_book, container, false);

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

        TextView tvLabelTitle = view.findViewById(R.id.tvLabelTitle);
        TextView tvLabelAuthor = view.findViewById(R.id.tvLabelAuthor);
        TextView tvLabelCategory = view.findViewById(R.id.tvLabelCategory);
        TextView tvLabelCondition = view.findViewById(R.id.tvLabelCondition);
        TextView tvLabelPrice = view.findViewById(R.id.tvLabelPrice);

        if (tvLabelTitle != null) tvLabelTitle.setText(Html.fromHtml("Tên sách <font color='#BA1A1A'>*</font>", Html.FROM_HTML_MODE_LEGACY));
        if (tvLabelAuthor != null) tvLabelAuthor.setText(Html.fromHtml("Tác giả <font color='#BA1A1A'>*</font>", Html.FROM_HTML_MODE_LEGACY));
        if (tvLabelCategory != null) tvLabelCategory.setText(Html.fromHtml("Thể loại <font color='#BA1A1A'>*</font>", Html.FROM_HTML_MODE_LEGACY));
        if (tvLabelCondition != null) tvLabelCondition.setText(Html.fromHtml("Tình trạng sách <font color='#BA1A1A'>*</font>", Html.FROM_HTML_MODE_LEGACY));
        if (tvLabelPrice != null) tvLabelPrice.setText(Html.fromHtml("Giá bán (đ) <font color='#BA1A1A'>*</font>", Html.FROM_HTML_MODE_LEGACY));

        TextView tvErrorTitle = view.findViewById(R.id.tvErrorTitle);
        TextView tvErrorAuthor = view.findViewById(R.id.tvErrorAuthor);
        TextView tvErrorCategory = view.findViewById(R.id.tvErrorCategory);
        TextView tvErrorCondition = view.findViewById(R.id.tvErrorCondition);
        TextView tvErrorPrice = view.findViewById(R.id.tvErrorPrice);

        Button btnCancel = view.findViewById(R.id.btnCancel);
        Button btnSave = view.findViewById(R.id.btnSaveBook);

        btnSelectImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        ivBookImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> navigateToHome());
        }

        // Setup TextChangeListeners to clear errors on typing
        edtTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvErrorTitle != null) tvErrorTitle.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtAuthor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvErrorAuthor != null) tvErrorAuthor.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        edtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvErrorPrice != null) tvErrorPrice.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        autoCompleteCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvErrorCategory != null) tvErrorCategory.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        autoCompleteCondition.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (tvErrorCondition != null) tvErrorCondition.setVisibility(View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);
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

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, conditionOptions);
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
            if (price != null) {
                // Keep only numeric digits inside input
                String cleanPrice = price.replaceAll("[^0-9]", "");
                edtPrice.setText(cleanPrice);
            }

            String origPrice = getArguments().getString("original_price");
            if (origPrice != null) {
                String cleanOrigPrice = origPrice.replaceAll("[^0-9]", "");
                edtOriginalPrice.setText(cleanOrigPrice);
            }

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

            if (!validateInputs(title, author, selectedCategory, selectedCondition, price,
                    tvErrorTitle, tvErrorAuthor, tvErrorCategory, tvErrorCondition, tvErrorPrice)) {
                return;
            }

            String bookCode = isbn.isEmpty() ? (editBookCode != null ? editBookCode : "MS-" + (int)(Math.random() * 90000 + 10000)) : isbn;

            // Format price string for DB display
            String finalPrice;
            try {
                long pVal = Long.parseLong(price);
                NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                finalPrice = nf.format(pVal) + " đ";
            } catch (Exception e) {
                finalPrice = price + " đ";
            }

            String finalOrigPrice = null;
            if (!originalPrice.isEmpty()) {
                try {
                    long opVal = Long.parseLong(originalPrice);
                    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                    finalOrigPrice = nf.format(opVal) + " đ";
                } catch (Exception e) {
                    finalOrigPrice = originalPrice + " đ";
                }
            }

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

            navigateToHome();
        });

        return view;
    }

    private boolean validateInputs(
            String title,
            String author,
            String category,
            String condition,
            String price,
            TextView tvErrorTitle,
            TextView tvErrorAuthor,
            TextView tvErrorCategory,
            TextView tvErrorCondition,
            TextView tvErrorPrice
    ) {
        boolean isValid = true;

        if (tvErrorTitle != null) tvErrorTitle.setVisibility(View.GONE);
        if (tvErrorAuthor != null) tvErrorAuthor.setVisibility(View.GONE);
        if (tvErrorCategory != null) tvErrorCategory.setVisibility(View.GONE);
        if (tvErrorCondition != null) tvErrorCondition.setVisibility(View.GONE);
        if (tvErrorPrice != null) tvErrorPrice.setVisibility(View.GONE);

        if (title.isEmpty()) {
            if (tvErrorTitle != null) {
                tvErrorTitle.setText("Vui lòng nhập tên sách");
                tvErrorTitle.setVisibility(View.VISIBLE);
            }
            isValid = false;
        }

        if (author.isEmpty()) {
            if (tvErrorAuthor != null) {
                tvErrorAuthor.setText("Vui lòng nhập tên tác giả");
                tvErrorAuthor.setVisibility(View.VISIBLE);
            }
            isValid = false;
        }

        if (category.isEmpty()) {
            if (tvErrorCategory != null) {
                tvErrorCategory.setText("Vui lòng chọn thể loại");
                tvErrorCategory.setVisibility(View.VISIBLE);
            }
            isValid = false;
        }

        if (condition.isEmpty()) {
            if (tvErrorCondition != null) {
                tvErrorCondition.setText("Vui lòng chọn tình trạng sách");
                tvErrorCondition.setVisibility(View.VISIBLE);
            }
            isValid = false;
        }

        if (price.isEmpty()) {
            if (tvErrorPrice != null) {
                tvErrorPrice.setText("Vui lòng nhập giá bán");
                tvErrorPrice.setVisibility(View.VISIBLE);
            }
            isValid = false;
        }

        return isValid;
    }

    private void navigateToHome() {
        if (getActivity() != null) {
            if (isEditing) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setScreenTitle("Trang chủ");
                }
                requireActivity().getSupportFragmentManager().popBackStack();
            } else {
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_inventory);
                } else {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setScreenTitle("Trang chủ");
                    }
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new BookInventoryFragment())
                            .commit();
                }
            }
        }
    }
}
