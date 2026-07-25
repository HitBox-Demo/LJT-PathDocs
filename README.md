# LJT RouteFlow 1.2.0

LJT RouteFlow is a mobile-first Java web application for document digitisation, boss approval and department routing. It is packaged as one Maven WAR for Apache Tomcat 9 and uses JSP, Java Servlets, JavaBeans, DAO/JDBC, Oracle Database 21c, HTML, CSS and vanilla JavaScript.

## Current status

The application is complete for **demo-mode development and workflow testing**. The remaining environment task is to connect and validate it against the target Oracle Database and office server.

Implemented functions:

- Login, logout, sessions, role-based pages and CSRF protection
- Clerk/System Administrator, Boss and Department User roles
- User creation and boss-approved role-change requests
- Create document, choose multiple images, remove selected images and combine them into one PDF
- Upload an existing main PDF and optional PDF/image/Word/Excel attachments
- Draft, submit, recall, return for correction, edit the same record and resubmit
- Individual and bulk boss approval
- Boss destination change with clerk notification
- Department-based repository access
- Approval history, notifications, pending and overdue indicators
- Responsive mobile/desktop interface and installable PWA shell
- Demo mode and prepared Oracle DAO/SQL mode

## Workflow

```text
Clerk/System Administrator
        -> Selected Boss
        -> IT / Finance / Management folder
```

A returned or recalled document is edited and resubmitted using the same document ID and document code. Its earlier rejection remains in approval history.

## Requirements

- Java JDK 21
- Maven 3.9 or later
- Apache Tomcat 9
- VS Code with Java extensions
- Oracle Database 21c access when database mode is enabled

## Quick demo setup

1. Open the folder containing `pom.xml` in VS Code.
2. Set Tomcat for the current PowerShell terminal:

```powershell
$env:CATALINA_HOME = "C:\apache-tomcat-9.0.XX"
```

3. Build, test and deploy in demo mode:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify-project.ps1
powershell -ExecutionPolicy Bypass -File scripts\run-demo.ps1
& "$env:CATALINA_HOME\bin\startup.bat"
```

4. Open:

```text
http://localhost:8082/LJTRouteFlow/
```

## Demo accounts

| Role | Username | Password |
|---|---|---|
| Clerk/System Administrator | `clerk` | `Clerk@123` |
| Boss/Approver | `boss` | `Boss@123` |
| IT Department User | `it.user` | `Dept@123` |
| Finance Department User | `finance.user` | `Dept@123` |
| Management Department User/Boss | `management.user` | `Dept@123` |

Demo records are stored in memory and reset when Tomcat restarts. Uploaded demo files are stored under the configured storage directory.

## Multiple-image behaviour

The **Choose Images** control supports several images in one picker operation when the browser allows it. Opening the picker again adds another selection batch without replacing earlier images. Selected pages are listed as thumbnails and may be removed individually before submission. The remaining pages are converted into one PDF in the displayed order.

## Oracle connection — next milestone

1. Confirm the Oracle host, port, service name and application-schema credentials.
2. Test the listener:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\test-oracle.ps1 -HostName 192.168.1.20 -Port 1521
```

3. Run in SQL Developer:

```text
database/01_create_schema.sql
database/02_seed_data.sql
database/03_verify_schema.sql
```

4. Copy and edit the local environment file:

```powershell
Copy-Item scripts\set-local-env.ps1.example scripts\set-local-env.ps1
. .\scripts\set-local-env.ps1
```

5. Set `DMS_DEMO_MODE=false`, deploy, restart Tomcat and open:

```text
http://localhost:8082/LJTRouteFlow/setup/database-test
```

Do not commit `scripts/set-local-env.ps1` or database passwords.

## Useful commands

```powershell
mvn clean test
mvn clean package
powershell -ExecutionPolicy Bypass -File scripts\check-tools.ps1
powershell -ExecutionPolicy Bypass -File scripts\verify-project.ps1
powershell -ExecutionPolicy Bypass -File scripts\deploy-tomcat.ps1
```

The Maven output is:

```text
target\LJTRouteFlow.war
```

## Documentation

- `docs/SETUP_WINDOWS.md`
- `docs/PROJECT_MAP.md`
- `docs/CLEANUP_REPORT.md`
- `docs/TEST_REPORT.md`
- `docs/MANUAL_ACCEPTANCE_CHECKLIST.md`
- `docs/NEXT_STEPS_ORACLE.md`
- `docs/KNOWN_LIMITATIONS.md`
- `docs/CHANGELOG.md`
