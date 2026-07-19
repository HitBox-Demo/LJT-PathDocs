# Windows Setup — VS Code, Tomcat 9 and Oracle

## 1. Tool check

```powershell
powershell -ExecutionPolicy Bypass -File scripts/check-tools.ps1
```

Required: Java 21, Maven, Git and Apache Tomcat 9.

## 2. Demo mode

```powershell
$env:CATALINA_HOME = "C:\apache-tomcat-9.0.XX"
powershell -ExecutionPolicy Bypass -File scripts/run-demo.ps1
& "$env:CATALINA_HOME\bin\startup.bat"
```

Open `http://localhost:8082/LJTRouteFlow/`.

## 3. Oracle mode

Test the database server:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/test-oracle.ps1 -HostName 192.168.1.20 -Port 1521
```

Run these scripts using the application schema, not `SYS` or `SYSTEM`:

1. `database/01_create_schema.sql`
2. `database/02_seed_data.sql`
3. `database/03_verify_schema.sql`

Create the local environment file:

```powershell
Copy-Item scripts\set-local-env.ps1.example scripts\set-local-env.ps1
```

Edit and load it:

```powershell
. .\scripts\set-local-env.ps1
powershell -ExecutionPolicy Bypass -File scripts/deploy-tomcat.ps1
```

Restart Tomcat, sign in and open `http://localhost:8082/LJTRouteFlow/setup/database-test`.

## 4. Clean redeployment

The deployment script removes both the current `LJTRouteFlow` deployment and the legacy `docroute` deployment. For a manual clean deployment:

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
Remove-Item "$env:CATALINA_HOME\webapps\LJTRouteFlow.war" -Force -ErrorAction SilentlyContinue
Remove-Item "$env:CATALINA_HOME\webapps\LJTRouteFlow" -Recurse -Force -ErrorAction SilentlyContinue
mvn clean package
Copy-Item ".\target\LJTRouteFlow.war" "$env:CATALINA_HOME\webapps\LJTRouteFlow.war"
& "$env:CATALINA_HOME\bin\startup.bat"
```

## Common errors

- **404:** Confirm `LJTRouteFlow.war` exists in `webapps` and use `/LJTRouteFlow/` exactly.
- **No suitable driver:** Rebuild with `mvn clean package` and redeploy the new WAR.
- **ORA-12541:** Listener/port is unreachable.
- **ORA-12514:** The configured service name is not registered with the listener.
- **ORA-01017:** The username/password or PDB is incorrect.
