# Luồng tìm kiếm và lọc sách

Toàn bộ chức năng tìm kiếm và lọc nằm trong
`app/src/main/java/com/example/quan_ly_cua_hang_sach_cu/fragments/BookInventoryFragment.java`
(màn hình "Trang chủ"). Việc lọc được làm **trong bộ nhớ** (in-memory) trên danh sách đã tải từ SQLite,
không chạy câu truy vấn `WHERE` nào xuống database.

> `SearchFragment.java` hiện chỉ inflate layout `search.xml`, chưa có logic tìm kiếm.

---

## 1. Các thành phần chính

| Thành phần | Vai trò |
|---|---|
| `allBooks` | Danh sách **gốc**, toàn bộ sách lấy từ DB (`BookStore.getAllBooks()`), sắp xếp `id` giảm dần (sách mới nhất lên đầu). |
| `filteredBooks` | Danh sách **hiển thị**, được truyền vào `BookAdapter`. Mỗi lần lọc sẽ `clear()` rồi thêm lại các sách thỏa điều kiện. |
| `searchQuery` | Từ khóa trong ô `edtSearch` (đã `toLowerCase` + `trim`). |
| `minPriceFilter`, `maxPriceFilter` | Khoảng giá, mặc định `0` – `1.000.000`. |
| `conditionFilter` | `"all"` / `"good"` / `"fair"` / `"avg"`. |
| `categoryFilter` | `"all"` hoặc tên thể loại. |
| `applyFilters()` | Hàm trung tâm: áp **tất cả** điều kiện cùng lúc lên `allBooks` → ghi vào `filteredBooks` → `notifyDataSetChanged()`. |

Hằng số khoảng giá:

```java
MIN_PRICE  = 0
MAX_PRICE  = 1_000_000
PRICE_STEP = 50_000
```

---

## 2. Sơ đồ tổng quan

```
           ┌──────────────────────────┐
           │  SQLite (BookStore)      │
           └────────────┬─────────────┘
                        │ loadBooks()  (onCreateView, onResume)
                        ▼
                  allBooks (gốc, id giảm dần)
                        │
   ┌────────────────────┼─────────────────────────────┐
   │                    │                             │
 Gõ ô tìm kiếm     Bấm "Áp dụng" trong          Bấm nút xóa lọc /
 (TextWatcher)     BottomSheet bộ lọc           nhận FragmentResult
   │                    │                             │
 cập nhật           cập nhật min/max giá,        resetFilters()
 searchQuery        condition, category               │
   │                    │                             │
   └────────────────────┴──────────────┬──────────────┘
                                       ▼
                               applyFilters()
                                       │
                 search  AND  giá  AND  tình trạng  AND  thể loại
                                       ▼
                         filteredBooks → BookAdapter → RecyclerView
                                       │
                          updateClearFilterButton()
```

---

## 3. Luồng tìm kiếm (search)

1. Người dùng gõ vào `edtSearch`.
2. `TextWatcher.onTextChanged` chạy **mỗi lần ký tự thay đổi** (tìm kiếm realtime, không cần bấm nút):
   ```java
   searchQuery = s.toString().toLowerCase(Locale.ROOT).trim();
   applyFilters();
   ```
3. Trong `applyFilters()`, từ khóa và dữ liệu sách đều được đưa qua `removeDiacritics()`:
   - Chuẩn hóa Unicode về dạng NFD (tách chữ và dấu).
   - Xóa các ký tự dấu (`\p{InCombiningDiacriticalMarks}`).
   - Chuyển về chữ thường.

   ⇒ Tìm **không phân biệt hoa/thường và không phân biệt dấu**. Ví dụ gõ `nguyen nhat anh` vẫn khớp `Nguyễn Nhật Ánh`.

   > Lưu ý: `đ`/`Đ` không phải dấu tổ hợp nên **không** bị chuyển thành `d`. Gõ `do` sẽ không khớp `Đỗ`.

4. Sách khớp nếu từ khóa là **chuỗi con** của một trong 3 trường:
   - `title` (tên sách)
   - `author` (tác giả)
   - `bookCode` (mã sách)

5. Nếu ô tìm kiếm rỗng → bỏ qua điều kiện search (`matchSearch = true`).

---

## 4. Luồng lọc (filter)

### 4.1. Mở bộ lọc

Bấm `btnOpenFilter` → `showFilterDialog()` mở một `BottomSheetDialog` (layout `dialog_filter.xml`), luôn ở trạng thái mở rộng hoàn toàn.

Dialog gồm:
- **RangeSlider giá** (`sliderPrice`): 0 – 1.000.000, bước 50.000.
- **Spinner tình trạng**: `Tất cả tình trạng`, `Tốt`, `Khá`, `Trung bình`.
- **Spinner thể loại**: `Tất cả thể loại` + danh sách thể loại lấy **động từ DB** (`CategoryStore.getAllCategories()`, bỏ tên rỗng và trùng).
- Nút **Đặt lại**, **Áp dụng**, **Đóng (X)**.

Khi mở, dialog hiển thị lại trạng thái lọc hiện tại:
- Slider được set theo `minPriceFilter`/`maxPriceFilter` (làm tròn về bội số 50.000 bằng `snapToPriceStep()` để tránh crash của RangeSlider khi giá trị không khớp step).
- Spinner chọn đúng vị trí của `conditionFilter` / `categoryFilter`.

### 4.2. Trạng thái tạm (pending) — chưa áp dụng ngay

- Kéo slider chỉ cập nhật mảng tạm `pendingPrice[]` và label `txtPriceRangeLabel`, **chưa** thay đổi `minPriceFilter`/`maxPriceFilter`.
- Đổi Spinner cũng chưa có tác dụng.
- **Đặt lại** (`btnResetFilter`): chỉ đưa các control trong dialog về mặc định, **chưa** áp dụng, danh sách chưa đổi.
- **Đóng** (`btnCloseFilter`) hoặc vuốt xuống: hủy mọi thay đổi chưa áp dụng.

