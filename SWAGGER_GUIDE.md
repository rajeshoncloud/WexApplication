# Swagger/OpenAPI Documentation Guide

## Overview

The application includes Swagger/OpenAPI documentation that provides an interactive interface to explore and test all API endpoints.

## Accessing Swagger UI

Once the application is running, you can access the Swagger UI at:

**Swagger UI:** `http://localhost:8080/swagger-ui.html`

**OpenAPI JSON:** `http://localhost:8080/api-docs`

## Features

### 1. Interactive API Testing
- **Try It Out**: Click "Try it out" on any endpoint to test it directly from the browser
- **Request Builder**: Fill in request parameters and body using the interactive forms
- **Response Viewer**: See actual API responses with status codes and response bodies

### 2. API Documentation
- **Endpoint Descriptions**: Each endpoint includes detailed descriptions
- **Request/Response Schemas**: View the structure of request and response objects
- **Parameter Documentation**: See all path parameters, query parameters, and request body fields
- **Authentication**: Understand how API key authentication works

### 3. Authentication

The Swagger UI includes an "Authorize" button at the top of the page:

1. Click the **"Authorize"** button (lock icon)
2. Enter your API key in the `X-API-Key` field
3. Click **"Authorize"**
4. Click **"Close"**

Now all authenticated requests will include your API key automatically.

**Note:** API key management endpoints (`/api/apikeys/**`) do not require authentication, so you can test them without authorization.

## Endpoints Documented

### Purchase Controller
- `POST /api/purchases` - Create a new purchase
- `GET /api/purchases` - Get all purchases
- `GET /api/purchases/{id}` - Get purchase by ID
- `GET /api/purchases/converted` - Get purchases with currency conversion
- `GET /api/purchases/countries` - Get available countries and currencies
- `DELETE /api/purchases/{id}` - Delete a purchase

### API Key Controller
- `POST /api/apikeys` - Create a new API key
- `GET /api/apikeys` - Get all API keys
- `GET /api/apikeys/{id}` - Get API key by ID
- `DELETE /api/apikeys/{id}` - Delete an API key

## Example Workflow

1. **Start the application:**
   ```bash
   docker-compose up
   # or
   mvn spring-boot:run
   ```

2. **Open Swagger UI:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **Create an API Key:**
   - Navigate to "API Key Controller"
   - Click on `POST /api/apikeys`
   - Click "Try it out"
   - Enter:
     ```json
     {
       "name": "Test API Key",
       "expirationDate": "2026-12-31"
     }
     ```
   - Click "Execute"
   - Copy the generated API key from the response

4. **Authorize:**
   - Click the "Authorize" button at the top
   - Paste your API key
   - Click "Authorize" and "Close"

5. **Test Purchase Endpoints:**
   - Navigate to "Purchase Controller"
   - Try creating a purchase, getting all purchases, etc.
   - All requests will now include your API key automatically

## Configuration

Swagger configuration can be customized in:
- `src/main/java/com/wexapp/purchaseapp/config/OpenApiConfig.java` - Main configuration
- `src/main/resources/application.properties` - Path and UI settings

## Customization

To customize the Swagger documentation:

1. **Update API Info**: Edit `OpenApiConfig.java` to change title, description, version, etc.
2. **Add Examples**: Add `@Schema` annotations with examples to DTOs
3. **Add Tags**: Use `@Tag` annotation on controllers to group endpoints
4. **Add Descriptions**: Use `@Operation` annotation on methods for detailed descriptions

## Troubleshooting

### Swagger UI not loading
- Ensure the application is running on port 8080
- Check that `springdoc-openapi-starter-webmvc-ui` dependency is in `pom.xml`
- Verify the path: `http://localhost:8080/swagger-ui.html`

### API calls failing with 401
- Make sure you've authorized with a valid API key
- Check that the API key hasn't expired
- Verify the API key format: `wk_<uuid>`

### CORS errors
- The application is configured with `@CrossOrigin(origins = "*")` which should allow all origins
- If issues persist, check browser console for specific CORS error messages

## Additional Resources

- **OpenAPI Specification**: `http://localhost:8080/api-docs`
- **API Documentation (Markdown)**: See `API_DOCUMENTATION.md`
- **SpringDoc Documentation**: https://springdoc.org/

