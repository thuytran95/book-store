package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.DatabaseHelper;
import com.example.quan_ly_cua_hang_sach_cu.MainActivity;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.adapters.BookAdapter;
import com.example.quan_ly_cua_hang_sach_cu.models.Book;
import com.example.quan_ly_cua_hang_sach_cu.models.Category;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookInventoryFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private List<Book> allBooks = new ArrayList<>();
    private List<Book> filteredBooks = new ArrayList<>();
    private BookAdapter adapter;

    private EditText edtSearch;
    private ImageButton btnOpenFilter;
    private Chip chipAll, chipGoodUnder100k, chipLiterature, chipScience, chipBiography;

    // Filter state
    private int maxPriceFilter = 1000000;
    private String conditionFilter = "all";
    private String categoryFilter = "all";
    private String searchQuery = "";
    private String activeChip = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_inventory, container, false);

        dbHelper = new DatabaseHelper(getContext());

        edtSearch = view.findViewById(R.id.edtSearch);
        btnOpenFilter = view.findViewById(R.id.btnOpenFilter);
        chipAll = view.findViewById(R.id.chipAll);
        chipGoodUnder100k = view.findViewById(R.id.chipGoodUnder100k);
        chipLiterature = view.findViewById(R.id.chipLiterature);
        chipScience = view.findViewById(R.id.chipScience);
        chipBiography = view.findViewById(R.id.chipBiography);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadBooks();

        adapter = new BookAdapter(filteredBooks, new BookAdapter.OnBookActionListener() {
            @Override
            public void onEditClick(int position) {
                if (position == RecyclerView.NO_POSITION || position >= filteredBooks.size()) return;
                Book book = filteredBooks.get(position);
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

                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setScreenTitle("Chỉnh sửa sách");
                }

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onSellClick(int position) {
                if (position == RecyclerView.NO_POSITION || position >= filteredBooks.size()) return;
                Book book = filteredBooks.get(position);
                Toast.makeText(getContext(), "Đã xuất bán sách: " + book.getTitle() + " (" + book.getPrice() + ")", Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);

        setupListeners();

        return view;
    }

    private void loadBooks() {
        allBooks = dbHelper.getAllBooks();
        if (allBooks.isEmpty()) {
            dbHelper.addBook(new Book("MS-00124", "Nỗi Buồn Chiến Tranh", "Bảo Ninh", "Văn học", "Tốt", 85, "65.000 đ", "120.000 đ", null));
            dbHelper.addBook(new Book("MS-00208", "Vũ Trụ Trong Vỏ Hạt Dẻ", "Stephen Hawking", "Khoa học", "Rất mới", 95, "88.000 đ", null, null));
            dbHelper.addBook(new Book("MS-00315", "Steve Jobs - Tiểu Sử Chính Thức", "Walter Isaacson", "Tiểu sử", "Khá", 70, "95.000 đ", "190.000 đ", null));
            dbHelper.addBook(new Book("MS-00412", "Dế Mèn Phiêu Lưu Ký", "Tô Hoài", "Văn học", "Trung bình", 80, "45.000 đ", "85.000 đ", null));
            allBooks = dbHelper.getAllBooks();
        }
        filteredBooks = new ArrayList<>(allBooks);
    }

    private void setupListeners() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase(Locale.ROOT).trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (btnOpenFilter != null) {
            btnOpenFilter.setOnClickListener(v -> showFilterDialog());
        }

        View.OnClickListener chipListener = v -> {
            resetChipsState();
            Chip c = (Chip) v;
            c.setChecked(true);
            c.setChipBackgroundColorResource(R.color.primary);
            c.setTextColor(getResources().getColor(android.R.color.white, null));

            if (v.getId() == R.id.chipAll) {
                activeChip = "all";
                categoryFilter = "all";
                conditionFilter = "all";
            } else if (v.getId() == R.id.chipGoodUnder100k) {
                activeChip = "good_under_100k";
            } else if (v.getId() == R.id.chipLiterature) {
                activeChip = "category";
                categoryFilter = "Văn học";
            } else if (v.getId() == R.id.chipScience) {
                activeChip = "category";
                categoryFilter = "Khoa học";
            } else if (v.getId() == R.id.chipBiography) {
                activeChip = "category";
                categoryFilter = "Tiểu sử";
            }
            applyFilters();
        };

        if (chipAll != null) chipAll.setOnClickListener(chipListener);
        if (chipGoodUnder100k != null) chipGoodUnder100k.setOnClickListener(chipListener);
        if (chipLiterature != null) chipLiterature.setOnClickListener(chipListener);
        if (chipScience != null) chipScience.setOnClickListener(chipListener);
        if (chipBiography != null) chipBiography.setOnClickListener(chipListener);
    }

    private void resetChipsState() {
        Chip[] chips = {chipAll, chipGoodUnder100k, chipLiterature, chipScience, chipBiography};
        for (Chip c : chips) {
            if (c != null) {
                c.setChecked(false);
                c.setChipBackgroundColorResource(R.color.surface);
                c.setTextColor(getResources().getColor(R.color.on_surface, null));
            }
        }
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_filter, null);
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(dialogView);

        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
        }

        TextView txtPriceRangeLabel = dialogView.findViewById(R.id.txtPriceRangeLabel);
        SeekBar seekBarPrice = dialogView.findViewById(R.id.seekBarPrice);
        Spinner spinnerCondition = dialogView.findViewById(R.id.spinnerCondition);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        Button btnResetFilter = dialogView.findViewById(R.id.btnResetFilter);
        Button btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);
        ImageButton btnCloseFilter = dialogView.findViewById(R.id.btnCloseFilter);

        // Setup Spinners
        String[] conditions = {"Tất cả tình trạng", "Tốt", "Khá", "Trung bình"};
        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, conditions);
        spinnerCondition.setAdapter(condAdapter);

        // Fetch categories dynamically from database
        List<Category> dbCategories = dbHelper.getAllCategories();
        List<String> categoryList = new ArrayList<>();
        categoryList.add("Tất cả thể loại");
        for (Category cat : dbCategories) {
            if (cat.getName() != null && !cat.getName().isEmpty() && !categoryList.contains(cat.getName())) {
                categoryList.add(cat.getName());
            }
        }
        String[] categories = categoryList.toArray(new String[0]);
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        // Set current filter values
        seekBarPrice.setProgress(maxPriceFilter);
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        txtPriceRangeLabel.setText("Khoảng giá (VNĐ): 50.000 đ - " + nf.format(maxPriceFilter) + " đ");

        seekBarPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int actualProgress = Math.max(progress, 50000);
                maxPriceFilter = actualProgress;
                txtPriceRangeLabel.setText("Khoảng giá (VNĐ): 50.000 đ - " + nf.format(maxPriceFilter) + " đ");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnCloseFilter.setOnClickListener(v -> dialog.dismiss());

        btnResetFilter.setOnClickListener(v -> {
            maxPriceFilter = 1000000;
            conditionFilter = "all";
            categoryFilter = "all";
            seekBarPrice.setProgress(maxPriceFilter);
            spinnerCondition.setSelection(0);
            spinnerCategory.setSelection(0);
            txtPriceRangeLabel.setText("Khoảng giá (VNĐ): 50.000 đ - 1.000.000 đ");
        });

        btnApplyFilter.setOnClickListener(v -> {
            int condPos = spinnerCondition.getSelectedItemPosition();
            if (condPos == 1) conditionFilter = "good";
            else if (condPos == 2) conditionFilter = "fair";
            else if (condPos == 3) conditionFilter = "avg";
            else conditionFilter = "all";

            int catPos = spinnerCategory.getSelectedItemPosition();
            if (catPos > 0 && catPos < categories.length) {
                categoryFilter = categories[catPos];
            } else {
                categoryFilter = "all";
            }

            applyFilters();
            dialog.dismiss();
            Toast.makeText(getContext(), "Đã áp dụng bộ lọc thành công", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void applyFilters() {
        filteredBooks.clear();
        for (Book book : allBooks) {
            boolean matchSearch = true;
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String title = book.getTitle() != null ? book.getTitle().toLowerCase(Locale.ROOT) : "";
                String author = book.getAuthor() != null ? book.getAuthor().toLowerCase(Locale.ROOT) : "";
                String code = book.getBookCode() != null ? book.getBookCode().toLowerCase(Locale.ROOT) : "";
                matchSearch = title.contains(searchQuery) || author.contains(searchQuery) || code.contains(searchQuery);
            }

            boolean matchesChip = true;
            int price = parsePrice(book.getPrice());
            if ("good_under_100k".equals(activeChip)) {
                matchesChip = (book.getConditionPercent() >= 80 && price < 100000);
            } else if ("category".equals(activeChip)) {
                matchesChip = (categoryFilter.equals("all") || (book.getCategory() != null && book.getCategory().equalsIgnoreCase(categoryFilter)));
            }

            boolean matchesPrice = (price <= maxPriceFilter);

            boolean matchesCondition = true;
            if (!"all".equals(conditionFilter)) {
                String condStr = book.getCondition() != null ? book.getCondition() : "";
                if ("good".equals(conditionFilter)) matchesCondition = (condStr.contains("Tốt") || condStr.contains("Mới") || book.getConditionPercent() >= 80);
                else if ("fair".equals(conditionFilter)) matchesCondition = (condStr.contains("Khá") || (book.getConditionPercent() >= 65 && book.getConditionPercent() < 80));
                else if ("avg".equals(conditionFilter)) matchesCondition = (condStr.contains("Trung bình") || condStr.contains("Kém") || book.getConditionPercent() < 65);
            }

            boolean matchesCategory = true;
            if (!"all".equals(categoryFilter)) {
                matchesCategory = (book.getCategory() != null && book.getCategory().equalsIgnoreCase(categoryFilter));
            }

            if (matchSearch && matchesChip && matchesPrice && matchesCondition && matchesCategory) {
                filteredBooks.add(book);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private int parsePrice(String priceStr) {
        if (priceStr == null) return 0;
        try {
            String digits = priceStr.replaceAll("[^0-9]", "");
            if (digits.isEmpty()) return 0;
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
