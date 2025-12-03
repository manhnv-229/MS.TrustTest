#Scenario 1: Làm lần đầu
Student click "Bắt đầu làm bài"
```
→ Không có submission nào
→ Tạo submission mới: attemptNumber = 1, status = IN_PROGRESS
→ Student làm bài...
```
#Scenario 2: Tiếp tục bài dở
Student đang làm bài (IN_PROGRESS)
```
→ Thoát app, vào lại
→ Click "Tiếp tục làm bài"
→ Return existing submission (cùng attemptNumber, cùng answers)
→ Student tiếp tục từ chỗ cũ
```
#Scenario 3: Làm lần thứ 2
Student đã nộp bài lần 1 (SUBMITTED)
```
→ Click "Bắt đầu làm bài" lần 2
→ Tạo submission mới: attemptNumber = 2, status = IN_PROGRESS
→ Bắt đầu từ đầu, không có answers cũ
```
