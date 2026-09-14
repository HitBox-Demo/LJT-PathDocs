<%@ include file="../common/layout-top.jspf" %>

<section class="page-heading">
    <div>
        <h1><c:out value="${document.documentCode}"/></h1>
        <p><c:out value="${document.title}"/></p>
    </div>

    <button
        class="btn btn-outline"
        type="button"
        onclick="history.back()"
    >
        Back
    </button>
</section>

<div class="detail-grid">
    <section class="card detail-card">
        <c:choose>
            <c:when test="${not empty primaryFile}">
                <div class="document-thumbnail-preview">
                    <a
                        class="document-thumbnail-frame"
                        href="${ctx}/documents/download?fileId=${primaryFile.id}"
                        aria-label="Download the original primary document"
                    >
                        <img
                            src="${ctx}/documents/thumbnail?fileId=${primaryFile.id}"
                            alt="Blurred thumbnail of the first page of ${fn:escapeXml(primaryFile.originalName)}"
                            loading="eager"
                            decoding="async"
                        >

                        <span class="document-thumbnail-label">
                            Blurred front-page preview
                        </span>
                    </a>

                    <div class="document-thumbnail-meta">
                        <div>
                            <strong>
                                <c:out value="${primaryFile.originalName}"/>
                            </strong>
                            <small>
                                The original file is available only through the
                                authorised download route.
                            </small>
                        </div>

                        <%-- <a
                            class="btn btn-small btn-outline"
                            href="${ctx}/documents/download?fileId=${primaryFile.id}"
                        >
                            Download original
                        </a> --%>
                    </div>
                </div>
            </c:when>

            <c:otherwise>
                <div class="document-thumbnail-empty">
                    <span aria-hidden="true">PDF</span>
                    <strong>No primary document preview is available</strong>
                    <small>
                        Attach or generate a primary PDF to display its
                        privacy-blurred front-page thumbnail.
                    </small>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty files}">
            <div class="file-list">
                <c:forEach items="${files}" var="file">
                    <a href="${ctx}/documents/download?fileId=${file.id}">
                        <span aria-hidden="true">
                            <c:choose>
                                <c:when test="${file.primaryFile}">PDF</c:when>
                                <c:otherwise>FILE</c:otherwise>
                            </c:choose>
                        </span>

                        <div>
                            <strong>
                                <c:out value="${file.originalName}"/>
                            </strong>
                            <small>
                                <c:out value="${file.mimeType}"/>
                                <c:out value="${file.fileSize}"/> bytes
                            </small>
                        </div>

                        <b>Download</b>
                    </a>
                </c:forEach>
            </div>
        </c:if>

        <h2>Description</h2>
        <p class="body-copy">
            <c:out value="${document.description}"/>
        </p>

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
            <dd>
                <span class="badge status-${fn:toLowerCase(fn:replace(document.status,'_','-'))}">
                    <c:out value="${document.status}"/>
                </span>
            </dd>

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

            <dt>Destination</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty document.destinationDepartmentName}">
                        <c:out value="${document.destinationDepartmentName}"/>
                    </c:when>
                    <c:otherwise>
                        <span class="muted">Not assigned</span>
                    </c:otherwise>
                </c:choose>
            </dd>

            <dt>Selected Boss</dt>
            <dd>
                <c:choose>
                    <c:when test="${not empty document.bossName}">
                        <c:out value="${document.bossName}"/>
                    </c:when>
                    <c:otherwise>
                        <span class="muted">Not assigned</span>
                    </c:otherwise>
                </c:choose>
            </dd>

            <dt>Created By</dt>
            <dd><c:out value="${document.createdByName}"/></dd>

            <dt>Due Date</dt>
            <dd><c:out value="${document.dueDate}"/></dd>
        </dl>

        <c:if test="${document.createdById == currentUser.id && (document.status == 'RETURNED_FOR_CORRECTION' || document.status == 'DRAFT' || document.status == 'RECALLED')}">
            <a
                class="btn btn-secondary btn-block"
                href="${ctx}/documents/edit?id=${document.id}"
            >
                <c:choose>
                    <c:when test="${document.status == 'RETURNED_FOR_CORRECTION'}">
                        Correct Document
                    </c:when>
                    <c:otherwise>
                        Edit Document
                    </c:otherwise>
                </c:choose>
            </a>
        </c:if>

        <c:if test="${document.status == 'PENDING_APPROVAL' && document.createdById == currentUser.id}">
            <form
                method="post"
                action="${ctx}/documents/action"
                data-confirm="Recall this document and stop the current approval request?"
            >
                <input
                    type="hidden"
                    name="csrfToken"
                    value="${csrfToken}"
                >
                <input
                    type="hidden"
                    name="documentId"
                    value="${document.id}"
                >
                <input
                    type="hidden"
                    name="action"
                    value="RECALL"
                >

                <button
                    class="btn btn-warning btn-block"
                    type="submit"
                >
                    Recall Document
                </button>
            </form>
        </c:if>

        <c:if test="${currentUser.hasRole('BOSS') && document.status == 'PENDING_APPROVAL'}">
            <a
                class="btn btn-primary btn-block"
                href="${ctx}/approvals/review?id=${document.id}"
            >
                Review and Decide
            </a>
        </c:if>
    </aside>
</div>

<%@ include file="../common/layout-bottom.jspf" %>
