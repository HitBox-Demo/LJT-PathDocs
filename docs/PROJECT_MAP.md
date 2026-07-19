# Project Map

| Location | Purpose |
|---|---|
| `pom.xml` | Maven dependencies and `LJTRouteFlow.war` build |
| `src/main/java/com/chekrol/dms/controller` | Servlet routes and form handling |
| `src/main/java/com/chekrol/dms/filter` | Authentication, authorisation, CSRF and security headers |
| `src/main/java/com/chekrol/dms/model` | JavaBeans used by JSP and DAO |
| `src/main/java/com/chekrol/dms/dao` | Oracle SQL using prepared statements |
| `src/main/java/com/chekrol/dms/service` | Authentication, document and approval workflows |
| `src/main/java/com/chekrol/dms/util` | Configuration, storage, password and PDF helpers |
| `src/main/webapp/WEB-INF/views` | Protected JSP views |
| `src/main/webapp/assets` | Application CSS, JavaScript and PWA icons |
| `database` | Oracle create, seed, verify and drop scripts |
| `scripts` | Windows tool-check, build and deployment helpers |
| `config` | Optional Tomcat configuration examples |

Important starting points:

1. `src/main/java/com/chekrol/dms/util/DemoData.java`
2. `src/main/java/com/chekrol/dms/controller/DocumentCreateServlet.java`
3. `src/main/java/com/chekrol/dms/service/DocumentService.java`
4. `src/main/java/com/chekrol/dms/dao/DocumentDAO.java`
5. `src/main/webapp/WEB-INF/views/documents/create.jsp`
6. `src/main/webapp/assets/css/app.css`
