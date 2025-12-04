# Purchase Application

A sample web application that allows customers to purchase items and view their purchases in different currencies. The application is fully dockerized with MySQL database, Java Spring Boot backend, and HTML frontend.

## Features

- Add purchases with date, description, amount, and country
- View all purchases in different currencies
- Automatic currency conversion from USD to selected currency
- Country-specific transaction tracking
- Modern, responsive HTML UI

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Database**: MySQL 8.0
- **Frontend**: HTML, CSS, JavaScript
- **Containerization**: Docker, Docker Compose
- **Web Server**: Nginx (for serving static files)

## Project Structure

```
.
├── src/
│   └── main/
│       ├── java/com/wexapp/purchaseapp/
│       │   ├── PurchaseApplication.java
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── entity/
│       │   └── dto/
│       └── resources/
│           └── application.properties
├── ui/
│   └── index.html
├── docker-compose.yml
├── Dockerfile
├── init.sql
├── nginx.conf
└── pom.xml
```

## Getting Started

### Prerequisites

- Docker and Docker Compose installed
- Maven (optional, for local development)

### Running the Application

# Run all tests
mvn test

# Run only PurchaseControllerTest
mvn test -Dtest=PurchaseControllerTest

# Run a specific test method
mvn test -Dtest=PurchaseControllerTest#testCreatePurchase_Success

1. **Clone or navigate to the project directory**

2. **Start all services using Docker Compose:**
   ```bash
   docker-compose up --build
   ```

   This will:
   - Build the Java application
   - Start MySQL database
   - Start the Spring Boot application
   - Start Nginx web server

3. **Access the application:**
   - Web UI: http://localhost
   - API: http://localhost/api/purchases

## API Key Authentication

All purchase API endpoints require API key authentication. You can manage API keys through the web interface or set a default API key via environment variable.

### Creating a New API Key

1. **Using the Web Interface:**
   - Navigate to http://localhost/apikeys.html
   - Enter a name for your API key (e.g., "My Application Key")
   - Optionally set an expiration date
   - Click "Generate API Key"
   - Copy the generated API key (format: `wk_xxxxxxxxxxxxx`)
   - **Important:** Save the API key immediately as it won't be shown again

2. **Using SQL (for initial setup):**
   ```sql
   INSERT INTO api_keys (name, api_key, expiration_date) 
   VALUES ('Default Key', 'wk_3c1f0f65a19444879772ff82833f5347', DATE_ADD(CURDATE(), INTERVAL 1 YEAR));
   ```

### Setting the API Key in the Web UI

1. **On the Purchase Application Page:**
   - Navigate to http://localhost
   - In the "🔑 API Key Configuration" section at the top
   - Enter your API key in the input field
   - Click "Set API Key"
   - The API key will be saved in your browser's session storage
   - All subsequent API calls will automatically include this API key

### Setting a Default API Key (Environment Variable)

You can set a default API key that will be used if no API key is provided in requests:

**Option 1: Using Docker Compose**
Edit `docker-compose.yml` and add the environment variable:
```yaml
environment:
  DEFAULT_API_KEY: wk_3c1f0f65a19444879772ff82833f5347
```

**Option 2: Using Environment Variable**
```bash
export DEFAULT_API_KEY=wk_3c1f0f65a19444879772ff82833f5347
```

**Option 3: Using application.properties**
Add to `src/main/resources/application.properties`:
```properties
default.api.key=wk_3c1f0f65a19444879772ff82833f5347
```

### Using API Keys in API Calls

**Option 1: HTTP Header (Recommended)**
```bash
curl -H "X-API-Key: wk_xxxxxxxxxxxxx" http://localhost/api/purchases
```

**Option 2: Query Parameter**
```bash
curl http://localhost/api/purchases?apiKey=wk_xxxxxxxxxxxxx
```

**Option 3: Using Default API Key**
If a default API key is configured via environment variable, requests without an API key will use the default.

### API Key Management Endpoints

- `POST /api/apikeys` - Create a new API key (no authentication required)
- `GET /api/apikeys` - List all API keys (no authentication required)
- `GET /api/apikeys/{id}` - Get API key by ID (no authentication required)

**Note:** API key management endpoints (`/api/apikeys/**`) do not require authentication, allowing you to create keys without an existing key.

### API Endpoints

**Note:** All purchase endpoints require API key authentication via `X-API-Key` header or `apiKey` query parameter.

- `GET /api/purchases` - Get all purchases
- `GET /api/purchases/{id}` - Get purchase by ID
- `POST /api/purchases` - Create a new purchase
- `DELETE /api/purchases/{id}` - Delete a purchase
- `GET /api/purchases/converted?currency={code}` - Get purchases with currency conversion
- `GET /api/purchases/countries` - Get available countries and currencies

### Sample Purchase JSON

```json
{
  "date": "2024-01-20",
  "description": "Laptop Computer",
  "purchaseAmount": 1299.99,
  "country": "United States"
}
```

## Database Schema

### Purchases Table

The `purchases` table includes:
- `id` (CHAR(36), Primary Key, UUID)
- `date` (DATE)
- `description` (VARCHAR(50))
- `purchase_amount` (DECIMAL(10,2))
- `country` (VARCHAR(100))
- `currency_code` (VARCHAR(3))
- `created_at` (TIMESTAMP)

### API Keys Table

The `api_keys` table includes:
- `id` (BIGINT, Primary Key, Auto Increment)
- `name` (VARCHAR(100), Unique)
- `api_key` (VARCHAR(255), Unique)
- `expiration_date` (DATE)
- `created_at` (TIMESTAMP)

## Stopping the Application

To stop all services:
```bash
docker-compose down
```

To stop and remove volumes (including database data):
```bash
docker-compose down -v
```

## Development

### Running Locally (without Docker)

1. Start MySQL database separately
2. Update `application.properties` with your database connection
3. Run: `mvn spring-boot:run`
4. Access the application at http://localhost:8080

## Notes

- The application assumes all purchase amounts are in USD
- Currency conversion is performed when viewing purchases
- Sample data is automatically loaded on first database initialization
- API keys are required for all purchase API endpoints
- API keys stored in browser session storage persist for the browser session
- Description field is limited to 50 characters

