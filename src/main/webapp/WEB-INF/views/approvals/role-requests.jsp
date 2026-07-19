<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div>
    <h1>Role Approval</h1><p>Approve or reject role changes submitted by the Clerk/System Administrator.</p></div></section>
<section class="card"><div class="table-wrap"><table><thead>
    <tr><th>Request</th><th>User</th><th>Current > Requested</th>
        <th>Requested By</th><th>Date</th><th>Decision</th></tr></thead><tbody>
<c:forEach items="${requests}" var="r"><tr><td>#${r.id}</td>
    <td><c:out value="${r.userName}"/></td>
    <td><c:out value="${r.currentRole}"/><strong>
        <c:out value="${r.requestedRole}"/></strong></td>
        <td><c:out value="${r.requestedByName}"/></td>
        <td><c:out value="${r.requestedAt}"/></td>
        <td><form method="post" action="${ctx}/approvals/role-requests" class="role-decision">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <input type="hidden" name="requestId" value="${r.id}">
            <input name="remarks" placeholder="Optional remarks">
            <button class="btn btn-small btn-primary" name="decision" value="APPROVE">Approve</button>
            <button class="btn btn-small btn-danger" name="decision" value="REJECT">Reject</button>
        </form></td></tr></c:forEach>
<c:if test="${empty requests}"><tr><td colspan="6" class="empty-state">No pending role requests.</td></tr></c:if>
</tbody></table></div></section>
<%@ include file="../common/layout-bottom.jspf" %>
