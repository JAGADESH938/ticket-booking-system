# Architecture

## Layered Request Flow

```
Client (Postman / Swagger UI)
   |
   v
JwtAuthenticationFilter  -- reads Authorization header, populates SecurityContext
   |
   v
Spring Security  -- authorizeHttpRequests role checks
   |
   v
Controller  -- @Valid DTO binding, delegates to Service
   |
   v
Service  -- business logic, @Transactional boundaries, locking
   |
   v
Repository (Spring Data JPA)  -- generates SQL, manages entities
   |
   v
MySQL
```

## Booking Concurrency Flow (the core mechanism)

```
POST /api/bookings
   |
   v
Sort requested seat IDs (deadlock prevention)
   |
   v
SELECT ... FOR UPDATE on those seat rows  -- blocks concurrent requests for the same seats
   |
   v
Re-check availability (safe: protected by the lock above)
   |
   +-- seat taken -> 409 CONFLICT
   |
   v
Create Booking (PENDING) -> create BookingSeat rows
   |
   v
Simulated payment
   |
   +-- fails -> throw -> @Transactional rolls back everything -> seat released
   |
   v
Booking CONFIRMED -> COMMIT -> locks released
```

## Package Structure

```
com.ticketbook
├── controller/    REST endpoints
├── service/       business logic + impl/
├── repository/    Spring Data JPA interfaces
├── entity/        JPA entities + enums
├── dto/           request/response contracts
├── mapper/        entity <-> DTO conversion
├── exception/     custom exceptions + GlobalExceptionHandler
├── security/      JWT filter, JwtService, UserDetailsServiceImpl
└── config/        SecurityConfig, OpenApiConfig
```
