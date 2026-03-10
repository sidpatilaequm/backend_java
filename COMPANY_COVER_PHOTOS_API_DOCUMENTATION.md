image.png# Company Cover Photos API Documentation

## Overview
This API provides endpoints to manage company cover photos with automatic replacement functionality. When a company uploads a new cover photo, it automatically replaces the previous one.

---

## Endpoints

### **1. Upload Cover Photo (Auto-Replace)**

**Endpoint:** `POST /api/company/cover-photos/upload`

**Description:** Upload a new cover photo for the company. If the company already has a cover photo, it will be automatically replaced.

**Request:**
- **Content-Type:** `multipart/form-data`
- **Parameter:** `file` (MultipartFile) - Image file to upload

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Cover photo uploaded and previous photo replaced successfully",
  "errorCode": null,
  "data": {
    "coverPhotoId": 1,
    "coverPhotoName": "company-logo.jpg",
    "coverPhotoType": "image/jpeg",
    "sequenceOrder": 0,
    "fileSize": 245760
  },
  "dataString": ""
}
```

**Auto-Replace Behavior:**
- If company has no existing cover photo: Uploads new photo
- If company has existing cover photo: Deactivates old photo and uploads new one
- Only one active cover photo per company at any time

### **2. Get All Cover Photos**

**Endpoint:** `GET /api/company/cover-photos/all`

**Description:** Retrieve all active cover photos for the company (typically just one).

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Cover photos retrieved successfully",
  "errorCode": null,
  "data": {
    "coverPhotos": [
      {
        "coverPhotoId": 1,
        "companyId": 1,
        "coverPhotoName": "company-logo.jpg",
        "coverPhotoType": "image/jpeg",
        "isActive": true,
        "sequenceOrder": 0,
        "createdDate": "2024-01-15T10:30:00",
        "modifiedDate": "2024-01-15T10:30:00",
        "createdBy": "admin@company.com",
        "modifiedBy": null
      }
    ],
    "totalCoverPhotos": 1
  },
  "dataString": ""
}
```

### **3. Get Primary Cover Photo**

**Endpoint:** `GET /api/company/cover-photos/primary`

**Description:** Retrieve the primary (active) cover photo for the company.

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Primary cover photo retrieved successfully",
  "errorCode": null,
  "data": {
    "coverPhoto": {
      "coverPhotoId": 1,
      "companyId": 1,
      "coverPhotoName": "company-logo.jpg",
      "coverPhotoType": "image/jpeg",
      "isActive": true,
      "sequenceOrder": 0,
      "createdDate": "2024-01-15T10:30:00",
      "modifiedDate": "2024-01-15T10:30:00",
      "createdBy": "admin@company.com",
      "modifiedBy": null
    }
  },
  "dataString": ""
}
```

---

## Database Schema

### **company_cover_photos Table**
```sql
CREATE TABLE company_cover_photos (
    cover_photo_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    company_id BIGINT NOT NULL,
    cover_photo_name VARCHAR(255) NOT NULL,
    cover_photo_type VARCHAR(100) NOT NULL,
    cover_photo_data LONGBLOB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sequence_order INT DEFAULT 0,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    modified_by VARCHAR(100)
);
```

---

## Usage Examples

### **JavaScript/Frontend - Upload Cover Photo**
```javascript
// Upload cover photo with file input
const fileInput = document.getElementById('coverPhotoInput');
const formData = new FormData();
formData.append('file', fileInput.files[0]);

fetch('/api/company/cover-photos/upload', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => {
  if (data.status === 'SUCCESS') {
    console.log('Cover photo uploaded:', data.data.coverPhotoName);
    if (data.statusMsg.includes('replaced')) {
      console.log('Previous photo was automatically replaced');
    }
    // Refresh cover photo display
    loadCoverPhotos();
  } else {
    console.error('Upload failed:', data.statusMsg);
  }
})
.catch(error => console.error('Error:', error));
```

### **JavaScript/Frontend - Get Cover Photos**
```javascript
// Get all cover photos (should be just one active photo)
fetch('/api/company/cover-photos/all')
  .then(response => response.json())
  .then(data => {
    if (data.status === 'SUCCESS') {
      const coverPhotos = data.data.coverPhotos;
      displayCoverPhotos(coverPhotos);
    }
  })
  .catch(error => console.error('Error:', error));

