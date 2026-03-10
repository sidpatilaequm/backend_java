# Channel Management API

## Overview
This module implements a comprehensive Channel Management system for the Multimedia Governance project. The API allows users to create, manage, and organize sales channels with their associated categories, with proper company and user isolation.

## Features

### Core Functionality
- **Channel Management**: Create, read, update, and delete sales channels
- **Category Management**: Associate multiple categories with each channel
- **Status Management**: Toggle channel active/inactive status
- **Data Isolation**: Company and user-based data isolation for security

### Security Features
- **Authentication Required**: All endpoints require JWT authentication
- **Company Isolation**: Users can only access channels from their company
- **User Isolation**: Data is scoped to the authenticated user
- **Input Validation**: Comprehensive validation for all inputs

## Architecture

### Entities
1. **Channel**: Main entity representing a sales channel
   - `channelId`: Primary key
   - `channelName`: Human-readable name
   - `channelCode`: Unique identifier code
   - `description`: Optional description
   - `isActive`: Status flag
   - `company`: Associated company (foreign key)
   - `user`: Associated user (foreign key)
   - `categories`: List of associated categories
   - `createdAt`/`updatedAt`: Timestamps

2. **ChannelCategory**: Categories associated with channels
   - `categoryId`: Primary key
   - `categoryCode`: Unique code within the channel
   - `categoryName`: Human-readable name
   - `isActive`: Status flag
   - `channel`: Associated channel (foreign key)
   - `createdAt`: Timestamp

### Database Design
- **Foreign Key Relationships**: Proper relationships with `company_details` and `user_detail` tables
- **Cascade Operations**: Deleting a channel cascades to delete its categories
- **Unique Constraints**: Channel codes are unique globally, category codes are unique per channel
- **Indexes**: Optimized indexes for performance on common queries

## API Endpoints

### Channel Operations
- `POST /api/channels/create` - Create new channel with categories
- `GET /api/channels/all` - Get all channels for current user
- `GET /api/channels/{id}` - Get specific channel by ID
- `PUT /api/channels/{id}` - Update existing channel
- `DELETE /api/channels/{id}` - Delete channel and its categories
- `PATCH /api/channels/{id}/toggle-status` - Toggle channel active status

## Implementation Details

### Service Layer
The `ChannelService` class provides:
- **CRUD Operations**: Complete create, read, update, delete functionality
- **Business Logic**: Validation, error handling, and data processing
- **Security**: Company and user isolation logic
- **Transaction Management**: Proper transaction handling for data consistency

### Repository Layer
- **ChannelRepository**: Data access for Channel entities
- **ChannelCategoryRepository**: Data access for ChannelCategory entities
- **Custom Queries**: Optimized queries for company/user isolation

### Controller Layer
- **RESTful Design**: Standard REST endpoints
- **Error Handling**: Comprehensive error responses
- **Logging**: Detailed logging for debugging and monitoring
- **Response Format**: Consistent response format using `ServiceResponse`

## Data Flow

### Creating a Channel
1. User sends POST request with channel data
2. Controller validates request and calls service
3. Service extracts current user and company from authentication
4. Service validates channel code uniqueness
5. Service creates channel and associated categories
6. Service returns success response with channel ID

### Retrieving Channels
1. User sends GET request
2. Controller calls service
3. Service extracts current user and company
4. Service queries database with company/user filters
5. Service converts entities to DTOs
6. Service returns filtered channel list

## Security Implementation

### Authentication
- JWT token required for all endpoints
- Token contains user information for isolation

### Authorization
- Company ID extracted from user's company association
- User ID extracted from authentication token
- All database queries filtered by company and user

### Data Validation
- Channel code uniqueness validation
- Category code uniqueness within channel
- Required field validation
- Input sanitization

## Error Handling

### Response Format
All responses use the `ServiceResponse` format:
```json
{
  "status": "SUCCESS|ERROR",
  "statusMsg": "Human-readable message",
  "data": { ... }
}
```

### Error Scenarios
- **404 Not Found**: Channel not found, no company for user
- **409 Conflict**: Duplicate channel code
- **400 Bad Request**: Invalid input data
- **500 Internal Server Error**: Server errors

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

## Usage Examples

### Create Channel
```bash
curl -X POST http://localhost:8080/api/channels/create \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "channel_name": "Amazon",
    "channel_code": "AMZ",
    "description": "Amazon India marketplace",
    "categories": [
      {
        "category_code": "ELEC",
        "category_name": "Electronics"
      }
    ]
  }'
```

### Get All Channels
```bash
curl -X GET http://localhost:8080/api/channels/all \
  -H "Authorization: Bearer <jwt-token>"
```

## Testing

### Unit Tests
- Service layer business logic testing
- Repository layer data access testing
- Controller layer endpoint testing

### Integration Tests
- End-to-end API testing
- Database integration testing
- Security testing

### Manual Testing
- Postman collection provided
- Sample data scripts included
- Error scenario testing

## Deployment

### Prerequisites
- Java 17+
- Spring Boot 3.x
- MySQL 8.0+
- JWT authentication configured

### Database Setup
1. Run the `channel_tables.sql` script
2. Ensure `company_details` and `user_detail` tables exist
3. Configure database connection in `application.properties`

### Application Configuration
- JWT secret key configured
- Database connection properties set
- Logging level configured

## Monitoring and Logging

### Logging
- Request/response logging
- Error logging with stack traces
- Performance logging
- Security event logging

### Metrics
- API response times
- Error rates
- Database query performance
- User activity metrics

## Future Enhancements

### Planned Features
- Bulk operations for channels and categories
- Channel templates for quick setup
- Advanced filtering and search
- Channel analytics and reporting
- Integration with external systems

### Performance Optimizations
- Caching for frequently accessed data
- Pagination for large datasets
- Database query optimization
- Connection pooling tuning

## Support

### Documentation
- API documentation: `CHANNEL_API_DOCUMENTATION.md`
- Database schema: `channel_tables.sql`
- Code comments and JavaDoc

### Troubleshooting
- Common error scenarios documented
- Debug logging enabled
- Performance monitoring tools
- Database query analysis tools
