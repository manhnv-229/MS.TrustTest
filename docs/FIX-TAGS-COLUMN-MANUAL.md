# Fix Tags Column - Manual SQL Guide

## Problem
Column `questions.tags` is JSON type but application sends plain string → Invalid JSON error

## Solution: Run SQL Manually

### Step 1: Connect to MySQL
```bash
mysql -u root -p
# Enter password when prompted
```

### Step 2: Select Database
```sql
USE ms_trusttest;
```

### Step 3: Verify Current Type
```sql
DESCRIBE questions;
-- Check "tags" field → should show "json"
```

### Step 4: Run ALTER TABLE
```sql
ALTER TABLE questions 
MODIFY COLUMN tags VARCHAR(500) NULL;
```

### Step 5: Verify Change
```sql
DESCRIBE questions;
-- Check "tags" field → should now show "varchar(500)"
```

### Step 6: Restart Spring Boot Server
```bash
cd backend && mvn spring-boot:run
```

## Alternative: MySQL Workbench

1. Open MySQL Workbench
2. Connect to `localhost:3306`
3. Select database: `ms_trusttest`
4. Run query:
```sql
ALTER TABLE questions 
MODIFY COLUMN tags VARCHAR(500) NULL;
```
5. Click Execute (⚡ icon)
6. Verify: Right-click `questions` table → Table Inspector → Columns

## Troubleshooting

**Error: "Table is read-only"**
- Check user permissions
- Run as root user

**Error: "Table doesn't exist"**
- Verify database name: `SHOW DATABASES;`
- Use correct database: `USE ms_trusttest;`

**Change not taking effect:**
- Make sure you're connected to correct database
- Check if you have multiple MySQL instances running

---
CreatedBy: K24DTCN210-NVMANH (19/11/2025 13:56)
