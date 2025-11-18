# MySQL TrustTest MCP Server

MCP Server để truy cập MySQL database MS.TrustTest

## Thông tin Database

- **Host**: 104.199.231.104
- **Port**: 3306
- **Database**: MS.TrustTest
- **User**: nvmanh

## Cài đặt

```bash
npm install
npm run build
```

## Cấu hình trong VSCode

Thêm vào file `settings.json` của VSCode:

```json
{
  "mcp.servers": {
    "mysql-trusttest": {
      "command": "node",
      "args": ["C:\\Users\\manhn\\OneDrive\\Documents\\Cline\\MCP\\mysql-trusttest\\build\\index.js"]
    }
  }
}
```

## Tools

### execute_query
Thực thi SQL query trên MS.TrustTest database

**Parameters:**
- `query` (string, required): SQL query cần thực thi
- `params` (array, optional): Parameters cho prepared statement

**Example:**
```json
{
  "query": "SELECT * FROM users WHERE id = ?",
  "params": ["1"]
}
```

### get_table_info
Lấy thông tin chi tiết về table (structure, indexes, constraints)

**Parameters:**
- `tableName` (string, required): Tên table cần lấy thông tin

**Example:**
```json
{
  "tableName": "users"
}
```

### describe_database
Mô tả tổng quan về database schema và relationships

**Parameters:** None

## Resources

- `mysql://tables` - Danh sách tất cả tables
- `mysql://views` - Danh sách tất cả views
- `mysql://procedures` - Danh sách stored procedures
- `mysql://table/{tableName}` - Thông tin cấu trúc và sample data của table
- `mysql://table/{tableName}/schema` - Chi tiết cấu trúc của table

## Tác giả

NVMANH with Cline - 15/11/2025
