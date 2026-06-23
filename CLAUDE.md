# Movie Ticket Booking System

## Tech Stack
- Spring Boot 3
- Spring Data JPA
- Spring Security
- H2 Database
- Lombok

## Architecture
- Layered architecture
    - Controller
    - Service
    - Repository
    - Entity

## Requirements
- Seat-level booking
- Seat hold with expiry
- Booking confirmation
- Cancellation and refunds
- Async notifications
- RBAC

## Constraints
- No Redis
- No Kafka
- No microservices
- No external payment providers

## Concurrency
- Use optimistic locking on ShowSeat
- Prevent double booking

## Coding Standards
- Constructor injection only
- DTOs for requests/responses
- Global exception handling
- Validation annotations
- Unit and integration tests