### 4.3. Áp dụng

Bấm **Áp dụng** (`btnApplyFilter`):
1. `minPriceFilter`, `maxPriceFilter` ← `pendingPrice`.
2. Vị trí Spinner tình trạng → `conditionFilter`:
   | Vị trí | Giá trị |
   |---|---|
   | 0 | `all` |
   | 1 | `good` |
   | 2 | `fair` |
   | 3 | `avg` |
3. Vị trí Spinner thể loại → `categoryFilter` (vị trí 0 = `all`, còn lại là tên thể loại).
4. Gọi `applyFilters()`, đóng dialog, hiện Toast "Đã áp dụng bộ lọc thành công".

### 4.4. Quy tắc từng điều kiện trong `applyFilters()`

Một sách được hiển thị khi **thỏa đồng thời cả 4 điều kiện** (AND):

**a) Tìm kiếm** — xem mục 3.

**b) Giá**
```java
int price = parsePrice(book.getPrice());   // bỏ mọi ký tự không phải số, lỗi → 0
matchesPrice = price >= minPriceFilter
            && (maxPriceFilter >= MAX_PRICE || price <= maxPriceFilter);
```
- Khi đầu trên của slider ở mức tối đa (1.000.000) nghĩa là **"không giới hạn trên"** → sách giá > 1 triệu vẫn hiển thị. Label hiện "1.000.000 đ trở lên".
- Giá không đọc được sẽ tính là `0`.

**c) Tình trạng** — khớp theo **chuỗi `condition`** HOẶC theo **`conditionPercent`**:
| Bộ lọc | Khớp khi |
|---|---|
| `good` (Tốt) | condition chứa "Tốt" hoặc "Mới", **hoặc** `conditionPercent >= 80` |
| `fair` (Khá) | condition chứa "Khá", **hoặc** `65 <= conditionPercent < 80` |
| `avg` (Trung bình) | condition chứa "Trung bình" hoặc "Kém", **hoặc** `conditionPercent < 65` |

> Vì dùng phép OR giữa chuỗi và phần trăm, một sách có thể khớp nhiều mức (vd: condition "Khá" nhưng percent = 85 sẽ xuất hiện ở cả "Tốt" và "Khá"). Sách có `conditionPercent = 0` (chưa set) sẽ luôn khớp "Trung bình".

**d) Thể loại**
```java
matchesCategory = book.getCategory() != null
               && book.getCategory().equalsIgnoreCase(categoryFilter);
```
So khớp **chính xác** tên thể loại (không phân biệt hoa/thường).

### 4.5. Sau khi lọc

- `adapter.notifyDataSetChanged()` vẽ lại RecyclerView.
- `updateClearFilterButton()`: hiện nút `btnClearFilter` nếu `isFilterActive()` = true, tức là có ít nhất một trong:
  - `minPriceFilter > 0`
  - `maxPriceFilter < 1.000.000`
  - `conditionFilter != "all"`
  - `categoryFilter != "all"`

  > Ô tìm kiếm **không** được tính vào `isFilterActive()`, nên chỉ gõ từ khóa thì nút xóa lọc không hiện.

---

## 5. Xóa bộ lọc

### 5.1. Bấm nút xóa lọc trên màn hình
`btnClearFilter` → `resetFilters()` → `applyFilters()` → Toast "Đã xóa bộ lọc".

`resetFilters()` đưa giá / tình trạng / thể loại về mặc định, **không đụng tới ô tìm kiếm**.

### 5.2. Sau khi sửa sách xong
Trong `AddBookFragment.navigateToHome()`, khi đang ở chế độ **sửa** (`isEditing`):
```java
getParentFragmentManager().setFragmentResult(BookInventoryFragment.RESULT_CLEAR_FILTER, new Bundle());
popBackStack();
```
`BookInventoryFragment` đã đăng ký listener trong `onCreate()`:
```java
getParentFragmentManager().setFragmentResultListener(RESULT_CLEAR_FILTER, this,
        (requestKey, result) -> resetFilters());
```
→ Bộ lọc được reset, rồi khi quay lại màn hình `onResume()` gọi `loadBooks()` + `applyFilters()` để hiển thị dữ liệu mới nhất (đảm bảo sách vừa sửa không bị bộ lọc cũ che mất).

### 5.3. Sau khi thêm sách mới
Ở chế độ **thêm mới**, app chuyển tab bottom navigation sang `nav_inventory` → tạo một `BookInventoryFragment` **mới** nên mọi trạng thái lọc tự động về mặc định.

---

## 6. Vòng đời & làm mới dữ liệu

| Thời điểm | Hành động |
|---|---|
| `onCreate` | Đăng ký listener `RESULT_CLEAR_FILTER`. |
| `onCreateView` | Khởi tạo `BookStore`/`CategoryStore`, `loadBooks()`, tạo adapter, `applyFilters()`, gắn listener cho ô tìm kiếm và các nút. |
| `onResume` | `loadBooks()` + `applyFilters()` → luôn đồng bộ với DB khi quay lại màn hình, **giữ nguyên** trạng thái lọc (trừ khi vừa nhận `RESULT_CLEAR_FILTER`). |
| Xóa sách | Xóa trong DB, xóa khỏi cả `filteredBooks` và `allBooks`, `notifyItemRemoved(position)` (không gọi lại `applyFilters()`). |

Các thao tác Sửa/Xóa trên item dùng `position` của **`filteredBooks`** (danh sách đang hiển thị), nên luôn trỏ đúng sách kể cả khi đang lọc.
