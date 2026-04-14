# Microservice Migration Plan

## Current State Summary

The backend is partly split already:
- DEMP: authentication, users, and proxy-style calls to ADMIN
- EVENT: events and registrations
- ADMIN: speakers and addresses
- PAYMENT: scaffold only
- eureka-server: service discovery

Key blocker: all services still use the same database schema (`event_management_db`) and duplicate domain models.

## Target Architecture

Use bounded-context ownership:
- Identity Service (from DEMP): users, login, JWT issuing
- Event Service (EVENT): events, event lifecycle, event capacity
- Registration Service (split from EVENT): registrations, status, user-event enrollment
- Admin Catalog Service (ADMIN): speakers, addresses
- Payment Service (PAYMENT): payment and refunds for paid events
- API Gateway (new): single frontend entrypoint, auth token relay, route mapping
- Discovery (existing): Eureka

Each service should own a separate database/schema.

## Biggest Issues To Resolve First

1. Shared database across services
2. Duplicated entities across service codebases
3. DEMP acting as both Identity service and API aggregator/proxy
4. Duplicated JWT validation logic in each service
5. Hard-coded sensitive values in properties files

## Phase Plan

### Phase 1: Stabilize Contracts (No Domain Split Yet)
- Define external DTO contracts for cross-service responses
- Replace direct entity-shaped dependencies with DTOs
- Keep current APIs stable for frontend

### Phase 2: Separate Data Ownership
- Create separate schemas:
  - `identity_db`
  - `event_db`
  - `admin_db`
  - `payment_db`
- Move each service to its own datasource config
- Remove cross-service JPA entity dependencies entirely

### Phase 3: Introduce API Gateway
- Add Spring Cloud Gateway service
- Route rules:
  - `/api/auth/**` -> DEMP (Identity)
  - `/api/events/**` -> EVENT
  - `/api/registrations/**` -> Registration service (or EVENT during transition)
  - `/api/admin/**` and `/api/speakers/**` -> ADMIN
  - `/api/payments/**` -> PAYMENT
- Frontend calls only gateway URL

### Phase 4: Split Registration Out Of EVENT
- New Registration service with its own repository and database
- Event service exposes capacity APIs needed by registration flow
- Use resilient communication with retries/timeouts/circuit breaker

### Phase 5: Security Hardening
- Centralize JWT creation in Identity service only
- Other services validate JWT with shared secret/public key from secure config
- Move secrets to environment variables or config server

### Phase 6: Async Workflows
- Publish registration-created event
- Consume for:
  - email notification
  - calendar invite
  - analytics/audit

## Concrete First Refactor In This Repo

1. Keep DEMP as Identity only:
- Remove proxy endpoints that forward admin/speaker operations
- Keep only auth/user endpoints

2. EVENT service:
- Replace local `Users` class dependency with user-summary DTO from Identity service
- Keep calls read-only for user profile lookup

3. ADMIN service:
- Expose only admin-owned resources and enforce role checks

4. PAYMENT service:
- Build initial CRUD and payment status flow

## Suggested Execution Order For Your Team

1. Create API Gateway
2. Move frontend base URL to gateway
3. Split DEMP proxy logic out
4. Split databases
5. Extract registration service
6. Implement payment service
7. Add async events

## Definition Of Done

- No service reads or writes another service database tables
- No service imports another service JPA entities
- Frontend uses one gateway endpoint
- Service-to-service calls use DTO contracts and are resilient
- Secrets not hard-coded in repository
