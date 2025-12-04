# API Documentation

## Overview

This document describes the REST API endpoints for the Purchase Application. The API provides endpoints for managing purchases and API keys.

**Base URL:** `http://localhost:8080/api`

**Content-Type:** `application/json`

---

## Authentication

Most endpoints require API key authentication. API keys can be provided in two ways:

1. **HTTP Header** (Recommended):
   ```
   X-API-Key: wk_xxxxxxxxxxxxx
   ```

2. **Query Parameter**:
   ```
   ?apiKey=wk_xxxxxxxxxxxxx
   ```

**Note:** API key management endpoints (`/api/apikeys/**`) do **NOT** require authentication.

**Default API Key:** If a default API key is configured via the `DEFAULT_API_KEY` environment variable, requests without an API key will use the default key.

---

## Purchase Controller Endpoints

All purchase endpoints require API key authentication and are prefixed with `/api/purchases`.

### 1. Create Purchase

Creates a new purchase record.

**Endpoint:** `POST /api/purchases`

**Authentication:** Required

**Request Body:**
```json
{
  "date": "2025-01-20",
  "description": "Laptop Computer",
  "purchaseAmount": 1299.99,
  "country": "United States"
}
```

**Request Fields:**
- `date` (string, required): Purchase date in `YYYY-MM-DD` format
- `description` (string, required): Purchase description (max 50 characters)
- `purchaseAmount` (number, required): Purchase amount in USD
- `country` (string, required): Country name (e.g., "United States", "Canada", "Mexico")

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2025-01-20",
  "description": "Laptop Computer",
  "purchaseAmount": 1299.99,
  "country": "United States",
  "currencyCode": "United States-Dollar"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
  ```json
  {
    "description": "Description must be less than 51 characters"
  }
  ```
- `401 Unauthorized`: Missing or invalid API key
  ```json
  {
    "error": "API key is required. Please provide X-API-Key header or apiKey query parameter."
  }
  ```

**Example:**
```bash
curl -X POST http://localhost:8080/api/purchases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347" \
  -d '{
    "date": "2025-01-20",
    "description": "Laptop Computer",
    "purchaseAmount": 1299.99,
    "country": "United States"
  }'
```

---

### 2. Get All Purchases

Retrieves all purchase records.

**Endpoint:** `GET /api/purchases`

**Authentication:** Required

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "date": "2025-01-20",
    "description": "Laptop Computer",
    "purchaseAmount": 1299.99,
    "country": "United States",
    "currencyCode": "United States-Dollar"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "date": "2025-01-21",
    "description": "Wireless Mouse",
    "purchaseAmount": 29.99,
    "country": "United States",
    "currencyCode": "United States-Dollar"
  }
]
```

**Error Responses:**
- `401 Unauthorized`: Missing or invalid API key

**Example:**
```bash
curl -X GET http://localhost:8080/api/purchases \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

---

### 3. Get Purchase by ID

Retrieves a specific purchase by its UUID.

**Endpoint:** `GET /api/purchases/{id}`

**Authentication:** Required

**Path Parameters:**
- `id` (string, required): Purchase UUID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2025-01-20",
  "description": "Laptop Computer",
  "purchaseAmount": 1299.99,
  "country": "United States",
  "currencyCode": "United States-Dollar"
}
```

**Error Responses:**
- `404 Not Found`: Purchase not found
- `401 Unauthorized`: Missing or invalid API key

**Example:**
```bash
curl -X GET http://localhost:8080/api/purchases/550e8400-e29b-41d4-a716-446655440000 \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

---

### 4. Get Purchases with Currency Conversion

Retrieves all purchases with amounts converted to a specified currency. Exchange rates are fetched from the U.S. Treasury API based on the purchase date (within 6 months).

**Endpoint:** `GET /api/purchases/converted`

**Authentication:** Required

