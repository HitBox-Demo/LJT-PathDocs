# Oracle XE 21c database scripts

Confirmed target:

- Oracle Database XE 21c
- Host: `localhost`
- Port: `1521`
- Service/container: `XEPDB1`
- Schema: `LJT_ROUTE_FLOW`

The schema has already been installed when all scripts through
`04_transaction_smoke_test.sql` completed successfully.

Fresh order:

```sql
@database/00_preflight.sql
@database/00_cleanup_partial.sql
@database/01_create_schema.sql
@database/02_seed_data.sql
@database/03_verify_schema.sql
@database/04_transaction_smoke_test.sql
```

Do not rerun `01_create_schema.sql` against an already-complete schema.
`99_drop_schema.sql` permanently deletes all RouteFlow data.

The `DMS_DOCUMENT.SUBMISSION_KEY` unique constraint is used by application
version 1.3.0 for database-level duplicate-request protection.
