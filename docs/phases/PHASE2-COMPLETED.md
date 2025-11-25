# Phase 2: Authentication & Authorization - COMPLETED ✅

**Completion Date:** 13/11/2025 15:04
**Duration:** ~1 hour
**Files Created:** 26 files
**Lines of Code:** ~2,500 lines

## Overview

Phase 2 triển khai đầy đủ hệ thống Authentication & Authorization với JWT tokens, multi-login support, và role-based access control (RBAC).

## Deliverables

### 1. Entity Layer (4 files)

#### Role Entity
- Quản lý 5 roles: STUDENT, TEACHER, CLASS_MANAGER, DEPT_MANAGER, ADMIN
- Many-to-many relationship với User
- Audit fields (created_by, updated_by, timestamps)

#### User Entity
- **Multi-login support:** student_code, email, phone_number
- Password hashing với BCrypt
- Account locking mechanism (failed_login_attempts, account_locked_until)
- Soft delete pattern
- Relationships: Department, Class, Roles
- Gender enum: MALE, FEMALE, OTHER

#### Department & ClassEntity
- Quản lý tổ chức: Khoa/phòng ban và lớp hành chính
- One-to-many với User

### 2. Repository Layer (4 files)

#### Custom Query Methods
- `findByUsername(String)` - Multi-login: tìm theo student_code/email/phone
- `existsByEmail/StudentCode/PhoneNumber` - Validation
- `findByRoleName(String)` - Tìm role theo tên

### 3. Security Configuration (4 files)

#### JwtTokenProvider
- Generate access token (24h expiration)
- Generate refresh token (7 days expiration)
- Validate token
- Extract user ID from token
- HS512 algorithm with secret key

#### CustomUserDetailsService
- Load user by username (multi-login)
- Load user by ID
- Account validation (active, locked, deleted)
- Map User entity to Spring Security UserDetails

#### JwtAuthenticationFilter
- Intercept HTTP requests
- Extract JWT from Authorization header
- Validate token
- Set authentication in SecurityContext

#### SecurityConfig
- HTTP security configuration
- CORS configuration
- Role-based authorization
- Session management (STATELESS)
- Password encoder (BCrypt cost factor 12)

### 4. DTO Layer (5 files)

#### LoginRequest
- username (có thể là student_code, email, hoặc phone)
- password
- Validation annotations

#### LoginResponse
- JWT token
- Refresh token
- Token type ("Bearer")
- User information (UserDTO)

#### UserDTO
- User information without password
- Static method `from(User)` để convert từ Entity
- Includes roles, department, class info

#### RegisterRequest
- Full user registration fields
- Email validation
- Password strength validation (min 6 chars)
- Phone number format validation

#### ChangePasswordRequest
- Old password verification
- New password
- Confirm password matching

### 5. Exception Handling (5 files)

#### Custom Exceptions
1. **ResourceNotFoundException** - HTTP 404
   - User not found
   - Role not found
   
2. **DuplicateResourceException** - HTTP 409
   - Email already exists
   - Student code already exists
   - Phone number already exists

3. **InvalidCredentialsException** - HTTP 401
   - Invalid username/password
   - Account locked
   - Account inactive

4. **BadRequestException** - HTTP 400
   - Validation errors
   - Invalid input

#### GlobalExceptionHandler
- Centralized exception handling
- Standardized error response format
- Validation error details
- Security exception mapping

### 6. Service Layer (2 files)

#### AuthService
**Methods:**
- `login(LoginRequest)` → LoginResponse
  - Authenticate user
  - Update last_login_at
  - Reset failed_login_attempts
  - Generate JWT tokens

- `register(RegisterRequest)` → UserDTO
  - Validate duplicates (email, student_code, phone)
  - Hash password với BCrypt
  - Assign default STUDENT role
  - Create new user

- `validateToken(String)` → boolean
- `refreshToken(String)` → LoginResponse

#### UserService
**Methods:**
- `getAllUsers()` → List<UserDTO>
- `getUsersPage(Pageable)` → Page<UserDTO>
- `getUserById(Long)` → UserDTO
- `getUserByStudentCode(String)` → UserDTO
- `getUserByEmail(String)` → UserDTO
- `updateUser(Long, UserDTO)` → UserDTO
- `deleteUser(Long)` - Soft delete
- `changePassword(Long, ChangePasswordRequest)`
- `setUserActive(Long, boolean)` → UserDTO

### 7. Controller Layer (2 files)

#### AuthController (`/api/auth`)
**Endpoints:**
- `POST /login` - Login user
- `POST /register` - Register new user
- `GET /me` - Get current logged in user
- `POST /refresh` - Refresh JWT token
- `POST /validate` - Validate token
- `POST /logout` - Logout (client-side token removal)

#### UserController (`/api/users`)
**Endpoints:**
- `GET /` - Get all users [@PreAuthorize("hasRole('ADMIN')")]
- `GET /page` - Get users with pagination [@PreAuthorize("hasRole('ADMIN')")]
- `GET /{id}` - Get user by ID
- `GET /student-code/{code}` - Get user by student code
- `PUT /{id}` - Update user
- `DELETE /{id}` - Soft delete user [@PreAuthorize("hasRole('ADMIN')")]
- `PUT /{id}/password` - Change password
- `PUT /{id}/active` - Activate/Deactivate [@PreAuthorize("hasRole('ADMIN')")]

