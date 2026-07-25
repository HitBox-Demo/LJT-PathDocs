<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Review <c:out value="${document.documentCode}"/></h1><p><c:out value="${document.title}"/></p></div><a class="btn btn-outline" href="${ctx}/approvals/list">Back to Queue</a></section>
<div class="detail-grid">
    <section class="card detail-card">
        <div class="document-preview"><span>PDF</span><strong>Main Document</strong><small>Open each attachment before making a decision.</small></div>
        <h2>Document Summary</h2><p class="body-copy"><c:out value="${document.description}"/></p>
        <dl class="summary-grid"><dt>Reference</dt><dd><c:out value="${document.referenceNo}"/></dd><dt>Sender</dt><dd><c:out value="${document.sender}"/></dd><dt>Priority</dt><dd><c:out value="${document.priority}"/></dd><dt>Original Destination</dt><dd><c:out value="${document.destinationDepartmentName}"/></dd></dl>
        <c:if test="${not empty files}"><div class="file-list"><c:forEach items="${files}" var="file"><a href="${ctx}/documents/download?fileId=${file.id}"><span>FILE</span><div><strong><c:out value="${file.originalName}"/></strong><small><c:out value="${file.mimeType}"/></small></div><b>Download</b></a></c:forEach></div></c:if>
    </section>
    <aside class="card decision-card">
        <h2>Boss Decision</h2>
        <form method="post" action="${ctx}/approvals/action" class="stack-form">
            <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="documentId" value="${document.id}">
            <label>Final Destination<select name="departmentId"><c:forEach items="${departments}" var="dept"><option value="${dept.id}" ${dept.id == document.destinationDepartmentId ? 'selected' : ''}><c:out value="${dept.name}"/></option></c:forEach></select></label>
            <label>Remarks<textarea name="remarks" rows="5" placeholder="Required when rejecting; recommended when changing destination"></textarea></label>
            <button class="btn btn-primary btn-block" name="decision" value="APPROVE" type="submit" data-confirm-click="Approve and route this document?">Approve and Route</button>
            <button class="btn btn-danger btn-block" name="decision" value="REJECT" type="submit" data-require-remarks>Return for Correction</button>
        </form>
    </aside>
</div>
<%@ include file="../common/layout-bottom.jspf" %>
