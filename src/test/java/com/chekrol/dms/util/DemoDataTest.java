package com.chekrol.dms.util;

import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DemoDataTest {
    @Test
    void completeDocumentWorkflowShouldPreserveIdentityAndRouteCorrectly() {
        User clerk = DemoData.authenticate("clerk", "Clerk@123");
        User boss = DemoData.authenticate("boss", "Boss@123");
        User itUser = DemoData.authenticate("it.user", "Dept@123");
        User financeUser = DemoData.authenticate("finance.user", "Dept@123");

        assertNotNull(clerk);
        assertNotNull(boss);
        assertNotNull(itUser);
        assertNotNull(financeUser);

        DocumentRecord document = newDocument("Complete workflow test", 3L, boss.getId());
        DemoData.createDocument(clerk, document, true);
        long originalId = document.getId();
        String originalCode = document.getDocumentCode();

        assertEquals("Management", document.getDestinationDepartmentName());
        assertEquals(boss.getFullName(), document.getBossName());
        assertEquals("PENDING_APPROVAL", document.getStatus());

        DemoData.action(clerk, document.getId(), "RECALL", null, null);
        assertEquals("RECALLED", document.getStatus());
        assertFalse(DemoData.listDocuments(boss, "pending", "").stream()
                .anyMatch(item -> item.getId() == document.getId()));

        DocumentRecord recalledUpdate = copyForUpdate(document);
        recalledUpdate.setDestinationDepartmentId(1L);
        DemoData.updateDocument(clerk, recalledUpdate, List.of(), Set.of(), "resubmit");

        assertEquals(originalId, document.getId());
        assertEquals(originalCode, document.getDocumentCode());
        assertEquals("PENDING_APPROVAL", document.getStatus());
        assertEquals("Information Technology", document.getDestinationDepartmentName());

        assertThrows(
                IllegalArgumentException.class,
                () -> DemoData.action(boss, document.getId(), "REJECT", "", null)
        );

        DemoData.action(
                boss,
                document.getId(),
                "REJECT",
                "Please replace the unclear page.",
                null
        );
        assertEquals("RETURNED_FOR_CORRECTION", document.getStatus());
        int approvalHistorySize = DemoData.approvals().size();

        DocumentRecord returnedUpdate = copyForUpdate(document);
        returnedUpdate.setTitle("Corrected workflow test");
        DemoData.updateDocument(clerk, returnedUpdate, List.of(), Set.of(), "resubmit");

        assertEquals("PENDING_APPROVAL", document.getStatus());
        assertNull(document.getRejectionReason());
        assertEquals(approvalHistorySize, DemoData.approvals().size());

        DemoData.action(boss, document.getId(), "APPROVE", "Route to Finance.", 2L);
        assertEquals("ROUTED", document.getStatus());
        assertEquals("Finance", document.getDestinationDepartmentName());
        assertTrue(DemoData.listDocuments(financeUser, "department", "").stream()
                .anyMatch(item -> item.getId() == document.getId()));
        assertFalse(DemoData.listDocuments(itUser, "department", "").stream()
                .anyMatch(item -> item.getId() == document.getId()));
    }

    @Test
    void demoUserAndRoleRequestShouldBeAppliedAfterApproval() {
        User clerk = DemoData.authenticate("clerk", "Clerk@123");
        User boss = DemoData.authenticate("boss", "Boss@123");

        User user = new User();
        String uniqueUsername = "workflow.user." + System.nanoTime();
        user.setUsername(uniqueUsername);
        user.setFullName("Workflow User");
        user.setEmail(uniqueUsername + "@example.test");
        user.setDepartmentId(1L);

        long userId = DemoData.createUser(user, "Testing123", "DEPARTMENT_USER");
        User created = DemoData.authenticate(uniqueUsername, "Testing123");
        assertNotNull(created);
        assertEquals(userId, created.getId());

        long requestId = DemoData.createRoleRequest(
                userId,
                "DEPARTMENT_USER",
                "BOSS",
                clerk,
                "Temporary approval coverage"
        );
        DemoData.decideRoleRequest(requestId, boss, "APPROVE", "Approved for testing");

        User updated = DemoData.authenticate(uniqueUsername, "Testing123");
        assertNotNull(updated);
        assertTrue(updated.hasRole("BOSS"));
    }

    private DocumentRecord newDocument(String title, long departmentId, long bossId) {
        DocumentRecord document = new DocumentRecord();
        document.setTitle(title);
        document.setReferenceNo("TEST-" + System.nanoTime());
        document.setSender("Automated Test");
        document.setDateReceived(LocalDate.now());
        document.setCategory("General");
        document.setPriority("NORMAL");
        document.setDescription("Automated workflow test");
        document.setDestinationDepartmentId(departmentId);
        document.setBossId(bossId);
        return document;
    }

    private DocumentRecord copyForUpdate(DocumentRecord source) {
        DocumentRecord copy = new DocumentRecord();
        copy.setId(source.getId());
        copy.setDocumentCode(source.getDocumentCode());
        copy.setTitle(source.getTitle());
        copy.setReferenceNo(source.getReferenceNo());
        copy.setSender(source.getSender());
        copy.setDateReceived(source.getDateReceived());
        copy.setCategory(source.getCategory());
        copy.setPriority(source.getPriority());
        copy.setDescription(source.getDescription());
        copy.setDestinationDepartmentId(source.getDestinationDepartmentId());
        copy.setBossId(source.getBossId());
        return copy;
    }
}
