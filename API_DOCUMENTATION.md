# Vendor Registration API Documentation

## Overview

This API provides endpoints for vendor registration, document upload, and data extraction. The system supports uploading various documents (GST, PAN, Cheque, COI) and extracting relevant information using OCR technology. The API automatically converts PDF files to JPG format before processing them through the OCR service.

## Authentication

All API endpoints except registration and login require JWT authentication. Include the JWT token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Endpoints

### 1. User Registration

**Endpoint:** `POST /api/users/register`

**Description:** Register a new vendor user.

**Request Body:**
```json
{
  "email": "vendor@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210"
}
```

**Response:**
```json
{
  "userId": 1,
  "email": "vendor@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "9876543210",
  "signupDate": "2024-04-08T10:30:00",
  "active": true
}
```

### 2. User Login

**Endpoint:** `POST /api/users/login`

**Description:** Authenticate a user and receive a JWT token.

**Request Body:**
```json
{
  "email": "vendor@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "vendor@example.com",
  "userId": 1
}
```

### 3. Document Upload and OCR Processing

**Endpoint:** `POST /api/files/upload-all`

**Description:** Upload multiple documents (GST, PAN, Cheque, COI) and process them through OCR.

**Request:**
- Content-Type: `multipart/form-data`
- Authentication: Required (JWT token)

**Form Fields:**
- `gstFile`: GST document (PDF or JPG)
- `panFile`: PAN document (PDF or JPG)
- `chequeFile`: Cheque document (PDF or JPG)
- `coiFile`: Certificate of Incorporation (PDF or JPG, optional)
- `userId`: User ID (Long)

**Response:**
```json
{
  "message": "All files processed successfully",
  "gst": {
    "status": "success",
    "confidence": 0.95,
    "data": {
      "gstin": "24ABCD1234R1ZN",
      "legalName": "ACME INFO PRIVATE LIMITED",
      "tradeName": "ACME INFO PRIVATE LIMITED",
      "address": "10th Floor, 1006, Road, Ahmedabad, Gujarat, 380015",
      "type": "Regular",
      "registered": "30/03/2021",
      "issued": "30/03/2021",
      "approvingAuthority": "Gujarat",
      "jurisdiction": "Ghatak 11 (Ahmedabad)",
      "constitution": "Private Limited Company"
    }
  },
  "pan": {
    "status": "success",
    "confidence": 0.98,
    "data": {
      "pan": "ABCDP3063A",
      "name": "PEGADROID IQ SOLUTIONS PRIVATE LIMITED",
      "incorporationDate": "13/07/2017",
      "category": "BUSINESS",
      "address": "10th Floor, 1006, Road, Ahmedabad, Gujarat, 380015",
      "fatherName": "Not Available",
      "dateOfIssue": "13/07/2017"
    }
  },
  "cheque": {
    "status": "success",
    "confidence": 0.92,
    "data": {
      "bank_name": "HDFC Bank",
      "account_number": "12345678901234",
      "ifsc_code": "HDFC0001234",
      "branch": "Mumbai Main",
      "issued_to": "ACME INFO PRIVATE LIMITED",
      "signatory": "John Doe",
      "code": "CHQ123456",
      "date": "08/04/2024",
      "amount": "10000.00"
    }
  },
  "files": [
    {
      "fileId": 1,
      "fileName": "gst_document.pdf",
      "fileType": "application/pdf",
      "documentType": "GST",
      "filePath": "abc123.pdf",
      "uploadDate": "2024-04-08T10:35:00"
    },
    {
      "fileId": 2,
      "fileName": "pan_document.jpg",
      "fileType": "image/jpeg",
      "documentType": "PAN",
      "filePath": "def456.jpg",
      "uploadDate": "2024-04-08T10:35:00"
    },
    {
      "fileId": 3,
      "fileName": "cheque_document.pdf",
      "fileType": "application/pdf",
      "documentType": "Cheque",
      "filePath": "ghi789.pdf",
      "uploadDate": "2024-04-08T10:35:00"
    }
  ]
}
```

### 4. Add Company Address

**Endpoint:** `POST /api/company-addresses`

**Description:** Add company address details for a vendor.

