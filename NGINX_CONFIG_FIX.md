# Cấu hình Nginx cho Domain ttapi.manhhao.com

## Vấn đề hiện tại

1. Backend có `context-path: /api` nhưng nginx không xử lý
2. Port mismatch giữa application.yml (8080) và Docker (80)

## Giải pháp

### Bước 1: Sửa docker-compose.yml

Thêm biến môi trường `SERVER_PORT=80` để override port từ application.yml:

```yaml
environment:
  - SERVER_PORT=80  # Override port 8080 thành 80
  # ... các biến khác
```

### Bước 2: Cập nhật cấu hình Nginx

Có 2 cách:

#### Cách 1: Rewrite URL trong nginx (KHUYẾN NGHỊ)

Cách này giữ nguyên context-path `/api` trong backend và rewrite trong nginx:

```nginx
server {
    listen 80;
    server_name ttapi.manhhao.com;

    # Map cho WebSocket upgrade
    map $http_upgrade $connection_upgrade {
        default upgrade;
        ''      close;
    }

    location / {
        # Rewrite: /exams -> /api/exams
        rewrite ^/(.*)$ /api/$1 break;
        
        proxy_pass http://localhost:8181;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Configure the SignalR Endpoint
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

**Lưu ý:** Với cách này:
- User truy cập: `ttapi.manhhao.com/exams`
- Nginx rewrite: `/api/exams`
- Backend nhận: `/api/exams` ✅

#### Cách 2: Proxy trực tiếp đến /api và ẩn trong URL

```nginx
server {
    listen 80;
    server_name ttapi.manhhao.com;

    map $http_upgrade $connection_upgrade {
        default upgrade;
        ''      close;
    }

    location / {
        # Proxy trực tiếp đến /api trong backend
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

**Lưu ý:** Với cách này:
- User truy cập: `ttapi.manhhao.com/exams`
- Nginx proxy đến: `localhost:8181/api/exams`
- Backend nhận: `/api/exams` ✅

## So sánh 2 cách

| Tiêu chí | Cách 1 (Rewrite) | Cách 2 (Proxy /api) |
|----------|------------------|---------------------|
| URL user thấy | `ttapi.manhhao.com/exams` | `ttapi.manhhao.com/exams` |
| URL backend nhận | `/api/exams` | `/api/exams` |
| Độ phức tạp | Cao hơn (cần rewrite) | Đơn giản hơn |
| Xử lý sub-path | Cần cẩn thận | Tự động |

**KHUYẾN NGHỊ: Dùng Cách 2 (proxy_pass với /api)** vì đơn giản và ít lỗi hơn.

## Kiểm tra sau khi cấu hình

1. Restart nginx:
```bash
sudo nginx -t  # Kiểm tra config
sudo systemctl restart nginx
```

2. Kiểm tra container:
```bash
sudo docker ps  # Xem container có chạy không
sudo docker logs ms-trust-backend  # Xem logs
```

3. Test API:
```bash
curl http://ttapi.manhhao.com/exams
```

## Nếu vẫn không được

Kiểm tra:
- Container có đang chạy không: `sudo docker ps`
- Port 8181 có listen không: `netstat -tulpn | grep 8181`
- Firewall có block không: `sudo ufw status`
- Nginx có đọc được config không: `sudo nginx -t`

