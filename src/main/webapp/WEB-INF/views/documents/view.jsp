<%@ include file="../common/layout-top.jspf" %>

<section class="page-heading">
    <div>
        <h1><c:out value="${document.documentCode}"/></h1>
        <p><c:out value="${document.title}"/></p>
    </div>
    <button class="btn btn-outline" type="button" onclick="history.back()">Back</button>
</section>

<div class="detail-grid">
    <section class="card detail-card">
        <div class="document-preview">
            <span>PDF</span>
            <strong><c:out value="${empty document.primaryFileName ? 'Main document preview' : document.primaryFileName}"/></strong>
            <small>Downloads are served through an authorised servlet and are not cached by the PWA.</small>
        </div>

        <c:if test="${not empty files}">
            <div class="file-list">
                <c:forEach items="${files}" var="file">
                    <a href="${ctx}/documents/download?fileId=${file.id}">
                        <span aria-hidden="true">□</span>
                        <div>
                            <strong><c:out value="${file.originalName}"/></strong>
                            <small><c:out value="${file.mimeType}"/> · ${file.fileSize} bytes</small>
                        </div>
                        <b>Download</b>
                    </a>
                </c:forEach>
            </div>
        </c:if>

        <h2>Description</h2>
        <p class="body-copy"><c:out value="${document.description}"/></p>

        <c:if test="${not empty document.rejectionReason}">
            <div class="alert alert-error">
                <strong>Correction required:</strong>
                <c:out value="${document.rejectionReason}"/>
            </div>
        </c:if>
    </section>

    <aside class="card info-card">
        <h2>Document Information</h2>
        <dl>
            <dt>Status</dt>
            <dd><span class="badge status-${fn:toLowerCase(fn:replace(document.status,'_','-'))}"><c:out value="${document.status}"/></span></dd>

            <dt>Reference</dt>
            <dd><c:out value="${document.referenceNo}"/></dd>

            <dt>Sender</dt>
            <dd><c:out value="${document.sender}"/></dd>

            <dt>Date Received</dt>
            <dd><c:out value="${document.dateReceived}"/></dd>

            <dt>Category</dt>
            <dd><c:out value="${document.category}"/></dd>

            <dt>Priority</dt>
            <dd><c:out value="${document.priority}"/></dd>

            <%-- <dt>Confidential</dt>
            <dd>${document.confidential ? 'Yes' : 'No'}</dd> --%>

            <dt>Destination</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty document.destinationDepartmentName}">
                        <c:out value="${document.destinationDepartmentName}"/>
                    </c:when>
                    <c:otherwise><span class="muted">Not assigned</span></c:otherwise>
                </c:choose>
            </dd>

            <dt>Selected Boss</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty document.bossName}">
                        <c:out value="${document.bossName}"/>
                    </c:when>
                    <c:otherwise><span class="muted">Not assigned</span></c:otherwise>
                </c:choose>
            </dd>

            <dt>Created By</dt>
            <dd><c:out value="${document.createdByName}"/></dd>

            <dt>Due Date</dt>
            <dd><c:out value="${document.dueDate}"/></dd>
        </dl>

        <c:if test="${document.status == 'PENDING_APPROVAL' && document.createdById == currentUser.id}">
            <form method="post" action="${ctx}/documents/action" data-confirm="Recall this document and stop the current approval request?">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="documentId" value="${document.id}">
                <input type="hidden" name="action" value="RECALL">
                <button class="btn btn-warning btn-block" type="submit">Recall Document</button>
            </form>
        </c:if>

        <c:if test="${currentUser.hasRole('BOSS') && document.status == 'PENDING_APPROVAL'}">
            <a class="btn btn-primary btn-block" href="${ctx}/approvals/review?id=${document.id}">Review and Decide</a>
        </c:if>
    </aside>
</div>

<%@ include file="../common/layout-bottom.jspf" %>
