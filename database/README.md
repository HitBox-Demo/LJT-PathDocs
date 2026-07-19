# Oracle Database Setup

Use a dedicated application schema such as `DOC_ROUTE`. Do not run the web application as `SYS` or `SYSTEM`.

Run in order:

1. `01_create_schema.sql`
2. `02_seed_data.sql`
3. `03_verify_schema.sql`

Seed accounts:

- `clerk / Clerk@123`
- `boss / Boss@123`
- `it.user / Dept@123`
- `finance.user / Dept@123`
- `management.user / Dept@123`

Change all temporary passwords before a real deployment.
