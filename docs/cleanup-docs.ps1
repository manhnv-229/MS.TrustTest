# Script don dep thu muc docs - Chi giu file quan trong
# Created: 25/11/2025
# Author: K24DTCN210-NVMANH

Write-Host "=== BAT DAU DON DEP THU MUC DOCS ===" -ForegroundColor Cyan

# Danh sach file CAN GIU LAI
$keepFiles = @(
    # File tong hop chinh
    "phases-summary-REDEFINED.md",
    
    # Phase completion chinh (1 file cho moi phase)
    "PHASE1-COMPLETED.md",
    "PHASE2-COMPLETED.md",
    "PHASE3-STEP6-COMPLETION-REPORT.md",
    "PHASE4-COMPLETE-SUMMARY.md",
    "PHASE5-COMPLETE-SUMMARY.md",
    "PHASE5B-WEBSOCKET-ENHANCED-APIS.md",
    "PHASE6A-MONITORING-BACKEND-COMPLETE.md",
    "PHASE6B-JAVAFX-CLIENT-PROGRESS.md",
    "PHASE7-GRADING-SYSTEM-COMPLETION-FINAL.md",
    "PHASE8-PROGRESS-UPDATED.md",
    "PHASE8-TECHNICAL-DECISIONS.md",
    "PHASE8-PROJECT-STRUCTURE.md",
    
    # Thunder Client collections (API testing)
    "thunder-client-collection-FULL.json",
    "thunder-client-complete-workflow.json",
    "thunder-client-exam-workflow-FINAL.json",
    "thunder-client-phase4-exam-management.json",
    "thunder-client-phase4-question-bank.json",
    "thunder-client-phase5-exam-taking.json",
    "thunder-client-phase5-grading.json",
    "thunder-client-phase5b-websocket.json",
    "thunder-client-phase7-grading.json",
    "thunder-client-phase8-exam-taking-full.json",
    "THUNDER-CLIENT-VARIABLE-SETUP.md",
    
    # Testing guides chinh
    "PHASE5B-TESTING-GUIDE.md",
    "PHASE7-TESTING-GUIDE.md",
    "PHASE8-API-TESTING-GUIDE.md",
    
    # Setup guides
    "REGISTER-TEACHER-ACCOUNT.md",
    "SETUP-TEST-DATA-PHASE5B.md",
    
    # Archive
    "ARCHIVE-OLD-DOCS.md"
)

# Lay tat ca file .md trong thu muc docs
$allFiles = Get-ChildItem -Path "." -Filter "*.md"

Write-Host ""
Write-Host "Tong so file .md: $($allFiles.Count)" -ForegroundColor Yellow

# Dem file se xoa
$filesToDelete = $allFiles | Where-Object { $keepFiles -notcontains $_.Name }
Write-Host "File se XOA: $($filesToDelete.Count)" -ForegroundColor Red
Write-Host "File se GIU: $($keepFiles.Count)" -ForegroundColor Green

# Hien thi danh sach file se xoa
Write-Host ""
Write-Host "Danh sach file se XOA:" -ForegroundColor Yellow
foreach ($file in $filesToDelete) {
    Write-Host "   - $($file.Name)" -ForegroundColor DarkGray
}

# Xac nhan
Write-Host ""
Write-Host "Ban co chac chan muon XOA $($filesToDelete.Count) file?" -ForegroundColor Red
$confirm = Read-Host "Nhap 'YES' de xac nhan xoa"

if ($confirm -eq "YES") {
    Write-Host ""
    Write-Host "Dang xoa file..." -ForegroundColor Cyan
    
    $deletedCount = 0
    foreach ($file in $filesToDelete) {
        try {
            Remove-Item -Path $file.FullName -Force
            Write-Host "   [OK] Da xoa: $($file.Name)" -ForegroundColor DarkGray
            $deletedCount++
        }
        catch {
            Write-Host "   [ERROR] Loi khi xoa: $($file.Name) - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Hoan thanh! Da xoa $deletedCount/$($filesToDelete.Count) file" -ForegroundColor Green
    
    # Hien thi file con lai
    $remainingFiles = Get-ChildItem -Path "." -Filter "*.md"
    Write-Host ""
    Write-Host "File con lai: $($remainingFiles.Count)" -ForegroundColor Cyan
    foreach ($file in $remainingFiles) {
        Write-Host "   [KEEP] $($file.Name)" -ForegroundColor Green
    }
}
else {
    Write-Host ""
    Write-Host "Da huy thao tac xoa" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== SCRIPT HOAN THANH ===" -ForegroundColor Cyan
