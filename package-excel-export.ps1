# PowerShell script to package files for deployment

# Create temporary directory structure
$tempDir = "deployment-package"
$srcDir = "$tempDir/src/main/java/com/example/multimedia/file_upload_api"

# Create directories
New-Item -ItemType Directory -Force -Path "$srcDir/controller"
New-Item -ItemType Directory -Force -Path "$srcDir/service"
New-Item -ItemType Directory -Force -Path "$srcDir/repository"
New-Item -ItemType Directory -Force -Path "$srcDir/dto"
New-Item -ItemType Directory -Force -Path "$srcDir/entity"
New-Item -ItemType Directory -Force -Path "$srcDir/security"

# Copy files
# Controllers
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/controller/ExcelExportController.java" "$srcDir/controller/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/controller/AuthController.java" "$srcDir/controller/"

# Services
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/service/ExcelExportService.java" "$srcDir/service/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/security/CustomUserDetailsService.java" "$srcDir/security/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/security/JwtUtil.java" "$srcDir/security/"

# Repositories
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/repository/CompanyDetailsRepository.java" "$srcDir/repository/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/repository/UserAuthenticationRepository.java" "$srcDir/repository/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/repository/UserDetailRepository.java" "$srcDir/repository/"

# DTOs
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/dto/LoginResponse.java" "$srcDir/dto/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/dto/LoginRequest.java" "$srcDir/dto/"

# Entities
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/entity/UserAuthentication.java" "$srcDir/entity/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/entity/Authorization.java" "$srcDir/entity/"
Copy-Item "src/main/java/com/example/multimedia/file_upload_api/entity/UserDetail.java" "$srcDir/entity/"

# Project files
Copy-Item "pom.xml" "$tempDir/"

# Create zip file
Compress-Archive -Path "$tempDir/*" -DestinationPath "deployment-package.zip" -Force

# Clean up temporary directory
Remove-Item -Path $tempDir -Recurse -Force

Write-Host "Deployment package has been created in deployment-package.zip" 