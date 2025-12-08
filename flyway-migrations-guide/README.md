# Database Migrations with Flyway

Code samples for: [Database Migrations with Flyway: Never Edit Applied Migrations](https://techyowls.io/blog/flyway-database-migrations-spring-boot)

## The Golden Rule

```
┌──────────────────────────────────────────────────────────────────┐
│  NEVER edit a migration that has been applied to any database.  │
│  Create a new migration instead.                                 │
└──────────────────────────────────────────────────────────────────┘
```

## Migration Files

```
src/main/resources/db/migration/
├── V1__create_users_table.sql        # Versioned (runs once)
├── V2__create_products_table.sql
├── V3__add_user_status.sql           # Expand phase
├── V4__make_status_not_null.sql      # Contract phase
├── V5__rename_column_safely.sql      # Safe column rename
├── V6__drop_old_password_column.sql  # Cleanup
└── R__refresh_statistics.sql         # Repeatable (runs on change)
```

## Naming Convention

```
V{version}__{description}.sql   - Versioned (runs once, in order)
R__{description}.sql            - Repeatable (runs when checksum changes)
```

## Expand-Contract Pattern

For zero-downtime deployments:

```
1. EXPAND: Add new column (nullable, with default)
   ├── Old code works (ignores new column)
   └── New code can write to both

2. MIGRATE: Backfill existing data
   └── Copy data to new column

3. DEPLOY: Roll out new code
   └── Writes to new column

4. CONTRACT: Remove old column
   └── Only after all instances updated
```

## Run

```bash
# Start PostgreSQL
docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres:16-alpine

# Run migrations
./mvnw spring-boot:run

# Or just validate
./mvnw flyway:validate -Dflyway.url=jdbc:postgresql://localhost:5432/postgres
```

## Commands

```bash
# Info - show migration status
./mvnw flyway:info

# Migrate - apply pending migrations
./mvnw flyway:migrate

# Validate - check applied match files
./mvnw flyway:validate

# Repair - fix checksum mismatches (use carefully)
./mvnw flyway:repair

# Clean - DROP ALL (never in production!)
./mvnw flyway:clean
```

## Configuration

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
    clean-disabled: true  # ALWAYS true in production
```

## Testing Migrations

```bash
./mvnw test
```

Tests run against real PostgreSQL via Testcontainers.

## Common Patterns

### Adding a Column

```sql
-- V7__add_phone_number.sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

### Adding NOT NULL Column

```sql
-- V7__add_phone_expand.sql (deploy first)
ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT 'unknown';
UPDATE users SET phone = 'unknown' WHERE phone IS NULL;

-- V8__add_phone_contract.sql (deploy after app update)
ALTER TABLE users ALTER COLUMN phone SET NOT NULL;
```

### Renaming Column

Use the expand-contract pattern shown in V5 and V6.

## License

MIT
