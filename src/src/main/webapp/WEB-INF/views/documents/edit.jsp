<%@ include file="../common/layout-top.jspf" %>

<section class="page-heading">
    <div>
        <h1>Correct Document</h1>
        <p>Edit the same document record and preserve its document code and rejection history.</p>
    </div>
    <a class="btn btn-outline" href="${ctx}/documents/view?id=${document.id}">Back to Details</a>
</section>

<c:if test="${not empty document.rejectionReason}">
    <div class="alert alert-error correction-banner">
        <strong>Boss correction reason</strong>
        <span><c:out value="${document.rejectionReason}"/></span>
        <small>The reason remains in Approval History after resubmission.</small>
    </div>
</c:if>

<form method="post" action="${ctx}/documents/edit" enctype="multipart/form-data" class="form-layout">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <input type="hidden" name="documentId" value="${document.id}">

    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>Document Identity</h2>
                <p>The ID and document code cannot be changed.</p>
            </div>
        </div>
        <div class="field-grid readonly-grid">
            <label class="field">Document Code
                <input value="${fn:escapeXml(document.documentCode)}" readonly>
            </label>
            <label class="field">Current Status
                <input value="${fn:escapeXml(document.status)}" readonly>
            </label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>1. Document Information</h2>
                <p>Correct the metadata while keeping the same document record.</p>
            </div>
        </div>
        <div class="field-grid">
            <label class="field field-wide">Document Title *
                <input name="title" maxlength="250" required value="${fn:escapeXml(document.title)}">
            </label>
            <label class="field">Reference Number *
                <input name="referenceNo" maxlength="100" required value="${fn:escapeXml(document.referenceNo)}">
            </label>
            <label class="field">Sender *
                <input name="sender" maxlength="200" required value="${fn:escapeXml(document.sender)}">
            </label>
            <label class="field">Date Received *
                <input type="date" name="dateReceived" required value="${document.dateReceived}">
            </label>
            <label class="field">Category *
                <select name="category" required>
                    <option value="">Select category</option>
                    <option value="Procurement" ${document.category == 'Procurement' ? 'selected' : ''}>Procurement</option>
                    <option value="Finance" ${document.category == 'Finance' ? 'selected' : ''}>Finance</option>
                    <option value="Contract" ${document.category == 'Contract' ? 'selected' : ''}>Contract</option>
                    <option value="Policy" ${document.category == 'Policy' ? 'selected' : ''}>Policy</option>
                    <option value="Report" ${document.category == 'Report' ? 'selected' : ''}>Report</option>
                    <option value="General" ${document.category == 'General' ? 'selected' : ''}>General</option>
                </select>
            </label>
            <label class="field">Priority *
                <select name="priority">
                    <option value="NORMAL" ${document.priority == 'NORMAL' ? 'selected' : ''}>Normal - 5 days</option>
                    <option value="URGENT" ${document.priority == 'URGENT' ? 'selected' : ''}>Urgent - 2 days</option>
                </select>
            </label>
            <label class="field field-wide">Description
                <textarea name="description" rows="5" maxlength="1000"><c:out value="${document.description}"/></textarea>
            </label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>2. Routing</h2>
                <p>You may select a different boss or destination before resubmitting.</p>
            </div>
        </div>
        <div class="field-grid">
            <label class="field">Selected Boss *
                <select name="bossId" required>
                    <option value="">Select boss</option>
                    <c:forEach items="${bosses}" var="boss">
                        <option value="${boss.id}" ${document.bossId == boss.id ? 'selected' : ''}>
                            <c:out value="${boss.fullName}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>
            <label class="field">Destination Folder *
                <select name="departmentId" required>
                    <option value="">Select department</option>
                    <c:forEach items="${departments}" var="dept">
                        <option value="${dept.id}" ${document.destinationDepartmentId == dept.id ? 'selected' : ''}>
                            <c:out value="${dept.name}"/>
                        </option>
                    </c:forEach>
                </select>
            </label>
        </div>
    </section>

    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>3. Replace Main Document</h2>
                <p>Leave these fields empty to keep the current main document.</p>
            </div>
        </div>
        <div class="upload-grid">
            <label class="upload-box">
                <strong>Replace with Captured Images</strong>
                <span>Select several pictures or add them one at a time. They will be combined into a new PDF.</span>
                <input id="edit-capture-images" class="file-control" type="file" name="captureImages" accept="image/*" capture="environment" multiple data-image-preview="edit-image-list">
            </label>
            <label class="upload-box">
                <strong>Replace with an Existing PDF</strong>
                <span>Used only when no replacement camera images are selected.</span>
                <input class="file-control" type="file" name="primaryPdf" accept="application/pdf" data-file-list="edit-pdf-list">
            </label>
        </div>
        <div id="edit-image-list" class="selected-files image-preview-grid" aria-live="polite"></div>
        <div id="edit-pdf-list" class="selected-files"></div>
    </section>

    <section class="card form-section">
        <div class="card-header">
            <div>
                <h2>4. Existing Files and New Attachments</h2>
                <p>To replace an attachment, mark the old attachment for removal and upload the replacement.</p>
            </div>
        </div>

        <c:choose>
            <c:when test="${not empty files}">
                <div class="existing-files">
                    <c:forEach items="${files}" var="file">
                        <div class="existing-file-row ${fn:startsWith(file.mimeType, 'image/') ? 'existing-image-row' : ''}" data-existing-file>
                            <c:if test="${fn:startsWith(file.mimeType, 'image/')}">
                                <img class="existing-image-thumb" src="${ctx}/documents/download?fileId=${file.id}&amp;inline=true" alt="Preview of ${fn:escapeXml(file.originalName)}">
                            </c:if>
                            <div class="existing-file-copy">
                                <strong><c:out value="${file.originalName}"/></strong>
                                <small><c:out value="${file.mimeType}"/> - ${file.fileSize} bytes</small>
                            </div>
                            <c:choose>
                                <c:when test="${file.primaryFile}">
                                    <span class="badge priority-normal">Main document</span>
                                </c:when>
                                <c:otherwise>
                                    <input class="remove-file-checkbox" type="checkbox" name="removeFileId" value="${file.id}" hidden>
                                    <button class="file-remove-button" type="button" data-remove-existing aria-label="Remove ${fn:escapeXml(file.originalName)}" title="Remove file">&times;</button>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="muted form-pad">No stored file metadata is available for this older demo record.</p>
            </c:otherwise>
        </c:choose>

        <label class="upload-box compact">
            <strong>Add Replacement or Additional Attachments</strong>
            <span>PDF, JPG, PNG, DOC, DOCX, XLS or XLSX</span>
            <input class="file-control" type="file" name="attachments" accept=".pdf,.jpg,.jpeg,.png,.doc,.docx,.xls,.xlsx" multiple data-file-list="edit-attachment-list">
        </label>
        <div id="edit-attachment-list" class="selected-files"></div>
    </section>

    <div class="form-actions edit-actions">
        <a class="btn btn-outline" href="${ctx}/documents/view?id=${document.id}">Cancel</a>
        <button class="btn btn-secondary" name="action" value="save" type="submit">Save Changes</button>
        <button class="btn btn-warning" name="action" value="draft" type="submit">Save as Draft</button>
        <button class="btn btn-primary" name="action" value="resubmit" type="submit" data-confirm="Resubmit this corrected document to the selected boss?">Resubmit for Approval</button>
    </div>
</form>

<%@ include file="../common/layout-bottom.jspf" %>
