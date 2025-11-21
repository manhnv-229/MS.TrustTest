# Hướng Dẫn Setup Biến Trong Thunder Client

## Vấn Đề
Thunder Client không tự động thay thế biến `{{exam_id}}` và `{{submission_id}}` từ response. Cần phải set thủ công.

## Cách Setup

### 1. Mở Thunder Client Environment Settings
1. Mở Thunder Client trong VSCode (biểu tượng sét)
2. Click vào tab **"Env"** (Environment)
3. Click **"New Environment"** hoặc select environment hiện có

### 2. Tạo Environment Variables

Tạo environment mới tên **"MS TrustTest Local"** với các biến sau:

```json
{
  "name": "MS TrustTest Local",
  "variables": [
    {
      "name": "base_url",
      "value": "http://localhost:8080/api"
    },
    {
      "name": "student_token",
      "value": ""
    },
    {
      "name": "teacher_token",
      "value": ""
    },
    {
      "name": "exam_id",
      "value": "1"
    },
    {
      "name": "submission_id",
      "value": "1"
    }
  ]
}
```

### 3. Workflow Testing

#### Step 1: Login và Copy Token

**3.1. Login as Student:**
```
POST {{base_url}}/auth/login
Body: {
  "username": "student@test.com",
  "password": "password123"
}
```

- Copy `token` từ response
- Paste vào environment variable `student_token`

**3.2. Login as Teacher:**
```
POST {{base_url}}/auth/login
Body: {
  "username": "teacher1@mstrust.edu.vn",
  "password": "Teacher@123"
}
```

- Copy `token` từ response
- Paste vào environment variable `teacher_token`

#### Step 2: Get Exam ID

**Get Available Exams:**
```
GET {{base_url}}/exam-taking/available
Authorization: Bearer {{student_token}}
```

- Xem response, tìm `id` của exam đầu tiên trong `content` array
- Copy giá trị này (ví dụ: `1`)
- Paste vào environment variable `exam_id`

#### Step 3: Start Exam để lấy Submission ID

**Start Exam:**
```
POST {{base_url}}/exam-taking/start/{{exam_id}}
Authorization: Bearer {{student_token}}
```

- Copy `submissionId` từ response
- Paste vào environment variable `submission_id`

#### Step 4: Tiếp tục workflow

Bây giờ tất cả requests khác sẽ tự động sử dụng `{{exam_id}}` và `{{submission_id}}` từ environment.

## URLs Đã Được Cập Nhật

Tất cả URLs trong collection đã sử dụng variables:

### Student Flow:
- `GET {{base_url}}/exam-taking/available`
- `GET {{base_url}}/exam-taking/check-eligibility/{{exam_id}}`
- `POST {{base_url}}/exam-taking/start/{{exam_id}}`
- `GET {{base_url}}/exam-taking/questions/{{submission_id}}`
- `POST {{base_url}}/exam-taking/save-answer/{{submission_id}}`
- `POST {{base_url}}/exam-taking/submit/{{submission_id}}`
- `GET {{base_url}}/exam-taking/result/{{submission_id}}`

### Teacher Grading Flow:
- `GET {{base_url}}/grading/submissions?examId={{exam_id}}&...`
- `GET {{base_url}}/grading/submissions/{{submission_id}}/detail`
- `POST {{base_url}}/grading/finalize/{{submission_id}}`

## Lưu Ý Quan Trọng

1. **Không cần tự gán mỗi lần:** Sau khi set variables một lần, chúng sẽ được lưu trong environment
2. **Chỉ cần update khi cần:** Nếu muốn test với exam khác, chỉ cần update `exam_id`
3. **Token có thời hạn:** Nếu token expired, chỉ cần login lại và update token mới

## Quick Setup Script

Để nhanh chóng test, làm theo thứ tự:

```bash
# 1. Login Student → Copy token → Set student_token
# 2. Login Teacher → Copy token → Set teacher_token
# 3. Get Available Exams → Copy first exam id → Set exam_id = 1
# 4. Start Exam → Copy submissionId → Set submission_id = 1
# 5. Tiếp tục workflow bình thường
```

## Troubleshooting

**Lỗi:** `Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: "{{exam_id}}"`

**Nguyên nhân:** Biến `{{exam_id}}` chưa được set trong Environment

**Giải pháp:** 
1. Mở Thunder Client → Tab "Env"
2. Select environment đang dùng
3. Set giá trị cho `exam_id` (ví dụ: `1`)
4. Save environment
5. Chạy lại request
