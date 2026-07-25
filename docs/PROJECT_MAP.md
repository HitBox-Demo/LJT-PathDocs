# Project Map

| Location | Purpose |
|---|---|
| `pom.xml` | Maven dependencies, tests and `LJTRouteFlow.war` build |
| `src/main/java/com/chekrol/dms/controller` | Servlet routes and form handling |
| `src/main/java/com/chekrol/dms/filter` | Authentication, authorisation, CSRF, encoding and security headers |
| `src/main/java/com/chekrol/dms/listener` | Scheduled overdue-notification check in Oracle mode |
| `src/main/java/com/chekrol/dms/model` | JavaBeans used by JSP, services and DAO |
| `src/main/java/com/chekrol/dms/dao` | Oracle SQL using prepared statements and transactions |
| `src/main/java/com/chekrol/dms/service` | Authentication, document storage/PDF and approval workflows |
| `src/main/java/com/chekrol/dms/util` | Configuration, demo data, storage, passwords and PDF helpers |
| `src/test/java` | Workflow, password and PDF tests run by Maven |
| `src/main/webapp/WEB-INF/views` | Protected JSP views |
| `src/main/webapp/assets/css/app.css` | Main design, responsive tables, forms and image previews |
| `src/main/webapp/assets/js/app.js` | Drawer, validation, PWA and multi-image picker interactions |
| `src/main/webapp/service-worker.js` | Static-only PWA cache and offline fallback |
| `database` | Oracle create, seed, verify and drop scripts |
| `scripts` | Windows checks, verification, build and Tomcat deployment helpers |
| `config` | Optional Tomcat JNDI configuration example |
| `docs` | Setup, tests, cleanup, limitations and Oracle next steps |

## Most important starting points

1. `src/main/java/com/chekrol/dms/util/DemoData.java`
2. `src/main/java/com/chekrol/dms/service/DocumentService.java`
3. `src/main/java/com/chekrol/dms/service/ApprovalService.java`
4. `src/main/java/com/chekrol/dms/dao/DocumentDAO.java`
5. `src/main/java/com/chekrol/dms/dao/ApprovalDAO.java`
6. `src/main/webapp/WEB-INF/views/documents/create.jsp`
7. `src/main/webapp/WEB-INF/views/documents/edit.jsp`
8. `src/main/webapp/assets/js/app.js`
9. `src/main/webapp/assets/css/app.css`
