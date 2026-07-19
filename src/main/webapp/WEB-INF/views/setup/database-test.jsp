<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Oracle Database Test</h1><p>Confirms whether Tomcat can reach the configured Oracle service.</p></div></section>
<section class="card setup-card">
    <dl><dt>Application mode</dt><dd>${demoMode ? 'DEMO - in-memory records' : 'ORACLE - persistent records'}</dd><dt>Configuration source</dt><dd><c:out value="${configSource}"/></dd><dt>Storage root</dt><dd>Environment variable DMS_STORAGE_ROOT or user-home/ljtrouteflow-storage</dd></dl>
    <c:choose><c:when test="${demoMode}"><div class="alert alert-warning">Demo mode is enabled. Set DMS_DEMO_MODE=false and the three DMS_DB_* variables, then restart Tomcat.</div></c:when><c:when test="${databaseOk}"><div class="alert alert-success">Oracle connection succeeded.</div><dl><dt>Database</dt><dd><c:out value="${dbName}"/></dd><dt>Service</dt><dd><c:out value="${serviceName}"/></dd><dt>Server Host</dt><dd><c:out value="${serverHost}"/></dd><dt>Connected User</dt><dd><c:out value="${dbUser}"/></dd></dl></c:when><c:otherwise><div class="alert alert-error"><strong>Connection failed:</strong> <c:out value="${databaseError}"/></div></c:otherwise></c:choose>
</section>
<%@ include file="../common/layout-bottom.jspf" %>
