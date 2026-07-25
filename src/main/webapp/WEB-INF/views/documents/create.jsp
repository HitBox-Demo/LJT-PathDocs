<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>Create New Document</h1><p>Choose document images, convert them to PDF, add metadata and send the document to a boss.</p></div></section>
<form   
        method="post" 
        action="${ctx}/documents/create" 
        enctype="multipart/form-data" 
        class="form-layout"
        data-submit-once="true">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <input type="hidden" name="submissionToken" value="<c:out value='${submissionToken}'/>">
    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>1. Choose or Upload Main Document</h2>
                <p>Choose one or more images, or upload one existing PDF. Selected images are combined into one PDF.</p>
            </div>
        </div>

        <div class="upload-grid">
            <div class="upload-box image-upload-card" data-image-collector>
                <strong class="upload-title">Choose Images</strong>
                <small class="upload-help">
                    Select one or several pictures. Open the picker again to add more.
                    All selected pictures will be combined into one PDF in the displayed order.
                </small>

                <div class="image-picker-row" data-image-picker-host>
                    <input
                        id="createCaptureImages"
                        class="visually-hidden-file"
                        type="file"
                        name="captureImages"
                        accept="image/*"
                        multiple
                        data-image-input
                    >
                    <label class="btn btn-secondary image-picker-button" for="createCaptureImages">
                        Choose Images
                    </label>
                    <span class="image-picker-summary" data-image-summary>No images selected</span>
                </div>

                <div class="image-input-store" data-image-input-store hidden></div>

                <div class="selected-image-area" data-image-area hidden>
                    <div class="selected-image-header">
                        <strong>Selected Pages: <span data-image-count>0</span></strong>
                        <button class="btn-text-danger" type="button" data-clear-images>
                            Remove All
                        </button>
                    </div>
                    <div class="selected-image-grid" data-image-preview></div>
                </div>
            </div>

            <label class="upload-box">
                <strong class="upload-title">Upload Existing PDF</strong>
                <small class="upload-help">Used only when no images are selected.</small>
                <input
                    class="file-control"
                    type="file"
                    name="primaryPdf"
                    accept="application/pdf"
                    data-file-list="pdf-list"
                >
            </label>
        </div>

        <div id="pdf-list" class="selected-files"></div>
    </section>

    <section class="card form-section">
        <div class="card-header"><div><h2>2. Document Information</h2><p>Fields marked * are required.</p></div></div>
        <div class="field-grid">
            <label class="field field-wide">Document Title *<input name="title" maxlength="250" required value="${fn:escapeXml(param.title)}" placeholder="Example: Procurement Request - Server Upgrade"></label>
            <label class="field">Reference Number *<input name="referenceNo" maxlength="100" required value="${fn:escapeXml(param.referenceNo)}" placeholder="IT/PR/2026/001"></label>
            <label class="field">Sender *<input name="sender" maxlength="200" required value="${fn:escapeXml(param.sender)}" placeholder="Department or organisation"></label>
            <label class="field">Date Received *<input type="date" name="dateReceived" required value="${empty param.dateReceived ? '' : param.dateReceived}"></label>
            <label class="field">Category *
                <select name="category" required>
                    <option value="">Select category</option>
                    <option value="Procurement" ${param.category == 'Procurement' ? 'selected' : ''}>Procurement</option>
                    <option value="Finance" ${param.category == 'Finance' ? 'selected' : ''}>Finance</option>
                    <option value="Contract" ${param.category == 'Contract' ? 'selected' : ''}>Contract</option>
                    <option value="Policy" ${param.category == 'Policy' ? 'selected' : ''}>Policy</option>
                    <option value="Facilities" ${param.category == 'Facilities' ? 'selected' : ''}>Facilities</option>
                    <option value="Audit" ${param.category == 'Audit' ? 'selected' : ''}>Audit</option>
                    <option value="Report" ${param.category == 'Report' ? 'selected' : ''}>Report</option>
                    <option value="General" ${param.category == 'General' ? 'selected' : ''}>General</option>
                </select>
            </label>
            <label class="field">Priority *
                <select name="priority">
                    <option value="NORMAL" ${param.priority != 'URGENT' ? 'selected' : ''}>Normal - 5 days</option>
                    <option value="URGENT" ${param.priority == 'URGENT' ? 'selected' : ''}>Urgent - 2 days</option>
                </select>
            </label>
            <label class="field field-wide">Description
                <textarea name="description" rows="4" maxlength="1000" placeholder="Short summary or handling instruction"><c:out value="${param.description}"/></textarea>
            </label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header"><div>
            <h2>3. Routing</h2><p>Select one boss and the intended destination folder.</p></div></div>
        <div class="field-grid">
            <label class="field">Selected Boss *
                <select name="bossId" required>
                    <option value="">Select boss</option>
                    <c:forEach items="${bosses}" var="boss">
                        <option value="${boss.id}" ${param.bossId == boss.id ? 'selected' : ''}>
                            <c:out value="${boss.fullName}"/>
                        </option></c:forEach></select></label>
            <label class="field">Destination Folder *<select name="departmentId" required>
                <option value="">Select department</option>
                <c:forEach items="${departments}" var="dept">
                    <option value="${dept.id}" ${param.departmentId == dept.id ? 'selected' : ''}>
                        <c:out value="${dept.name}"/>
                    </option>
                </c:forEach>
            </select></label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>4. Attachments</h2><p>Optional images, PDF, Word or Excel files.</p></div></div>
        <label class="upload-box compact">
            <strong>Add Attachments</strong>
            <span>PDF, JPG, PNG, DOC, DOCX, XLS or XLSX</span>
            <input 
                class="file-control" 
                type="file" 
                name="attachments" 
                accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx" 
                multiple data-file-list="attachment-list">
        </label>
        <div id="attachment-list" class="selected-files"></div>
    </section>

    <div class="form-actions"><a class="btn btn-outline" href="${ctx}/app/dashboard">Cancel</a><button class="btn btn-secondary" name="action" value="draft" type="submit">Save Draft</button><button class="btn btn-primary" name="action" value="submit" type="submit">Submit for Approval</button></div>
</form>
<%@ include file="../common/layout-bottom.jspf" %>
