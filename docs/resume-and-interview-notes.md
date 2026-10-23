# Resume Bullets

- Designed and built **TicketBook**, an individual full-stack backend project simulating an
  online movie ticket booking system, using **Java 21, Spring Boot 3, Spring Data JPA/Hibernate,
  and MySQL**, with a normalized 9-entity relational schema across users, theatres, screens,
  seats, shows, and bookings.
- Implemented **JWT-based stateless authentication** with **Spring Security**, BCrypt password
  hashing, and role-based authorization (USER/ADMIN) securing 25+ REST endpoints, with
  server-side role enforcement to prevent privilege escalation.
- Solved a **concurrent seat-booking race condition** using database-level **pessimistic
  locking** (`SELECT ... FOR UPDATE`) combined with sorted lock ordering (deadlock prevention)
  and a unique-constraint backstop, guaranteeing exactly one booking succeeds when multiple
  users compete for the same seat; verified with a custom **multi-threaded JUnit 5 integration
  test** simulating 10 concurrent requests.
- Built a **transaction-safe booking and payment simulation flow** (`@Transactional`
  boundaries, automatic rollback on payment failure), consistent global exception handling
  across the API, and **Swagger/OpenAPI** documentation, following clean layered architecture
  (Controller–Service–Repository–DTO) and REST API design principles.

*Individual project — not built as part of a team or employer engagement.*

---

# Interview Explanation Outline

1. **Title**: TicketBook — Online Ticket Booking System
2. **Problem statement**: guarantee no two users are ever sold the same seat for the same
   show, even under simultaneous requests.
3. **Why this project**: demonstrates a genuinely hard backend problem (concurrency/
   transactions), not just CRUD.
4. **Objective**: full booking lifecycle — browse, select seats, book (concurrency-safe),
   pay (simulated), cancel — with production-inspired practices at fresher scope.
5. **Key features**: auth, admin CRUD, search/pagination, live seat availability,
   concurrency-safe booking, payment simulation, booking history/cancellation.
6. **Tech stack**: Java 21, Spring Boot 3.3, Spring Security, JWT, JPA/Hibernate, MySQL,
   Maven, JUnit 5, Mockito, Swagger, Docker.
7. **Architecture**: Controller -> Service -> Repository -> Entity, DTO/Mapper layer,
   centralized exception handling, single Spring Boot app (no microservices).
8. **Database design**: 9 entities; Seat is physical/screen-scoped, "booked" status is
   show-scoped via BookingSeat (which denormalizes show_id for the concurrency constraint).
9. **Authentication**: stateless JWT (HS256), BCrypt, custom OncePerRequestFilter.
10. **Authorization**: role-based (URL rules) + object-level (booking ownership checks).
11. **Booking flow**: select -> validate -> lock -> re-check -> price server-side -> create
    PENDING -> pay -> CONFIRMED or full rollback.
12. **Concurrency problem**: TOCTOU race condition; `synchronized` insufficient (single-JVM
    only, doesn't govern DB writes).
13. **Prevention mechanism**: pessimistic locking (`FOR UPDATE`), sorted lock acquisition
    (deadlock prevention), unique constraint backstop.
14. **Transaction management**: booking + payment in one `@Transactional` method; failure
    rolls back everything, releasing the seat.
15. **Payment simulation**: ~90% success rate, formatted transaction IDs, persisted `Payment`
    record only on success.
16. **Challenges faced**: understanding why `synchronized` fails at the DB level; deciding
    PENDING should block a seat; deadlock-safe multi-seat locking (sort by ID); rollback
    semantics inside one transactional method.
17. **Solutions implemented**: locking + sorted acquisition + unique constraint; aligned
    transaction boundaries; DTO/mapper layering; centralized exception handling.
18. **Testing**: JUnit 5 + Mockito unit tests, plus a genuine multi-threaded
    `CountDownLatch`-based integration test proving at most one booking succeeds under
    contention.
19. **Project impact**: portfolio depth on a hard backend problem, testable and explainable
    end to end.
20. **Future enhancements**: real payment gateway, PENDING-booking expiry job, Redis caching
    for seat availability, email/SMS confirmations, `JOIN FETCH` optimizations.
