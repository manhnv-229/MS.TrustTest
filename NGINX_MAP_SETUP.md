# 🔧 Hướng Dẫn Thêm Map Directive Vào Nginx

## ⚠️ Lỗi Gặp Phải

Nếu cụ gặp lỗi:
```
"map" directive is not allowed here
```

Điều này xảy ra vì `map` directive phải được đặt trong `http` block của file `nginx.conf`, KHÔNG được đặt trong `server` block.

## ✅ Giải Pháp

### Cách 1: Thêm Map Vào nginx.conf (Khuyến nghị - hỗ trợ WebSocket tốt hơn)

1. Mở file nginx.conf:
```bash
sudo nano /etc/nginx/nginx.conf
```

2. Tìm phần `http {` và thêm đoạn sau NGAY SAU dòng `http {`:

```nginx
http {
    # Map cho WebSocket upgrade
    map $http_upgrade $connection_upgrade {
        default upgrade;
        ''      close;
    }
    
    # ... các config khác (include, types, etc.)
}
```

3. Sau đó trong file server config (`nginx-ttapi-manhhao-com.conf`), cụ có thể dùng:
```nginx
proxy_set_header Connection $connection_upgrade;
```

4. Kiểm tra và reload nginx:
```bash
sudo nginx -t
sudo systemctl reload nginx
```

### Cách 2: Dùng Hardcode (Đơn giản - không cần map)

Nếu cụ không muốn thêm map, có thể dùng hardcode như đã sửa trong file `nginx-ttapi-manhhao-com.conf`:

```nginx
proxy_set_header Connection "upgrade";
```

Cách này đơn giản hơn nhưng không linh hoạt bằng cách dùng map.

## 📝 Tóm Tắt

- **Cách 1:** Thêm map vào nginx.conf → Hỗ trợ WebSocket tốt hơn
- **Cách 2:** Dùng hardcode "upgrade" → Đơn giản, không cần sửa nginx.conf

**File `nginx-ttapi-manhhao-com.conf` hiện tại đã được sửa để dùng cách 2 (hardcode), nên cụ có thể dùng ngay mà không cần thêm map vào nginx.conf.**

