# Docker Setup Instructions

## How to Ensure init.sql Runs

The `init.sql` file is automatically mounted to MySQL's initialization directory in `docker-compose.yml`:
```yaml
volumes:
  - ./init.sql:/docker-entrypoint-initdb.d/init.sql
```

### Important Notes:

1. **init.sql only runs on FIRST initialization**: MySQL's `/docker-entrypoint-initdb.d/` directory only executes scripts when the database is first created (when the data volume is empty).

2. **To re-run init.sql**, you need to remove the existing MySQL volume:

   ```bash
   # Stop containers
   docker-compose down
   
   # Remove the MySQL volume (WARNING: This deletes all data!)
   docker volume rm wexapp_mysql_data
   
   # Or remove all volumes
   docker-compose down -v
   
   # Start fresh
   docker-compose up -d
   ```

3. **First-time setup**:
   ```bash
   docker-compose up -d
   ```
   The init.sql will run automatically when MySQL starts for the first time.

### Verifying init.sql Ran Successfully

After starting the containers, you can verify:

```bash
# Check MySQL logs
docker logs purchase-mysql

# Connect to MySQL and verify tables
docker exec -it purchase-mysql mysql -u purchase_user -ppurchase_password purchase_db -e "SHOW TABLES;"

# Check if sample data exists
docker exec -it purchase-mysql mysql -u purchase_user -ppurchase_password purchase_db -e "SELECT COUNT(*) FROM purchases;"
```

### Troubleshooting

If init.sql doesn't run:
1. Check MySQL container logs: `docker logs purchase-mysql`
2. Ensure the volume doesn't already exist (remove it if needed)
3. Verify the file path is correct in docker-compose.yml
4. Check file permissions (should be readable)

