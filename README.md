# Multimedia Governance API

A Spring Boot application for document processing and verification with OCR capabilities.

## Features

- Document Upload and Processing
- GST Document OCR and Verification
- PAN Card OCR and Verification
- Cheque OCR and Verification
- User Authentication with JWT
- File Management
- Mock API Support for Development

## Prerequisites

- Java 17 or higher
- Maven
- MySQL Database
- Spring Boot 3.4.4

## Configuration

### Application Properties

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/multimedia_governance
spring.datasource.username=root
spring.datasource.password=your_password

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Mock Response Configuration
use.mock.responses=true  # Set to false when using real API

# API Keys (Replace with your actual keys)
attestr.auth.token=your_attestr_token
rapidapi.key=your_rapidapi_key
rapidapi.host=gst-insights-api.p.rapidapi.com
```

## API Endpoints

### Authentication
- POST `/api/users/register` - Register new user
- POST `/api/users/login` - User login

### File Upload and Processing
- POST `/api/upload/process` - Process and extract data from documents
- POST `/api/upload/confirm` - Save processed data to database

### GST Verification
- GET `/api/playground/gst/details/gst/{gstNumber}` - Get GST details
- GET `/api/playground/gst/validate/{gstNumber}` - Validate GST number
- GET `/api/playground/gst/status/{gstNumber}` - Check GST status

## Mock Response Support

The application includes mock response support for development purposes. To use mock responses:

1. Set `use.mock.responses=true` in application.properties
2. Mock responses will be returned instead of calling external APIs
3. When ready for production, set `use.mock.responses=false` and configure real API keys

## Installation and Setup

1. Clone the repository
2. Configure application.properties
3. Run MySQL database
4. Build the project:
   ```bash
   mvn clean install
   ```
5. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## Security

- JWT based authentication
- Role-based access control
- Secure file upload handling
- API key protection

## Error Handling

The application includes comprehensive error handling for:
- File upload errors
- OCR processing errors
- Database errors
- Authentication errors

## Development Mode

For development without API keys:
1. Use mock responses by setting `use.mock.responses=true`
2. Test functionality with predefined response data
3. Switch to real API integration when ready 