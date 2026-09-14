# Oracle integration review — version 1.3.0

Reviewed against the final Oracle XE 21c schema:

- `UserDAO` → users, departments and roles
- `DocumentDAO` → documents, files, history and notifications
- `ApprovalDAO` → approval decision, destination change and routing notification
- `RoleRequestDAO` → role change transaction and requester notification
- `NotificationDAO` → list and mark-read operations
- `DepartmentDAO` → active department lookup

## Corrections made

1. Added `DocumentRecord.submissionKey`.
2. Added `submission_key` to document INSERT and SELECT mapping.
3. Added database idempotency handling for repeated create requests.
4. Kept document, file, history and notification inserts in one transaction.
5. Cleaned newly stored files when a repeated request resolves to an existing row.
6. Changed database configuration precedence so explicit `DMS_DB_*` values win
   over an old Tomcat JNDI resource.
7. Added connection and read timeouts.
8. Upgraded the database-test page to verify Oracle while demo mode is still on.
9. Database test now validates schema user, XEPDB1 container and all 10 tables.

## Remaining runtime verification

A real Oracle connection cannot be executed in the build environment. Complete
these on the target PC:

- JDBC database-test page
- Oracle login using seed hashes
- Create Draft and Submit for Approval
- repeated-submit test
- Recall and Resubmit
- Boss Approve/Reject and destination change
- Department folder visibility
- file download authorization
