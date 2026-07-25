# Cleanup and Reconstruction Report

## Removed from the source handover

- Duplicate nested source directory: `src/src`
- Generated Maven output: `target`
- Bundled `.git` history
- Duplicate image-picker markup and nested `data-image-collector` elements
- Old `Take Photo`, gallery-button and dynamic `.click()` picker implementations
- Old image-preview JavaScript that competed with the current collector
- Unused commented upload controls and duplicate selected-image containers
- Confidential UI fields, roles and access checks for version 1
- Unrecognised emoji/special-character UI markers that produced mojibake characters
- Stale application-name/runtime references from the earlier prototype where they affected deployment

## Reconstructed areas

- `create.jsp` and `edit.jsp` now contain one image collector each.
- `app.js` contains one multi-image implementation.
- The image picker retains earlier batches, prevents accidental replacement and supports per-image removal.
- Controllers, filters and major DAO methods were reformatted and given clearer validation/error handling.
- Document update and approval operations retain IDs, history and role restrictions.
- File replacement cleanup no longer deletes newly committed files when old-file deletion fails.
- Oracle DDL and seed statements were corrected and aligned with DAO column usage.
- `pom.xml` now identifies version `1.2.0` and produces `LJTRouteFlow.war`.

## Intentionally retained

- `confidential_flag` remains in Oracle with default `N` for future compatibility, but version 1 does not expose or enforce the feature.
- Legacy `docroute` cleanup remains in the deployment script only to remove an old Tomcat deployment safely.
- The migration brief remains under `docs` as project background; it is not loaded at runtime.
