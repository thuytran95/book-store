# Kiến trúc tổng thể – Ứng dụng Quản lý Cửa hàng Sách cũ

> Tài liệu tóm tắt kiến trúc hiện tại của ứng dụng Android `quan-ly-cua-hang-sach-cu`, kèm đánh giá kỹ thuật và hướng cải tiến.
> Cập nhật: 2026-11-08 (theo commit `23af9b5`).

---

## 1. Tổng quan

| Hạng mục | Giá trị |
|---|---|
| Nền tảng | Android native, ngôn ngữ **Java 11** |
| Package / applicationId | `com.example.quan_ly_cua_hang_sach_cu` |
| minSdk / targetSdk / compileSdk | 24 / 36 / 36 |
| Module | 1 module duy nhất: `:app` |
| UI toolkit | XML Layout + View system (Material Components, RecyclerView, CardView, ConstraintLayout) |
| Lưu trữ | SQLite cục bộ qua `SQLiteOpenHelper` (file `bookstore.db`, version 2) |
| Mạng / Backend | **Không có** – ứng dụng hoàn toàn offline |
| Thư viện DI / Architecture Components | **Không dùng** (không ViewModel, LiveData, Room, Hilt…) |
| ViewBinding | Đã bật trong Gradle nhưng **chưa sử dụng** (code vẫn dùng `findViewById`) |

**Chức năng chính:**
1. Kho sách: xem danh sách, tìm kiếm (bỏ dấu tiếng Việt), lọc theo giá / tình trạng / thể loại, sửa, xoá.
2. Thể loại: thêm, sửa (đổi tên lan truyền sang sách), xoá (chặn nếu còn sách).
3. Thêm / sửa sách: nhập thông tin, chọn ảnh bìa từ thiết bị, validate form.

---

## 2. Kiến trúc phân lớp

Ứng dụng theo mô hình **Single-Activity + nhiều Fragment**, kiến trúc gần với **MVC truyền thống** (Fragment kiêm cả Controller và một phần logic nghiệp vụ). Có thể chia thành 3 lớp:

```
┌──────────────────────────────────────────────────────────────┐
│  PRESENTATION (UI)                                           │
│  MainActivity ── BottomNavigationView + fragment_container   │
│     ├── BookInventoryFragment  (danh sách, search, filter)   │
│     ├── CategoryFragment       (CRUD thể loại)               │
│     ├── AddBookFragment        (thêm / sửa sách)             │
│     └── SearchFragment         (chưa dùng – chỉ inflate)     │
│  Adapters: BookAdapter, CategoryAdapter (RecyclerView)       │
├──────────────────────────────────────────────────────────────┤
│  DATA ACCESS (DAO-like)                                      │
│  BookStore      – CRUD bảng `book`, seed dữ liệu mẫu         │
│  CategoryStore  – CRUD bảng `category`, cascade đổi tên      │
├──────────────────────────────────────────────────────────────┤
│  PERSISTENCE                                                 │
│  DatabaseHelper (SQLiteOpenHelper) → bookstore.db            │
├──────────────────────────────────────────────────────────────┤
│  MODEL & UTILS                                               │
│  models/Book, models/Category (POJO)                         │
│  utils/ImageUtils (decode bitmap có sampling)                │
└──────────────────────────────────────────────────────────────┘
```

Luồng phụ thuộc: `Fragment → Store → DatabaseHelper → SQLite`. Fragment khởi tạo trực tiếp `new DatabaseHelper(getContext())` và `new BookStore(...)` / `new CategoryStore(...)` (không có DI, không singleton).

---

## 3. Cấu trúc mã nguồn

```
app/src/main/java/com/example/quan_ly_cua_hang_sach_cu/
├── MainActivity.java             # Host activity, điều hướng bằng BottomNav
├── DatabaseHelper.java           # Tạo/nâng cấp schema
├── BookStore.java                # Truy cập bảng book
├── CategoryStore.java            # Truy cập bảng category
├── models/
│   ├── Book.java
│   └── Category.java
├── adapters/
│   ├── BookAdapter.java          # Bind item_book.xml, badge màu theo tình trạng
│   └── CategoryAdapter.java      # Bind item_category.xml
├── fragments/
│   ├── BookInventoryFragment.java
│   ├── CategoryFragment.java
│   ├── AddBookFragment.java
│   └── SearchFragment.java
└── utils/
    └── ImageUtils.java

app/src/main/res/
├── layout/   activity_main, book_inventory, category, add_book, search,
│             item_book, item_category, dialog_filter, dialog_add_category
├── menu/     bottom_nav_menu.xml
├── drawable/ shape nền (badge, chip, button) + vector icon
└── values/   colors, strings, themes (+ values-night)
```

