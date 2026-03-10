# Channel Management API Documentation

## Overview
The Channel Management API provides endpoints for managing sales channels and their associated categories. The system supports company and user isolation, ensuring data security and proper access control.

## Base URL
```
http://localhost:8080/api/channels
```

## Authentication
All endpoints require authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## Endpoints

### 1. Create Channel
**POST** `/api/channels/create`

Creates a new channel with optional categories.

**Request Body:**
```json
{
  "channel_name": "Amazon",
  "channel_code": "AMZ",
  "description": "Amazon India marketplace",
  "categories": [
    {
      "category_code": "ELEC",
      "category_name": "Electronics"
    },
    {
      "category_code": "FASH",
      "category_name": "Fashion"
    },
    {
      "category_code": "SHOE",
      "category_name": "Shoes"
    }
  ]
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Channel created successfully",
  "data": {
    "channelId": 1
  }
}
```

**Status Codes:**
- `200 OK`: Channel created successfully
- `400 Bad Request`: Invalid request data
- `409 Conflict`: Channel code already exists
- `500 Internal Server Error`: Server error

### 2. Get All Channels
**GET** `/api/channels/all`

Retrieves all channels for the current authenticated user.

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Channels retrieved successfully",
  "data": {
    "channels": [
      {
        "channelId": 1,
        "channelName": "Amazon",
        "channelCode": "AMZ",
        "description": "Amazon India marketplace",
        "isActive": true,
        "companyId": 1,
        "userId": 1,
        "categories": [
          {
            "categoryId": 1,
            "categoryCode": "ELEC",
            "categoryName": "Electronics",
            "isActive": true,
            "channelId": 1,
            "createdAt": "2024-01-15T10:30:00"
          }
        ],
        "createdAt": "2024-01-15T10:30:00",
        "updatedAt": "2024-01-15T10:30:00"
      }
    ]
  }
}
```

**Status Codes:**
- `200 OK`: Channels retrieved successfully
- `404 Not Found`: No company found for user
- `500 Internal Server Error`: Server error

### 3. Get Channel by ID
**GET** `/api/channels/{channelId}`

Retrieves a specific channel by its ID.

**Path Parameters:**
- `channelId` (Long): The ID of the channel to retrieve

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Channel retrieved successfully",
  "data": {
    "channel": {
      "channelId": 1,
      "channelName": "Amazon",
      "channelCode": "AMZ",
      "description": "Amazon India marketplace",
      "isActive": true,
      "companyId": 1,
      "userId": 1,
      "categories": [...],
      "createdAt": "2024-01-15T10:30:00",
      "updatedAt": "2024-01-15T10:30:00"
    }
  }
}
```

**Status Codes:**
- `200 OK`: Channel retrieved successfully
- `404 Not Found`: Channel not found
- `500 Internal Server Error`: Server error

### 4. Update Channel
**PUT** `/api/channels/{channelId}`

Updates an existing channel and its categories.

**Path Parameters:**
- `channelId` (Long): The ID of the channel to update

**Request Body:**
```json
{
  "channel_name": "Amazon Updated",
  "channel_code": "AMZ_UPD",
  "description": "Updated Amazon India marketplace",
  "is_active": true,
  "categories": [
    {
      "categoryId": 1,
      "category_code": "ELEC_UPD",
      "category_name": "Electronics Updated",
      "is_active": true
    },
    {
      "category_code": "NEW_CAT",
      "category_name": "New Category",
      "is_active": true
    }
  ]
}
```

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Channel updated successfully",
  "data": {
    "channelId": 1
  }
}
```

**Status Codes:**
- `200 OK`: Channel updated successfully
- `404 Not Found`: Channel not found
- `409 Conflict`: Channel code already exists
- `500 Internal Server Error`: Server error

### 5. Delete Channel
**DELETE** `/api/channels/{channelId}`

Deletes a channel and all its associated categories.

**Path Parameters:**
- `channelId` (Long): The ID of the channel to delete

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Channel deleted successfully"
}
```

