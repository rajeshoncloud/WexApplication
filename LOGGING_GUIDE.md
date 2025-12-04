# Logging Guide

## How to View Logs

### 1. View Logs in Docker Containers

#### View Real-Time Logs (Follow Mode)
```bash
# View all application logs in real-time
docker-compose logs -f app

# View last 100 lines and follow
docker-compose logs --tail=100 -f app

# View logs from all services
docker-compose logs -f
```

#### View Specific Number of Lines
```bash
# View last 50 lines
docker-compose logs --tail=50 app

# View last 200 lines
docker-compose logs --tail=200 app
```

#### View Logs for Specific Time Period
```bash
# View logs since a specific time
docker-compose logs --since 10m app    # Last 10 minutes
docker-compose logs --since 1h app    # Last 1 hour
docker-compose logs --since 2024-12-04T08:00:00 app
```

#### Using Docker Commands Directly
```bash
# View logs for the app container
docker logs -f purchase-app

# View last 100 lines
docker logs --tail=100 purchase-app

# View logs with timestamps
docker logs -t purchase-app
```

### 2. Filter Logs by Level or Content

#### Using grep (Linux/Mac/WSL)
```bash
# View only DEBUG logs
docker-compose logs app | grep DEBUG

# View only ERROR logs
docker-compose logs app | grep ERROR

# View logs for specific class
docker-compose logs app | grep CurrencyService

# View logs for currency conversion
docker-compose logs app | grep -i currency
```

#### Using PowerShell (Windows)
```powershell
# View only DEBUG logs
docker-compose logs app | Select-String "DEBUG"

# View only ERROR logs
docker-compose logs app | Select-String "ERROR"

# View logs for specific class
docker-compose logs app | Select-String "CurrencyService"

# View logs for currency conversion
docker-compose logs app | Select-String -Pattern "currency" -CaseSensitive:$false
```

### 3. Save Logs to File

```bash
# Save all logs to a file
docker-compose logs app > app-logs.txt

# Save logs with timestamps
docker-compose logs -t app > app-logs-with-timestamps.txt

# Append logs to existing file
docker-compose logs app >> app-logs.txt
```

### 4. View Logs in Real-Time with Filtering

```bash
# Follow logs and filter for errors only
docker-compose logs -f app | grep ERROR

# Follow logs and filter for currency-related messages
docker-compose logs -f app | grep -i currency
```

## Log Levels

Based on the current configuration in `application.properties`:

- **DEBUG**: Detailed debugging information (enabled for `com.wexapp.purchaseapp`)
- **INFO**: General informational messages
- **WARN**: Warning messages
- **ERROR**: Error messages with stack traces

### Current Log Format

Logs are formatted as:
```
yyyy-MM-dd HH:mm:ss - message
```

Example:
```
2024-12-04 08:57:07 - Fetching currencies from Treasury API
2024-12-04 08:57:07 - Base URL: https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange
```

## Changing Log Levels

### Option 1: Edit application.properties

Edit `src/main/resources/application.properties`:

```properties
# For production (less verbose)
logging.level.com.wexapp.purchaseapp=INFO

# For debugging (more verbose)
logging.level.com.wexapp.purchaseapp=DEBUG

# For only warnings and errors
logging.level.com.wexapp.purchaseapp=WARN
```

Then rebuild and restart:
```bash
docker-compose up -d --build app
```

### Option 2: Use Environment Variables

Add to `docker-compose.yml`:
```yaml
environment:
  LOGGING_LEVEL_COM_WEXAPP_PURCHASEAPP: DEBUG
```

Or set when running:
```bash
LOGGING_LEVEL_COM_WEXAPP_PURCHASEAPP=DEBUG docker-compose up -d
```

## Logging to File

To save logs to a file instead of just console, add to `application.properties`:

```properties
# Log to file
logging.file.name=logs/purchase-app.log
logging.file.max-size=10MB
logging.file.max-history=10
```

Or use logback configuration file (more advanced).

## Common Log Viewing Commands

### Quick Reference

```bash
# Most common: View real-time logs
docker-compose logs -f app

# View recent errors
docker-compose logs app | grep ERROR

# View last 50 lines
docker-compose logs --tail=50 app

# View logs since container started
docker logs purchase-app

# Save logs to file
docker-compose logs app > logs.txt
```

## Troubleshooting

### If logs are not showing:

1. **Check if container is running:**
   ```bash
   docker-compose ps
   ```

2. **Check if logs are being generated:**
   ```bash
   docker-compose logs app | head -20
   ```

3. **Verify log level configuration:**
   ```bash
   docker exec purchase-app cat /app/application.properties | grep logging
   ```

4. **Check container logs directly:**
   ```bash
   docker logs purchase-app --tail=50
   ```

### If too many logs:

1. **Change log level to INFO or WARN:**
   ```properties
   logging.level.com.wexapp.purchaseapp=INFO
   ```

2. **Filter logs when viewing:**
   ```bash
   docker-compose logs app | grep -v DEBUG
   ```

## Example: Viewing Currency Conversion Logs

```bash
# View all currency-related logs
docker-compose logs app | grep -i currency

# Follow currency conversion in real-time
docker-compose logs -f app | grep -i "exchange rate\|currency\|converting"

# View errors related to currency
docker-compose logs app | grep -i "error.*currency\|currency.*error"
```

