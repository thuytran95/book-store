# Giải thích BookAdapter và CategoryAdapter

`BookAdapter` và `CategoryAdapter` là các **RecyclerView Adapter**. Chúng nối giữa dữ liệu (danh sách `Book` / `Category`) và giao diện danh sách trên màn hình.

## Vai trò chung

Mỗi adapter làm 3 việc chính:

1. **Tạo view cho từng dòng** (`onCreateViewHolder`): inflate layout `item_book.xml` hoặc `item_category.xml`.
2. **Đổ dữ liệu vào view** (`onBindViewHolder`): lấy object ở vị trí `position` rồi gán lên TextView/ImageView.
3. **Báo sự kiện click về Fragment**: khi người dùng bấm nút Sửa/Xóa, adapter gọi callback (`onEditClick` / `onDeleteClick`). Fragment là nơi xử lý logic, adapter không xử lý.

## BookAdapter

File: `app/src/main/java/com/example/quan_ly_cua_hang_sach_cu/adapters/BookAdapter.java`

- Dùng trong `BookInventoryFragment` (màn hình Trang chủ), hiển thị danh sách `filteredBooks`.
- Mỗi dòng hiển thị: mã sách, tên sách, tác giả, thể loại, tình trạng, giá bán, giá gốc và ảnh bìa.
- Badge tình trạng đổi màu theo nội dung:
  - "Tốt"/"Mới": xanh lá
  - "Khá": xanh dương
  - "Trung bình"/"Kém": hồng
- Giá được định dạng kiểu Việt Nam, ví dụ `50.000 đ` (hàm `formatPrice`).
- Giá gốc bị ẩn nếu trống hoặc bằng 0.
- Ảnh bìa được thu nhỏ bằng `ImageUtils.decodeSampledBitmapFromUri` cho đỡ tốn bộ nhớ. Nếu không có ảnh hoặc lỗi thì dùng icon mặc định `ic_book_open`.
- Nút Sửa và Xóa gọi về `OnBookActionListener`. Ví dụ khi bấm Sửa, Fragment đóng gói thông tin sách vào `Bundle` rồi mở `AddBookFragment` ở chế độ chỉnh sửa.

## CategoryAdapter

File: `app/src/main/java/com/example/quan_ly_cua_hang_sach_cu/adapters/CategoryAdapter.java`

- Dùng trong `CategoryFragment` (màn hình quản lý thể loại).
- Đơn giản hơn: mỗi dòng chỉ có tên thể loại (`txtCatName`), nút Sửa (`btnEditCat`) và nút Xóa (`btnDeleteCat`).
- Click được báo về Fragment qua `OnCategoryActionListener`.

## Chi tiết chung

Cả hai đều dùng `holder.getBindingAdapterPosition()` và kiểm tra `RecyclerView.NO_POSITION` trước khi gọi callback. Nhờ vậy click luôn lấy đúng vị trí hiện tại của item, kể cả khi danh sách vừa thay đổi (lọc, xóa phần tử…).
