# 🔍 Hướng Dẫn Truy Cập Swagger UI Qua Domain

## 📋 Phân Tích Hiện Trạng

### Cấu hình Backend:
- **Context-path:** `/api`
- **Swagger UI path:** `/swagger-ui.html`
- **URL đầy đủ trong backend:** `/api/swagger-ui.html` hoặc `/api/swagger-ui/index.html`

### Cấu hình Nginx hiện tại:
- **Domain:** `ttapi.manhhao.com`
- **Proxy pass:** `http://localhost:8181/api` (đã có `/api`)

## ⚠️ Vấn Đề

Với cấu hình nginx hiện tại, có 2 cách truy cập:

### Cách 1: Truy cập trực tiếp (KHÔNG có `/api` trong URL)
```
https://ttapi.manhhao.com/swagger-ui.html
→ Nginx proxy đến: localhost:8181/api/swagger-ui.html ✅
→ Backend nhận: /api/swagger-ui.html ✅
```

### Cách 2: Truy cập với `/api` trong URL (SAI)
```
https://ttapi.manhhao.com/api/swagger-ui.html
→ Nginx proxy đến: localhost:8181/api/api/swagger-ui.html ❌
→ Backend nhận: /api/api/swagger-ui.html ❌ (LỖI - duplicate /api)
```

## ✅ URL Đúng Để Truy Cập

Với cấu hình nginx hiện tại, cụ nên truy cập:

```
https://ttapi.manhhao.com/swagger-ui.html
```

Hoặc:

```
https://ttapi.manhhao.com/swagger-ui/index.html
```

**KHÔNG** dùng:

```
❌ https://ttapi.manhhao.com/api/swagger-ui.html  (sẽ bị duplicate /api)
```

## 🔧 Nếu Cụ Muốn Truy Cập Qua `/api/swagger-ui.html`

Nếu cụ muốn URL là `https://ttapi.manhhao.com/api/swagger-ui.html` (giữ nguyên `/api` trong URL), cần sửa nginx config:

### Option 1: Sửa nginx để strip `/api` prefix

```nginx
location /api {
    # Strip /api prefix và proxy đến backend
    rewrite ^/api(.*)$ $1 break;
    proxy_pass http://localhost:8181/api;
    # ... các config khác
}
```

### Option 2: Sửa nginx để giữ nguyên path (KHUYẾN NGHỊ)

```nginx
location / {
    # Proxy đến backend mà KHÔNG thêm /api vào proxy_pass
    proxy_pass http://localhost:8181;
    # ... các config khác
}
```

Với cách này:
- User truy cập: `https://ttapi.manhhao.com/api/swagger-ui.html`
- Nginx proxy đến: `localhost:8181/api/swagger-ui.html` ✅
- Backend nhận: `/api/swagger-ui.html` ✅

## 🚀 Giải Pháp Khuyến Nghị

### Sửa nginx config để hỗ trợ cả 2 cách:

```nginx
server {
    listen 80;
    server_name ttapi.manhhao.com;

    map $http_upgrade $connection_upgrade {
        default upgrade;
        ''      close;
    }

    # Location cho các path bắt đầu bằng /api
    location /api {
        # Proxy đến backend, giữ nguyên path (không thêm /api nữa)
        proxy_pass http://localhost:8181;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Location cho root path (fallback)
    location / {
        # Proxy đến backend với /api prefix
        proxy_pass http://localhost:8181/api;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # SignalR endpoint
    location /NotificationHub {
        proxy_pass http://localhost:6686;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_cache off;
        proxy_http_version 1.1;
        proxy_buffering off;
        proxy_read_timeout 100s;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Với config này, cụ có thể truy cập:

1. ✅ `https://ttapi.manhhao.com/api/swagger-ui.html`
2. ✅ `https://ttapi.manhhao.com/api/swagger-ui/index.html`
3. ✅ `https://ttapi.manhhao.com/swagger-ui.html`
4. ✅ `https://ttapi.manhhao.com/exams`

## 📝 Lưu Ý Về HTTPS

Nếu cụ dùng HTTPS (`https://ttapi.manhhao.com`), cần:

1. **Cấu hình SSL certificate** trong nginx:
```nginx
server {
    listen 443 ssl;
    server_name ttapi.manhhao.com;
    
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    
    # ... các config khác
}
```

2. **Redirect HTTP sang HTTPS** (nếu muốn):
```nginx
server {
    listen 80;
    server_name ttapi.manhhao.com;
    return 301 https://$server_name$request_uri;
}
```

## ✅ Tóm Tắt

**Câu trả lời:** CÓ, cụ có thể xem Swagger UI online qua:

1. **Với config nginx hiện tại:**
   - ✅ `https://ttapi.manhhao.com/swagger-ui.html`
   - ❌ `https://ttapi.manhhao.com/api/swagger-ui.html` (sẽ bị lỗi duplicate /api)

2. **Nếu sửa nginx config như khuyến nghị:**
   - ✅ `https://ttapi.manhhao.com/api/swagger-ui.html`
   - ✅ `https://ttapi.manhhao.com/swagger-ui.html`

**Cụ muốn em sửa nginx config để hỗ trợ cả 2 cách không ạ?**