## Key Features Implemented

### 1. Multi-Login Support
Users có thể login bằng:
- Student code (VD: K24DTCN210)
- Email (VD: student@example.com)
- Phone number (VD: 0123456789)

### 2. JWT Authentication
- Stateless authentication
- Access token: 24 hours
- Refresh token: 7 days
- Secure with HS512 algorithm

### 3. Role-Based Access Control (RBAC)
5 levels of authorization:
- **STUDENT** - Học sinh
- **TEACHER** - Giáo viên
- **CLASS_MANAGER** - Quản lý lớp
- **DEPT_MANAGER** - Quản lý khoa/phòng ban
- **ADMIN** - Quản trị viên hệ thống

### 4. Security Features
- Password hashing with BCrypt (cost factor 12)
- Account locking after failed login attempts
- Soft delete (không xóa thật data)
- Account activation/deactivation
- Token validation and refresh

### 5. Audit Trail
- created_by, created_at
- updated_by, updated_at
- deleted_at (soft delete)
- last_login_at

## API Usage Examples

### 1. Register New User
```bash
POST /api/auth/register
Content-Type: application/json

{
  "studentCode": "K24DTCN210",
  "email": "student@example.com",
  "phoneNumber": "0123456789",
  "password": "Student@123",
  "fullName": "Nguyen Van A",
  "dateOfBirth": "2000-01-15",
  "gender": "MALE",
  "address": "Ha Noi"
}
```

### 2. Login
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "K24DTCN210",  // hoặc email hoặc phone
  "password": "Student@123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "user": {
    "id": 1,
    "studentCode": "K24DTCN210",
    "email": "student@example.com",
    "fullName": "Nguyen Van A",
    "roles": ["STUDENT"]
  }
}
```

### 3. Access Protected Endpoint
```bash
GET /api/auth/me
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### 4. Get All Users (Admin Only)
```bash
GET /api/users
Authorization: Bearer {admin-token}
```

### 5. Change Password
```bash
PUT /api/users/1/password
Authorization: Bearer {user-token}
Content-Type: application/json

{
  "oldPassword": "Student@123",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```

## Database Integration

### Sample Data (from V3 migration)
- Admin user: ADMIN / Admin@123
- Department: CNTT
- Class: K24DTCN210
- Role: All 5 roles pre-populated

### Connection
- Host: 104.199.231.104:3306
- Database: MS.TrustTest
- User: manhnv
- MCP Server: ms-trust-test-server (5 tools available)

## Security Considerations

### ✅ Implemented
- BCrypt password hashing
- JWT token expiration
- Role-based authorization
- Soft delete for data integrity
- Account locking mechanism
- CORS configuration
- SQL injection prevention (parameterized queries)

### ⚠️ To Consider for Production
- Rate limiting for login attempts
- Password complexity requirements
- Token blacklisting for logout
- HTTPS enforcement
- Security headers (X-Frame-Options, X-XSS-Protection, etc.)
- Audit logging for sensitive operations

## Testing Recommendations

### Unit Tests
- Service layer methods
- JWT token generation/validation
- Password hashing/verification
- Custom query methods

### Integration Tests
- Login flow
- Registration flow
- Token refresh
- Password change
- RBAC authorization

### API Tests
- All endpoints với different roles
- Invalid credentials handling
- Token expiration scenarios
- Validation error handling

## Next Steps

Phase 2 completed successfully! Ready for:

### Phase 3: Department & Class Management
- Department CRUD operations
- Class management
- Student enrollment
- Teacher assignments

### Phase 4: Subject & Course Management
- Subject CRUD
- Subject classes
- Student course registration
- Academic records

## Files Structure

```
backend/src/main/java/com/mstrust/exam/
├── entity/
│   ├── Role.java
│   ├── User.java
│   ├── Department.java
│   └── ClassEntity.java
├── repository/
│   ├── RoleRepository.java
│   ├── UserRepository.java
│   ├── DepartmentRepository.java
│   └── ClassRepository.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── CustomUserDetailsService.java
│   └── JwtAuthenticationFilter.java
├── config/
│   └── SecurityConfig.java
├── dto/
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── UserDTO.java
│   ├── RegisterRequest.java
│   └── ChangePasswordRequest.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── InvalidCredentialsException.java
│   ├── BadRequestException.java
│   └── GlobalExceptionHandler.java
├── service/
│   ├── AuthService.java
│   └── UserService.java
└── controller/
    ├── AuthController.java
    └── UserController.java
```

## Conclusion

Phase 2 successfully implements a complete authentication and authorization system with:
- ✅ 26 files created
- ✅ JWT-based authentication
- ✅ Multi-login support
- ✅ Role-based access control
- ✅ Comprehensive error handling
- ✅ RESTful API design
- ✅ Security best practices

The system is now ready for Phase 3 development!

---
**Author:** K24DTCN210-NVMANH  
**Date:** 13/11/2025 15:04
