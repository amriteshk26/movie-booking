# Movie Ticket Booking System

A scalable, seat-level movie ticket booking backend built with **Spring Boot 3** and a
layered architecture. It supports multiple cities → theaters → screens → shows, time-bound
seat holds that auto-expire, tiered pricing, discount codes, mock payment, configurable
refund policies, asynchronous notifications, and role-based access control.

The headline challenge — *multiple users booking the same seat at the same time* — is solved
with **optimistic locking** on the per-show seat record, so bookings are serialized correctly
with no double-allocation.

---

## Tech Stack

| Concern        | Choice                                  |
|----------------|-----------------------------------------|
| Language       | Java 17                                 |
| Framework      | Spring Boot 3.5 (Web, Data JPA, Security, Validation) |
| Persistence    | H2 (in-memory)                          |
| Auth           | Spring Security, HTTP Basic, BCrypt     |
| Async/Schedule | Spring `@Async` + `@Scheduled`          |
| Boilerplate    | Lombok                                  |
| Testing        | JUnit 5, Mockito, Spring MockMvc, AssertJ |

No Redis, Kafka, microservices, or external payment providers are used (per the constraints).

---

## How to Run

```bash
# Start the application (http://localhost:8080)
./mvnw spring-boot:run

# Run the full test suite
./mvnw test
```

On first start an in-memory database is created and **seeded with demo data**. The H2 console
is available at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:moviebooking`, user
`sa`, empty password).

### Seeded accounts (HTTP Basic)

| Username   | Password      | Role     |
|------------|---------------|----------|
| `admin`    | `admin123`    | ADMIN    |
| `customer` | `customer123` | CUSTOMER |
| `alice`    | `alice123`    | CUSTOMER |

Seeded discount codes: `WELCOME10` (10% off), `FLAT50` (flat 50 off, max 3 uses).

---

## Architecture

Standard layered architecture with constructor injection throughout:

```
controller  ->  service  ->  repository  ->  entity (JPA)
                   |
                   +-- dto (request/response, validated)
                   +-- mapper (entity <-> dto)
                   +-- exception (custom + @RestControllerAdvice)
                   +-- security (UserDetails, RBAC)
                   +-- config (security, async, scheduling, seed)
```

### Core domain model

- **User / Role** — accounts with `ADMIN` and/or `CUSTOMER` roles.
- **City → Theater → Screen → Seat** — the physical location hierarchy. `Seat` is the
  physical seat in a screen layout (independent of any show).
- **Movie**, **Show** — a show is a movie scheduled on a screen at a time, with a pricing tier
  and (optionally) a refund policy.
- **ShowSeat** — *the bookable unit*: one row per seat per show, carrying status
  (`AVAILABLE`/`HELD`/`BOOKED`), price, and an `@Version` for optimistic locking.
- **SeatHold** — a time-bound reservation of show seats for a user; expires automatically.
- **Booking / Payment** — a confirmed set of seats and its (mock) payment.
- **PricingTier**, **DiscountCode**, **RefundPolicy** — admin-configurable pricing & policy.
- **Notification** — persisted record of an asynchronously delivered message.

---

## Booking Lifecycle

```
browse shows  ->  view seat map  ->  HOLD seats  ->  CONFIRM (pay)  ->  booking CONFIRMED
                                        |                                      |
                                   auto-expires                            CANCEL -> refund
                                   (scheduler)                           (per refund policy)