**Query Parameters:**
- `currency` (string, optional): Target currency in `country_currency_desc` format (e.g., "Canada-Dollar", "UK-Pound", "Japan-Yen")
  - Default: `"United States-Dollar"`

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "date": "2025-01-20",
    "description": "Laptop Computer",
    "purchaseAmount": 1299.99,
    "country": "United States",
    "currencyCode": "United States-Dollar",
    "convertedAmount": 1754.99,
    "exchangeRate": 1.35
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "date": "2025-01-21",
    "description": "Wireless Mouse",
    "purchaseAmount": 29.99,
    "country": "United States",
    "currencyCode": "United States-Dollar",
    "convertedAmount": 40.49,
    "exchangeRate": 1.35
  }
]
```

**Note:** If an exchange rate cannot be found for a purchase (e.g., purchase date is more than 6 months old, or no rate available for that currency/date), `convertedAmount` and `exchangeRate` will be `null`.

**Error Responses:**
- `400 Bad Request`: Exchange rate not found (if explicitly required)
  ```json
  {
    "error": "Exchange rate not found for currency Canada-Dollar for purchase date 2024-01-20"
  }
  ```
- `401 Unauthorized`: Missing or invalid API key

**Example:**
```bash
curl -X GET "http://localhost:8080/api/purchases/converted?currency=Canada-Dollar" \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

---

### 5. Get Available Countries and Currencies

Retrieves the list of all available countries and their currencies. This includes 20 popular currencies (always available) plus additional currencies fetched from the U.S. Treasury API.

**Endpoint:** `GET /api/purchases/countries`

**Authentication:** Required

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "country": "United States",
    "currencyCode": "United States-Dollar",
    "currencyName": "United States-Dollar"
  },
  {
    "country": "Canada",
    "currencyCode": "Canada-Dollar",
    "currencyName": "Canada-Dollar"
  },
  {
    "country": "United Kingdom",
    "currencyCode": "UK-Pound",
    "currencyName": "UK-Pound"
  },
  {
    "country": "Japan",
    "currencyCode": "Japan-Yen",
    "currencyName": "Japan-Yen"
  }
]
```

**Popular Currencies (Always Available):**
- North America: United States, Canada, Mexico
- Europe: United Kingdom, Germany, France, Italy, Spain, Netherlands, Switzerland
- Asia-Pacific: Japan, China, India, South Korea, Singapore, Hong Kong, Australia, New Zealand
- South America: Brazil, Argentina, Chile, Colombia
- Other: South Africa

**Error Responses:**
- `401 Unauthorized`: Missing or invalid API key

**Example:**
```bash
curl -X GET http://localhost:8080/api/purchases/countries \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

---

### 6. Delete Purchase

Deletes a purchase by its UUID.

**Endpoint:** `DELETE /api/purchases/{id}`

**Authentication:** Required

**Path Parameters:**
- `id` (string, required): Purchase UUID

**Response:** `204 No Content` (on success)

**Error Responses:**
- `404 Not Found`: Purchase not found
- `401 Unauthorized`: Missing or invalid API key

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/purchases/550e8400-e29b-41d4-a716-446655440000 \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

---

## API Key Controller Endpoints

All API key endpoints are prefixed with `/api/apikeys` and do **NOT** require authentication.

### 1. Create API Key

Creates a new API key. The API key value is automatically generated in the format `wk_<uuid>`.

**Endpoint:** `POST /api/apikeys`

**Authentication:** Not required

**Request Body:**
```json
{
  "name": "Production API Key",
  "expirationDate": "2026-12-31"
}
```

**Request Fields:**
- `name` (string, required): API key name/description (max 100 characters)
- `expirationDate` (string, required): Expiration date in `YYYY-MM-DD` format

**Response:** `201 Created`

**Response Body:**
```json
{
  "id": 1,
  "name": "Production API Key",
  "apiKey": "wk_3c1f0f65a19444879772ff82833f5347",
  "expirationDate": "2026-12-31"
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
  ```json
  {
    "name": "Name is required",
    "expirationDate": "Expiration date is required"
  }
  ```

**Example:**
```bash
curl -X POST http://localhost:8080/api/apikeys \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Production API Key",
    "expirationDate": "2026-12-31"
  }'
