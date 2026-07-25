# Changelog

## 1.2.0 — 20 July 2026

- Reconstructed the project into one clean Maven source tree.
- Removed the duplicate nested `src/src` tree, generated `target` content and bundled Git history from the handover package.
- Removed duplicate/conflicting image collectors, old camera/gallery JavaScript and unused preview markup.
- Rebuilt the single **Choose Images** control so repeated picker operations retain earlier selections.
- Added selected-page thumbnails, page numbering, individual removal and Remove All.
- Verified that multiple selected images become one ordered multi-page PDF.
- Completed returned/recalled document editing and same-record resubmission.
- Preserved rejection remarks in approval history after resubmission.
- Completed demo user creation and boss-approved role-change requests.
- Improved recall, reject, destination-change, routing and notification validation.
- Removed the Confidential feature from Java and the interface while retaining an unused default-`N` Oracle column for future compatibility.
- Fixed Oracle schema and seed-data statement errors.
- Consolidated mobile table and text-overflow rules.
- Removed problematic emoji/special-character UI symbols that caused mojibake characters.
- Reformatted compact Java controllers, filters and DAO code for maintainability.
- Added expanded workflow, PDF, file-storage and password tests.
- Updated the PWA cache to `ljtrouteflow-static-v12`.

## 1.1.0 — July 2026

- Standardised the application name and context path as `LJTRouteFlow`.
- Updated build, demo and deployment scripts to create and deploy `LJTRouteFlow.war`.
- Fixed demo-mode destination and selected-boss display-name resolution.
- Applied the official LJT colour palette and responsive interface.
