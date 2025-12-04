# Debugging Guide

## How to Debug the Application

### 1. View Application Logs

#### Using Docker Compose
```bash
# View all logs
docker-compose logs -f

# View only app logs
docker-compose logs -f app

# View only MySQL logs
docker-compose logs -f mysql

# View last 100 lines
docker-compose logs --tail=100 app
```

#### View Logs in Real-Time
```bash
# Follow logs for specific service
docker logs -f purchase-app
docker logs -f purchase-mysql
```

### 2. Check Application Console Output

The application now includes DEBUG logging statements that will show:
- Currency conversion requests
- Treasury API calls
- Exchange rate lookups
- Error messages

Look for lines starting with `DEBUG:` in the logs.

### 3. Test API Endpoints Directly

#### Using curl
```bash
# Get all purchases
curl -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347" http://localhost:8080/api/purchases

# Get purchases with conversion
curl -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347" "http://localhost:8080/api/purchases/converted?currency=Canada-Dollar"

# Get available countries
curl -H "X-API-Key: wk_3c1f0f65a19444879772ff82833f5347" http://localhost:8080/api/purchases/countries
```

#### Using Browser Developer Tools
1. Open browser Developer Tools (F12)
2. Go to Network tab
3. Make a request from the HTML page
4. Check the request URL and response

### 4. Test Treasury API Directly

Test if the Treasury API is accessible and returning data:

```bash
# Test Treasury API directly
curl "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc,exchange_rate,record_date&filter=country_currency_desc:in:(Canada-Dollar),record_date:gte:2024-06-01,record_date:lte=2024-12-31&sort=-record_date&page[size]=1"
```

### 5. Check Database

```bash
# Connect to MySQL
docker exec -it purchase-mysql mysql -u purchase_user -ppurchase_password purchase_db

# Check purchases
SELECT * FROM purchases;

# Check purchase dates
SELECT id, date, country, currency_code FROM purchases;
```

### 6. Common Issues and Solutions

#### Issue: N/A showing for converted amounts

**Possible Causes:**
1. **Currency format mismatch**: The frontend sends "Canada-Dollar" but Treasury API might use different format
2. **Date range issue**: Purchase date might be too old (more than 6 months) or in the future
3. **Treasury API not accessible**: Network issue or API endpoint changed
4. **No exchange rate data**: Treasury API doesn't have data for that currency/date range

**Debugging Steps:**
1. Check application logs for DEBUG messages
2. Verify the currency format matches Treasury API format
3. Check the purchase date is within valid range
4. Test Treasury API directly with the same parameters

#### Issue: Treasury API returns empty results

**Check:**
- Currency name format (e.g., "Canada-Dollar" vs "Canada Dollar")
- Date format (must be YYYY-MM-DD)
- Date range (must be within last 6 months from purchase date)

#### Issue: Application not starting

```bash
# Check if containers are running
docker-compose ps

# Check container logs
docker-compose logs app

# Restart containers
docker-compose restart
```

### 7. Enable More Verbose Logging

Edit `src/main/resources/application.properties`:

```properties
# Enable debug logging for specific packages
logging.level.com.wexapp.purchaseapp=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.springframework.web.reactive=DEBUG
```

Then rebuild and restart:
```bash
mvn clean package
docker-compose up -d --build
```

### 8. Check Network Connectivity

```bash
# Test if Treasury API is accessible from container
docker exec -it purchase-app curl "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc&page[size]=1"
```

### 9. Verify Currency Format

The Treasury API uses specific currency description formats. Check what formats are available:

```bash
curl "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?fields=country_currency_desc&sort=-record_date&page[size]=10"
```

Look for the exact format used (e.g., "Canada-Dollar", "Mexico-Peso", etc.)

### 10. Test with Sample Data

Create a purchase with a recent date (within last 6 months) and try converting to a currency that should have data:

1. Create purchase with date: Today's date or within last 6 months
2. Try converting to: "Canada-Dollar" or "Mexico-Peso"
3. Check logs for any errors

## Quick Debug Checklist

- [ ] Application is running (`docker-compose ps`)
- [ ] API key is set correctly
- [ ] Check application logs for DEBUG messages
- [ ] Verify Treasury API URL in application.properties
- [ ] Test Treasury API directly with curl
- [ ] Check purchase dates are within valid range (not too old)
- [ ] Verify currency format matches Treasury API format
- [ ] Check browser console for JavaScript errors
- [ ] Check Network tab in browser DevTools for API responses