```

---

### 2. Get All API Keys

Retrieves all API keys (excluding the actual API key values for security).

**Endpoint:** `GET /api/apikeys`

**Authentication:** Not required

**Response:** `200 OK`

**Response Body:**
```json
[
  {
    "id": 1,
    "name": "Production API Key",
    "apiKey": "wk_3c1f0f65a19444879772ff82833f5347",
    "expirationDate": "2026-12-31"
  },
  {
    "id": 2,
    "name": "Development API Key",
    "apiKey": "wk_4d2e9516b2955598a883gg93944g6458",
    "expirationDate": "2025-06-30"
  }
]
```

**Example:**
```bash
curl -X GET http://localhost:8080/api/apikeys
```

---

### 3. Get API Key by ID

Retrieves a specific API key by its ID.

**Endpoint:** `GET /api/apikeys/{id}`

**Authentication:** Not required

**Path Parameters:**
- `id` (integer, required): API key ID

**Response:** `200 OK`

**Response Body:**
```json
{
  "id": 1,
  "name": "Production API Key",
  "apiKey": "wk_3c1f0f65a19444879772ff82833f5347",
  "expirationDate": "2026-12-31"
}
```

**Error Responses:**
- `404 Not Found`: API key not found

**Example:**
```bash
curl -X GET http://localhost:8080/api/apikeys/1
```

---

### 4. Delete API Key

Deletes an API key by its ID.

**Endpoint:** `DELETE /api/apikeys/{id}`

**Authentication:** Not required

**Path Parameters:**
- `id` (integer, required): API key ID

**Response:** `204 No Content` (on success)

**Example:**
```bash
curl -X DELETE http://localhost:8080/api/apikeys/1
```

---

## Data Models

### PurchaseDTO

```json
{
  "id": "string (UUID)",
  "date": "string (YYYY-MM-DD)",
  "description": "string (max 50 characters, required)",
  "purchaseAmount": "number (decimal)",
  "country": "string",
  "currencyCode": "string"
}
```

### PurchaseWithConversionDTO

Extends `PurchaseDTO` with conversion fields:

```json
{
  "id": "string (UUID)",
  "date": "string (YYYY-MM-DD)",
  "description": "string",
  "purchaseAmount": "number (decimal)",
  "country": "string",
  "currencyCode": "string",
  "convertedAmount": "number (decimal) | null",
  "exchangeRate": "number (decimal) | null"
}
```

### ApiKeyDTO

```json
{
  "id": "integer",
  "name": "string (max 100 characters, required)",
  "apiKey": "string (format: wk_<uuid>)",
  "expirationDate": "string (YYYY-MM-DD, required)"
}
```

### CountryCurrencyDTO

```json
{
  "country": "string",
  "currencyCode": "string (country_currency_desc format)",
  "currencyName": "string"
}
```

---

## Error Responses

### Standard Error Format

Most errors return a JSON object with an `error` field:

```json
{
  "error": "Error message here"
}
```

### Validation Errors

Validation errors return a map of field names to error messages:

```json
{
  "fieldName": "Error message for this field",
  "anotherField": "Another error message"
}
```

### HTTP Status Codes

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Request successful, no content to return
- `400 Bad Request`: Invalid request (validation errors, missing fields)
- `401 Unauthorized`: Missing or invalid API key
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

---

## Currency Conversion Notes

1. **Exchange Rate Source**: Exchange rates are fetched from the U.S. Treasury Fiscal Data API
2. **Date Range**: Exchange rates are only available for purchases within the last 6 months from the purchase date
3. **Rate Lookup**: The system searches for exchange rates from the purchase date backwards up to 6 months
4. **Missing Rates**: If no exchange rate is found, `convertedAmount` and `exchangeRate` will be `null` in the response
5. **Currency Format**: Currencies use the `country_currency_desc` format (e.g., "Canada-Dollar", "UK-Pound", "Japan-Yen")

---

## CORS Support

All endpoints support Cross-Origin Resource Sharing (CORS) with `Access-Control-Allow-Origin: *`.

---

## Examples

### Complete Workflow Example

1. **Create an API Key:**
```bash
curl -X POST http://localhost:8080/api/apikeys \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My API Key",
    "expirationDate": "2026-12-31"
  }'
```

2. **Create a Purchase:**
```bash
curl -X POST http://localhost:8080/api/purchases \
  -H "Content-Type: application/json" \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347" \
  -d '{
    "date": "2025-01-20",
    "description": "Laptop Computer",
    "purchaseAmount": 1299.99,
    "country": "United States"
  }'
```

3. **Get Purchases with Currency Conversion:**
```bash
curl -X GET "http://localhost:8080/api/purchases/converted?currency=Canada-Dollar" \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

4. **Get Available Countries:**
```bash
curl -X GET http://localhost:8080/api/purchases/countries \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

5. **Delete a Purchase:**
```bash
curl -X DELETE http://localhost:8080/api/purchases/550e8400-e29b-41d4-a716-446655440000 \
  -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347"
```

---

## Support

For issues or questions, please refer to the main README.md file or check the application logs.

