package com.example.quan_ly_cua_hang_sach_cu.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quan_ly_cua_hang_sach_cu.R;
import com.example.quan_ly_cua_hang_sach_cu.models.Book;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;
    private OnBookActionListener listener;

    public interface OnBookActionListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public BookAdapter(List<Book> bookList, OnBookActionListener listener) {
        this.bookList = bookList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        if (book == null) return;
        holder.txtBookId.setText(book.getBookCode() != null ? book.getBookCode() : "MS-" + book.getId());
        holder.txtBookTitle.setText(book.getTitle() != null ? book.getTitle() : "");
        holder.txtAuthor.setText("Tác giả: " + (book.getAuthor() != null ? book.getAuthor() : "Đang cập nhật"));
        holder.txtCategoryTag.setText(book.getCategory() != null ? book.getCategory() : "Khác");
        holder.txtCondition.setText((book.getCondition() != null ? book.getCondition() : "Tốt") + " (" + book.getConditionPercent() + "%)");
        holder.txtPrice.setText(book.getPrice() != null ? book.getPrice() : "0 đ");

        if (book.getOriginalPrice() != null && !book.getOriginalPrice().isEmpty()) {
            holder.txtOriginalPrice.setVisibility(View.VISIBLE);
            holder.txtOriginalPrice.setText(book.getOriginalPrice());
        } else {
            holder.txtOriginalPrice.setVisibility(View.GONE);
        }

        if (book.getImageUri() != null && !book.getImageUri().isEmpty()) {
            try {
                Uri uri = Uri.parse(book.getImageUri());
                android.graphics.Bitmap bmp = com.example.quan_ly_cua_hang_sach_cu.utils.ImageUtils.decodeSampledBitmapFromUri(holder.itemView.getContext(), uri, 150, 200);
                if (bmp != null) {
                    holder.ivBookCover.setImageBitmap(bmp);
                } else {
                    holder.ivBookCover.setImageURI(uri);
                }
            } catch (Exception e) {
                holder.ivBookCover.setImageResource(R.drawable.ic_book_open);
            }
        } else {
            holder.ivBookCover.setImageResource(R.drawable.ic_book_open);
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onEditClick(adapterPos);
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(adapterPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView txtBookId, txtBookTitle, txtAuthor, txtCategoryTag, txtCondition, txtPrice, txtOriginalPrice;
        ImageView ivBookCover, btnEdit, btnDelete;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookId = itemView.findViewById(R.id.txtBookId);
            txtBookTitle = itemView.findViewById(R.id.txtBookTitle);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            txtCategoryTag = itemView.findViewById(R.id.txtCategoryTag);
            txtCondition = itemView.findViewById(R.id.txtCondition);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
