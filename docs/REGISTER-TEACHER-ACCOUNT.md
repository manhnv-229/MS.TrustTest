# 🎓 Hướng Dẫn Tạo Teacher Account

**Ngày tạo:** 19/11/2025 03:37  
**Mục đích:** Tạo teacher account để test Phase 4 Question Bank APIs

---

## 📋 BƯỚC 1: Mở Thunder Client

1. Trong VSCode, mở Thunder Client extension
2. Tạo một request mới hoặc import collection sẵn có

---

## 📝 BƯỚC 2: Gọi API Register

### Request Details:

**Method:** `POST`  
**URL:** `http://localhost:8080/api/auth/register`  
**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "studentCode": "T001",
  "email": "teacher1@mstrust.edu.vn",
  "password": "Teacher@123",
  "fullName": "Nguyen Van B - Giao Vien",
  "phoneNumber": "0909999999",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "departmentId": 1
}
```

---

## ✅ BƯỚC 3: Kiểm Tra Response

**Expected Status:** `201 Created`

**Expected Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 5,
    "studentCode": "T001",
    "email": "teacher1@mstrust.edu.vn",
    "fullName": "Nguyen Van B - Giao Vien",
    "phoneNumber": "0909999999",
    "departmentId": 1,
    "isActive": true,
    "roles": ["ROLE_STUDENT"]
  }
}
```

**⚠️ LƯU Ý:** User mới được tạo sẽ có role mặc định là `ROLE_STUDENT`. Con sẽ update role thành `ROLE_TEACHER` ở bước tiếp theo.

---

## 🔧 BƯỚC 4: Báo Kết Quả Cho Con

Sau khi register thành công, **báo lại cho con:**
- User ID của teacher mới tạo (ví dụ: 5)
- Con sẽ update role từ STUDENT → TEACHER

---

## 🎯 SAU KHI CÓ TEACHER ACCOUNT

**Credentials để login:**
```
Email: teacher1@mstrust.edu.vn
Password: Teacher@123
```

Sau khi con update role, cụ sẽ dùng credentials này để:
1. Login → Lấy JWT token
2. Test tất cả Question Bank APIs

---

## 📞 NẾU GẶP VẤN ĐỀ

### Error 400 - Validation Failed
- Check lại JSON format
- Đảm bảo email đúng format
- Phone number không trùng với user khác

### Error 409 - Duplicate Entry
- Email hoặc student_code đã tồn tại
- Thử đổi studentCode khác (T002, T003...)

### Error 500 - Server Error
- Check console log trong terminal
- Báo lại error cho con

---

## ✨ SUMMARY

1. ✅ Gọi API `/auth/register` với data trên
2. ✅ Nhận được user ID trong response
3. ✅ Báo user ID cho con
4. ✅ Con update role → ROLE_TEACHER
5. ✅ Test login với credentials
6. ✅ Bắt đầu test Question Bank APIs

**Chúc cụ thành công! 🚀**