```

1. **Hold** — customer selects seats; they move `AVAILABLE → HELD` with a TTL
   (default 300s, configurable). A scheduled sweeper releases expired holds.
2. **Confirm** — within the TTL, the customer confirms. Pricing is summed, an optional
   discount applied, a mock payment charged, seats move `HELD → BOOKED`, and a confirmation
   notification is sent asynchronously.
3. **Cancel** — refund is computed from the show's (or default) refund policy based on how
   far ahead of showtime the cancellation is; seats return to `AVAILABLE`.

---

## Concurrency & Double-Booking Prevention

`ShowSeat` carries a JPA `@Version` column. When two users try to hold the same seat:

1. Both transactions read the seat as `AVAILABLE`.
2. Both attempt `AVAILABLE → HELD`. The hold service calls `saveAllAndFlush`, forcing the
   write within the transaction.
3. The first commit wins; the second fails the version check
   (`OptimisticLockingFailureException`), which the service translates into a **409 Conflict**.

This guarantees a seat is allocated to exactly one booking. A unique constraint on
`(show_id, seat_id)` and a defensive 409 handler in the global exception advice provide
backstops. This behaviour is proven by `ConcurrentSeatHoldIntegrationTest` (two threads race;
exactly one wins).

---

## Roles & Access Control

- **Admin** (`ROLE_ADMIN`) — manage cities, theaters, screens, seat layouts, movies, shows,
  pricing tiers, discount codes, refund policies. All under `/api/admin/**`.
- **Customer** (`ROLE_CUSTOMER`) — browse, hold, book, cancel, view history & notifications.

Enforced at two levels: a URL rule (`/api/admin/**` requires `ROLE_ADMIN`) and method-level
`@PreAuthorize` on controllers. Unauthenticated requests get **401**; authenticated-but-
unauthorized get **403**.

---

## API Reference

All non-public endpoints use HTTP Basic auth. Base URL `http://localhost:8080`.

### Auth
| Method | Path                  | Access   | Description                |
|--------|-----------------------|----------|----------------------------|
| POST   | `/api/auth/register`  | Public   | Register a customer        |
| GET    | `/api/auth/me`        | Any auth | Current user               |

### Browse (any authenticated user)
| Method | Path                                  | Description        |
|--------|---------------------------------------|--------------------|
| GET    | `/api/cities`                         | List cities        |
| GET    | `/api/cities/{id}/theaters`           | Theaters in city   |
| GET    | `/api/theaters/{id}/screens`          | Screens in theater |
| GET    | `/api/screens/{id}/seats`             | Physical layout    |
| GET    | `/api/movies`                         | List movies        |
| GET    | `/api/shows`                          | List shows         |
| GET    | `/api/shows/{id}`                     | Show details       |
| GET    | `/api/shows/{id}/seats`               | Show seat map      |

### Customer
| Method | Path                            | Description                    |
|--------|---------------------------------|--------------------------------|
| POST   | `/api/holds`                    | Hold seats                     |
| DELETE | `/api/holds/{id}`               | Release a hold                 |
| POST   | `/api/bookings/confirm`         | Confirm hold → booking (+pay)  |
| GET    | `/api/bookings`                 | Booking history                |
| GET    | `/api/bookings/{id}`            | Booking details                |
| POST   | `/api/bookings/{id}/cancel`     | Cancel → refund                |
| GET    | `/api/notifications`            | My notifications               |

### Admin (`ROLE_ADMIN`)
| Method | Path                                          | Description            |
|--------|-----------------------------------------------|------------------------|
| POST/PUT/DELETE | `/api/admin/cities[/{id}]`           | Manage cities          |
| POST/DELETE     | `/api/admin/theaters[/{id}]`         | Manage theaters        |
| POST            | `/api/admin/screens`                 | Create screen          |
| POST            | `/api/admin/seats/layout`            | Bulk-create seat layout|
| POST            | `/api/admin/movies`                  | Create movie           |
| POST            | `/api/admin/shows`                   | Create show (+seats)   |
| POST/PUT/GET    | `/api/admin/pricing-tiers[/{id}]`    | Manage pricing tiers   |
| POST/GET/PATCH  | `/api/admin/discount-codes[/{id}/active]` | Manage discounts  |
| POST/PUT/GET    | `/api/admin/refund-policies[/{id}]`  | Manage refund policies |

### Example flow (curl)

```bash
# 1. Browse a show's seat map (as customer)
curl -u customer:customer123 http://localhost:8080/api/shows/1/seats

# 2. Hold two seats (use show-seat ids from the seat map)
curl -u customer:customer123 -X POST http://localhost:8080/api/holds \
  -H 'Content-Type: application/json' \
  -d '{"showId":1,"showSeatIds":[1,2]}'

# 3. Confirm the booking with a discount code
curl -u customer:customer123 -X POST http://localhost:8080/api/bookings/confirm \
  -H 'Content-Type: application/json' \
  -d '{"holdId":1,"discountCode":"WELCOME10"}'

# 4. View booking history
curl -u customer:customer123 http://localhost:8080/api/bookings

# 5. Cancel (refund per policy)
curl -u customer:customer123 -X POST http://localhost:8080/api/bookings/1/cancel
```

### Error format

All errors return a consistent envelope:

```json
{
  "timestamp": "2026-06-23T14:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "One or more selected seats were just taken by another user",
  "path": "/api/holds",
  "fieldErrors": null
}
```

`fieldErrors` is populated (field → message) for validation failures (400).

---

## Pricing & Discounts

- Each `Show` has a **pricing tier** (`REGULAR` / `PREMIUM` / `WEEKEND`) with a base price.
- **Premium seats** are scaled by the tier's premium multiplier (e.g. 200 × 1.5 = 300).
- Per-seat prices are computed and frozen onto each `ShowSeat` when the show is created.
- **Discount codes** are `PERCENT` or `FLAT`, validated for active status, validity window,
  and usage limit; the discount is capped at the subtotal. Usage count is incremented under
  optimistic locking to serialize concurrent redemptions.

## Refund Policies

A `RefundPolicy` is tiered by hours-before-showtime:

- cancel ≥ `fullRefundHoursBefore` → `fullRefundPercent`
- cancel ≥ `partialRefundHoursBefore` → `partialRefundPercent`
- otherwise → `noRefundPercent`

A show may reference a specific policy; otherwise the **default** policy applies. If no policy
exists at all, the system defaults to no refund (safe default).

## Notifications

Confirmation, cancellation, and show reminders are delivered **asynchronously** (`@Async` on a
dedicated thread pool) so they never block the booking flow. A `@Scheduled` job sends one-time
reminders for shows starting soon. Delivery is simulated by persisting a `Notification` and
logging it (no real email/SMS, per scope).

---

## Configuration

`src/main/resources/application.properties`:

| Key                              | Default | Meaning                                  |
|----------------------------------|---------|------------------------------------------|
| `booking.hold.ttl-seconds`       | 300     | Seat hold lifetime before auto-release   |
| `booking.hold.sweep-interval-ms` | 30000   | How often the expiry/reminder jobs run   |

---

## Testing

27 tests across unit and integration layers:

- **Unit** (Mockito): pricing computation, discount validation/application, refund-policy tiers.
- **Integration** (`@SpringBootTest`): seed-data correctness & idempotency; the full booking
  lifecycle and RBAC via `MockMvc`; and a **concurrent double-booking** test proving exactly
  one of two racing holds succeeds.

Each integration test runs against an isolated in-memory database (a unique `${random.uuid}`
H2 instance per context) so tests never interfere with one another.

```bash
./mvnw test
```

---

## Assumptions & Design Decisions

- **Optimistic (not pessimistic) locking** for seat allocation — fits the constraint (no
  Redis), scales for read-heavy browsing, and the conflict path is cheap and explicit (409).
- **Holds are first-class** (`SeatHold` entity) rather than implicit, enabling clean expiry
  sweeping and conversion to bookings.
- **Prices are snapshotted** onto `ShowSeat` at show creation so later tier edits don't
  retroactively change existing show prices.
- **Mock payment** always succeeds and produces a synthetic transaction reference; the
  `Payment` entity and refund path are modelled so a real provider could be slotted in.
- **HTTP Basic + BCrypt** for auth — simple and testable; advanced auth (OAuth/SSO/MFA) is out
  of scope.
- **Registration creates customers only**; admin accounts are provisioned via seed data.
- **In-memory H2** with `create-drop` and seed data for a zero-setup, demoable system; the JPA
  mapping is database-agnostic and could point at a persistent DB by changing configuration.
- **`open-in-view=false`** to keep transaction boundaries explicit and avoid lazy-loading
  surprises outside services.
- One notable mapping detail: the discount `value` column is mapped to `discount_value`
  because `VALUE` is a reserved word in H2.

### Possible extensions (out of scope)
- Pessimistic locking or a queue for extreme contention on a single hot show.
- Admin-initiated cancellations / overrides; partial seat cancellation.
- Real notification channels; pagination & filtering on browse endpoints.
- Persistent database and Flyway/Liquibase migrations.
```
