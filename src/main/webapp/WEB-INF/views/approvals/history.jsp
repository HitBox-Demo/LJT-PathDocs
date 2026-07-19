<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Approval History</h1><p>Recorded approval and rejection decisions.</p></div><a class="btn btn-outline" href="${ctx}/approvals/list">Pending Queue</a></section>
<section class="card"><div class="table-wrap"><table><thead><tr><th>Document</th><th>Decision</th><th>Approver</th><th>Original > Final Department</th><th>Remarks</th><th>Date</th></tr></thead><tbody>
<c:forEach items="${approvals}" var="a"><tr><td><strong><c:out value="${a.documentCode}"/></strong><small><c:out value="${a.documentTitle}"/></small></td><td><span class="badge ${a.decision == 'APPROVE' ? 'status-routed' : 'status-returned-for-correction'}"><c:out value="${a.decision}"/></span></td><td><c:out value="${a.approverName}"/></td><td><c:out value="${a.previousDepartment}"/> → <c:out value="${a.finalDepartment}"/></td><td><c:out value="${a.remarks}"/></td><td><c:out value="${a.decisionAt}"/></td></tr></c:forEach>
<c:if test="${empty approvals}"><tr><td colspan="6" class="empty-state">No approval history yet.</td></tr></c:if>
</tbody></table></div></section>
<%@ include file="../common/layout-bottom.jspf" %>
