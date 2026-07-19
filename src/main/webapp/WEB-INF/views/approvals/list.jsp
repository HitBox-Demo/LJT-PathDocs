<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Pending Approvals</h1><p>Review individually or select multiple normal documents for bulk approval.</p></div><a class="btn btn-outline" href="${ctx}/approvals/list?view=history">Approval History</a></section>
<form method="post" action="${ctx}/approvals/action" class="card" data-confirm="Approve all selected documents?">
    <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="decision" value="APPROVE">
    <div class="bulk-toolbar"><label><input type="checkbox" data-select-all> Select all</label><button class="btn btn-primary" type="submit">Approve Selected</button></div>
    <div class="table-wrap"><table><thead><tr><th></th><th>Document</th><th>Priority</th><th>Destination</th><th>Submitted By</th><th>Due Date</th><th></th></tr></thead><tbody>
    <c:forEach items="${documents}" var="doc"><tr><td><input type="checkbox" name="documentId" value="${doc.id}" data-select-item></td><td><a class="doc-link" href="${ctx}/approvals/review?id=${doc.id}"><strong><c:out value="${doc.documentCode}"/></strong><span><c:out value="${doc.title}"/></span></a></td><td><span class="badge priority-${fn:toLowerCase(doc.priority)}"><c:out value="${doc.priority}"/></span></td><td><c:out value="${doc.destinationDepartmentName}"/></td><td><c:out value="${doc.createdByName}"/></td><td><c:out value="${doc.dueDate}"/></td><td><a class="btn btn-small btn-dark" href="${ctx}/approvals/review?id=${doc.id}">Review</a></td></tr></c:forEach>
    <c:if test="${empty documents}"><tr><td colspan="7" class="empty-state">No documents are waiting for your approval.</td></tr></c:if>
    </tbody></table></div>
</form>
<%@ include file="../common/layout-bottom.jspf" %>
