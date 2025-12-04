# 🔧 Hướng Dẫn Sửa Lỗi Domain ttapi.manhhao.com

## 📋 Vấn đề đã phát hiện

1. **Port mismatch:** Backend chạy port 8080 nhưng Docker expose port 80
2. **Context-path không được xử lý:** Nginx không thêm `/api` prefix khi proxy

## ✅ Đã sửa

### 1. docker-compose.yml
- ✅ Đã thêm `SERVER_PORT=80` để override port trong container

### 2. Cấu hình Nginx
- ✅ Đã tạo file `nginx-ttapi-manhhao-com.conf` với cấu hình đúng

## 🚀 Các bước triển khai

### Bước 1: Cập nhật docker-compose.yml trên server

File `backend/docker-compose.yml` đã được sửa. Cụ cần:
1. Copy file mới lên server (hoặc git pull)
2. Restart container:
```bash
cd ~/MS.TrustTest/backend
sudo docker compose down
sudo docker compose up -d --build
```

### Bước 2: Cập nhật cấu hình Nginx

**Option A: Sửa file hiện tại**

```bash
sudo nano /etc/nginx/sites-available/ttapi.manhhao.com
```

Thay thế nội dung bằng file `nginx-ttapi-manhhao-com.conf` đã tạo.

**Option B: Copy file mới**

Nếu cụ đã copy file `nginx-ttapi-manhhao-com.conf` lên server:
```bash
sudo cp nginx-ttapi-manhhao-com.conf /etc/nginx/sites-available/ttapi.manhhao.com
```

### Bước 3: Kiểm tra cấu hình Nginx

```bash
# Kiểm tra syntax
sudo nginx -t

# Nếu OK, restart nginx
sudo systemctl restart nginx
```

### Bước 4: Kiểm tra map $connection_upgrade

Nếu cụ gặp lỗi `[Unknown "$connection_upgrade"] Variable`, cần thêm vào `/etc/nginx/nginx.conf`:

```bash
sudo nano /etc/nginx/nginx.conf
```

Thêm vào **đầu scope http** (sau dòng `http {`):

```nginx
http {
    # Map cho WebSocket upgrade
    map $http_upgrade $connection_upgrade {
        default upgrade;
        ''      close;
    }
    
    # ... các config khác
}
```

Sau đó kiểm tra và restart:
```bash
sudo nginx -t
sudo systemctl restart nginx
```

## 🧪 Kiểm tra

### 1. Kiểm tra container
```bash
# Xem container có chạy không
sudo docker ps | grep ms-trust-backend

# Xem logs
sudo docker logs ms-trust-backend

# Kiểm tra port
sudo netstat -tulpn | grep 8181
```

### 2. Test API từ server
```bash
# Test local
curl http://localhost:8181/api/exams

# Test qua domain
curl http://ttapi.manhhao.com/exams
```

### 3. Test từ browser/Postman
- `http://ttapi.manhhao.com/exams`
- `http://ttapi.manhhao.com/swagger-ui.html`

## 📝 Lưu ý quan trọng

### Về Context-Path
- Backend có `context-path: /api` trong `application.yml`
- Nginx proxy đến `http://localhost:8181/api` (có `/api`)
- User truy cập `ttapi.manhhao.com/exams` → Backend nhận `/api/exams` ✅

### Về Port
- Container expose port **80** (đã set `SERVER_PORT=80`)
- Docker map **8181:80** (host port 8181 → container port 80)
- Nginx proxy đến **localhost:8181** ✅

## 🐛 Troubleshooting

### Nếu vẫn không truy cập được:

1. **Kiểm tra firewall:**
```bash
sudo ufw status
# Nếu cần mở port 80
sudo ufw allow 80/tcp
```

2. **Kiểm tra DNS:**
```bash
nslookup ttapi.manhhao.com
# Phải trỏ về IP server của cụ
```

3. **Kiểm tra container logs:**
```bash
sudo docker logs -f ms-trust-backend
```

4. **Kiểm tra nginx error logs:**
```bash
sudo tail -f /var/log/nginx/error.log
```

5. **Test trực tiếp từ server:**
```bash
# Test container
curl http://localhost:8181/api/exams

# Test nginx
curl -H "Host: ttapi.manhhao.com" http://localhost/exams
```

## ✅ Checklist

- [ ] docker-compose.yml đã có `SERVER_PORT=80`
- [ ] Container đang chạy và listen port 8181
- [ ] Nginx config đã proxy đến `localhost:8181/api`
- [ ] Map `$connection_upgrade` đã có trong nginx.conf
- [ ] Nginx đã restart sau khi sửa config
- [ ] Firewall đã mở port 80
- [ ] DNS đã trỏ về đúng IP server

---

**Tóm tắt:** Đã sửa 2 vấn đề chính:
1. ✅ Thêm `SERVER_PORT=80` vào docker-compose.yml
2. ✅ Sửa nginx config để proxy đến `/api` path

