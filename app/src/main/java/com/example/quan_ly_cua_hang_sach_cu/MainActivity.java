package com.example.quan_ly_cua_hang_sach_cu;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.quan_ly_cua_hang_sach_cu.fragments.AddBookFragment;
import com.example.quan_ly_cua_hang_sach_cu.fragments.BookInventoryFragment;
import com.example.quan_ly_cua_hang_sach_cu.fragments.CategoryFragment;
import com.example.quan_ly_cua_hang_sach_cu.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView txtScreenTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtScreenTitle = findViewById(R.id.txtScreenTitle);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BookInventoryFragment())
                    .commit();
            txtScreenTitle.setText("Kho sách");
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_inventory) {
                selectedFragment = new BookInventoryFragment();
                txtScreenTitle.setText("Kho sách");
            } else if (itemId == R.id.nav_category) {
                selectedFragment = new CategoryFragment();
                txtScreenTitle.setText("Thể loại");
            } else if (itemId == R.id.nav_add) {
                selectedFragment = new AddBookFragment();
                txtScreenTitle.setText("Thêm sách");
            } else if (itemId == R.id.nav_search) {
                selectedFragment = new SearchFragment();
                txtScreenTitle.setText("Bộ lọc");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
