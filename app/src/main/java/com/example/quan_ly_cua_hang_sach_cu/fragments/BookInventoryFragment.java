package com.example.quan_ly_cua_hang_sach_cu.fragments;

import android.app.AlertDialog;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.BookStore;
import com.example.quan_ly_cua_hang_sach_cu.CategoryStore;
import com.example.quan_ly_cua_hang_sach_cu.DatabaseHelper;
import com.example.quan_ly_cua_hang_sach_cu.MainActivity;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.adapters.BookAdapter;
import com.example.quan_ly_cua_hang_sach_cu.models.Book;
import com.example.quan_ly_cua_hang_sach_cu.models.Category;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.slider.RangeSlider;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookInventoryFragment extends Fragment {

    private BookStore bookStore;
    private CategoryStore categoryStore;
    private List<Book> allBooks = new ArrayList<>();
    private List<Book> filteredBooks = new ArrayList<>();
    private BookAdapter adapter;

    private EditText edtSearch;
    private ImageButton btnOpenFilter;
    private ImageButton btnClearFilter;
    // Khoảng giá của slider lọc
    private static final int MIN_PRICE = 0;
    private static final int MAX_PRICE = 1000000;
    private static final int PRICE_STEP = 50000;

    // Filter state
    private int minPriceFilter = MIN_PRICE;
    private int maxPriceFilter = MAX_PRICE;
    private String conditionFilter = "all";
    private String categoryFilter = "all";
    private String searchQuery = "";

    public static final String RESULT_CLEAR_FILTER = "clear_filter";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(RESULT_CLEAR_FILTER, this,
                (requestKey, result) -> resetFilters());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.book_inventory, container, false);

        DatabaseHelper database = new DatabaseHelper(getContext());
        bookStore = new BookStore(database);
        categoryStore = new CategoryStore(database);

        edtSearch = view.findViewById(R.id.edtSearch);
        btnOpenFilter = view.findViewById(R.id.btnOpenFilter);
        btnClearFilter = view.findViewById(R.id.btnClearFilter);

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
            public void onDeleteClick(int position) {
                if (position == RecyclerView.NO_POSITION || position >= filteredBooks.size()) return;
                Book book = filteredBooks.get(position);
                
                new AlertDialog.Builder(getContext())
                        .setTitle("Xóa sách")
                        .setMessage("Bạn có muốn xóa \"" + (book.getTitle() != null ? book.getTitle() : "sách này") + "\" không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            if (position >= filteredBooks.size()) return;
                            bookStore.deleteBook(book);
                            filteredBooks.remove(position);
                            allBooks.remove(book);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(getContext(), "Đã xóa sách: " + book.getTitle(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        recyclerView.setAdapter(adapter);
        applyFilters();

        setupListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBooks();
        applyFilters();
    }

    private void loadBooks() {
        allBooks = bookStore.getAllBooks();
        allBooks.sort((b1, b2) -> Integer.compare(b2.getId(), b1.getId()));
        filteredBooks.clear();
        filteredBooks.addAll(allBooks);
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

        if (btnClearFilter != null) {
            btnClearFilter.setOnClickListener(v -> {
                resetFilters();
                applyFilters();
                Toast.makeText(getContext(), "Đã xóa bộ lọc", Toast.LENGTH_SHORT).show();
            });
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
        RangeSlider sliderPrice = dialogView.findViewById(R.id.sliderPrice);
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
        List<Category> dbCategories = categoryStore.getAllCategories();
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
        final int[] pendingPrice = {minPriceFilter, maxPriceFilter};

        sliderPrice.setLabelFormatter(value -> formatPrice((int) value));
        sliderPrice.setValues(snapToPriceStep(minPriceFilter), snapToPriceStep(maxPriceFilter));
        txtPriceRangeLabel.setText(priceRangeLabel(minPriceFilter, maxPriceFilter));
        spinnerCondition.setSelection(conditionSpinnerPosition());
        int currentCatPos = categoryList.indexOf(categoryFilter);
        spinnerCategory.setSelection(currentCatPos > 0 ? currentCatPos : 0);

        sliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            pendingPrice[0] = Math.round(values.get(0));
            pendingPrice[1] = Math.round(values.get(1));
            txtPriceRangeLabel.setText(priceRangeLabel(pendingPrice[0], pendingPrice[1]));
        });

        btnCloseFilter.setOnClickListener(v -> dialog.dismiss());

        btnResetFilter.setOnClickListener(v -> {
            pendingPrice[0] = MIN_PRICE;
            pendingPrice[1] = MAX_PRICE;
            sliderPrice.setValues((float) MIN_PRICE, (float) MAX_PRICE);
            spinnerCondition.setSelection(0);
            spinnerCategory.setSelection(0);
            txtPriceRangeLabel.setText(priceRangeLabel(MIN_PRICE, MAX_PRICE));
        });

        btnApplyFilter.setOnClickListener(v -> {
            minPriceFilter = pendingPrice[0];
            maxPriceFilter = pendingPrice[1];

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
        String normalizedQuery = removeDiacritics(searchQuery);
        for (Book book : allBooks) {
            boolean matchSearch = true;
            if (normalizedQuery != null && !normalizedQuery.isEmpty()) {
                String title = removeDiacritics(book.getTitle());
                String author = removeDiacritics(book.getAuthor());
                String code = removeDiacritics(book.getBookCode());
                matchSearch = title.contains(normalizedQuery) || author.contains(normalizedQuery) || code.contains(normalizedQuery);
            }

            int price = parsePrice(book.getPrice());

            // Đầu trên ở mức tối đa nghĩa là "không giới hạn trên"
            boolean matchesPrice = price >= minPriceFilter
                    && (maxPriceFilter >= MAX_PRICE || price <= maxPriceFilter);

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

            if (matchSearch && matchesPrice && matchesCondition && matchesCategory) {
                filteredBooks.add(book);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateClearFilterButton();
    }

    /** Đưa bộ lọc giá / tình trạng / thể loại về mặc định (không đụng ô tìm kiếm). */
    private void resetFilters() {
        minPriceFilter = MIN_PRICE;
        maxPriceFilter = MAX_PRICE;
        conditionFilter = "all";
        categoryFilter = "all";
    }

    private boolean isFilterActive() {
        return minPriceFilter > MIN_PRICE || maxPriceFilter < MAX_PRICE
                || !"all".equals(conditionFilter) || !"all".equals(categoryFilter);
    }

    private String formatPrice(int price) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format(price) + " đ";
    }

    private String priceRangeLabel(int min, int max) {
        if (min <= MIN_PRICE && max >= MAX_PRICE) {
            return "Khoảng giá (VNĐ): Tất cả";
        }
        String upper = max >= MAX_PRICE ? formatPrice(MAX_PRICE) + " trở lên" : formatPrice(max);
        return "Khoảng giá (VNĐ): " + formatPrice(min) + " - " + upper;
    }

    private void updateClearFilterButton() {
        if (btnClearFilter != null) {
            btnClearFilter.setVisibility(isFilterActive() ? View.VISIBLE : View.GONE);
        }
    }

    /** Làm tròn giá về bội số PRICE_STEP, giới hạn trong [MIN_PRICE, MAX_PRICE]. */
    private float snapToPriceStep(int price) {
        int clamped = Math.max(MIN_PRICE, Math.min(MAX_PRICE, price));
        int steps = Math.round((clamped - MIN_PRICE) / (float) PRICE_STEP);
        return MIN_PRICE + steps * PRICE_STEP;
    }

    private int conditionSpinnerPosition() {
        if ("good".equals(conditionFilter)) return 1;
        if ("fair".equals(conditionFilter)) return 2;
        if ("avg".equals(conditionFilter)) return 3;
        return 0;
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

    private String removeDiacritics(String str) {
        if (str == null) return "";
        String nfdNormalizedString = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").toLowerCase(Locale.ROOT);
    }
}
