# Oracle integration build verification

Version: 1.3.0

## Completed checks

- 50 Java source files compiled with Java 21.
- Existing utility and workflow tests compiled and executed.
- 11 test methods passed.
- 14 additional workflow smoke checks passed.
- Oracle seed hashes match the documented passwords.
- JavaScript syntax checks passed for `app.js` and `service-worker.js`.
- `pom.xml` and `web.xml` parsed successfully.
- Create Document contains exactly one CSRF token, one submission token, and one submit-once form marker.
- Every SQL script enables `SQLBLANKLINES` and disables substitution variables.
- Final schema includes `DMS_DOCUMENT.SUBMISSION_KEY`.
- Java model, servlet, service and DAO all carry the submission key.
- All main DAOs were checked against the 10-table Oracle XE 21c schema.

## DAO transaction review

- User creation: user and initial role commit together.
- Document creation: document, files, history and boss notification commit together.
- Document update: metadata, file metadata, history and resubmit notification commit together.
- Recall: status, history and boss notification commit together.
- Approval: document decision, approval history, audit history and notifications commit together.
- Role decision: role assignment, request status and requester notification commit together.

## Runtime limitation

The build environment cannot connect to the user's local Oracle listener. The final runtime checks must be completed at:

```text
http://localhost:8082/LJTRouteFlow/setup/database-test
```
