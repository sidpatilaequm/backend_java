# Material Channel Mapping API Documentation

## Overview
This API manages the mapping between materials and channels, allowing you to set different pricing, stock levels, and categories for each material across different sales channels.

## Database Schema

### Table: `material_channel_mapping`
```sql
CREATE TABLE material_channel_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    channel_id BIGINT NOT NULL,
    category_id BIGINT NULL,
    price DECIMAL(10,2) NULL,
    stock INT NULL,
    status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (company_id) REFERENCES company_details(company_id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES material(material_id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channel(channel_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES channel_category(category_id) ON DELETE SET NULL,
    
    UNIQUE KEY unique_material_channel_company (company_id, material_id, channel_id)
);
```

## API Endpoints

### 1. Bulk Upsert Material Channel Mappings

**Endpoint:** `POST /api/materials/{materialId}/mappings`

**Description:** Creates or updates multiple channel mappings for a specific material.

**Path Parameters:**
- `materialId` (Long, required): The ID of the material

**Request Body:**
```json
{
  "materialId": 1,
  "mappings": [
    {
      "channelId": 1,
      "categoryId": 3,
      "price": 999.00,
      "stock": 50,
      "status": true
    },
    {
      "channelId": 2,
      "categoryId": 4,
      "price": 1050.00,
      "stock": 40,
      "status": true
    },
    {
      "channelId": 3,
      "categoryId": 6,
      "price": 1100.00,
      "stock": 60,
      "status": false
    }
  ]
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Mappings processed successfully. Success: 3, Errors: 0",
  "errorCode": null,
  "data": {
    "successCount": 3,
    "errorCount": 0
  },
  "dataString": ""
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/materials/1/mappings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "materialId": 1,
    "mappings": [
      {
        "channelId": 1,
        "categoryId": 3,
        "price": 999.00,
        "stock": 50,
        "status": true
      },
      {
        "channelId": 2,
        "categoryId": 4,
        "price": 1050.00,
        "stock": 40,
        "status": true
      }
    ]
  }'
```

### 2. Get Material Channel Mappings

**Endpoint:** `GET /api/materials/{materialId}/mappings`

**Description:** Retrieves all channel mappings for a specific material.

**Path Parameters:**
- `materialId` (Long, required): The ID of the material

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Mappings retrieved successfully",
  "errorCode": null,
  "data": {
    "materialId": 1,
    "mappings": [
      {
        "id": 1,
        "companyId": 1,
        "materialId": 1,
        "channelId": 1,
        "categoryId": 3,
        "channelName": "Amazon",
        "channelCode": "AMZ",
        "categoryName": "Electronics",
        "categoryCode": "ELEC",
        "price": 999.00,
        "stock": 50,
        "status": true,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      },
      {
        "id": 2,
        "companyId": 1,
        "materialId": 1,
        "channelId": 2,
        "categoryId": 4,
        "channelName": "Flipkart",
        "channelCode": "FLP",
        "categoryName": "Fashion",
        "categoryCode": "FASH",
        "price": 1050.00,
        "stock": 40,
        "status": true,
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ],
    "totalMappings": 2
  },
  "dataString": ""
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/materials/1/mappings \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Delete Material Channel Mapping

**Endpoint:** `DELETE /api/materials/{materialId}/mappings/{channelId}`

**Description:** Deletes a specific channel mapping for a material.

**Path Parameters:**
- `materialId` (Long, required): The ID of the material
- `channelId` (Long, required): The ID of the channel

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Mapping deleted successfully",
  "errorCode": null,
  "data": {},
  "dataString": ""
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/materials/1/mappings/2 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Delete All Material Channel Mappings for a Channel

**Endpoint:** `DELETE /api/materials/mappings/channel/{channelId}`

**Description:** Deletes all material-channel mappings for a specific channel. This is useful when you need to clean up all mappings before deleting a channel.

**Path Parameters:**
- `channelId` (Long, required): The ID of the channel

**Request Payload:** None (DELETE request)

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Successfully deleted 5 material-channel mappings for channel ID: 1",
  "errorCode": null,
  "data": {
    "deletedCount": 5,
    "channelId": 1
  },
  "dataString": ""
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/materials/mappings/channel/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Business Rules

### Validation Rules
1. **Material Validation**: The material must exist and belong to the current company
2. **Channel Validation**: The channel must exist and belong to the current company
3. **Category Validation**: If a category is provided, it must belong to the specified channel
4. **Unique Constraint**: Only one mapping can exist per (company, material, channel) combination
5. **Company Isolation**: All operations are scoped to the company of the authenticated user

### Upsert Logic
- If a mapping exists for (company, material, channel), it will be updated
- If no mapping exists, a new one will be created
- The operation is atomic - either all mappings succeed or none do

### Data Types
- `price`: Decimal with 2 decimal places (e.g., 999.99)
- `stock`: Integer representing available quantity
- `status`: Boolean (true = active, false = inactive)
- `categoryId`: Optional - if provided, must belong to the channel

## Error Responses

### Common Error Scenarios
1. **Material not found**: `"Material not found for the current company"`
2. **Channel not found**: Channel doesn't exist or doesn't belong to company
3. **Invalid category**: Category doesn't exist or doesn't belong to the channel
4. **Validation errors**: Missing required fields or invalid data types

### Error Response Format
```json
{
  "status": "ERROR",
  "statusMsg": "Error description",
  "errorCode": null,
  "data": {},
  "dataString": ""
}
```

## Frontend Integration Example

### JavaScript/TypeScript Example
```javascript
// Upsert mappings
const upsertMappings = async (materialId, mappings) => {
  const response = await fetch(`/api/materials/${materialId}/mappings`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      materialId: materialId,
      mappings: mappings
    })
  });
  
  return await response.json();
};

// Get mappings
const getMappings = async (materialId) => {
  const response = await fetch(`/api/materials/${materialId}/mappings`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
};

// Delete mapping
const deleteMapping = async (materialId, channelId) => {
  const response = await fetch(`/api/materials/${materialId}/mappings/${channelId}`, {
    method: 'DELETE',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  return await response.json();
};
```

## Database Setup

Run the SQL script to create the table:
```bash
mysql -u root -p -e "USE your_database_name; SOURCE material_channel_mapping_table.sql;"
```

## Security Notes

- All endpoints require JWT authentication
- Company isolation is enforced - users can only access mappings for their company
- Input validation prevents SQL injection and ensures data integrity
- Foreign key constraints maintain referential integrity
