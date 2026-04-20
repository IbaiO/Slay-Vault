# Slay Vault API (Local setup)

Quick local backend for Android app using Docker: MySQL + PHP (Apache).

## 1) Start services

```bash
docker-compose up -d --build
```

If your Docker installation supports the plugin form, this also works:

```bash
docker compose up -d --build
```

This starts:
- MySQL on `localhost:3307` (database `slay_vault`)
- PHP API on `http://localhost:8080/slay-vault-api/`

## 2) Configure Android base URL (emulator)

In `local.properties`:

```ini
API_BASE_URL=http://10.0.2.2:8080/slay-vault-api/
```

For a physical phone, use your PC LAN IP:

```ini
API_BASE_URL=http://192.168.1.50:8080/slay-vault-api/
```

## 3) Smoke test endpoints

Register:

```bash
curl -s -X POST "http://localhost:8080/slay-vault-api/auth_register.php" \
  -d "usuario=tester&password=1234"
```

Login:

```bash
curl -s -X POST "http://localhost:8080/slay-vault-api/auth_login.php" \
  -d "usuario=tester&password=1234"
```

Sync (replace with returned user id):

```bash
curl -s "http://localhost:8080/slay-vault-api/sync_user_data.php?usuario=u_xxx"
```

Remote CRUD endpoints used by Android push:

- `POST /slay-vault-api/upsert_queen.php`
- `POST /slay-vault-api/delete_queen.php`
- `POST /slay-vault-api/upsert_shade.php`
- `POST /slay-vault-api/delete_shade.php`

## 4) Stop services

```bash
docker-compose down
```

To stop and remove DB volume too:

```bash
docker-compose down -v
```

