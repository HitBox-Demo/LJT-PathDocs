<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Create New Document</h1><p>Capture hardcopy pages, convert them to PDF, add metadata and send to a boss.</p></div></section>
<form method="post" action="${ctx}/documents/create" enctype="multipart/form-data" class="form-layout">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <section class="card form-section">
        <div class="card-header"><div><h2>1. Capture or Upload Main Document</h2><p>Choose camera pages or one existing PDF. Camera images are combined into a single PDF.</p></div></div>
        <div class="upload-grid">
            <label class="upload-box"><strong>Capture or Choose Images</strong><span>Select several pictures or add them one at a time. They will be combined into one PDF.</span><input id="create-capture-images" class="file-control" type="file" name="captureImages" accept="image/*" capture="environment" multiple data-image-preview="image-list"></label>
            <label class="upload-box"><strong>Upload Existing PDF</strong><span>Used only when no camera images are selected.</span><input class="file-control" type="file" name="primaryPdf" accept="application/pdf" data-file-list="pdf-list"></label>
        </div>
        <div id="image-list" class="selected-files image-preview-grid" aria-live="polite"></div><div id="pdf-list" class="selected-files"></div>
    </section>

    <section class="card form-section">
        <div class="card-header"><div><h2>2. Document Information</h2><p>Fields marked * are required.</p></div></div>
        <div class="field-grid">
            <label class="field field-wide">Document Title *<input name="title" maxlength="250" required value="${fn:escapeXml(param.title)}" placeholder="Example: Procurement Request - Server Upgrade"></label>
            <label class="field">Reference Number *<input name="referenceNo" maxlength="100" required value="${fn:escapeXml(param.referenceNo)}" placeholder="IT/PR/2026/001"></label>
            <label class="field">Sender *<input name="sender" maxlength="200" required value="${fn:escapeXml(param.sender)}" placeholder="Department or organisation"></label>
            <label class="field">Date Received *<input type="date" name="dateReceived" required value="${empty param.dateReceived ? '' : param.dateReceived}"></label>
            <label class="field">Category *<select name="category" required><option value="">Select category</option><option>Procurement</option><option>Finance</option><option>Contract</option><option>Policy</option><option>Report</option><option>General</option></select></label>
            <label class="field">Priority *<select name="priority"><option value="NORMAL">Normal - 5 days</option><option value="URGENT">Urgent - 2 days</option></select></label>
            <label class="field field-wide">Description<textarea name="description" rows="4" maxlength="1000" placeholder="Short summary or handling instruction"></textarea></label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header"><div><h2>3. Routing</h2><p>Select one boss and the intended destination folder.</p></div></div>
        <div class="field-grid">
            <label class="field">Selected Boss *<select name="bossId" required><option value="">Select boss</option><c:forEach items="${bosses}" var="boss"><option value="${boss.id}"><c:out value="${boss.fullName}"/></option></c:forEach></select></label>
            <label class="field">Destination Folder *<select name="departmentId" required><option value="">Select department</option><c:forEach items="${departments}" var="dept"><option value="${dept.id}"><c:out value="${dept.name}"/></option></c:forEach></select></label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header"><div><h2>4. Attachments</h2><p>Optional images, PDF, Word or Excel files.</p></div></div>
        <label class="upload-box compact"><strong>Add Attachments</strong><span>PDF, JPG, PNG, DOC, DOCX, XLS or XLSX</span><input class="file-control" type="file" name="attachments" accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx" multiple data-file-list="attachment-list"></label>
        <div id="attachment-list" class="selected-files"></div>
    </section>

    <div class="form-actions"><a class="btn btn-outline" href="${ctx}/app/dashboard">Cancel</a><button class="btn btn-secondary" name="action" value="draft" type="submit">Save Draft</button><button class="btn btn-primary" name="action" value="submit" type="submit">Submit for Approval</button></div>
</form>
<%@ include file="../common/layout-bottom.jspf" %>
