<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading">
    <div>
        <h1>Connection Database</h1>
        <p>Confirms whether Tomcat can reach the configured Oracle service and RouteFlow schema.</p>
    </div>
</section>

<section class="card setup-card">
    <dl>
        <dt>Application mode</dt>
        <dd>${demoMode ? 'DEMO - in-memory records' : 'ORACLE - persistent records'}</dd>

        <dt>Configuration source</dt>
        <dd><c:out value="${configSource}"/></dd>

        <dt>Storage root</dt>
        <dd><c:out value="${storageRoot}"/></dd>
    </dl>

    <c:choose>
        <c:when test="${not databaseConfigured}">
            <div class="alert alert-warning">
                Oracle is not configured in Tomcat. Set DMS_DB_URL,
                DMS_DB_USERNAME and DMS_DB_PASSWORD, then restart Tomcat.
            </div>
        </c:when>

        <c:when test="${databaseOk}">
            <div class="alert alert-success">
                Oracle connection and the complete RouteFlow schema are ready.
            </div>

            <dl>
                <dt>Database</dt>
                <dd><c:out value="${dbName}"/></dd>

                <dt>Service</dt>
                <dd><c:out value="${serviceName}"/></dd>

                <dt>Container</dt>
                <dd><c:out value="${containerName}"/></dd>

                <dt>Server host</dt>
                <dd><c:out value="${serverHost}"/></dd>

                <dt>Connected user</dt>
                <dd><c:out value="${dbUser}"/></dd>

                <dt>RouteFlow tables</dt>
                <dd><c:out value="${routeFlowTableCount}"/> / 10</dd>
            </dl>

            <c:if test="${demoMode}">
                <div class="alert alert-warning">
                    The database test passed, but the application is intentionally
                    still using demo data. Change DMS_DEMO_MODE=false only when
                    you are ready for Oracle workflow testing.
                </div>
            </c:if>
        </c:when>

        <c:when test="${not empty databaseError}">
            <div class="alert alert-error">
                <strong>Connection failed:</strong>
                <c:out value="${databaseError}"/>
            </div>
        </c:when>

        <c:otherwise>
            <div class="alert alert-error">
                Oracle was reached, but the expected RouteFlow database setup is incomplete.
            </div>

            <dl>
                <dt>Connected user</dt>
                <dd><c:out value="${dbUser}"/></dd>

                <dt>Expected user</dt>
                <dd>LJT_ROUTE_FLOW</dd>

                <dt>Container</dt>
                <dd><c:out value="${containerName}"/></dd>

                <dt>Expected container</dt>
                <dd>XEPDB1</dd>

                <dt>RouteFlow tables</dt>
                <dd><c:out value="${routeFlowTableCount}"/> / 10</dd>
            </dl>
        </c:otherwise>
    </c:choose>
</section>
<%@ include file="../common/layout-bottom.jspf" %>
