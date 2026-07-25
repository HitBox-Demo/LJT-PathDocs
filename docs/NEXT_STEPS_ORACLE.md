# Next Steps — Oracle Integration

The code is already split between demo mode and DAO/JDBC mode. Do not redesign the frontend before completing these steps.

## 1. Obtain connection information

- Oracle server IP/hostname
- Listener port, normally `1521`
- Service name, for example `XEPDB1`
- Dedicated application-schema username and password

## 2. Test network access

```powershell
powershell -ExecutionPolicy Bypass -File scripts\test-oracle.ps1 -HostName YOUR_ORACLE_IP -Port 1521
```

Continue only when `TcpTestSucceeded` is `True`.

## 3. Create and verify the schema

Run as the dedicated application schema, not as the web application using `SYS` or `SYSTEM`:

1. `database/01_create_schema.sql`
2. `database/02_seed_data.sql`
3. `database/03_verify_schema.sql`

## 4. Configure the application locally

```powershell
Copy-Item scripts\set-local-env.ps1.example scripts\set-local-env.ps1
```

Set values such as:

```powershell
$env:DMS_DEMO_MODE = "false"
$env:DMS_DB_URL = "jdbc:oracle:thin:@//192.168.1.20:1521/XEPDB1"
$env:DMS_DB_USERNAME = "LJT_ROUTE_FLOW"
$env:DMS_DB_PASSWORD = "your-password"
$env:DMS_STORAGE_ROOT = "C:\LJTRouteFlowStorage"
```

Load the file in the same terminal that starts Tomcat.

## 5. Deploy and test

```powershell
. .\scripts\set-local-env.ps1
powershell -ExecutionPolicy Bypass -File scripts\deploy-tomcat.ps1
```

Restart Tomcat and open:

```text
http://localhost:8082/LJTRouteFlow/setup/database-test
```

Then perform the complete manual acceptance checklist using newly created Oracle records.

## 6. Production preparation

- Change all seed passwords.
- Use HTTPS and secure session cookies.
- Confirm the storage directory is writable only by the Tomcat service account.
- Configure backups and retention.
- Add malware scanning for uploaded files.
- Test simultaneous clerk/boss/department sessions.
