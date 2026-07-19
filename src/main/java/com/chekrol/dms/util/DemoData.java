package com.chekrol.dms.util;

import com.chekrol.dms.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public final class DemoData {
    private static final AtomicLong IDS = new AtomicLong(100);
    private static final List<User> USERS = new CopyOnWriteArrayList<>();
    private static final List<Department> DEPARTMENTS = new CopyOnWriteArrayList<>();
    private static final List<DocumentRecord> DOCUMENTS = new CopyOnWriteArrayList<>();
    private static final List<Notification> NOTIFICATIONS = new CopyOnWriteArrayList<>();
    private static final List<ApprovalRecord> APPROVALS = new CopyOnWriteArrayList<>();
    private static final List<RoleRequest> ROLE_REQUESTS = new CopyOnWriteArrayList<>();

    static {
        DEPARTMENTS.add(new Department(1, "IT", "Information Technology", "ACTIVE"));
        DEPARTMENTS.add(new Department(2, "FINANCE", "Finance", "ACTIVE"));
        DEPARTMENTS.add(new Department(3, "MANAGEMENT", "Management", "ACTIVE"));

        USERS.add(user(1, "clerk", "Ahmad Fadzli", "ahmad.fadzli@agency.gov.my", null, null, "CLERK", "SYSTEM_ADMIN"));
        USERS.add(user(2, "boss", "En. Razali Osman", "razali@agency.gov.my", null, null, "BOSS"));
        USERS.add(user(3, "it.user", "Nur Izzati", "izzati.it@agency.gov.my", 1L, "Information Technology", "DEPARTMENT_USER"));
        USERS.add(user(4, "finance.user", "Siti Rahimah", "siti.finance@agency.gov.my", 2L, "Finance", "DEPARTMENT_USER"));
        USERS.add(user(5, "management.user", "Pn. Norhaslinda", "norhaslinda@agency.gov.my", 3L, "Management", "DEPARTMENT_USER", "BOSS"));

        addDoc(1, "DOC-2026-0001", "Procurement Request - Server Upgrade", "IT/PR/2026/001", "Jabatan IT", "Procurement", "URGENT", false, 1L, "Information Technology", 2L, "En. Razali Osman", "PENDING_APPROVAL", 1L, "Ahmad Fadzli", 6, null);
        addDoc(2, "DOC-2026-0002", "Budget Allocation Q3 2026", "FIN/BA/2026/003", "Jabatan Kewangan", "Finance", "NORMAL", true, 2L, "Finance", 2L, "En. Razali Osman", "PENDING_APPROVAL", 1L, "Ahmad Fadzli", 7, null);
        addDoc(3, "DOC-2026-0003", "Staff Leave Policy Amendment", "HR/POL/2026/011", "Jabatan HR", "Policy", "NORMAL", false, 3L, "Management", 5L, "Pn. Norhaslinda", "PENDING_APPROVAL", 1L, "Ahmad Fadzli", 3, null);
        addDoc(4, "DOC-2026-0004", "Vendor Contract Renewal - Telco Services", "IT/CON/2026/007", "TM Berhad", "Contract", "NORMAL", true, 1L, "Information Technology", 2L, "En. Razali Osman", "ROUTED", 4L, "Siti Rahimah", 0, null);
        addDoc(5, "DOC-2026-0005", "Office Renovation Quotation", "FAC/QU/2026/002", "Syarikat Bina Maju", "Facilities", "NORMAL", false, 3L, "Management", 5L, "Pn. Norhaslinda", "RETURNED_FOR_CORRECTION", 1L, "Ahmad Fadzli", 0, "Missing page 3 of quotation. Please attach the complete document.");
        addDoc(6, "DOC-2026-0006", "Annual Performance Report 2025", "HR/APR/2025/001", "Jabatan HR", "Report", "NORMAL", true, 3L, "Management", 5L, "Pn. Norhaslinda", "ROUTED", 4L, "Siti Rahimah", 0, null);
        addDoc(7, "DOC-2026-0007", "IT Infrastructure Audit 2026", "IT/AUD/2026/001", "Audit Unit", "Audit", "URGENT", false, 1L, "Information Technology", 2L, "En. Razali Osman", "DRAFT", 1L, "Ahmad Fadzli", 0, null);

        Notification n = new Notification();
        n.setId(1); n.setRecipientId(1); n.setType("DESTINATION_CHANGED");
        n.setMessage("DOC-2026-0004 was redirected to the IT folder by En. Razali Osman."); n.setDocumentId(4L); n.setCreatedAt(LocalDateTime.now().minusDays(1));
        NOTIFICATIONS.add(n);
        Notification n2 = new Notification();
        n2.setId(2); n2.setRecipientId(2); n2.setType("OVERDUE");
        n2.setMessage("DOC-2026-0002 is overdue and requires approval."); n2.setDocumentId(2L); n2.setCreatedAt(LocalDateTime.now().minusHours(4));
        NOTIFICATIONS.add(n2);

        RoleRequest rr = new RoleRequest();
        rr.setId(1); rr.setUserId(3); rr.setUserName("Nur Izzati"); rr.setCurrentRole("DEPARTMENT_USER");
        rr.setRequestedRole("BOSS"); rr.setRequestedById(1); rr.setRequestedByName("Ahmad Fadzli");
        rr.setStatus("PENDING"); rr.setRequestedAt(LocalDateTime.now().minusDays(2));
        ROLE_REQUESTS.add(rr);
    }

    private DemoData() {}

    private static User user(long id, String username, String name, String email, Long deptId, String deptName, String... roles) {
        User user = new User(id, username, name, email, "ACTIVE", deptId, deptName, new LinkedHashSet<>(Arrays.asList(roles)));
        user.setPasswordHash("demo");
        return user;
    }

    private static void addDoc(long id, String code, String title, String ref, String sender, String category,
                        String priority, boolean confidential, Long deptId, String deptName, Long bossId,
                        String bossName, String status, Long creatorId, String creatorName, int days, String reason) {
        DocumentRecord d = new DocumentRecord();
        d.setId(id); d.setDocumentCode(code); d.setTitle(title); d.setReferenceNo(ref); d.setSender(sender);
        d.setDateReceived(LocalDate.now().minusDays(Math.max(days, 1))); d.setCategory(category); d.setPriority(priority);
        d.setConfidential(false); d.setDestinationDepartmentId(deptId); d.setDestinationDepartmentName(deptName);
        d.setBossId(bossId); d.setBossName(bossName); d.setStatus(status); d.setCreatedById(creatorId);
        d.setCreatedByName(creatorName); d.setCreatedAt(LocalDateTime.now().minusDays(Math.max(days, 1)));
        d.setSubmittedAt(status.equals("DRAFT") ? null : LocalDateTime.now().minusDays(Math.max(days, 1)));
        d.setDueDate(d.getSubmittedAt() == null ? null : d.getSubmittedAt().toLocalDate().plusDays(priority.equals("URGENT") ? 2 : 5));
        d.setDaysPending(days); d.setRejectionReason(reason); d.setDescription("Demo record converted from the React/Vite prototype.");
        DOCUMENTS.add(d);
    }

    public static User authenticate(String username, String password) {
        String expected = switch (username == null ? "" : username.toLowerCase(Locale.ROOT)) {
            case "clerk" -> "Clerk@123";
            case "boss" -> "Boss@123";
            case "it.user", "finance.user", "management.user" -> "Dept@123";
            default -> "";
        };
        if (!expected.equals(password)) return null;
        return USERS.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public static List<User> users() { return new ArrayList<>(USERS); }
    public static List<User> bosses() { return USERS.stream().filter(u -> u.hasRole("BOSS")).collect(Collectors.toList()); }
    public static List<Department> departments() { return new ArrayList<>(DEPARTMENTS); }
    public static List<RoleRequest> roleRequests() { return new ArrayList<>(ROLE_REQUESTS); }
    public static List<ApprovalRecord> approvals() { return new ArrayList<>(APPROVALS); }

    public static List<Notification> notifications(User user) {
        return NOTIFICATIONS.stream().filter(n -> n.getRecipientId() == user.getId()).sorted(Comparator.comparing(Notification::getCreatedAt).reversed()).collect(Collectors.toList());
    }

    public static Map<String, Long> stats(User user) {
        Map<String, Long> map = new LinkedHashMap<>();
        List<DocumentRecord> visible = listDocuments(user, "repository", "");
        map.put("TOTAL", (long) visible.size());
        map.put("PENDING", DOCUMENTS.stream().filter(d -> "PENDING_APPROVAL".equals(d.getStatus()) && canSee(user, d)).count());
        map.put("RETURNED", DOCUMENTS.stream().filter(d -> "RETURNED_FOR_CORRECTION".equals(d.getStatus()) && canSee(user, d)).count());
        map.put("ROUTED", DOCUMENTS.stream().filter(d -> "ROUTED".equals(d.getStatus()) && canSee(user, d)).count());
        map.put("OVERDUE", DOCUMENTS.stream().filter(d -> "PENDING_APPROVAL".equals(d.getStatus()) && d.getDueDate() != null && d.getDueDate().isBefore(LocalDate.now()) && canSee(user, d)).count());
        return map;
    }

    public static List<DocumentRecord> listDocuments(User user, String view, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        return DOCUMENTS.stream().filter(d -> canSee(user, d)).filter(d -> switch (view == null ? "repository" : view) {
            case "draft" -> "DRAFT".equals(d.getStatus()) && Objects.equals(d.getCreatedById(), user.getId());
            case "pending" -> "PENDING_APPROVAL".equals(d.getStatus());
            case "returned" -> "RETURNED_FOR_CORRECTION".equals(d.getStatus()) && Objects.equals(d.getCreatedById(), user.getId());
            case "department" -> "ROUTED".equals(d.getStatus()) && user.hasRole("DEPARTMENT_USER") && Objects.equals(
                        d.getDestinationDepartmentId(),
                        user.getDepartmentId()
                );
            case "search", "repository" -> true;
            default -> true;
        }).filter(d -> q.isBlank() || (d.getDocumentCode() + " " + d.getTitle() + " " + d.getReferenceNo() + " " + d.getSender()).toLowerCase(Locale.ROOT).contains(q))
            .sorted(Comparator.comparing(DocumentRecord::getCreatedAt).reversed()).collect(Collectors.toList());
    }

    private static boolean canSee(User user, DocumentRecord d) {
        if (user.hasRole("SYSTEM_ADMIN") || user.hasRole("CLERK")) return true;
        if (user.hasRole("BOSS") && Objects.equals(d.getBossId(), user.getId())) return true;
        return user.hasRole("DEPARTMENT_USER") && "ROUTED".equals(d.getStatus())
                && Objects.equals(d.getDestinationDepartmentId(), user.getDepartmentId());
    }

    public static DocumentRecord findDocument(User user, long id) {
        return DOCUMENTS.stream().filter(d -> d.getId() == id && canSee(user, d)).findFirst().orElse(null);
    }

    public static synchronized DocumentRecord createDocument(
        User user,
        DocumentRecord document,
        boolean submit
    ) {
        Department destination = DEPARTMENTS.stream()
                .filter(department ->
                        Objects.equals(
                                department.getId(),
                                document.getDestinationDepartmentId()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "The selected destination department is invalid."
                        )
                );

        User selectedBoss = USERS.stream()
                .filter(candidate ->
                        Objects.equals(candidate.getId(), document.getBossId())
                                && candidate.hasRole("BOSS")
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "The selected boss is invalid or no longer has the Boss role."
                        )
                );

        long id = IDS.incrementAndGet();

        document.setId(id);
        document.setDocumentCode(
                "DOC-" + LocalDate.now().getYear()
                        + "-" + String.format("%04d", id)
        );

        document.setCreatedById(user.getId());
        document.setCreatedByName(user.getFullName());

        // Resolve the IDs selected in the form into display names.
        document.setDestinationDepartmentName(destination.getName());
        document.setBossName(selectedBoss.getFullName());

        document.setCreatedAt(LocalDateTime.now());
        document.setStatus(submit ? "PENDING_APPROVAL" : "DRAFT");

        if (submit) {
            document.setSubmittedAt(LocalDateTime.now());

            int approvalDays =
                    "URGENT".equals(document.getPriority()) ? 2 : 5;

            document.setDueDate(
                    LocalDate.now().plusDays(approvalDays)
            );
        }

        DOCUMENTS.add(document);

        if (submit) {
            addNotification(
                    document.getBossId(),
                    "PENDING_APPROVAL",
                    document.getDocumentCode()
                            + " is waiting for your approval for "
                            + document.getDestinationDepartmentName()
                            + ".",
                    id
            );
        }

        return document;
    }

    public static synchronized void action(User user, long documentId, String action, String remarks, Long newDepartmentId) {
        DocumentRecord d = findDocument(user, documentId);
        if (d == null) throw new IllegalArgumentException("Document not found or access denied.");
        if ("RECALL".equals(action) && Objects.equals(d.getCreatedById(), user.getId()) && "PENDING_APPROVAL".equals(d.getStatus())) {
            d.setStatus("RECALLED");
            addNotification(d.getBossId(), "RECALLED", d.getDocumentCode() + " was recalled by the clerk.", d.getId());
            return;
        }
        if (!user.hasRole("BOSS") || !Objects.equals(d.getBossId(), user.getId())) throw new IllegalArgumentException("Only the assigned boss can decide this document.");
        if (!"PENDING_APPROVAL".equals(d.getStatus())) throw new IllegalArgumentException("Document is not pending approval.");

        String oldDepartment = d.getDestinationDepartmentName();
        if (newDepartmentId != null) {
            Department dept = DEPARTMENTS.stream().filter(x -> x.getId() == newDepartmentId).findFirst().orElse(null);
            if (dept != null) { d.setDestinationDepartmentId(dept.getId()); d.setDestinationDepartmentName(dept.getName()); }
        }
        ApprovalRecord approval = new ApprovalRecord();
        approval.setId(IDS.incrementAndGet()); approval.setDocumentId(d.getId()); approval.setDocumentCode(d.getDocumentCode());
        approval.setDocumentTitle(d.getTitle()); approval.setApproverId(user.getId()); approval.setApproverName(user.getFullName());
        approval.setDecision(action); approval.setRemarks(remarks); approval.setPreviousDepartment(oldDepartment);
        approval.setFinalDepartment(d.getDestinationDepartmentName()); approval.setDecisionAt(LocalDateTime.now());
        APPROVALS.add(approval);

        if ("APPROVE".equals(action)) {
            d.setStatus("ROUTED");
            d.setApprovedAt(LocalDateTime.now());
            d.setDaysPending(0);

            addNotification(
                    d.getCreatedById(),
                    "APPROVED",
                    d.getDocumentCode()
                            + " was approved and routed to "
                            + d.getDestinationDepartmentName()
                            + ".",
                    d.getId()
            );

            // Notify every user belonging to the destination department.
            USERS.stream()
                    .filter(departmentUser ->
                            departmentUser.hasRole("DEPARTMENT_USER")
                                    && Objects.equals(
                                            departmentUser.getDepartmentId(),
                                            d.getDestinationDepartmentId()
                                    )
                    )
                    .forEach(departmentUser ->
                            addNotification(
                                    departmentUser.getId(),
                                    "DOCUMENT_ROUTED",
                                    d.getDocumentCode()
                                            + " has been routed to the "
                                            + d.getDestinationDepartmentName()
                                            + " folder.",
                                    d.getId()
                            )
                    );

            System.out.println(
                    "ROUTING SUCCESS: "
                            + d.getDocumentCode()
                            + " -> departmentId="
                            + d.getDestinationDepartmentId()
                            + ", department="
                            + d.getDestinationDepartmentName()
            );

        } else if ("REJECT".equals(action)) {
            d.setStatus("RETURNED_FOR_CORRECTION");
            d.setRejectionReason(remarks);

            addNotification(
                    d.getCreatedById(),
                    "RETURNED",
                    d.getDocumentCode()
                            + " was returned for correction: "
                            + remarks,
                    d.getId()
            );
        }
    }

    private static void addNotification(Long recipient, String type, String message, Long documentId) {
        if (recipient == null) return;
        Notification n = new Notification(); n.setId(IDS.incrementAndGet()); n.setRecipientId(recipient);
        n.setType(type); n.setMessage(message); n.setDocumentId(documentId); n.setCreatedAt(LocalDateTime.now());
        NOTIFICATIONS.add(n);
    }
}
