LJT RouteFlow 1.2.0 release

LJTRouteFlow.war is ready for demo-mode deployment to Apache Tomcat 9.
Copy it to: %CATALINA_HOME%\webapps\LJTRouteFlow.war

The application defaults to demo mode when DMS_DEMO_MODE is not set.
For Oracle mode, follow docs\NEXT_STEPS_ORACLE.md and rebuild locally with:
  mvn clean test
  mvn clean package

Open after Tomcat starts:
  http://localhost:8082/LJTRouteFlow/
