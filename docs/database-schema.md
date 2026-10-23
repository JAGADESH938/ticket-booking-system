# Database Schema

9 tables, InnoDB engine, `utf8mb4` charset.

## Entity Relationship Summary

- `theatres` 1—* `screens` 1—* `seats`
- `movies` 1—* `shows` *—1 `screens`
- `users` 1—* `bookings` 1—* `booking_seats` *—1 `seats`
- `bookings` 1—1 `payments`

## Key Constraints

| Table | Constraint | Purpose |
|---|---|---|
| `users` | UNIQUE(email) | Prevent duplicate accounts |
| `seats` | UNIQUE(screen_id, seat_number, row_number) | Prevent duplicate physical seats |
| `booking_seats` | UNIQUE(show_id, seat_id) | Concurrency backstop — prevents double-booking at the DB level |
| `bookings` | UNIQUE(booking_reference) | Human-readable unique booking lookup |
| `payments` | UNIQUE(booking_id), UNIQUE(transaction_id) | One payment per booking |

## Generating a Live Schema Dump

```bash
mysqldump -u ticketbook_user -p --no-data --skip-comments ticketbook_db > docs/schema-dump.sql
```

Run this after starting the app once (Hibernate's `ddl-auto: update` will have created
all tables) to get the exact, current schema alongside this summary.
