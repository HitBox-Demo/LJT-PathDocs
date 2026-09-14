<%@ include file="common/layout-top.jspf" %>

<section class="page-heading">
    <div>
        <h1>Welcome, <c:out value="${currentUser.fullName}"/></h1>
        <p>Overview of documents requiring your attention.</p>
    </div>

    <c:if test="${currentUser.hasRole('CLERK') || currentUser.hasRole('SYSTEM_ADMIN')}">
        <a class="btn btn-primary" href="${ctx}/documents/create">
            Create Document
        </a>
    </c:if>
</section>

<section class="stats-grid" aria-label="Dashboard document filters">
    <a
        class="stat-card<c:if test="${selectedFilter == 'total'}"> is-active</c:if>"
        href="${ctx}/app/dashboard?filter=total"
        aria-label="Show all visible documents"
    >
        <span>Total Documents</span>
        <strong>${stats.TOTAL}</strong>
        <small>Documents within your access</small>
    </a>

    <a
        class="stat-card stat-blue<c:if test="${selectedFilter == 'pending'}"> is-active</c:if>"
        href="${ctx}/app/dashboard?filter=pending"
        aria-label="Show pending documents"
    >
        <span>Pending Documents</span>
        <strong>${stats.PENDING}</strong>
        <small>Awaiting boss decision</small>
    </a>

    <a
        class="stat-card stat-yellow<c:if test="${selectedFilter == 'overdue'}"> is-active</c:if>"
        href="${ctx}/app/dashboard?filter=overdue"
        aria-label="Show overdue documents"
    >
        <span>Overdue Documents</span>
        <strong>${stats.OVERDUE}</strong>
        <small>Past the priority deadline</small>
    </a>

    <a
        class="stat-card stat-green<c:if test="${selectedFilter == 'routed'}"> is-active</c:if>"
        href="${ctx}/app/dashboard?filter=routed"
        aria-label="Show routed documents"
    >
        <span>Routed Documents</span>
        <strong>${stats.ROUTED}</strong>
        <small>Stored in destination folders</small>
    </a>
</section>

<section class="card">
    <div class="card-header">
        <div>
            <h2><c:out value="${selectedFilterLabel}"/></h2>
            <p>
                Showing the latest records matching this dashboard filter.
                Select another summary card to change the list.
            </p>
        </div>

        <a href="${ctx}/documents/list?view=${dashboardListView}">
            View all
        </a>
    </div>

    <div class="table-wrap">
        <table>
            <thead>
                <tr>
                    <th>Document</th>
                    <th>Priority</th>
                    <th>Destination</th>
                    <th>Status</th>
                    <th>Due Date</th>
                    <th></th>
                </tr>
            </thead>

            <tbody>
                <c:forEach items="${documents}" var="doc" end="7">
                    <tr>
                        <td>
                            <a
                                class="doc-link"
                                href="${ctx}/documents/view?id=${doc.id}"
                            >
                                <strong>
                                    <c:out value="${doc.documentCode}"/>
                                </strong>
                                <span>
                                    <c:out value="${doc.title}"/>
                                </span>
                            </a>
                        </td>

                        <td>
                            <span class="badge priority-${fn:toLowerCase(doc.priority)}">
                                <c:out value="${doc.priority}"/>
                            </span>
                        </td>

                        <td>
                            <c:out value="${doc.destinationDepartmentName}"/>
                        </td>

                        <td>
                            <span class="badge status-${fn:toLowerCase(fn:replace(doc.status,'_','-'))}">
                                <c:out value="${doc.status}"/>
                            </span>
                        </td>

                        <td>
                            <c:out value="${doc.dueDate}"/>
                        </td>

                        <td>
                            <a
                                class="btn btn-small btn-outline"
                                href="${ctx}/documents/view?id=${doc.id}"
                            >
                                View
                            </a>
                        </td>
                    </tr>
                </c:forEach>

                <c:if test="${empty documents}">
                    <tr>
                        <td colspan="6" class="empty-state">
                            No documents match this dashboard filter.
                        </td>
                    </tr>
                </c:if>
            </tbody>
        </table>
    </div>
</section>

<%@ include file="common/layout-bottom.jspf" %>
