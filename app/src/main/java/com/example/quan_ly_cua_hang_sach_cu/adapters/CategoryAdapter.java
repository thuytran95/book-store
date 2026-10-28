package com.example.quan_ly_cua_hang_sach_cu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.models.CategoryModel;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<CategoryModel> categoryList;
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onDeleteClick(int position);
        void onEditClick(int position);
    }

    public CategoryAdapter(List<CategoryModel> categoryList, OnCategoryActionListener listener) {
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel cat = categoryList.get(position);
        holder.txtCatName.setText(cat.getName());

        holder.btnDeleteCat.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });

        holder.btnEditCat.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtCatName;
        ImageView btnEditCat, btnDeleteCat;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCatName = itemView.findViewById(R.id.txtCatName);
            btnEditCat = itemView.findViewById(R.id.btnEditCat);
            btnDeleteCat = itemView.findViewById(R.id.btnDeleteCat);
        }
    }
}
