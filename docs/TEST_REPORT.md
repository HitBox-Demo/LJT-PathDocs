# Verification and Test Report

Verification date: 20 July 2026

## Automated checks passed

- Main Java source compilation with Java 21
- 43 demo workflow and file-processing smoke checks
- Demo login for Clerk, Boss, IT and Finance users
- Destination and selected-boss name resolution
- Recall and removal from the boss queue
- Same-ID and same-code edit/resubmit behaviour
- Mandatory rejection reason
- Rejection history preservation after resubmission
- Boss destination change
- Correct department visibility and wrong-department denial
- Clerk and department routing notifications
- Mark-all-notifications-read
- Demo user creation and authentication
- Role request approval and role application
- Two-image to two-page PDF conversion
- DocumentService storage of a multi-page main PDF and attachment
- Replacement of a returned document's main PDF
- Removal of an attachment and old physical files
- Seed password hashes for `Clerk@123`, `Boss@123` and `Dept@123`
- JavaScript syntax validation
- `manifest.json` JSON parsing
- `web.xml` XML parsing
- CSS brace/structure validation
- JSP image-picker structure: exactly one collector/input/store per create/edit page
- Internal JSP route-to-servlet mapping validation
- No duplicate `src/src`, `.git` or generated `target` in the clean source package
- No detected mojibake sequences in source text
- WAR content validation: classes, JSP, static assets and runtime libraries present

## Not executable in the verification environment

- Live Oracle connection and transaction execution
- A full browser session on the user's actual Apache Tomcat 9 installation
- Native mobile gallery behaviour on every Android/iOS browser
- Production HTTPS, filesystem permissions and multi-user concurrency

These must be completed using `docs/MANUAL_ACCEPTANCE_CHECKLIST.md` after deployment on the target PC/server.
