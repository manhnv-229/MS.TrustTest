# Hướng Dẫn Deploy Backend MS.TrustTest Lên Ubuntu Server

Tài liệu này hướng dẫn chi tiết cách deploy ứng dụng Backend lên server Ubuntu sử dụng Docker và Docker Compose.

## 1. Chuẩn bị Server (Thực hiện một lần đầu)

Đảm bảo server Ubuntu của cụ đã được cài đặt Docker và Docker Compose.

### Cài đặt Docker
```bash
# Update package index
sudo apt-get update

# Cài đặt các gói cần thiết
sudo apt-get install ca-certificates curl gnupg

# Thêm GPG key của Docker
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Thêm repository
echo \
  "deb [arch="$(dpkg --print-architecture)" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  "$(. /etc/os-release && echo "$VERSION_CODENAME")" stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Cài đặt Docker Engine
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Kiểm tra cài đặt
sudo docker run hello-world
```

## 2. Deploy Ứng Dụng

### Bước 1: Copy source code lên server
Cụ có thể dùng Git để pull code về, hoặc copy folder dự án lên server (dùng WinSCP hoặc scp).

Giả sử cụ để code ở thư mục `~/MS.TrustTest`

### Bước 2: Build và Run
Tại thư mục chứa file `docker-compose.yml` (thư mục gốc của dự án):

```bash
# Chạy lệnh build và start container (chạy ngầm -d)
sudo docker compose up -d --build
```

*Lưu ý: Lần đầu chạy sẽ mất vài phút để tải maven dependencies và build project.*

### Bước 3: Kiểm tra Logs
Để xem ứng dụng đang chạy thế nào, có lỗi gì không:

```bash
# Xem logs thời gian thực
sudo docker compose logs -f backend
```

### Bước 4: Dừng ứng dụng (nếu cần)
```bash
sudo docker compose down
```

## 3. Cập nhật Code Mới

Khi cụ có code mới cần update lên server:

1.  Copy code mới lên server (git pull hoặc copy đè).
2.  Chạy lại lệnh sau để rebuild và restart container:
    ```bash
    sudo docker compose up -d --build
    ```
    *Docker sẽ tự động detect thay đổi, build lại image mới và restart container.*

## 4. Cấu hình Database & FTP

Thông tin cấu hình Database và FTP đã được thiết lập sẵn trong file `docker-compose.yml`. Nếu cụ cần thay đổi (ví dụ đổi pass DB), cụ chỉ cần sửa file này trực tiếp trên server:

```bash
nano docker-compose.yml
```
Sửa xong nhấn `Ctrl+X`, chọn `Y` để lưu, sau đó chạy lại `sudo docker compose up -d` để áp dụng thay đổi.
