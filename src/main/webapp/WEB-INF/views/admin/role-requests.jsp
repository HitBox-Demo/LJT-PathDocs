<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Role Requests</h1>
    <p>Role additions/removals require a boss decision before activation.</p></div></section>

<section class="card"><div class="table-wrap"><table><thead><tr><th>Request</th><th>User</th>
    <th>Current Role</th><th>Requested Role</th><th>Requested By</th><th>Date</th><th>Status</th></tr>
</thead><tbody><c:forEach items="${requests}" var="r"><tr><td>#${r.id}</td>
    <td><c:out value="${r.userName}"/></td><td><c:out value="${r.currentRole}"/></td>
    <td><strong><c:out value="${r.requestedRole}"/></strong></td>
    <td><c:out value="${r.requestedByName}"/></td><td><c:out value="${r.requestedAt}"/></td>
    <td><span class="badge status-pending-approval"><c:out value="${r.status}"/></span></td></tr>
</c:forEach><c:if test="${empty requests}"><tr><td colspan="7" class="empty-state">No pending role requests.</td></tr></c:if></tbody></table></div></section>
<div></div>
<div class="alert alert-warning">
    Boss role-request approval is represented in the schema and DAO foundation. 
    Complete the organisation's exact role-change policy after the Oracle workflow is tested.</div>
<%@ include file="../common/layout-bottom.jspf" %>