**Status Codes:**
- `200 OK`: Channel deleted successfully
- `404 Not Found`: Channel not found
- `500 Internal Server Error`: Server error

### 6. Toggle Channel Status
**PATCH** `/api/channels/{channelId}/toggle-status`

Toggles the active status of a channel.

**Path Parameters:**
- `channelId` (Long): The ID of the channel to toggle

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Channel status updated successfully",
  "data": {
    "isActive": false
  }
}
```

**Status Codes:**
- `200 OK`: Status updated successfully
- `404 Not Found`: Channel not found
- `500 Internal Server Error`: Server error

### 7. Delete a specific category from a channel

**Endpoint:** `DELETE /api/channels/{channelId}/categories/{categoryId}`

**Description:** Deletes a specific category from a channel. This is useful for removing unwanted categories while keeping the channel active.

**Path Parameters:**
- `channelId` (Long, required): The ID of the channel
- `categoryId` (Long, required): The ID of the category to delete

**Request Payload:** None (DELETE request)

**Response:**
```json
{
  "status": "SUCCESS",
  "statusMsg": "Category deleted successfully from channel",
  "errorCode": null,
  "data": {
    "channelId": 1,
    "categoryId": 3,
    "categoryName": "Electronics"
  },
  "dataString": ""
}
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8080/api/channels/1/categories/3 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Important Notes:**
- The category must belong to the specified channel
- If the category is referenced by material-channel mappings, deletion will be blocked
- You must remove the material-channel mappings first before deleting the category

**Status Codes:**
- `200 OK`: Category deleted successfully
- `400 Bad Request`: Invalid parameters
- `404 Not Found`: Channel or category not found
- `409 Conflict`: Category cannot be deleted due to existing references
- `500 Internal Server Error`: Server error

## Data Models

### Channel Entity
```java
public class Channel {
    private Long channelId;
    private String channelName;
    private String channelCode;
    private String description;
    private Boolean isActive;
    private CompanyDetails company;
    private UserDetail user;
    private List<ChannelCategory> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### ChannelCategory Entity
```java
public class ChannelCategory {
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Boolean isActive;
    private Channel channel;
    private LocalDateTime createdAt;
}
```

## Security Features

### Company and User Isolation
- All operations are scoped to the authenticated user's company
- Users can only access channels associated with their company
- Company ID is automatically extracted from the user's authentication token

### Data Validation
- Channel codes must be unique across the system
- Category codes must be unique within a channel
- Required fields are validated before processing

### Error Handling
- Comprehensive error messages for different scenarios
- Proper HTTP status codes for different error types
- Detailed logging for debugging and monitoring

## Database Schema

### Channel Table
```sql
CREATE TABLE channel (
    channel_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    channel_name VARCHAR(255) NOT NULL,
    channel_code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company_details(company_id),
    FOREIGN KEY (user_id) REFERENCES user_detail(user_id)
);
```

### ChannelCategory Table
```sql
CREATE TABLE channel_category (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) NOT NULL,
    category_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    channel_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (channel_id) REFERENCES channel(channel_id) ON DELETE CASCADE,
    UNIQUE KEY uk_channel_category_code (channel_id, category_code)
);
```

## Testing Examples

### Create Channel with Categories
```bash
curl -X POST http://localhost:8080/api/channels/create \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "channel_name": "Amazon",
    "channel_code": "AMZ",
    "description": "Amazon India marketplace",
    "categories": [
      {
        "category_code": "ELEC",
        "category_name": "Electronics"
      },
      {
        "category_code": "FASH",
        "category_name": "Fashion"
      }
    ]
  }'
```

### Get All Channels
```bash
curl -X GET http://localhost:8080/api/channels/all \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Update Channel
```bash
curl -X PUT http://localhost:8080/api/channels/1 \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "channel_name": "Amazon Updated",
    "description": "Updated description"
  }'
```

## Notes
- All timestamps are in ISO 8601 format
- Boolean values are represented as `true`/`false` in JSON
- The system automatically handles company and user isolation based on the authenticated user
- Channel codes must be unique across the entire system
- Category codes must be unique within a specific channel
