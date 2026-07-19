<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Notifications</h1>
    <p>Approval, correction, destination-change and overdue alerts.</p>
</div><form method="post" action="${ctx}/notifications/list">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <button class="btn btn-outline" type="submit">Mark All Read</button></form></section>
<section class="notification-list"><c:forEach items="${notifications}" var="n">
    <article class="card notification ${n.read ? '' : 'unread'}">
        <%--<span class="notification-icon" aria-hidden="true"></span>--%>
        <div><strong>
            <c:out value="${n.type}"/></strong><p><c:out value="${n.message}"/></p>
            <small><c:out value="${n.createdAt}"/></small></div>
            <c:if test="${not empty n.documentId}">
                <a class="btn btn-small btn-outline" href="${ctx}/documents/view?id=${n.documentId}">Open</a>
            </c:if></article></c:forEach><c:if test="${empty notifications}">
                <div class="card empty-state">No notifications.</div></c:if></section>
<%@ include file="../common/layout-bottom.jspf" %>
