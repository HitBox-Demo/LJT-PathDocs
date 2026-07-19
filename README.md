# LJT RouteFlow

LJT RouteFlow is a mobile-first document capture, approval and department-routing Progressive Web Application. It uses Java JSP, Java Servlets, JavaBeans, DAO/JDBC, Oracle Database 21c, Apache Tomcat 9 and Maven WAR packaging.

## Main workflow

`Clerk/System Administrator → Selected Boss → IT, Finance or Management folder`

The clerk captures or uploads a document, selects the destination and boss, and submits it. The boss can approve, return it for correction or change the destination. Approved documents are routed to the authorised department repository.

## Technology

- Java 21
- JSP and Java Servlets using `javax.servlet` for Tomcat 9
- Maven WAR
- Oracle Database 21c
- HTML, CSS and vanilla JavaScript
- PWA manifest, service worker and offline fallback

## Quick demo setup

1. Install Java 21, Maven and Apache Tomcat 9.
2. Open this folder in VS Code.
3. Set Tomcat for the current PowerShell terminal:

```powershell
$env:CATALINA_HOME = "C:\apache-tomcat-9.0.XX"
```

4. Build and deploy in demo mode:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/run-demo.ps1
```

5. Start Tomcat and open:

```text
http://localhost:8082/LJTRouteFlow/
```

To use another port in the helper scripts:

```powershell
$env:DMS_TOMCAT_PORT = "8082"
```

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Clerk/System Administrator | `clerk` | `Clerk@123` |
| Boss/Approver | `boss` | `Boss@123` |
| IT Department User | `it.user` | `Dept@123` |
| Finance Department User | `finance.user` | `Dept@123` |
| Management Department User | `management.user` | `Dept@123` |

Demo records are stored in memory and reset when Tomcat restarts.

## Oracle setup

1. Test the remote listener:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/test-oracle.ps1 -HostName 192.168.1.20 -Port 1521
```

2. Run `database/01_create_schema.sql`, `02_seed_data.sql` and `03_verify_schema.sql` in SQL Developer.
3. Copy `scripts/set-local-env.ps1.example` to `scripts/set-local-env.ps1` and enter the local credentials.
4. Load the file and deploy:

```powershell
. .\scripts\set-local-env.ps1
powershell -ExecutionPolicy Bypass -File scripts/deploy-tomcat.ps1
```

5. Sign in as Clerk and open `/LJTRouteFlow/setup/database-test`.

## Useful commands

```powershell
mvn test
mvn clean package
powershell -ExecutionPolicy Bypass -File scripts/check-tools.ps1
powershell -ExecutionPolicy Bypass -File scripts/deploy-tomcat.ps1
```

## Documentation

- `docs/SETUP_WINDOWS.md`
- `docs/PROJECT_MAP.md`
- `docs/KNOWN_LIMITATIONS.md`
- `docs/CHANGELOG.md`
- `database/README.md`
