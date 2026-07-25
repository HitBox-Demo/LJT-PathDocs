package com.chekrol.dms.listener;

import com.chekrol.dms.util.AppConfig;
import com.chekrol.dms.util.DatabaseConnection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class OverdueNotificationListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        if (AppConfig.isDemoMode()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "ljtrouteflow-overdue-check");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(
                this::createOverdueNotifications,
                1,
                1,
                TimeUnit.HOURS
        );
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void createOverdueNotifications() {
        String sql = """
                INSERT INTO dms_notification(
                    recipient_id, type, message, document_id, read_flag
                )
                SELECT d.boss_id,
                       'OVERDUE',
                       d.document_code || ' is overdue and requires approval.',
                       d.document_id,
                       'N'
                FROM dms_document d
                WHERE d.status='PENDING_APPROVAL'
                  AND d.due_date < TRUNC(SYSDATE)
                  AND NOT EXISTS(
                      SELECT 1
                      FROM dms_notification n
                      WHERE n.document_id=d.document_id
                        AND n.recipient_id=d.boss_id
                        AND n.type='OVERDUE'
                        AND n.created_at > SYSTIMESTAMP - INTERVAL '1' DAY
                  )
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (Exception exception) {
            System.err.println(
                    "LJT RouteFlow overdue notification check failed: "
                            + exception.getMessage()
            );
        }
    }
}