// Get primary cover photo
fetch('/api/company/cover-photos/primary')
  .then(response => response.json())
  .then(data => {
    if (data.status === 'SUCCESS' && data.data.coverPhoto) {
      const primaryPhoto = data.data.coverPhoto;
      displayPrimaryCoverPhoto(primaryPhoto);
    }
  })
  .catch(error => console.error('Error:', error));
```

### **cURL Examples**
```bash
# Upload cover photo (auto-replaces existing)
curl -X POST "http://localhost:8080/api/company/cover-photos/upload" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@company-logo.jpg"

# Get all cover photos
curl -X GET "http://localhost:8080/api/company/cover-photos/all" \
  -H "Content-Type: application/json"

# Get primary cover photo
curl -X GET "http://localhost:8080/api/company/cover-photos/primary" \
  -H "Content-Type: application/json"
```

---

## Features

### **✅ What's Included**
- **Auto-Replace Functionality**: New uploads automatically replace existing photos
- **Single Photo Per Company**: Only one active cover photo per company
- **File Upload**: Support for image files (JPEG, PNG, etc.)
- **Company Isolation**: Each company has its own cover photos
- **Audit Trail**: Created/modified timestamps and user tracking
- **Primary Photo Access**: Easy access to the main cover photo

### **🔄 Auto-Replace Logic**
1. **Check Existing**: Look for active cover photos for the company
2. **Deactivate Old**: Set `is_active = false` for existing photos
3. **Upload New**: Save new photo with `is_active = true`
4. **Sequence Order**: New photo gets `sequence_order = 0`

### **🔒 Security Features**
- **Authentication Required**: User must be logged in
- **Company Isolation**: Users can only access their own company's photos
- **File Type Validation**: Only image files are allowed
- **File Size Limits**: Configurable file size restrictions

### **📊 Use Cases**
- **Company Branding**: Upload company logos and cover images
- **PDF Generation**: Use cover photos in catalog PDFs
- **Website Display**: Show company cover photos on websites
- **Document Headers**: Include cover photos in generated documents

---

## Error Handling

| Error | Description | Solution |
|-------|-------------|----------|
| `No file provided` | Missing file in request | Include file in multipart request |
| `Only image files are allowed` | Invalid file type | Upload image files only |
| `File size too large. Maximum allowed size is 10MB` | File exceeds size limit | Compress image or use smaller file |
| `File too large or database error` | Database column size limit | Use smaller image or check database configuration |
| `Transaction silently rolled back` | Database constraint violation | Check file size and database column definition |
| `User not authenticated` | User not logged in | Login required |
| `Company not found` | User's company not found | Contact admin |

## Troubleshooting

### **Database Column Size Issues**
If you get "Data too long for column" errors:

1. **Check Database Column**: Ensure `cover_photo_data` is defined as `LONGBLOB`
2. **Run Migration Script**: Execute the provided SQL migration script
3. **File Size**: Keep images under 10MB for best performance
4. **Image Compression**: Compress images before upload

### **File Size Recommendations**
- **Maximum Size**: 10MB
- **Recommended Size**: 2-5MB
- **Optimal Dimensions**: 1920x1080 or similar
- **Format**: JPEG for photos, PNG for graphics with transparency

---

## File Requirements

- **Supported Formats**: JPEG, PNG, GIF, BMP, WebP
- **Maximum Size**: Configurable (default: 10MB)
- **Recommended Size**: 1920x1080 or similar aspect ratio
- **File Naming**: Original filename is preserved

---

## Integration Notes

- **Frontend Integration**: Perfect for file upload components
- **PDF Integration**: Cover photos can be used in PDF generation
- **Image Optimization**: Consider compressing images before upload
- **CDN Integration**: Can be extended to store images in CDN
- **Caching**: Implement caching for frequently accessed cover photos
- **Auto-Replace**: No need for manual deletion - just upload new photo

---

## Future Considerations

- **Delete Functionality**: Can be added later if needed for manual deletion
- **Multiple Photos**: Can be extended to support multiple photos per company
- **Photo History**: Can track photo change history for audit purposes
- **Photo Versions**: Can maintain version history of cover photos
