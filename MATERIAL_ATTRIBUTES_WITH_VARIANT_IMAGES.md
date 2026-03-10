# Material Attributes with Variant Images API

## Endpoint

**Method:** `GET`  
**URL:** `/api/materials/{materialId}/attributes`

**Example:** `GET /api/materials/1/attributes`

## Description

This endpoint retrieves all material attributes along with all variant images for the specified material ID. It combines attribute information with variant image data in a single response.

## Response Format

### Success Response (200 OK)

```json
{
  "status": "SUCCESS",
  "code": "200",
  "message": "Material attributes and variant images fetched successfully",
  "data": {
    "materialId": 1,
    "attributes": [
      {
        "attributeName": "Color",
        "attributeValue": "Red,Blue,Green",
        "type": "VARIANT"
      },
      {
        "attributeName": "Size",
        "attributeValue": "S,M,L,XL",
        "type": "VARIANT"
      },
      {
        "attributeName": "Brand",
        "attributeValue": "Nike",
        "type": "GENERAL"
      }
    ],
    "variantImages": [
      {
        "variantCode": "MTshirt-001-0001",
        "variantId": 1,
        "barcodeImage": "base64_encoded_barcode_image_or_null",
        "variantImage": "base64_encoded_variant_image_or_null",
        "isActive": true,
        "mrp": 999.0,
        "sellingPrice": 799.0,
        "cost": 400.0,
        "stock": 100.0
      },
      {
        "variantCode": "MTshirt-001-0002",
        "variantId": 2,
        "barcodeImage": "base64_encoded_barcode_image_or_null",
        "variantImage": "base64_encoded_variant_image_or_null",
        "isActive": true,
        "mrp": 999.0,
        "sellingPrice": 799.0,
        "cost": 400.0,
        "stock": 50.0
      }
    ]
  }
}
```

## Response Fields

### Attributes Array
- **attributeName** (String): Name of the attribute (e.g., "Color", "Size", "Brand")
- **attributeValue** (String): Comma-separated values for VARIANT attributes, single value for GENERAL attributes
- **type** (String): Either "GENERAL" or "VARIANT"

### Variant Images Array
- **variantCode** (String): Unique variant code (e.g., "MTshirt-001-0001")
- **variantId** (Long): Database ID of the variant
- **barcodeImage** (String): Base64 encoded barcode image or null if not uploaded
- **variantImage** (String): Base64 encoded variant image or null if not uploaded
- **isActive** (Boolean): Whether the variant is active
- **mrp** (Double): Maximum retail price
- **sellingPrice** (Double): Selling price
- **cost** (Double): Cost price
- **stock** (Double): Available stock quantity

## Error Response

### Material Not Found (400 Bad Request)
```json
{
  "status": "ERROR",
  "code": "400",
  "message": "Material not found",
  "data": {}
}
```

## Usage Examples

### JavaScript/Fetch Example
```javascript
async function getMaterialAttributesWithVariantImages(materialId) {
  try {
    const response = await fetch(`/api/materials/${materialId}/attributes`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    return result;
  } catch (error) {
    console.error('Error fetching material attributes:', error);
    throw error;
  }
}

// Usage
getMaterialAttributesWithVariantImages(1)
  .then(result => {
    console.log('Material attributes:', result.data.attributes);
    console.log('Variant images:', result.data.variantImages);
    
    // Display variant images
    result.data.variantImages.forEach(variant => {
      if (variant.variantImage) {
        const img = document.createElement('img');
        img.src = `data:image/jpeg;base64,${variant.variantImage}`;
        img.alt = `Variant ${variant.variantCode}`;
        document.body.appendChild(img);
      }
    });
  })
  .catch(error => {
    console.error('Failed to fetch:', error);
  });
```

### cURL Example
```bash
curl -X GET \
  http://localhost:8080/api/materials/1/attributes \
  -H 'Content-Type: application/json'
```

## Frontend Integration

### Display Variant Images
```javascript
function displayVariantImages(variantImages) {
  const container = document.getElementById('variant-images-container');
  container.innerHTML = '';
  
  variantImages.forEach(variant => {
    const variantDiv = document.createElement('div');
    variantDiv.className = 'variant-item';
    
    // Variant info
    const infoDiv = document.createElement('div');
    infoDiv.innerHTML = `
      <h4>${variant.variantCode}</h4>
      <p>MRP: $${variant.mrp}</p>
      <p>Stock: ${variant.stock}</p>
    `;
    variantDiv.appendChild(infoDiv);
    
    // Variant image
    if (variant.variantImage) {
      const img = document.createElement('img');
      img.src = `data:image/jpeg;base64,${variant.variantImage}`;
      img.alt = `Variant ${variant.variantCode}`;
      img.style.maxWidth = '200px';
      variantDiv.appendChild(img);
    }
    
    // Barcode image
    if (variant.barcodeImage) {
      const barcodeImg = document.createElement('img');
      barcodeImg.src = `data:image/jpeg;base64,${variant.barcodeImage}`;
      barcodeImg.alt = `Barcode ${variant.variantCode}`;
      barcodeImg.style.maxWidth = '150px';
      variantDiv.appendChild(barcodeImg);
    }
    
    container.appendChild(variantDiv);
  });
}
```

### HTML Structure
```html
<div id="material-attributes">
  <h3>Material Attributes</h3>
  <div id="attributes-list"></div>
  
  <h3>Variant Images</h3>
  <div id="variant-images-container"></div>
</div>

<script>
// Fetch and display data
getMaterialAttributesWithVariantImages(1)
  .then(result => {
    // Display attributes
    const attributesList = document.getElementById('attributes-list');
    result.data.attributes.forEach(attr => {
      const attrDiv = document.createElement('div');
      attrDiv.innerHTML = `
        <strong>${attr.attributeName}</strong>: ${attr.attributeValue} (${attr.type})
      `;
      attributesList.appendChild(attrDiv);
    });
    
    // Display variant images
    displayVariantImages(result.data.variantImages);
  });
</script>
```

## Notes

- **Base64 Encoding**: All images are returned as Base64 strings for easy frontend consumption
- **Null Handling**: If no image is uploaded, the image field will be `null`
- **Performance**: This endpoint fetches all variant data for the material, so it may take longer for materials with many variants
- **Image Types**: 
  - `variantImage`: Specific image for the variant (e.g., red shirt, blue shirt)
  - `barcodeImage`: Barcode/QR code for the variant
- **Backward Compatibility**: The existing `attributes` field remains unchanged, `variantImages` is an additional field 