# Start LJT RouteFlow with Oracle XE 21c

The Oracle database is already prepared. Do not rerun the schema scripts.

## 1. Apply local environment variables

Open PowerShell and run:

```powershell
$env:CATALINA_HOME = "C:\apache-tomcat-9.0.XX"
$env:DMS_DEMO_MODE = "true"
$env:DMS_DB_URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1"
$env:DMS_DB_USERNAME = "LJT_ROUTE_FLOW"
$env:DMS_DB_PASSWORD = "YOUR_ACTUAL_PASSWORD"
$env:DMS_STORAGE_ROOT = "C:\LJTRouteFlowStorage"
```

Use the real Tomcat path and Oracle password.

## 2. Build and redeploy version 1.3.0

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
mvn clean package

Remove-Item "$env:CATALINA_HOME\webapps\LJTRouteFlow.war" `
    -Force -ErrorAction SilentlyContinue

Remove-Item "$env:CATALINA_HOME\webapps\LJTRouteFlow" `
    -Recurse -Force -ErrorAction SilentlyContinue

Remove-Item "$env:CATALINA_HOME\work\Catalina\localhost\LJTRouteFlow" `
    -Recurse -Force -ErrorAction SilentlyContinue

Copy-Item ".\target\LJTRouteFlow.war" `
    "$env:CATALINA_HOME\webapps\LJTRouteFlow.war"

& "$env:CATALINA_HOME\bin\startup.bat"
```

## 3. Verify Oracle without leaving demo mode

Open:

```text
http://localhost:8082/LJTRouteFlow/setup/database-test
```

Expected:

```text
Configuration source: Environment variables / Java system properties
Connected user: LJT_ROUTE_FLOW
Container: XEPDB1
RouteFlow tables: 10 / 10
```

## 4. Activate Oracle records

After the database test passes:

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
$env:DMS_DEMO_MODE = "false"
& "$env:CATALINA_HOME\bin\startup.bat"
```

Log in with:

```text
clerk / Clerk@123
```

Then test Create Draft, Submit, Recall, Boss Approval, and Department Folder.

## 5. Roll back to demo mode

If an Oracle workflow error appears:

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
$env:DMS_DEMO_MODE = "true"
& "$env:CATALINA_HOME\bin\startup.bat"
```

The Oracle tables remain untouched while demo mode is enabled.
