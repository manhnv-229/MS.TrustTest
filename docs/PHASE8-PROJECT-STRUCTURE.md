# Phase 8: Project Structure Organization

**Purpose:** Tài liệu này định nghĩa cấu trúc thư mục cho Phase 8 - Exam Taking UI

---

## 📁 Directory Structure

```
client-javafx/
├── src/main/
│   ├── java/com/mstrust/client/
│   │   ├── exam/                           # 🆕 Phase 8 Module
│   │   │   ├── api/                        # API Clients
│   │   │   │   └── ExamApiClient.java     ✅
│   │   │   │
│   │   │   ├── controller/                # JavaFX Controllers
│   │   │   │   ├── ExamListController.java           # 📝 Phase 8.2
│   │   │   │   ├── ExamTakingController.java         # 📝 Phase 8.3
│   │   │   │   ├── SubmitConfirmController.java      # 📝 Phase 8.5
│   │   │   │   └── ExamResultController.java         # 📝 Phase 8.5
│   │   │   │
│   │   │   ├── component/                 # UI Components
│   │   │   │   ├── TimerComponent.java               # 📝 Phase 8.3
│   │   │   │   ├── QuestionPaletteComponent.java     # 📝 Phase 8.3
│   │   │   │   ├── QuestionDisplayComponent.java     # 📝 Phase 8.3
│   │   │   │   └── AnswerInputFactory.java           # 📝 Phase 8.3
│   │   │   │
│   │   │   ├── dto/                        # Data Transfer Objects
│   │   │   │   ├── QuestionType.java      ✅
│   │   │   │   ├── ExamInfoDTO.java       ✅
│   │   │   │   └── QuestionDTO.java       ✅
│   │   │   │
│   │   │   ├── model/                      # Business Models
│   │   │   │   └── ExamSession.java       ✅
│   │   │   │
│   │   │   ├── service/                    # Business Services
│   │   │   │   ├── AutoSaveService.java              # 📝 Phase 8.4
│   │   │   │   ├── NetworkMonitor.java               # 📝 Phase 8.4
│   │   │   │   ├── ConnectionRecoveryService.java    # 📝 Phase 8.4
│   │   │   │   └── LocalStorageService.java          # 📝 Phase 8.4
│   │   │   │
│   │   │   └── util/                       # Utilities
│   │   │       └── TimeFormatter.java      ✅
│   │   │
│   │   ├── monitoring/                     # Phase 6B Module (existing)
│   │   ├── api/                            # Shared API (existing)
│   │   ├── config/                         # Configuration (existing)
│   │   └── util/                           # Shared Utils (existing)
│   │
│   └── resources/
│       ├── view/                           # 🆕 FXML Views
│       │   ├── exam-list.fxml             # 📝 Phase 8.2
│       │   ├── exam-taking.fxml           # 📝 Phase 8.3
│       │   ├── submit-confirm.fxml        # 📝 Phase 8.5
│       │   └── exam-result.fxml           # 📝 Phase 8.5
│       │
│       ├── css/                            # 🆕 Stylesheets
│       │   ├── exam-common.css            # 📝 Phase 8.6
│       │   └── exam-taking.css            # 📝 Phase 8.6
│       │
│       └── config.properties              # Existing
```

---

## 📦 Package Organization

### 1. `com.mstrust.client.exam.api`
**Purpose:** API communication layer  
**Files:** ExamApiClient.java  
**Responsibility:** HTTP requests to backend exam endpoints

### 2. `com.mstrust.client.exam.controller`
**Purpose:** JavaFX controllers  
**Files:** 4 controllers  
**Responsibility:** Handle UI events, bind data to views

### 3. `com.mstrust.client.exam.component`
**Purpose:** Reusable UI components  
**Files:** 4 components  
**Responsibility:** Self-contained UI widgets (Timer, Palette, etc.)

### 4. `com.mstrust.client.exam.dto`
**Purpose:** Data transfer objects  
**Files:** 3 DTOs  
**Responsibility:** Data structures for API communication

### 5. `com.mstrust.client.exam.model`
**Purpose:** Business logic models  
**Files:** ExamSession.java  
**Responsibility:** Application state management

### 6. `com.mstrust.client.exam.service`
**Purpose:** Business services  
**Files:** 4 services  
**Responsibility:** Auto-save, network monitoring, recovery logic

### 7. `com.mstrust.client.exam.util`
**Purpose:** Utility functions  
**Files:** TimeFormatter.java  
**Responsibility:** Helper methods for formatting, calculation

---

## 🎯 Naming Conventions

### Controllers
- Pattern: `{Feature}Controller.java`
- Examples: `ExamListController`, `ExamTakingController`
- Location: `controller/` package

### Components
- Pattern: `{Component}Component.java`
- Examples: `TimerComponent`, `QuestionPaletteComponent`
- Location: `component/` package

### Services
- Pattern: `{Function}Service.java`
- Examples: `AutoSaveService`, `NetworkMonitor`
- Location: `service/` package

### FXML Files
- Pattern: `{feature}-{view}.fxml`
- Examples: `exam-list.fxml`, `exam-taking.fxml`
- Location: `resources/view/`

### CSS Files
- Pattern: `{module}-{type}.css`
- Examples: `exam-common.css`, `exam-taking.css`
- Location: `resources/css/`

---

## ✅ Current Status (Phase 8.1 Complete)

```
✅ api/ExamApiClient.java
✅ dto/QuestionType.java
✅ dto/ExamInfoDTO.java
✅ dto/QuestionDTO.java
✅ model/ExamSession.java
✅ util/TimeFormatter.java
```

**Total:** 6 files created

---

## 📝 Next: Phase 8.2 Files

```
📝 controller/ExamListController.java
📝 resources/view/exam-list.fxml
```

---

## 🔒 Best Practices

1. **One Responsibility:** Mỗi class chỉ làm một việc
2. **Clear Naming:** Tên phải rõ ràng, mô tả chức năng
3. **Package Cohesion:** Files cùng chức năng ở cùng package
4. **Avoid Circular Deps:** Controller → Service → API
5. **Resource Organization:** FXML và CSS tách riêng khỏi code

---

**Created:** 23/11/2025 12:03  
**By:** K24DTCN210-NVMANH