---

## 4. Tầng dữ liệu

### 4.1 Schema SQLite

**Bảng `category`**

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | |
| name | TEXT | Không UNIQUE, không NOT NULL |

**Bảng `book`**

| Cột | Kiểu | Ghi chú |
|---|---|---|
| id | INTEGER PK AUTOINCREMENT | |
| book_code | TEXT | Mã sách/ISBN; nếu trống sinh ngẫu nhiên `MS-xxxxx` |
| title, author | TEXT | |
| category | TEXT | **Lưu tên thể loại**, không phải khoá ngoại |
| condition | TEXT | "Tốt" / "Khá" / "Trung bình" |
| condition_percent | INTEGER | Suy ra từ condition (90/75/60) |
| price, original_price | TEXT | Lưu chuỗi đã format, ví dụ `"65.000 đ"` |
| image_uri | TEXT | `content://` URI ảnh bìa |

Quan hệ `book ↔ category` là **quan hệ logic qua tên** (không có FOREIGN KEY):
- Đổi tên thể loại → `CategoryStore.updateCategory` cập nhật luôn `book.category` trong **một transaction**.
- Xoá thể loại → `CategoryFragment` kiểm tra `BookStore.hasBooksInCategory` (so sánh `COLLATE NOCASE`) trước khi xoá.

### 4.2 Vòng đời DB
- `onCreate`: tạo bảng `category` rồi `book`.
- `onUpgrade`: **DROP toàn bộ bảng và tạo lại** → mất dữ liệu người dùng khi tăng version.
- Seed dữ liệu: `getAllOrSeed()` của mỗi Store tự chèn dữ liệu mẫu khi bảng rỗng.

---

## 5. Tầng trình bày & điều hướng

### 5.1 Điều hướng
- `MainActivity` giữ `BottomNavigationView` với 3 tab: **Kho sách**, **Thể loại**, **Thêm sách**.
- Mỗi lần chọn tab: `FragmentTransaction.replace(...)` với **instance Fragment mới** (không giữ state, không back stack).
- Màn hình **Sửa sách**: `BookInventoryFragment` đóng gói toàn bộ field của `Book` vào `Bundle`, mở `AddBookFragment` với `addToBackStack(null)`; sau khi lưu thì `popBackStack()`.
- Tiêu đề màn hình được Fragment cập nhật qua `((MainActivity) getActivity()).setScreenTitle(...)` → Fragment phụ thuộc cứng vào Activity cụ thể.

### 5.2 Các luồng chính

**Xem / lọc kho sách** (`BookInventoryFragment`)
```
onCreateView/onResume → bookStore.getAllOrSeed() → sort theo id giảm dần
        → applyFilters() (lọc in-memory) → adapter.notifyDataSetChanged()
```
- Search: bỏ dấu bằng `Normalizer` NFD, tìm trong title/author/book_code.
- Filter (BottomSheetDialog): giá tối đa (SeekBar), tình trạng (Spinner), thể loại (Spinner lấy từ DB).
- Giá được parse ngược từ chuỗi `"65.000 đ"` về số để so sánh.

**Thêm / sửa sách** (`AddBookFragment`)
```
Chọn ảnh (ActivityResultContracts.GetContent) → takePersistableUriPermission
→ validate form (hiển thị lỗi inline) → format giá theo Locale vi-VN
→ bookStore.addBook / updateBook → Toast → quay về Kho sách
```

**Quản lý thể loại** (`CategoryFragment`)
```
Thêm/Sửa qua AlertDialog custom → categoryStore.add/update (cascade tên sang book)
Xoá → kiểm tra còn sách? → xác nhận → categoryStore.deleteCategory
```

---

## 6. Đánh giá kỹ thuật (góc nhìn Senior Mobile Engineer)

### 6.1 Điểm tốt
- Cấu trúc thư mục rõ ràng; đã tách persistence ra `BookStore` / `CategoryStore` thay vì dồn hết vào `DatabaseHelper`.
- Dùng tham số `?` trong truy vấn → tránh SQL injection.
- Đổi tên thể loại dùng transaction, đảm bảo tính nhất quán.
- Ảnh được decode có `inSampleSize` → giảm nguy cơ OOM.
- Search hỗ trợ tiếng Việt không dấu — trải nghiệm tốt cho người dùng.
- Dùng `getBindingAdapterPosition()` và kiểm tra `NO_POSITION` trong adapter.

### 6.2 Rủi ro & vấn đề cần lưu ý

