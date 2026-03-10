# Variant Image API Documentation

## Overview

The Variant Image API allows you to upload and manage images for individual material variants. Each variant can have its own unique image that is different from the main material image and the barcode image.

## Database Schema

The `material_variant` table now includes a new column:
- `variant_image` (LONGBLOB) - Stores the variant-specific image as raw bytes

## API Endpoints

### 1. Upload Variant Image by Variant ID

**Endpoint:** `PUT /api/materials/variants/id/{variantId}/variant-image`

**Description:** Upload an image for a specific variant using its database ID.

**Path Parameters:**
- `variantId` (Long) - The database ID of the variant

**Request Body:**
- `file` (MultipartFile) - The image file to upload

**Response:**
```json
{
  "status": "SUCCESS",
  "code": "200",
  "message": "Variant image uploaded",
  "data": {}
}
```

### 2. Upload Variant Image by Variant Code

**Endpoint:** `PUT /api/materials/variants/code/{variantCode}/variant-image`

**Description:** Upload an image for a specific variant using its variant code.

**Path Parameters:**
- `variantCode` (String) - The variant code (e.g., "MTshirt-001-0001")

**Request Body:**
- `file` (MultipartFile) - The image file to upload

**Response:**
```json
{
  "status": "SUCCESS",
  "code": "200",
  "message": "Variant image uploaded successfully",
  "data": {
    "variant": {
      "variantCode": "MTshirt-001-0001",
      "materialName": "T-Shirt",
      "materialDescription": "Cotton T-Shirt",
      "mrp": 999.0,
      "sellingPrice": 799.0,
      "cost": 400.0,
      "stock": 100.0,
      "barcodeImage": "base64_encoded_barcode_image_or_null",
      "variantImage": "base64_encoded_variant_image_or_null",
      "active": true,
      "attributes": [...]
    }
  }
}
```

### 3. Get Variants with Images

**Endpoint:** `GET /api/materials/{materialId}/variants`

**Description:** Retrieve all variants for a material, including their images.

**Path Parameters:**
- `materialId` (Long) - The database ID of the material

**Response:**
```json
{
  "status": "SUCCESS",
  "code": "200",
  "message": "Variants fetched successfully",
  "data": {
    "variants": [
      {
        "variantCode": "MTshirt-001-0001",
        "materialName": "T-Shirt",
        "materialDescription": "Cotton T-Shirt",
        "mrp": 999.0,
        "sellingPrice": 799.0,
        "cost": 400.0,
        "stock": 100.0,
        "barcodeImage": "base64_encoded_barcode_image_or_null",
        "variantImage": "base64_encoded_variant_image_or_null",
        "active": true,
        "attributes": [
          {
            "attributeName": "Color",
            "attributeValue": "Red"
          },
          {
            "attributeName": "Size",
            "attributeValue": "M"
          }
        ]
      }
    ]
  }
}
```

## Validation Rules

### File Upload Validation
- **Maximum File Size:** 5MB
- **Allowed File Types:** Image files only (content-type starting with "image/")
- **Supported Formats:** JPEG, PNG, GIF, WebP, etc.

### Error Responses

**File Too Large:**
```json
{
  "status": "ERROR",
  "code": "400",
  "message": "File size too large. Maximum allowed size is 5MB.",
  "data": {}
}
```

**Invalid File Type:**
```json
{
  "status": "ERROR",
  "code": "400",
  "message": "Invalid file type. Only image files are allowed.",
  "data": {}
}
```

**Variant Not Found:**
```json
{
  "status": "ERROR",
  "code": "400",
  "message": "Variant not found with code: MTshirt-001-0001",
  "data": {}
}
```

## Implementation Details

### Storage
- Images are stored as raw byte arrays in the database (LONGBLOB)
- No Base64 encoding is used for storage
- Images are stored directly as uploaded

### Retrieval
- When retrieved via API, images are converted to Base64 for easy frontend consumption
- This allows direct use in HTML `<img>` tags with `data:image/...` URLs

### Image Types
- **Variant Image:** Specific to each variant (e.g., red shirt, blue shirt)
- **Barcode Image:** Barcode/QR code for the variant
- **Material Image:** General images for the material (stored in `material_images` table)

## Usage Examples

### Frontend Integration

**HTML Form:**
```html
<form enctype="multipart/form-data">
  <input type="file" name="file" accept="image/*" required>
  <button type="submit">Upload Variant Image</button>
</form>
```

**JavaScript Upload:**
```javascript
async function uploadVariantImage(variantCode, imageFile) {
  const formData = new FormData();
  formData.append('file', imageFile);
  
  const response = await fetch(`/api/materials/variants/code/${variantCode}/variant-image`, {
    method: 'PUT',
    body: formData
  });
  
  return await response.json();
}
```

**Display Image:**
```javascript
// Assuming you have the Base64 image data from the API
const variantImage = response.data.variant.variantImage;
if (variantImage) {
  const imgElement = document.createElement('img');
  imgElement.src = `data:image/jpeg;base64,${variantImage}`;
  document.body.appendChild(imgElement);
}
```

## Migration

To add the variant image functionality to an existing database:

```sql
USE multimedia_governance;
ALTER TABLE material_variant ADD COLUMN variant_image LONGBLOB;
```

## Notes

- Each variant can have its own unique image
- Variant images are separate from material images and barcode images
- Images are optional and can be null if not uploaded
- The API maintains backward compatibility with existing endpoints
- All existing variant functionality remains unchanged 