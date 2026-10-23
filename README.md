# TicketBook — Online Ticket Booking System

A backend for booking movie/event tickets, built as an individual project to demonstrate
production-relevant backend engineering: relational data modeling, JWT authentication,
role-based authorization, and — the core technical focus — **safe concurrent seat booking**.

## The Problem This Project Solves

Two users can try to book the same seat for the same show at the same instant. This system
guarantees only one succeeds, using database-level pessimistic locking (`SELECT ... FOR UPDATE`)
combined with a unique-constraint backstop — not application-level checks alone, which are
vulnerable to race conditions. Proven with a multi-threaded JUnit integration test
(`BookingConcurrencyIntegrationTest`) that fires 10 concurrent booking requests at one seat.

## Tech Stack

Java 21 · Spring Boot 3.3 · Spring Security · JWT (jjwt) · Spring Data JPA / Hibernate · MySQL ·
Maven · JUnit 5 · Mockito · Swagger/OpenAPI · Docker (optional)

## Features

**Users** can register, log in, browse/search movies and shows, view live seat availability,
book seats (with simulated payment), view booking history, and cancel eligible bookings.

**Admins** can manage movies, theatres, screens, seats, and shows.

## Architecture

```
Controller -> Service -> Repository -> Entity (JPA) -> MySQL
                 |
               DTOs (never expose entities directly)
```



## Key Design Decisions

- **Concurrency**: pessimistic row locking (`FOR UPDATE`) on seats during booking, with
  seats locked in sorted ID order to prevent deadlocks on multi-seat bookings. A database
  unique constraint on `(show_id, seat_id)` acts as a defense-in-depth backstop.
- **Money**: all prices use `BigDecimal`, never floating-point types.
- **Security**: stateless JWT auth, BCrypt password hashing, role hardcoded server-side on
  registration (never trusted from the client).
- **Soft deletes**: movies/theatres are deactivated (`status` flag), never hard-deleted, to
  preserve booking history integrity.
- **Transaction boundaries**: booking creation and payment happen in one transaction — a
  failed payment rolls back the entire booking, releasing the seat automatically.

## Getting Started

### Prerequisites
- Java 21
- Maven
- MySQL 8.x (or Docker, see below)

### Option A — Run locally with your own MySQL

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE ticketbook_db CHARACTER SET utf8mb4;"
mysql -u root -p -e "CREATE USER 'ticketbook_user'@'localhost' IDENTIFIED BY 'your_password';"
mysql -u root -p -e "GRANT ALL PRIVILEGES ON ticketbook_db.* TO 'ticketbook_user'@'localhost'; FLUSH PRIVILEGES;"

# 2. Set environment variables
export DB_USERNAME=ticketbook_user
export DB_PASSWORD=your_password
export JWT_SECRET=a-real-32-byte-plus-secret-key

# 3. Run
mvn spring-boot:run
```

App runs at `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Option B — Run everything with Docker Compose

```bash
cp .env.example .env
# edit .env with real values, then:
docker compose up --build
```

### Running Tests

```bash
mvn test
```

Run just the concurrency test in isolation:
```bash
mvn test -Dtest=BookingConcurrencyIntegrationTest
```

## API Overview

| Area | Base path |
|---|---|
| Auth | `/api/auth/**` |
| Movies | `/api/movies`, `/api/admin/movies` |
| Theatres/Screens/Seats | `/api/theatres/**`, `/api/admin/**` |
| Shows | `/api/shows`, `/api/admin/shows` |
| Bookings | `/api/bookings/**` |

Full interactive documentation via Swagger UI once running:
`http://localhost:8080/swagger-ui/index.html`

## Quick Manual Test Flow

```text
1. POST /api/auth/register
2. POST /api/auth/login                          -> copy the token
3. (In MySQL) UPDATE users SET role='ADMIN' WHERE email='...';  -- for admin testing
4. Re-login to get a token with the ADMIN role claim
5. POST /api/admin/movies
6. POST /api/admin/theatres
7. POST /api/admin/theatres/{theatreId}/screens
8. POST /api/admin/screens/{screenId}/seats
9. POST /api/admin/shows
10. GET  /api/shows/{showId}/seats
11. POST /api/bookings   (as a regular USER token)
12. GET  /api/bookings/my
```


## Future Enhancements

- Real payment gateway integration
- Seat-hold expiry job for abandoned `PENDING` bookings
- Redis caching for high-traffic seat availability reads
- Email/SMS booking confirmations
- `JOIN FETCH` optimizations to remove the small-scale N+1 pattern in show listing
