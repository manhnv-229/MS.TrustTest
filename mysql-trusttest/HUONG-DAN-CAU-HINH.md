# Hướng Dẫn Cấu Hình MCP Server MySQL TrustTest

## Bước 1: Mở VSCode Settings

1. Nhấn `Ctrl + Shift + P` (hoặc `Cmd + Shift + P` trên Mac)
2. Gõ "Preferences: Open User Settings (JSON)"
3. Chọn kết quả đầu tiên

## Bước 2: Thêm Cấu Hình MCP Server

Trong file `settings.json`, thêm cấu hình sau vào phần `mcpServers`:

```json
{
  "mcpServers": {
    "mysql-trusttest": {
      "command": "node",
      "args": [
        "C:\\Users\\manhn\\OneDrive\\Documents\\Cline\\MCP\\mysql-trusttest\\build\\index.js"
      ]
    }
  }
}
```

**Lưu ý:** 
- Nếu chưa có phần `mcpServers`, hãy tạo mới
- Nếu đã có các MCP servers khác, thêm `mysql-trusttest` vào danh sách

### Ví dụ cấu hình đầy đủ:

```json
{
  "mcpServers": {
    "mysql-rem": {
      "command": "node",
      "args": [
        "C:\\Users\\manhn\\OneDrive\\Documents\\Cline\\MCP\\mysql-rem\\build\\index.js"
      ]
    },
    "mysql-trusttest": {
      "command": "node",
      "args": [
        "C:\\Users\\manhn\\OneDrive\\Documents\\Cline\\MCP\\mysql-trusttest\\build\\index.js"
      ]
    }
  }
}
```

## Bước 3: Khởi Động Lại VSCode

1. Nhấn `Ctrl + Shift + P`
2. Gõ "Developer: Reload Window"
3. Chọn để reload VSCode

## Bước 4: Kiểm Tra Kết Nối

Sau khi reload, MCP server sẽ tự động kết nối. Bạn có thể test bằng cách:

1. Mở Cline
2. Sử dụng tool `describe_database` để xem thông tin database:

```
Hãy mô tả database MS.TrustTest cho tôi
```

## Các Tools Có Sẵn

### 1. execute_query
Thực thi SQL query

**Ví dụ:**
```
Hãy lấy danh sách 10 users đầu tiên trong database MS.TrustTest
```

### 2. get_table_info
Lấy thông tin chi tiết về table

**Ví dụ:**
```
Hãy cho tôi xem cấu trúc của table User trong database MS.TrustTest
```

### 3. describe_database
Xem tổng quan database

**Ví dụ:**
```
Hãy mô tả tổng quan về database MS.TrustTest
```

## Xử Lý Lỗi

### Lỗi: Cannot connect to database

**Nguyên nhân:** Không thể kết nối đến database MySQL

**Giải pháp:**
1. Kiểm tra thông tin kết nối trong file `src/index.ts`
2. Đảm bảo MySQL server đang chạy
3. Kiểm tra firewall/network

### Lỗi: MCP server not found

**Nguyên nhân:** VSCode không tìm thấy MCP server

**Giải pháp:**
1. Kiểm tra đường dẫn trong settings.json
2. Đảm bảo đã build project (`npm run build`)
3. Reload VSCode

## Thông Tin Hỗ Trợ

- **Tác giả:** NVMANH with Cline
- **Ngày tạo:** 15/11/2025
- **Database:** MS.TrustTest
- **Host:** 104.199.231.104
