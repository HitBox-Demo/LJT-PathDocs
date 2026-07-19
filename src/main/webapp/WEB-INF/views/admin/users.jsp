<%@ include file="../common/layout-top.jspf" %>
<section class="page-heading"><div><h1>User Management</h1>
    <p>Create accounts and assign the initial role and department.</p></div></section>
<details class="card collapsible" ${empty error ? '' : 'open'}><summary>Add User</summary>
    <form method="post" action="${ctx}/admin/users" class="field-grid form-pad">
        <input type="hidden" name="csrfToken" value="${csrfToken}"><label class="field">
            Full Name *<input name="fullName" required></label><label class="field">
                Username *<input name="username" required></label><label class="field">
                    Email *<input type="email" name="email" required></label><label class="field">
                        Temporary Password *<input type="password" name="temporaryPassword" minlength="8" required></label>
                        <label class="field">Role *<select name="roleCode">
                            <option value="DEPARTMENT_USER">Department User</option>
                            <option value="CLERK">Clerk</option>
                            <option value="BOSS">Boss</option></select></label>
                            <label class="field">Department<select name="departmentId">
                                <option value="">No department</option>
                                <c:forEach items="${departments}" var="dept">
                                    <option value="${dept.id}"><c:out value="${dept.name}"/>
                                    </option></c:forEach></select></label>
                                    <div class="field field-wide">
                                        <button class="btn btn-primary" type="submit">Create Account</button></div></form></details>
<section class="card"><div class="table-wrap">
    <table><thead>
        <tr><th>User</th>
            <th>Email</th>
            <th>Roles</th>
            <th>Department</th>
            <th>Status</th>
            <th>Request Role Change</th>
        </tr></thead><tbody>
            <c:forEach items="${users}" var="u">
                <tr><td><strong><c:out value="${u.fullName}"/></strong>
                    <small>@<c:out value="${u.username}"/></small></td>
                    <td><c:out value="${u.email}"/></td>
                    <td><c:forEach items="${u.roles}" var="role">
                        <span class="badge"><c:out value="${role}"/></span> 
                    </c:forEach></td><td><c:out value="${u.departmentName}"/></td>
                    <td><span class="badge status-routed"><c:out value="${u.status}"/></span></td>
                    <td><form method="post" action="${ctx}/admin/users" class="role-request-inline">
                        <input type="hidden" name="csrfToken" value="${csrfToken}">
                        <input type="hidden" name="action" value="roleRequest">
                        <input type="hidden" name="userId" value="${u.id}">
                        <input type="hidden" name="currentRole" value="${u.primaryRole}">
                        <select name="requestedRole">
                            <option value="DEPARTMENT_USER">Department User</option>
                            <option value="CLERK">Clerk</option>
                            <option value="BOSS">Boss</option>
                        </select>
                        <input name="remarks" placeholder="Reason">
                        <button class="btn btn-small btn-outline">Request</button>
                    </form></td></tr></c:forEach></tbody></table></div></section>
<%@ include file="../common/layout-bottom.jspf" %>
