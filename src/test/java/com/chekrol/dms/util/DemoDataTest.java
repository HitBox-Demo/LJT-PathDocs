package com.chekrol.dms.util;

import com.chekrol.dms.model.Department;
import com.chekrol.dms.model.DocumentRecord;
import com.chekrol.dms.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DemoDataTest {
    @Test
    void createDocumentShouldResolveDestinationAndBossNames() {
        User clerk = DemoData.authenticate("clerk", "Clerk@123");
        Department destination = DemoData.departments().get(0);
        User boss = DemoData.bosses().get(0);

        DocumentRecord document = new DocumentRecord();
        document.setTitle("Regression Test Document");
        document.setDestinationDepartmentId(destination.getId());
        document.setBossId(boss.getId());
        document.setPriority("NORMAL");

        DocumentRecord created = DemoData.createDocument(clerk, document, true);

        assertNotNull(created.getId());
        assertEquals(destination.getName(), created.getDestinationDepartmentName());
        assertEquals(boss.getFullName(), created.getBossName());
    }
}
