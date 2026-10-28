package com.example.quan_ly_cua_hang_sach_cu.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.models.BookModel;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<BookModel> bookList;

    public BookAdapter(List<BookModel> bookList) {
        this.bookList = bookList;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        BookModel book = bookList.get(position);
        holder.txtBookId.setText(book.getId());
        holder.txtBookTitle.setText(book.getTitle());
        holder.txtAuthor.setText("Tác giả: " + book.getAuthor());
        holder.txtCategoryTag.setText(book.getCategory());
        holder.txtCondition.setText(book.getCondition() + " (" + book.getConditionPercent() + "%)");
        holder.txtPrice.setText(book.getPrice());
        if (book.getOriginalPrice() != null) {
            holder.txtOriginalPrice.setVisibility(View.VISIBLE);
            holder.txtOriginalPrice.setText(book.getOriginalPrice());
        } else {
            holder.txtOriginalPrice.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView txtBookId, txtBookTitle, txtAuthor, txtCategoryTag, txtCondition, txtPrice, txtOriginalPrice;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookId = itemView.findViewById(R.id.txtBookId);
            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            txtCategoryTag = itemView.findViewById(R.id.txtCategoryTag);
            txtCondition = itemView.findViewById(R.id.txtCondition);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
        }
    }
}
