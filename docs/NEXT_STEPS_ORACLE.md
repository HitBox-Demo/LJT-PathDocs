# Oracle activation steps

## Completed database setup

The local database is Oracle XE 21c with:

```text
Host: localhost
Port: 1521
Service: XEPDB1
Schema: LJT_ROUTE_FLOW
```

The schema, seed data, verification and transaction smoke test are complete.

## 1. Configure Tomcat environment

Use the same terminal that starts Tomcat:

```powershell
$env:DMS_DEMO_MODE = "true"
$env:DMS_DB_URL = "jdbc:oracle:thin:@//localhost:1521/XEPDB1"
$env:DMS_DB_USERNAME = "LJT_ROUTE_FLOW"
$env:DMS_DB_PASSWORD = "YOUR_ACTUAL_PASSWORD"
$env:DMS_STORAGE_ROOT = "C:\LJTRouteFlowStorage"
```

Restart Tomcat, then open:

```text
http://localhost:8082/LJTRouteFlow/setup/database-test
```

Version 1.3.0 tests Oracle even while demo mode remains enabled. Expect:

```text
Connected user: LJT_ROUTE_FLOW
Container: XEPDB1
RouteFlow tables: 10 / 10
```

## 2. Activate Oracle workflow

Only after the database test passes:

```powershell
$env:DMS_DEMO_MODE = "false"
```

Restart Tomcat and log in with the seeded Oracle account:

```text
clerk / Clerk@123
```

Then test draft, submit, recall, boss decision and department routing.

## Duplicate protection

Create Document now stores the validated form submission token in
`DMS_DOCUMENT.SUBMISSION_KEY`. A unique database constraint prevents the same
request from generating a second document, history row or boss notification.
