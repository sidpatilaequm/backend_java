# Barcode Image Upload Test Guide

## Database Update Required

Before testing, run the SQL script to update the database column:

```sql
-- Run this in your MySQL database
USE multimedia_governance;
ALTER TABLE material_variant MODIFY COLUMN barcode_image LONGBLOB;
```

## Test the API

### 1. Upload Barcode Image
```bash
curl -X PUT \
  "http://localhost:8080/api/materials/variants/MTshirt-001-0001/barcode-image" \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/your/barcode_image.png"
```

### 2. Get All Variants (to see the barcode image)
```bash
curl -X GET "http://localhost:8080/api/materials/variants"
```

## Validation Rules

- **File Size**: Maximum 5MB
- **File Type**: Only image files (PNG, JPEG, GIF, BMP)
- **Variant Code**: Must exist in the database

## Expected Response

```json
{
  "status": "SUCCESS",
  "code": "200",
  "message": "Barcode image uploaded successfully",
  "data": {
    "variant": {
      "variantCode": "MTshirt-001-0001",
      "materialName": "Men's T-Shirt",
      "barcodeImage": "iVBORw0KGgoAAAANSUhEUgAA...", // Base64 encoded
      // ... other fields
    }
  }
}
```

## Troubleshooting

1. **Data truncation error**: Run the database update script
2. **File too large**: Reduce image size or compress the image
3. **Invalid file type**: Use only image files
4. **Variant not found**: Check the variant code exists 