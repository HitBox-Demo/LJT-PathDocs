<%@ include file="common/layout-top.jspf" %>
<section class="page-heading">
    <div><h1>Welcome, <c:out value="${currentUser.fullName}"/></h1><p>Overview of documents requiring your attention.</p></div>
    <c:if test="${currentUser.hasRole('CLERK') || currentUser.hasRole('SYSTEM_ADMIN')}"><a class="btn btn-primary" href="${ctx}/documents/create">Create Document</a></c:if>
</section>

<section class="stats-grid">
    <article class="stat-card"><span>Total Visible</span><strong>${stats.TOTAL}</strong><small>Documents within your access</small></article>
    <article class="stat-card stat-blue"><span>Pending</span><strong>${stats.PENDING}</strong><small>Awaiting boss decision</small></article>
    <article class="stat-card stat-yellow"><span>Overdue</span><strong>${stats.OVERDUE}</strong><small>Past the priority deadline</small></article>
    <article class="stat-card stat-green"><span>Routed</span><strong>${stats.ROUTED}</strong><small>Stored in destination folders</small></article>
</section>

<section class="card">
    <div class="card-header"><div><h2>Recent Documents</h2><p>Latest records available to your role.</p></div><a href="${ctx}/documents/list?view=repository">View all</a></div>
    <div class="table-wrap">
        <table>
            <thead><tr><th>Document</th><th>Priority</th><th>Destination</th><th>Status</th><th>Due Date</th><th></th></tr></thead>
            <tbody>
            <c:forEach items="${documents}" var="doc" end="5">
                <tr>
                    <td><a class="doc-link" href="${ctx}/documents/view?id=${doc.id}"><strong><c:out value="${doc.documentCode}"/></strong><span><c:out value="${doc.title}"/></span></a></td>
                    <td><span class="badge priority-${fn:toLowerCase(doc.priority)}"><c:out value="${doc.priority}"/></span></td>
                    <td><c:out value="${doc.destinationDepartmentName}"/></td>
                    <td><span class="badge status-${fn:toLowerCase(fn:replace(doc.status,'_','-'))}"><c:out value="${doc.status}"/></span></td>
                    <td><c:out value="${doc.dueDate}"/></td>
                    <td><a class="btn btn-small btn-outline" href="${ctx}/documents/view?id=${doc.id}">View</a></td>
                </tr>
            </c:forEach>
            <c:if test="${empty documents}"><tr><td colspan="6" class="empty-state">No documents are available.</td></tr></c:if>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="common/layout-bottom.jspf" %>
