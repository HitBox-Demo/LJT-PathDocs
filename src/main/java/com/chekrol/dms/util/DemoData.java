package com.chekrol.dms.util;

import com.chekrol.dms.model.ApprovalRecord;
import com.chekrol.dms.model.Department;
import com.chekrol.dms.model.DocumentFile;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.Notification;
import com.chekrol.dms.model.RoleRequest;
import com.chekrol.dms.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class DemoData {
    private static final AtomicLong IDS = new AtomicLong(100);
    private static final List<User> USERS = new CopyOnWriteArrayList<>();
    private static final List<Department> DEPARTMENTS = new CopyOnWriteArrayList<>();
    private static final List<DocumentRecord> DOCUMENTS = new CopyOnWriteArrayList<>();
    private static final List<DocumentFile> FILES = new CopyOnWriteArrayList<>();
    private static final List<Notification> NOTIFICATIONS = new CopyOnWriteArrayList<>();
    private static final List<ApprovalRecord> APPROVALS = new CopyOnWriteArrayList<>();
    private static final List<RoleRequest> ROLE_REQUESTS = new CopyOnWriteArrayList<>();
    private static final Map<String, String> DEMO_PASSWORDS = new ConcurrentHashMap<>();

    static {
        DEPARTMENTS.add(new Department(1, "IT", "Information Technology", "ACTIVE"));
        DEPARTMENTS.add(new Department(2, "FINANCE", "Finance", "ACTIVE"));
        DEPARTMENTS.add(new Department(3, "MANAGEMENT", "Management", "ACTIVE"));

        USERS.add(user(1, "clerk", "Ahmad Fadzli", "ahmad.fadzli@agency.gov.my", null, null, "CLERK", "SYSTEM_ADMIN"));
        USERS.add(user(2, "boss", "En. Razali Osman", "razali@agency.gov.my", null, null, "BOSS"));
        USERS.add(user(3, "it.user", "Nur Izzati", "izzati.it@agency.gov.my", 1L, "Information Technology", "DEPARTMENT_USER"));
        USERS.add(user(4, "finance.user", "Siti Rahimah", "siti.finance@agency.gov.my", 2L, "Finance", "DEPARTMENT_USER"));
        USERS.add(user(5, "management.user", "Pn. Norhaslinda", "norhaslinda@agency.gov.my", 3L, "Management", "DEPARTMENT_USER", "BOSS"));

        DEMO_PASSWORDS.put("clerk", "Clerk@123");
        DEMO_PASSWORDS.put("boss", "Boss@123");
        DEMO_PASSWORDS.put("it.user", "Dept@123");
        DEMO_PASSWORDS.put("finance.user", "Dept@123");
        DEMO_PASSWORDS.put("management.user", "Dept@123");

        addDoc(
                1, "DOC-2026-0001", "Procurement Request - Server Upgrade",
                "IT/PR/2026/001", "Jabatan IT", "Procurement", "URGENT",
                1L, "Information Technology", 2L, "En. Razali Osman",
                "PENDING_APPROVAL", 1L, "Ahmad Fadzli", 6, null
        );
        addDoc(
                2, "DOC-2026-0002", "Budget Allocation Q3 2026",
                "FIN/BA/2026/003", "Jabatan Kewangan", "Finance", "NORMAL",
                2L, "Finance", 2L, "En. Razali Osman",
                "PENDING_APPROVAL", 1L, "Ahmad Fadzli", 7, null
        );
        addDoc(
                3, "DOC-2026-0003", "Staff Leave Policy Amendment",
                "HR/POL/2026/011", "Jabatan HR", "Policy", "NORMAL",
                3L, "Management", 5L, "Pn. Norhaslinda",
                "PENDING_APPROVAL", 1L, "Ahmad Fadzli", 3, null
        );
        addDoc(
                4, "DOC-2026-0004", "Vendor Contract Renewal - Telco Services",
                "IT/CON/2026/007", "TM Berhad", "Contract", "NORMAL",
                1L, "Information Technology", 2L, "En. Razali Osman",
                "ROUTED", 4L, "Siti Rahimah", 0, null
        );
        addDoc(
                5, "DOC-2026-0005", "Office Renovation Quotation",
                "FAC/QU/2026/002", "Syarikat Bina Maju", "Facilities", "NORMAL",
                3L, "Management", 5L, "Pn. Norhaslinda",
                "RETURNED_FOR_CORRECTION", 1L, "Ahmad Fadzli", 0,
                "Missing page 3 of quotation. Please attach the complete document."
        );
        addDoc(
                6, "DOC-2026-0006", "Annual Performance Report 2025",
                "HR/APR/2025/001", "Jabatan HR", "Report", "NORMAL",
                3L, "Management", 5L, "Pn. Norhaslinda",
                "ROUTED", 4L, "Siti Rahimah", 0, null
        );
        addDoc(
                7, "DOC-2026-0007", "IT Infrastructure Audit 2026",
                "IT/AUD/2026/001", "Audit Unit", "Audit", "URGENT",
                1L, "Information Technology", 2L, "En. Razali Osman",
                "DRAFT", 1L, "Ahmad Fadzli", 0, null
        );

        Notification notification = new Notification();
        notification.setId(1);
        notification.setRecipientId(1);
        notification.setType("DESTINATION_CHANGED");
        notification.setMessage("DOC-2026-0004 was redirected to the IT folder by En. Razali Osman.");
        notification.setDocumentId(4L);
        notification.setCreatedAt(LocalDateTime.now().minusDays(1));
        NOTIFICATIONS.add(notification);

        Notification overdue = new Notification();
        overdue.setId(2);
        overdue.setRecipientId(2);
        overdue.setType("OVERDUE");
        overdue.setMessage("DOC-2026-0002 is overdue and requires approval.");
        overdue.setDocumentId(2L);
        overdue.setCreatedAt(LocalDateTime.now().minusHours(4));
        NOTIFICATIONS.add(overdue);

        RoleRequest roleRequest = new RoleRequest();
        roleRequest.setId(1);
        roleRequest.setUserId(3);
        roleRequest.setUserName("Nur Izzati");
        roleRequest.setCurrentRole("DEPARTMENT_USER");
        roleRequest.setRequestedRole("BOSS");
        roleRequest.setRequestedById(1);
        roleRequest.setRequestedByName("Ahmad Fadzli");
        roleRequest.setStatus("PENDING");
        roleRequest.setRequestedAt(LocalDateTime.now().minusDays(2));
        ROLE_REQUESTS.add(roleRequest);
    }

    private DemoData() {
    }

    private static User user(
            long id,
            String username,
            String name,
            String email,
            Long departmentId,
            String departmentName,
            String... roles
    ) {
        User user = new User(
                id,
                username,
                name,
                email,
                "ACTIVE",
                departmentId,
                departmentName,
                new LinkedHashSet<>(Arrays.asList(roles))
        );
        user.setPasswordHash("demo");
        return user;
    }

    private static void addDoc(
            long id,
            String code,
            String title,
            String reference,
            String sender,
            String category,
            String priority,
            Long departmentId,
            String departmentName,
            Long bossId,
            String bossName,
            String status,
            Long creatorId,
            String creatorName,
            int days,
            String rejectionReason
    ) {
        DocumentRecord document = new DocumentRecord();
        document.setId(id);
        document.setDocumentCode(code);
        document.setTitle(title);
        document.setReferenceNo(reference);
        document.setSender(sender);
        document.setDateReceived(LocalDate.now().minusDays(Math.max(days, 1)));
        document.setCategory(category);
        document.setPriority(priority);
        document.setDestinationDepartmentId(departmentId);
        document.setDestinationDepartmentName(departmentName);
        document.setBossId(bossId);
        document.setBossName(bossName);
        document.setStatus(status);
        document.setCreatedById(creatorId);
        document.setCreatedByName(creatorName);
        document.setCreatedAt(LocalDateTime.now().minusDays(Math.max(days, 1)));
        document.setSubmittedAt("DRAFT".equals(status) ? null : LocalDateTime.now().minusDays(Math.max(days, 1)));
        document.setDueDate(document.getSubmittedAt() == null
                ? null
                : document.getSubmittedAt().toLocalDate().plusDays("URGENT".equals(priority) ? 2 : 5));
        document.setDaysPending(days);
        document.setRejectionReason(rejectionReason);
        document.setDescription("Demo record converted from the React/Vite prototype.");
        DOCUMENTS.add(document);
    }

    public static User authenticate(String username, String password) {
        String key = username == null ? "" : username.toLowerCase(Locale.ROOT);
        String expected = DEMO_PASSWORDS.get(key);
        if (expected == null || !expected.equals(password)) {
            return null;
        }
        return USERS.stream()
                .filter(user -> user.getUsername().equalsIgnoreCase(username))
                .filter(user -> "ACTIVE".equals(user.getStatus()))
                .findFirst()
                .orElse(null);
    }

    public static List<User> users() {
        return new ArrayList<>(USERS);
    }

    public static List<User> bosses() {
        return USERS.stream().filter(user -> user.hasRole("BOSS")).collect(Collectors.toList());
    }

    public static List<Department> departments() {
        return new ArrayList<>(DEPARTMENTS);
    }

    public static List<RoleRequest> roleRequests() {
        return new ArrayList<>(ROLE_REQUESTS);
    }

    public static List<RoleRequest> pendingRoleRequests() {
        return ROLE_REQUESTS.stream()
                .filter(request -> "PENDING".equals(request.getStatus()))
                .sorted(Comparator.comparing(RoleRequest::getRequestedAt))
                .collect(Collectors.toList());
    }

    public static synchronized long createUser(
            User user,
            String temporaryPassword,
            String roleCode
    ) {
        if (user == null || !ValidationUtil.hasText(user.getUsername())
                || !ValidationUtil.hasText(user.getFullName())
                || !ValidationUtil.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Username, full name and email are required.");
        }
        if (temporaryPassword == null || temporaryPassword.length() < 8) {
            throw new IllegalArgumentException("Temporary password must contain at least 8 characters.");
        }
        Set<String> allowedRoles = Set.of("CLERK", "BOSS", "DEPARTMENT_USER");
        if (!allowedRoles.contains(roleCode)) {
            throw new IllegalArgumentException("Invalid initial role.");
        }
        if (USERS.stream().anyMatch(existing -> existing.getUsername().equalsIgnoreCase(user.getUsername()))) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (USERS.stream().anyMatch(existing -> existing.getEmail().equalsIgnoreCase(user.getEmail()))) {
            throw new IllegalArgumentException("Email already exists.");
        }

        Department department = null;
        if (user.getDepartmentId() != null) {
            department = requireDepartment(user.getDepartmentId());
        }
        if ("DEPARTMENT_USER".equals(roleCode) && department == null) {
            throw new IllegalArgumentException("Department users must be assigned to a department.");
        }

        long id = IDS.incrementAndGet();
        user.setId(id);
        user.setStatus("ACTIVE");
        user.setDepartmentName(department == null ? null : department.getName());
        user.getRoles().add(roleCode);
        user.setPasswordHash("demo");
        USERS.add(user);
        DEMO_PASSWORDS.put(user.getUsername().toLowerCase(Locale.ROOT), temporaryPassword);
        return id;
    }

    public static synchronized long createRoleRequest(
            long userId,
            String currentRole,
            String requestedRole,
            User requestedBy,
            String remarks
    ) {
        User target = USERS.stream()
                .filter(user -> user.getId() == userId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!Set.of("CLERK", "BOSS", "DEPARTMENT_USER").contains(requestedRole)) {
            throw new IllegalArgumentException("Invalid requested role.");
        }
        if (target.hasRole(requestedRole)) {
            throw new IllegalArgumentException("The user already has the requested role.");
        }
        boolean duplicate = ROLE_REQUESTS.stream().anyMatch(request ->
                request.getUserId() == userId
                        && requestedRole.equals(request.getRequestedRole())
                        && "PENDING".equals(request.getStatus()));
        if (duplicate) {
            throw new IllegalArgumentException("A matching role request is already pending.");
        }

        RoleRequest request = new RoleRequest();
        request.setId(IDS.incrementAndGet());
        request.setUserId(userId);
        request.setUserName(target.getFullName());
        request.setCurrentRole(currentRole);
        request.setRequestedRole(requestedRole);
        request.setRequestedById(requestedBy.getId());
        request.setRequestedByName(requestedBy.getFullName());
        request.setStatus("PENDING");
        request.setRemarks(remarks);
        request.setRequestedAt(LocalDateTime.now());
        ROLE_REQUESTS.add(request);
        return request.getId();
    }

    public static synchronized void decideRoleRequest(
            long requestId,
            User boss,
            String decision,
            String remarks
    ) {
        if (boss == null || !boss.hasRole("BOSS")) {
            throw new IllegalArgumentException("Only a boss can decide role requests.");
        }
        if (!Set.of("APPROVE", "REJECT").contains(decision)) {
            throw new IllegalArgumentException("Unsupported role decision.");
        }
        RoleRequest request = ROLE_REQUESTS.stream()
                .filter(candidate -> candidate.getId() == requestId && "PENDING".equals(candidate.getStatus()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Role request is no longer pending."));
        User target = USERS.stream()
                .filter(user -> user.getId() == request.getUserId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if ("APPROVE".equals(decision)) {
            String currentRole = request.getCurrentRole();
            if (currentRole != null && !currentRole.isBlank() && !"SYSTEM_ADMIN".equals(currentRole)) {
                target.getRoles().remove(currentRole);
            }
            target.getRoles().add(request.getRequestedRole());
        }
        request.setStatus("APPROVE".equals(decision) ? "APPROVED" : "REJECTED");
        request.setRemarks(remarks);
        addNotification(
                request.getRequestedById(),
                "ROLE_REQUEST",
                "Role request #" + requestId + " was "
                        + ("APPROVE".equals(decision) ? "approved" : "rejected") + ".",
                null
        );
    }

    public static List<ApprovalRecord> approvals() {
        return APPROVALS.stream()
                .sorted(Comparator.comparing(ApprovalRecord::getDecisionAt).reversed())
                .collect(Collectors.toList());
    }

    public static List<Notification> notifications(User user) {
        return NOTIFICATIONS.stream()
                .filter(notification -> notification.getRecipientId() == user.getId())
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public static void markNotificationsRead(User user) {
        NOTIFICATIONS.stream()
                .filter(notification -> notification.getRecipientId() == user.getId())
                .forEach(notification -> notification.setRead(true));
    }

    public static Map<String, Long> stats(User user) {
        Map<String, Long> result = new LinkedHashMap<>();
        List<DocumentRecord> visible = listDocuments(user, "repository", "");
        result.put("TOTAL", (long) visible.size());
        result.put("PENDING", DOCUMENTS.stream().filter(document -> "PENDING_APPROVAL".equals(document.getStatus()) && canSee(user, document)).count());
        result.put("RETURNED", DOCUMENTS.stream().filter(document -> "RETURNED_FOR_CORRECTION".equals(document.getStatus()) && canSee(user, document)).count());
        result.put("ROUTED", DOCUMENTS.stream().filter(document -> "ROUTED".equals(document.getStatus()) && canSee(user, document)).count());
        result.put("OVERDUE", DOCUMENTS.stream().filter(document -> "PENDING_APPROVAL".equals(document.getStatus())
                && document.getDueDate() != null
                && document.getDueDate().isBefore(LocalDate.now())
                && canSee(user, document)).count());
        return result;
    }

    public static List<DocumentRecord> listDocuments(User user, String view, String query) {
        refreshDaysPending();
        String normalisedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return DOCUMENTS.stream()
                .filter(document -> canSee(user, document))
                .filter(document -> switch (view == null ? "repository" : view) {
                    case "draft" -> "DRAFT".equals(document.getStatus()) && Objects.equals(document.getCreatedById(), user.getId());
                    case "pending" -> "PENDING_APPROVAL".equals(document.getStatus());
                    case "returned" -> "RETURNED_FOR_CORRECTION".equals(document.getStatus()) && Objects.equals(document.getCreatedById(), user.getId());
                    case "department" -> "ROUTED".equals(document.getStatus());
                    case "search", "repository" -> true;
                    default -> true;
                })
                .filter(document -> normalisedQuery.isBlank()
                        || (document.getDocumentCode() + " "
                        + document.getTitle() + " "
                        + document.getReferenceNo() + " "
                        + document.getSender())
                        .toLowerCase(Locale.ROOT)
                        .contains(normalisedQuery))
                .sorted(Comparator.comparing(DocumentRecord::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    private static boolean canSee(User user, DocumentRecord document) {
        if (user.hasRole("SYSTEM_ADMIN") || user.hasRole("CLERK")) {
            return true;
        }
        if (user.hasRole("BOSS") && Objects.equals(document.getBossId(), user.getId())) {
            return true;
        }
        return user.hasRole("DEPARTMENT_USER")
                && "ROUTED".equals(document.getStatus())
                && Objects.equals(document.getDestinationDepartmentId(), user.getDepartmentId());
    }

    public static DocumentRecord findDocument(User user, long id) {
        return DOCUMENTS.stream()
                .filter(document -> document.getId() == id && canSee(user, document))
                .findFirst()
                .orElse(null);
    }

    public static List<DocumentFile> filesForDocument(User user, long documentId) {
        if (findDocument(user, documentId) == null) {
            return List.of();
        }
        return FILES.stream()
                .filter(file -> file.getDocumentId() == documentId)
                .sorted(Comparator.comparing(DocumentFile::isPrimaryFile).reversed().thenComparing(DocumentFile::getId))
                .collect(Collectors.toList());
    }

    public static DocumentFile findFileAuthorized(User user, long fileId) {
        DocumentFile file = FILES.stream()
                .filter(candidate -> candidate.getId() == fileId)
                .findFirst()
                .orElse(null);
        if (file == null || findDocument(user, file.getDocumentId()) == null) {
            return null;
        }
        return file;
    }

    public static synchronized DocumentRecord createDocument(
            User user,
            DocumentRecord document,
            List<DocumentFile> files,
            boolean submit
    ) {
        Department destination = requireDepartment(document.getDestinationDepartmentId());
        User selectedBoss = requireBoss(document.getBossId());
        long id = IDS.incrementAndGet();

        document.setId(id);
        if (document.getDocumentCode() == null || document.getDocumentCode().isBlank()) {
            document.setDocumentCode("DOC-" + LocalDate.now().getYear() + "-" + String.format("%04d", id));
        }
        document.setCreatedById(user.getId());
        document.setCreatedByName(user.getFullName());
        document.setDestinationDepartmentName(destination.getName());
        document.setBossName(selectedBoss.getFullName());
        document.setCreatedAt(LocalDateTime.now());
        document.setStatus(submit ? "PENDING_APPROVAL" : "DRAFT");

        if (submit) {
            document.setSubmittedAt(LocalDateTime.now());
            document.setDueDate(LocalDate.now().plusDays("URGENT".equals(document.getPriority()) ? 2 : 5));
        }

        DOCUMENTS.add(document);
        addFiles(id, files);
        refreshPrimaryFile(document);

        if (submit) {
            addNotification(
                    document.getBossId(),
                    "PENDING_APPROVAL",
                    document.getDocumentCode() + " is waiting for your approval for " + document.getDestinationDepartmentName() + ".",
                    id
            );
        }
        return document;
    }

    public static synchronized DocumentRecord createDocument(User user, DocumentRecord document, boolean submit) {
        return createDocument(user, document, List.of(), submit);
    }

    public static synchronized DocumentRecord updateDocument(
            User user,
            DocumentRecord update,
            List<DocumentFile> newFiles,
            Set<Long> removeFileIds,
            String action
    ) {
        DocumentRecord existing = DOCUMENTS.stream()
                .filter(document -> document.getId() == update.getId())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Document not found."));

        if (!Objects.equals(existing.getCreatedById(), user.getId())) {
            throw new IllegalArgumentException("Only the clerk who created this document can edit it.");
        }
        if (!Set.of("RETURNED_FOR_CORRECTION", "DRAFT", "RECALLED").contains(existing.getStatus())) {
            throw new IllegalArgumentException("Only returned, recalled or draft documents can be edited.");
        }

        Department destination = requireDepartment(update.getDestinationDepartmentId());
        User selectedBoss = requireBoss(update.getBossId());
        boolean replacingPrimary = newFiles.stream().anyMatch(DocumentFile::isPrimaryFile);

        if (replacingPrimary) {
            FILES.removeIf(file -> file.getDocumentId() == existing.getId() && file.isPrimaryFile());
        }
        if (removeFileIds != null && !removeFileIds.isEmpty()) {
            FILES.removeIf(file -> file.getDocumentId() == existing.getId()
                    && !file.isPrimaryFile()
                    && removeFileIds.contains(file.getId()));
        }
        addFiles(existing.getId(), newFiles);

        existing.setTitle(update.getTitle());
        existing.setReferenceNo(update.getReferenceNo());
        existing.setSender(update.getSender());
        existing.setDateReceived(update.getDateReceived());
        existing.setCategory(update.getCategory());
        existing.setPriority(update.getPriority());
        existing.setDescription(update.getDescription());
        existing.setDestinationDepartmentId(destination.getId());
        existing.setDestinationDepartmentName(destination.getName());
        existing.setBossId(selectedBoss.getId());
        existing.setBossName(selectedBoss.getFullName());
        existing.setApprovedAt(null);

        switch (action) {
            case "draft" -> {
                existing.setStatus("DRAFT");
                existing.setSubmittedAt(null);
                existing.setDueDate(null);
            }
            case "resubmit" -> {
                existing.setStatus("PENDING_APPROVAL");
                existing.setSubmittedAt(LocalDateTime.now());
                existing.setDueDate(LocalDate.now().plusDays("URGENT".equals(existing.getPriority()) ? 2 : 5));
                existing.setDaysPending(0);
                existing.setRejectionReason(null);
                addNotification(
                        existing.getBossId(),
                        "PENDING_APPROVAL",
                        existing.getDocumentCode() + " was corrected and resubmitted for your approval.",
                        existing.getId()
                );
            }
            case "save" -> {
                if ("RECALLED".equals(existing.getStatus())) {
                    existing.setStatus("DRAFT");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported document edit action.");
        }

        refreshPrimaryFile(existing);
        return existing;
    }

    public static synchronized void action(
            User user,
            long documentId,
            String action,
            String remarks,
            Long newDepartmentId
    ) {
        DocumentRecord document = findDocument(user, documentId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found or access denied.");
        }

        if ("RECALL".equals(action)
                && Objects.equals(document.getCreatedById(), user.getId())
                && "PENDING_APPROVAL".equals(document.getStatus())) {
            document.setStatus("RECALLED");
            addNotification(document.getBossId(), "RECALLED", document.getDocumentCode() + " was recalled by the clerk.", document.getId());
            return;
        }

        if (!user.hasRole("BOSS") || !Objects.equals(document.getBossId(), user.getId())) {
            throw new IllegalArgumentException("Only the assigned boss can decide this document.");
        }
        if (!"PENDING_APPROVAL".equals(document.getStatus())) {
            throw new IllegalArgumentException("Document is not pending approval.");
        }
        if (!Set.of("APPROVE", "REJECT").contains(action)) {
            throw new IllegalArgumentException("Unsupported approval decision.");
        }
        if ("REJECT".equals(action) && (remarks == null || remarks.isBlank())) {
            throw new IllegalArgumentException("A return reason is required.");
        }

        String previousDepartment = document.getDestinationDepartmentName();
        if (newDepartmentId != null) {
            Department department = requireDepartment(newDepartmentId);
            document.setDestinationDepartmentId(department.getId());
            document.setDestinationDepartmentName(department.getName());
        }
        if (!Objects.equals(previousDepartment, document.getDestinationDepartmentName())) {
            addNotification(
                    document.getCreatedById(),
                    "DESTINATION_CHANGED",
                    document.getDocumentCode() + " was redirected from " + previousDepartment
                            + " to " + document.getDestinationDepartmentName() + " by " + user.getFullName() + ".",
                    document.getId()
            );
        }

        ApprovalRecord approval = new ApprovalRecord();
        approval.setId(IDS.incrementAndGet());
        approval.setDocumentId(document.getId());
        approval.setDocumentCode(document.getDocumentCode());
        approval.setDocumentTitle(document.getTitle());
        approval.setApproverId(user.getId());
        approval.setApproverName(user.getFullName());
        approval.setDecision(action);
        approval.setRemarks(remarks);
        approval.setPreviousDepartment(previousDepartment);
        approval.setFinalDepartment(document.getDestinationDepartmentName());
        approval.setDecisionAt(LocalDateTime.now());
        APPROVALS.add(approval);

        if ("APPROVE".equals(action)) {
            document.setStatus("ROUTED");
            document.setApprovedAt(LocalDateTime.now());
            document.setDaysPending(0);
            addNotification(
                    document.getCreatedById(),
                    "APPROVED",
                    document.getDocumentCode() + " was approved and routed to " + document.getDestinationDepartmentName() + ".",
                    document.getId()
            );
            USERS.stream()
                    .filter(candidate -> candidate.hasRole("DEPARTMENT_USER"))
                    .filter(candidate -> Objects.equals(candidate.getDepartmentId(), document.getDestinationDepartmentId()))
                    .forEach(candidate -> addNotification(
                            candidate.getId(),
                            "DOCUMENT_ROUTED",
                            document.getDocumentCode() + " is now available in the "
                                    + document.getDestinationDepartmentName() + " folder.",
                            document.getId()
                    ));
        } else if ("REJECT".equals(action)) {
            document.setStatus("RETURNED_FOR_CORRECTION");
            document.setRejectionReason(remarks);
            addNotification(
                    document.getCreatedById(),
                    "RETURNED",
                    document.getDocumentCode() + " was returned for correction: " + remarks,
                    document.getId()
            );
        }
    }

    private static void refreshDaysPending() {
        LocalDate today = LocalDate.now();
        DOCUMENTS.forEach(document -> {
            if ("PENDING_APPROVAL".equals(document.getStatus()) && document.getSubmittedAt() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        document.getSubmittedAt().toLocalDate(), today
                );
                document.setDaysPending((int) Math.max(0, days));
            } else if (!"PENDING_APPROVAL".equals(document.getStatus())) {
                document.setDaysPending(0);
            }
        });
    }

    private static Department requireDepartment(Long departmentId) {
        return DEPARTMENTS.stream()
                .filter(department -> Objects.equals(department.getId(), departmentId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The selected destination department is invalid."));
    }

    private static User requireBoss(Long bossId) {
        return USERS.stream()
                .filter(user -> Objects.equals(user.getId(), bossId) && user.hasRole("BOSS"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("The selected boss is invalid or no longer has the Boss role."));
    }

    private static void addFiles(long documentId, List<DocumentFile> files) {
        if (files == null) {
            return;
        }
        for (DocumentFile file : files) {
            file.setId(IDS.incrementAndGet());
            file.setDocumentId(documentId);
            FILES.add(file);
        }
    }

    private static void refreshPrimaryFile(DocumentRecord document) {
        DocumentFile primary = FILES.stream()
                .filter(file -> file.getDocumentId() == document.getId() && file.isPrimaryFile())
                .findFirst()
                .orElse(null);
        document.setPrimaryFileName(primary == null ? null : primary.getOriginalName());
        document.setPrimaryFilePath(primary == null ? null : primary.getStoragePath());
    }

    private static void addNotification(Long recipientId, String type, String message, Long documentId) {
        if (recipientId == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setId(IDS.incrementAndGet());
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setDocumentId(documentId);
        notification.setCreatedAt(LocalDateTime.now());
        NOTIFICATIONS.add(notification);
    }
}
