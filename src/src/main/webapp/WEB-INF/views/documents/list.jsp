<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading">
    <div>
        <h1><c:out value="${pageTitle}"/></h1>
        <p>Search and review documents available to your role.</p>
    </div>
    <c:if test="${currentUser.hasRole('CLERK') || currentUser.hasRole('SYSTEM_ADMIN')}">
        <a class="btn btn-primary" href="${ctx}/documents/create">Create Document</a>
    </c:if>
</section>

<form class="search-bar" method="get" action="${ctx}/documents/list">
    <input type="hidden" name="view" value="${view}">
    <input name="q" value="${fn:escapeXml(query)}" placeholder="Search title, document ID, reference or sender">
    <button class="btn btn-dark" type="submit">Search</button>
</form>

<section class="card table-card">
    <div class="table-wrap" role="region" aria-label="Document table" tabindex="0">
        <table class="data-table document-table">
            <thead>
                <tr>
                    <th>Document</th>
                    <th>Sender / Reference</th>
                    <th>Priority</th>
                    <th>Destination</th>
                    <th>Boss</th>
                    <th>Status</th>
                    <th>Pending / Action</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${documents}" var="doc">
                    <tr>
                        <td class="wrap-cell document-cell">
                            <a class="doc-link" href="${ctx}/documents/view?id=${doc.id}">
                                <strong><c:out value="${doc.documentCode}"/></strong>
                                <span><c:out value="${doc.title}"/></span>
                            </a>
                        </td>
                        <td class="wrap-cell">
                            <strong><c:out value="${doc.sender}"/></strong>
                            <small><c:out value="${doc.referenceNo}"/></small>
                        </td>
                        <td>
                            <span class="badge priority-${fn:toLowerCase(doc.priority)}"><c:out value="${doc.priority}"/></span>
                        </td>
                        <td class="wrap-cell"><c:out value="${doc.destinationDepartmentName}"/></td>
                        <td class="wrap-cell"><c:out value="${doc.bossName}"/></td>
                        <td>
                            <span class="badge status-${fn:toLowerCase(fn:replace(doc.status,'_','-'))}"><c:out value="${doc.status}"/></span>
                        </td>
                        <td class="pending-action-cell">
                            <div class="pending-action-line">
                                <span class="pending-copy">
                                    <c:choose>
                                        <c:when test="${doc.daysPending > 0}">Pending ${doc.daysPending} day(s)</c:when>
                                        <c:otherwise>Not pending</c:otherwise>
                                    </c:choose>
                                </span>
                                <a class="btn btn-small btn-outline" href="${ctx}/documents/view?id=${doc.id}">View</a>
                                <c:if test="${doc.createdById == currentUser.id && (doc.status == 'RETURNED_FOR_CORRECTION' || doc.status == 'DRAFT' || doc.status == 'RECALLED')}">
                                    <a class="btn btn-small btn-secondary" href="${ctx}/documents/edit?id=${doc.id}">
                                        <c:choose>
                                            <c:when test="${doc.status == 'RETURNED_FOR_CORRECTION'}">Correct</c:when>
                                            <c:otherwise>Edit</c:otherwise>
                                        </c:choose>
                                    </a>
                                </c:if>
                            </div>
                        </td>
                    </tr>
                </c:forEach>
                <c:if test="${empty documents}">
                    <tr><td colspan="7" class="empty-state">No matching documents.</td></tr>
                </c:if>
            </tbody>
        </table>
    </div>
</section>
<%@ include file="../common/layout-bottom.jspf" %>