| # | Vấn đề | Mức độ | Ảnh hưởng |
|---|---|---|---|
| 1 | Mọi truy vấn DB và decode Bitmap chạy trên **main thread** (kể cả trong `onBindViewHolder`) | Cao | Giật lag khi danh sách lớn, có thể ANR |
| 2 | `onUpgrade` DROP bảng | Cao | Mất toàn bộ dữ liệu khi nâng version DB |
| 3 | Giá lưu dạng **TEXT đã format** | Trung bình | Khó sort/filter bằng SQL, phải parse ngược, dễ sai locale |
| 4 | Quan hệ thể loại qua **tên** (không FK, không UNIQUE) | Trung bình | Dữ liệu seed sách ("Văn học") không khớp seed thể loại ("Văn học cổ điển & Hiện đại"); có thể tạo trùng tên; rename so sánh phân biệt hoa thường còn kiểm tra xoá thì không |
| 5 | Đóng DB không nhất quán (`addBook`, `getAll*` gọi `db.close()`; `update`/`delete` thì không) và mỗi Fragment tạo `DatabaseHelper` riêng | Trung bình | Rủi ro `IllegalStateException` khi dùng đồng thời, lãng phí tài nguyên |
| 6 | Không có ViewModel / lưu state | Trung bình | Xoay màn hình hoặc đổi tab làm mất search/filter/form đang nhập |
| 7 | `GetContent` không đảm bảo URI persistable (lỗi bị nuốt bởi `catch`) | Trung bình | Ảnh bìa có thể không hiển thị sau khi khởi động lại app |
| 8 | Đọc Cursor theo **chỉ số cột** (`getString(1)`…) | Thấp | Dễ vỡ khi thay đổi thứ tự cột |
| 9 | Mã sách sinh ngẫu nhiên `MS-` + 5 chữ số | Thấp | Có thể trùng mã |
| 10 | Chuỗi UI hard-code trong Java (không dùng `strings.xml`) | Thấp | Khó đa ngôn ngữ, khó bảo trì |
| 11 | ViewBinding bật nhưng không dùng; `SearchFragment` không được tham chiếu | Thấp | Code thừa |
| 12 | `notifyDataSetChanged()` cho mọi lần lọc | Thấp | Mất animation, hiệu năng kém hơn `ListAdapter` + `DiffUtil` |
| 13 | Edit truyền cả object qua `Bundle` thay vì chỉ `book_id` | Thấp | Dữ liệu có thể cũ, trùng lặp logic mapping |
| 14 | Chưa có Unit test / UI test thực tế | Trung bình | Khó refactor an toàn |

---

## 7. Lộ trình cải tiến đề xuất

Đề xuất theo hướng **Android Recommended Architecture (MVVM + Repository)**, tiến hành từng bước để không phá vỡ ứng dụng:

1. **Ngắn hạn (ổn định):**
   - Đưa truy vấn DB và decode ảnh ra background (`ExecutorService` hoặc dùng thư viện ảnh như Glide/Coil).
   - Dùng một `DatabaseHelper` singleton, bỏ `db.close()` rải rác.
   - Viết `onUpgrade` dạng migration (`ALTER TABLE`) thay vì DROP.
   - Đổi image picker sang `ActivityResultContracts.OpenDocument` để có URI persistable.
2. **Trung hạn (kiến trúc):**
   - Chuyển sang **Room** (Entity/DAO, migration, query trả `LiveData`/`Flow`).
   - Lưu giá dạng `INTEGER` (đơn vị đồng), chỉ format khi hiển thị.
   - `book.category_id` là FOREIGN KEY tới `category.id`; `category.name` UNIQUE.
   - Thêm **Repository** + **ViewModel** cho từng màn hình; Fragment chỉ observe state.
   - Áp dụng ViewBinding, `ListAdapter` + `DiffUtil`, chuyển chuỗi sang `strings.xml`.
   - Dùng **Navigation Component** (Safe Args truyền `bookId`).
3. **Dài hạn (mở rộng):**
   - DI với Hilt; cân nhắc chuyển dần sang Kotlin + Coroutines/Flow, Jetpack Compose.
   - Bổ sung test: unit test cho Repository/ViewModel, instrumented test cho DAO và luồng UI chính.
   - Nếu cần đa thiết bị: đồng bộ backend + chiến lược offline-first.

---

## 8. Tài liệu liên quan
- [giai-thich-DatabaseHelper.md](giai-thich-DatabaseHelper.md)
- [giai-thich-BookStore-CategoryStore.md](giai-thich-BookStore-CategoryStore.md)
- [luong-them-sach.md](luong-them-sach.md)
