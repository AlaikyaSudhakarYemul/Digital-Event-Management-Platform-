# Digital-Event-Management-Platform-
A tool for providing a platform for a user to create and manage any type of event(In-Person, Virtual, Hybrid).

## Microservice Setup (ft-microservice)

### Backend Services
- `eureka-server` (port `8761`)
- `api-gateway` (port `8080`)
- `ADMIN` (port `8081`)
- `EVENT` (port `8082`)
- `DEMP` identity service (port `8083`)
- `PAYMENT` (port `8084`)
- `AI-ASSIST` (port `8090`, Python FastAPI)

### Database Schemas
- `identity_db` for DEMP
- `admin_db` for ADMIN
- `event_db` for EVENT
- `payment_db` for PAYMENT

### Optional Environment Variables
- `DEMP_DB_URL`, `DEMP_DB_USERNAME`, `DEMP_DB_PASSWORD`
- `ADMIN_DB_URL`, `ADMIN_DB_USERNAME`, `ADMIN_DB_PASSWORD`
- `EVENT_DB_URL`, `EVENT_DB_USERNAME`, `EVENT_DB_PASSWORD`
- `PAYMENT_DB_URL`, `PAYMENT_DB_USERNAME`, `PAYMENT_DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION`
- `MAIL_USERNAME`, `MAIL_PASSWORD`
- `REACT_APP_API_BASE_URL` (frontend base URL, default `http://localhost:8080`)

### Startup Order
1. Start Eureka server.
2. Start ADMIN, EVENT, DEMP, PAYMENT.
3. Start AI-ASSIST service.
4. Start API Gateway.
5. Start frontend app.

Frontend should call the gateway only.
