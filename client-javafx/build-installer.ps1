# ========================================
# Build Installer (exe) - Production Mode
# API Base URL: https://ttapi.manhhao.com
# ========================================
# CreatedBy: K24DTCN210-NVMANH
# ========================================

Write-Host ""
Write-Host "========================================"
Write-Host "  Building MS.TrustTest Client Installer"
Write-Host "========================================"
Write-Host ""
Write-Host "Profile: prod (Production)"
Write-Host "API Base URL: https://ttapi.manhhao.com"
Write-Host "Output: Windows Installer exe"
Write-Host ""

Set-Location $PSScriptRoot

# Check Java version (jpackage requires Java 17+)
Write-Host "[0/6] Checking Java and jpackage"

# Try to find jpackage
$jpackagePath = $null
$javaHome = $env:JAVA_HOME
if ($javaHome) {
    $jpackagePath = Join-Path $javaHome "bin\jpackage.exe"
    if (-not (Test-Path $jpackagePath)) {
        $jpackagePath = $null
    }
}

# Try common JDK locations
if (-not $jpackagePath) {
    $commonPaths = @(
        "C:\Program Files\Java\jdk-17\bin\jpackage.exe",
        "C:\Program Files\Java\jdk-18\bin\jpackage.exe",
        "C:\Program Files\Java\jdk-19\bin\jpackage.exe",
        "C:\Program Files\Java\jdk-20\bin\jpackage.exe",
        "C:\Program Files\Java\jdk-21\bin\jpackage.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\jpackage.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-18*\bin\jpackage.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-19*\bin\jpackage.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-20*\bin\jpackage.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\jpackage.exe"
    )
    
    foreach ($path in $commonPaths) {
        $resolved = Resolve-Path $path -ErrorAction SilentlyContinue
        if ($resolved) {
            $jpackagePath = $resolved.Path
            break
        }
    }
}

# Try PATH
if (-not $jpackagePath) {
    $jpackagePath = (Get-Command jpackage -ErrorAction SilentlyContinue).Source
}

if (-not $jpackagePath) {
    Write-Host "[ERROR] jpackage command not found!"
    Write-Host ""
    Write-Host "jpackage is included in JDK 17+ (not JRE)."
    Write-Host "Please ensure you have JDK 17+ installed and in PATH."
    Write-Host ""
    Write-Host "To check: java -version"
    Write-Host "Should show: openjdk version 17 or higher"
    Write-Host ""
    Write-Host "Note: You need JDK (Java Development Kit), not just JRE."
    Write-Host "Download from: https://adoptium.net/"
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Found jpackage at: $jpackagePath"
Write-Host "Java and jpackage OK."
Write-Host ""

# Clean
Write-Host "[1/6] Cleaning previous build"
& mvn clean -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Clean failed!"
    Read-Host "Press Enter to exit"
    exit 1
}

# Build
Write-Host "[2/6] Building JAR with PROD profile"
& mvn package -Pprod -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Build failed!"
    Read-Host "Press Enter to exit"
    exit 1
}

if (-not (Test-Path "target\exam-client-javafx-1.0.0.jar")) {
    Write-Host "[ERROR] JAR file not found after build!"
    Read-Host "Press Enter to exit"
    exit 1
}

# Verify config
Write-Host "[3/6] Verifying config in JAR"
& jar xf target\exam-client-javafx-1.0.0.jar config.properties 2>$null
if (Test-Path "config.properties") {
    $configContent = Get-Content "config.properties" | Select-String "api.base.url"
    if ($configContent -match "ttapi.manhhao.com") {
        Write-Host "Config verified: Production URL found"
    } else {
        Write-Host "[WARNING] Config may not have production URL"
        $configContent
    }
    Remove-Item "config.properties" -ErrorAction SilentlyContinue
}

# Create installer directory
Write-Host "[4/6] Creating installer directory"
if (-not (Test-Path "target\installer")) {
    New-Item -ItemType Directory -Path "target\installer" | Out-Null
}

# Build installer
Write-Host "[5/6] Creating Windows installer with jpackage"
Write-Host ""
Write-Host "This may take a few minutes"
Write-Host ""

$jpackageArgs = @(
    "--input", "target",
    "--name", "MSTrustTestClient",
    "--main-jar", "exam-client-javafx-1.0.0.jar",
    "--main-class", "com.mstrust.client.exam.ExamClientApplication",
    "--type", "exe",
    "--dest", "target\installer",
    "--app-version", "1.0.0",
    "--vendor", "MS.TrustTest",
    "--description", "MS.TrustTest Exam Client Application",
    "--win-dir-chooser",
    "--win-menu",
    "--win-menu-group", "MS.TrustTest",
    "--win-shortcut",
    "--java-options", "--add-reads=com.mstrust.client=ALL-UNNAMED",
    "--java-options", "--add-opens=com.mstrust.client/com.mstrust.client.teacher.api=ALL-UNNAMED",
    "--java-options", "--add-opens=com.mstrust.client/com.mstrust.client.teacher.dto=ALL-UNNAMED"
)

& $jpackagePath $jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Installer creation failed!"
    Write-Host ""
    Write-Host "Troubleshooting:"
    Write-Host "1. Ensure JDK 17+ is installed (not just JRE)"
    Write-Host "2. For exe installer, WiX Toolset may be required"
    Write-Host "   Download from: https://wixtoolset.org/"
    Write-Host "3. Try using --type msi instead of --type exe"
    Write-Host "4. Check jpackage documentation"
    Write-Host ""
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host ""
Write-Host "[6/6] Build completed successfully!"
Write-Host ""
Write-Host "========================================"
Write-Host "  Installer Location:"
Write-Host "========================================"
Write-Host ""

# Check for installer file
$installerFiles = Get-ChildItem "target\installer\*.exe" -ErrorAction SilentlyContinue
if ($installerFiles) {
    Write-Host "Installer files found:"
    $installerFiles | ForEach-Object { Write-Host "  $($_.Name)" }
    Write-Host ""
    Write-Host "Installer ready for distribution!"
} else {
    Write-Host "[WARNING] Installer file not found in target\installer\"
    Write-Host "Please check the build output above for errors."
}
Write-Host ""

Read-Host "Press Enter to exit"