**Request Body:**
```json
{
  "userId": 1,
  "companyId": "COMP123",
  "address1": "10th Floor, 1006",
  "address2": "Road",
  "city": "Ahmedabad",
  "state": "Gujarat",
  "country": "India",
  "pincode": "380015"
}
```

**Response:**
```json
{
  "companyAddressId": 1,
  "userId": 1,
  "companyId": "COMP123",
  "address1": "10th Floor, 1006",
  "address2": "Road",
  "city": "Ahmedabad",
  "state": "Gujarat",
  "country": "India",
  "pincode": "380015"
}
```

### 5. Get Company Addresses

**Endpoint:** `GET /api/company-addresses/user/{userId}`

**Description:** Retrieve all company addresses for a specific user.

**Response:**
```json
[
  {
    "companyAddressId": 1,
    "userId": 1,
    "companyId": "COMP123",
    "address1": "10th Floor, 1006",
    "address2": "Road",
    "city": "Ahmedabad",
    "state": "Gujarat",
    "country": "India",
    "pincode": "380015"
  }
]
```

## Frontend Integration Guide

### Document Upload and Data Display

1. **File Upload:**
   - Create a form with file input fields for GST, PAN, Cheque, and COI documents
   - Use `FormData` to send the files to the API
   - Include the user ID and JWT token in the request

2. **Displaying Extracted Data:**
   - After successful upload, the API will return the extracted data
   - Map the response data to the corresponding HTML elements:

```javascript
// Example of mapping API response to HTML elements
function displayExtractedData(response) {
  // GST Details
  document.getElementById('gstinNumber').textContent = response.gst.data.gstin;
  document.getElementById('legalTradeName').textContent = response.gst.data.legalName;
  document.getElementById('dateOfRegistration').textContent = response.gst.data.registered;
  document.getElementById('panTinCst').textContent = response.gst.data.pan; // If available
  document.getElementById('typeOfRegistration').textContent = response.gst.data.type;
  document.getElementById('registeredAddress').textContent = response.gst.data.address;
  document.getElementById('companyName').textContent = response.gst.data.legalName;

  // Bank Details
  document.getElementById('bank').textContent = response.cheque.data.bank_name;
  document.getElementById('code').textContent = response.cheque.data.code;
  document.getElementById('issuedTo').textContent = response.cheque.data.issued_to;
  document.getElementById('signatory').textContent = response.cheque.data.signatory;
  document.getElementById('accountNumber').textContent = response.cheque.data.account_number;
  document.getElementById('ifsc').textContent = response.cheque.data.ifsc_code;
  document.getElementById('issued').textContent = response.cheque.data.date;
  document.getElementById('branch').textContent = response.cheque.data.branch;

  // PAN Details
  document.getElementById('panNumber').textContent = response.pan.data.pan;
  document.getElementById('name').textContent = response.pan.data.name;
  document.getElementById('dateOfBirthIncorporation').textContent = response.pan.data.incorporationDate;
  document.getElementById('fathersName').textContent = response.pan.data.fatherName;
  document.getElementById('category').textContent = response.pan.data.category;
}
```

3. **Saving Data:**
   - After displaying the data, provide a "Submit" button
   - When clicked, send the extracted data to the company address endpoint
   - Use the address information from the GST document to pre-fill the company address form

### Error Handling

- Handle file size limits (recommended max: 5MB per file)
- Validate file types (PDF or JPG)
- Display appropriate error messages for failed uploads or OCR processing
- Implement retry mechanisms for failed API calls

## Notes for Frontend Developers

1. **PDF to JPG Conversion:**
   - The backend automatically converts PDF files to JPG before OCR processing
   - No additional conversion is needed on the frontend

2. **File Upload Best Practices:**
   - Implement client-side validation for file types and sizes
   - Show upload progress indicators
   - Handle network errors gracefully

3. **Data Display:**
   - Format dates consistently (DD/MM/YYYY)
   - Handle missing or null values with appropriate placeholders
   - Implement loading states while waiting for OCR results

4. **Security:**
   - Store JWT tokens securely (preferably in HttpOnly cookies)
   - Implement token refresh mechanisms
   - Clear sensitive data when the user logs out 