"""
Knowledge base for the Digital Event Management Platform (DEMP) chatbot.

Each entry has:
  - intent: short identifier
  - keywords: phrases that trigger the response
  - answer: markdown-friendly answer text

The matcher in `app.py` scores entries by keyword overlap and returns the
best match. Add new entries here to expand the bot's coverage.
"""

PROJECT_OVERVIEW = (
    "The **Digital Event Management Platform (DEMP)** is a full-stack "
    "application for creating, discovering, booking and managing in-person, "
    "virtual and hybrid events. It uses a Spring Boot microservices backend "
    "(ADMIN, EVENT, TICKETS, PAYMENT, api-gateway, eureka-server) and a "
    "React 19 + Tailwind frontend."
)

KB = [
    {
        "intent": "greeting",
        "keywords": ["hi", "hello", "hey", "hola", "good morning", "good evening"],
        "answer": (
            "Hi! I'm the DEMP assistant. I can help you with creating events, "
            "booking tickets, payments, accounts, and using the platform. "
            "Try asking: *How do I create an event?*"
        ),
    },
    {
        "intent": "about",
        "keywords": ["what is demp", "about", "project", "platform", "overview", "what does this do"],
        "answer": PROJECT_OVERVIEW,
    },
    {
        "intent": "features",
        "keywords": ["features", "capabilities", "what can", "modules"],
        "answer": (
            "**Key features**:\n"
            "- Browse & search upcoming events\n"
            "- Create events (organizers)\n"
            "- Book and pay for tickets\n"
            "- Organizer dashboard with analytics\n"
            "- Admin dashboard for user/event moderation\n"
            "- Role-based auth (User, Organizer, Admin)\n"
            "- In-person, virtual & hybrid event support"
        ),
    },
    {
        "intent": "create_event",
        "keywords": [
            "create event", "create an event", "new event", "host event",
            "add event", "organize event", "organise event", "make event",
            "host", "organize", "organise", "publish event", "list event",
            "start event", "setup event",
        ],
        "answer": (
            "**To create an event:**\n"
            "1. Sign in (or sign up as an Organizer).\n"
            "2. Click **Create Event** in the navbar (or the *Create Event* "
            "button on the home page).\n"
            "3. You'll be taken to `/CreateEvent` where you fill in:\n"
            "   - Title, description, category\n"
            "   - Date, time, venue (or virtual link)\n"
            "   - Ticket types & pricing\n"
            "   - Cover image\n"
            "4. Submit. The event appears in *Upcoming Events* once approved."
        ),
    },
    {
        "intent": "book_ticket",
        "keywords": [
            "book ticket", "book tickets", "buy ticket", "buy tickets",
            "register event", "register for", "purchase ticket",
            "purchase tickets", "get ticket", "get tickets",
            "how to book", "booking", "reserve", "reservation",
            "ticket booking", "book",
        ],
        "answer": (
            "**To book a ticket:**\n"
            "1. From the home page, browse *Upcoming Events* and click an event card.\n"
            "2. On the **Event Details** page, choose a ticket type and quantity.\n"
            "3. Click **Register / Buy Ticket** — you'll go to the payment page.\n"
            "4. Complete payment. Your ticket appears under *My Tickets*."
        ),
    },
    {
        "intent": "payment",
        "keywords": [
            "payment", "payments", "pay", "paying", "paid", "refund",
            "refunds", "transaction", "transactions", "billing", "money",
            "card", "credit card", "debit card", "upi", "checkout",
            "price", "cost", "charge", "invoice", "receipt",
        ],
        "answer": (
            "**Payments** are handled by the PAYMENT microservice. Supported "
            "flows: card payment during ticket booking, payment status tracking, "
            "and refunds (initiated from *My Tickets* if the event allows it). "
            "Receipts are emailed and visible in your account."
        ),
    },
    {
        "intent": "tickets_view",
        "keywords": [
            "my tickets", "view ticket", "view tickets", "ticket details",
            "see my booking", "see bookings", "my bookings",
            "where are my tickets", "download ticket", "qr code",
            "ticket qr", "my ticket",
        ],
        "answer": (
            "Open the user menu in the navbar and select **My Tickets** to see "
            "all your bookings, QR codes for entry, and download receipts."
        ),
    },
    {
        "intent": "organizer_dashboard",
        "keywords": ["organizer dashboard", "manage events", "event analytics", "my events", "organizer"],
        "answer": (
            "The **Organizer Dashboard** (`/organizer`) lets you:\n"
            "- View all events you've created\n"
            "- See ticket sales and revenue\n"
            "- Edit / cancel events\n"
            "- Export attendee lists"
        ),
    },
    {
        "intent": "admin",
        "keywords": ["admin", "admin dashboard", "moderate", "approve event", "manage users"],
        "answer": (
            "The **Admin Dashboard** is restricted to Admin role users. "
            "It allows: approving/rejecting events, managing users and "
            "organizers, viewing platform-wide stats, and resolving disputes."
        ),
    },
    {
        "intent": "signup",
        "keywords": [
            "sign up", "signup", "register", "create account",
            "new account", "make account", "join", "registration",
        ],
        "answer": (
            "Click **Sign Up** in the top-right of the home page. Choose a role "
            "(User or Organizer), fill in your details, and submit. You'll be "
            "logged in automatically."
        ),
    },
    {
        "intent": "login",
        "keywords": [
            "login", "log in", "sign in", "signin", "logon",
            "authenticate", "access account",
        ],
        "answer": (
            "Click **Sign Up / Login** in the navbar and switch to the *Login* "
            "tab. Use your registered email and password."
        ),
    },
    {
        "intent": "forgot_password",
        "keywords": ["forgot password", "reset password", "can't login", "cannot login"],
        "answer": (
            "On the login popup, click **Forgot Password?** to receive a reset "
            "link via email. If the email never arrives, contact an admin."
        ),
    },
    {
        "intent": "tech_stack",
        "keywords": ["tech stack", "technology", "built with", "stack", "frameworks"],
        "answer": (
            "**Frontend:** React 19, React Router 7, Tailwind CSS, Formik, Yup, Swiper.\n"
            "**Backend:** Spring Boot microservices — `eureka-server` (discovery), "
            "`api-gateway`, `ADMIN`, `EVENT`, `TICKETS`, `PAYMENT`, `DEMP` (core).\n"
            "**Chatbot:** Python + FastAPI."
        ),
    },
    {
        "intent": "architecture",
        "keywords": ["architecture", "microservices", "services", "backend services"],
        "answer": (
            "DEMP follows a **microservices** architecture:\n"
            "- `eureka-server` — service registry\n"
            "- `api-gateway` — single entry point at `http://localhost:8080`\n"
            "- `ADMIN` — admin operations\n"
            "- `EVENT` — event CRUD\n"
            "- `TICKETS` — ticket lifecycle\n"
            "- `PAYMENT` — payments & refunds\n"
            "- `DEMP` — core / user service"
        ),
    },
    {
        "intent": "run_project",
        "keywords": ["run project", "start project", "how to run", "setup", "install"],
        "answer": (
            "**Run locally:**\n"
            "1. Start backend services with `./start-all-services.ps1` (Eureka first, then gateway, then others).\n"
            "2. `cd frontend/demp-app && npm install && npm start` (opens http://localhost:3000).\n"
            "3. `cd chatbot && pip install -r requirements.txt && uvicorn app:app --port 8001` for the chatbot."
        ),
    },
    {
        "intent": "contact",
        "keywords": [
            "contact", "support", "help", "email", "reach out",
            "customer care", "helpdesk",
        ],
        "answer": (
            "For support, use the **Contact** section in the footer or reach out "
            "to your admin. For technical issues, check the project README."
        ),
    },
    {
        "intent": "roles",
        "keywords": [
            "roles", "user role", "user roles", "types of users", "permissions",
            "what can user do", "what can organizer do", "what can admin do",
            "rbac", "role based",
        ],
        "answer": (
            "DEMP uses **role-based access control** with three roles:\n"
            "- **USER** — browse/search events, book tickets, manage their bookings, pay & refund.\n"
            "- **ORGANIZER** — everything a user can do, plus create / edit / cancel events and view sales analytics on the Organizer Dashboard.\n"
            "- **ADMIN** — moderates the whole platform: approves/rejects events, manages users & organizers, sees platform-wide stats."
        ),
    },
    {
        "intent": "event_types",
        "keywords": [
            "event types", "types of events", "in person", "in-person",
            "virtual event", "online event", "hybrid event", "hybrid",
            "what event types", "categories", "category",
        ],
        "answer": (
            "Events come in three types (`EventType` enum):\n"
            "- **IN_PERSON** — held at a physical address.\n"
            "- **VIRTUAL** — fully online (a stream / meeting link is shared with attendees).\n"
            "- **HYBRID** — both physical venue and a virtual link, so people can attend either way."
        ),
    },
    {
        "intent": "ports",
        "keywords": [
            "port", "ports", "which port", "what port", "running on",
            "localhost", "url", "service url",
        ],
        "answer": (
            "Default ports used by DEMP:\n"
            "- **Frontend (React)** — http://localhost:3000\n"
            "- **API Gateway** — http://localhost:8080 (single entry point for all backend APIs)\n"
            "- **Eureka Server** — http://localhost:8761 (service discovery)\n"
            "- **Chatbot (FastAPI)** — http://localhost:8001\n"
            "- **MySQL** — localhost:3306 (database `event_management_db`)"
        ),
    },
    {
        "intent": "database",
        "keywords": [
            "database", "db", "mysql", "schema", "tables", "data store",
            "where is data stored", "storage",
        ],
        "answer": (
            "DEMP stores its data in **MySQL** (database `event_management_db` on port 3306). "
            "JPA/Hibernate manages schema with `ddl-auto=update`, so tables (events, users, "
            "tickets, registrations, addresses, speakers, payments, etc.) are created/migrated "
            "automatically on first run. Default dev credentials: `root / root123`."
        ),
    },
    {
        "intent": "auth_jwt",
        "keywords": [
            "jwt", "token", "auth token", "authentication", "authorization",
            "bearer", "how auth works", "security",
        ],
        "answer": (
            "Authentication uses **JWT (JSON Web Tokens)**. After login, the backend returns "
            "a signed token; the frontend stores it in `localStorage` as `auth_token` and sends "
            "it in the `Authorization: Bearer <token>` header on every protected API call. "
            "Endpoints under `/api/auth/login` and `/api/auth/register` are public; everything "
            "else requires a valid token, with role-based checks on admin/organizer routes."
        ),
    },
    {
        "intent": "search_events",
        "keywords": [
            "search event", "search events", "find event", "filter event",
            "filter events", "browse events", "look for event",
        ],
        "answer": (
            "On the home page's **Upcoming Events** section there's a search box that filters "
            "by event name. The list is paginated (3 per page) and calls "
            "`GET /api/events/paginated?page=&size=&eventName=` on the backend. "
            "Click any card to open *Event Details* and book a ticket."
        ),
    },
    {
        "intent": "edit_event",
        "keywords": [
            "edit event", "update event", "modify event", "change event",
            "cancel event", "delete event", "remove event",
        ],
        "answer": (
            "Organizers can edit or cancel their own events from the **Organizer Dashboard** "
            "(`/organizer`). Click an event row → *Edit* to update details, or *Delete* to "
            "remove it (soft-delete — `isDeleted=true`). Admins can also remove any event "
            "from the Admin Dashboard."
        ),
    },
    {
        "intent": "speakers",
        "keywords": [
            "speaker", "speakers", "add speaker", "guest", "presenter",
        ],
        "answer": (
            "Each event can have multiple **speakers** (many-to-many `event_speaker` table). "
            "When creating or editing an event, pick existing speakers or add new ones. "
            "Speakers show on the *Event Details* page so attendees know who's presenting."
        ),
    },
    {
        "intent": "capacity",
        "keywords": [
            "capacity", "max attendees", "seats", "seat", "sold out",
            "attendees", "how many seats",
        ],
        "answer": (
            "Every event has `maxAttendees` (10–500) and `currentAttendees`. When "
            "`currentAttendees == maxAttendees`, the event is **sold out** and the "
            "Register/Buy button is disabled. Seats remaining = `maxAttendees − currentAttendees`."
        ),
    },
    {
        "intent": "registrations",
        "keywords": [
            "registration", "registrations", "registered", "attendee list",
            "who registered", "rsvp",
        ],
        "answer": (
            "Bookings are stored as **Registrations** (`/api/registrations`). When a user buys "
            "a ticket, a Registration row links the user and the event. Organizers see the "
            "full attendee list per event from the Organizer Dashboard, and users see their "
            "own list under *My Tickets*."
        ),
    },
    {
        "intent": "profile",
        "keywords": [
            "profile", "my profile", "edit profile", "update profile",
            "change password", "account settings",
        ],
        "answer": (
            "Open the user menu (top-right) and click **Profile** to view or update your name, "
            "email, phone and password. Profile changes call `PUT /api/user`."
        ),
    },
    {
        "intent": "logout",
        "keywords": ["logout", "log out", "sign out", "signout"],
        "answer": (
            "Click your avatar in the navbar and choose **Logout**. This clears `auth_token` "
            "from localStorage and returns you to the home page."
        ),
    },
    {
        "intent": "chatbot_info",
        "keywords": [
            "chatbot", "eventmate", "who are you", "what are you",
            "bot", "assistant", "you",
        ],
        "answer": (
            "I'm **EventMate**, the in-app assistant for DEMP. I run as a FastAPI service on "
            "port 8001 and combine a project knowledge base with a local LLM (Ollama) for "
            "general questions. I can also fetch live event data from the backend — try "
            "*\"show present events\"*."
        ),
    },
    {
        "intent": "ollama_setup",
        "keywords": [
            "ollama", "llm", "language model", "enable llm", "install ollama",
            "model", "llama", "phi3", "qwen",
        ],
        "answer": (
            "For full LLM answers, install **Ollama** (https://ollama.com) and pull a model:\n"
            "```\nollama pull llama3.2\n```\n"
            "The chatbot auto-detects it at `http://localhost:11434`. Override with env vars "
            "`OLLAMA_URL` and `OLLAMA_MODEL`. Without Ollama I still answer DEMP questions "
            "from the local knowledge base."
        ),
    },
    {
        "intent": "frontend_routes",
        "keywords": [
            "routes", "pages", "url", "navigation", "frontend pages",
            "what pages", "react routes",
        ],
        "answer": (
            "Main frontend routes:\n"
            "- `/` — Home (upcoming events, hero, search)\n"
            "- `/CreateEvent` — Organizer creates a new event\n"
            "- `/event/:id` or *Event Details* — view & book\n"
            "- `/payment` — checkout flow\n"
            "- `/my-tickets` — user's bookings & QR codes\n"
            "- `/organizer` — organizer dashboard\n"
            "- `/admin` — admin dashboard"
        ),
    },
    {
        "intent": "backend_endpoints",
        "keywords": [
            "api", "endpoints", "rest api", "backend api", "what apis",
            "available endpoints", "routes backend",
        ],
        "answer": (
            "Key backend endpoints (via api-gateway on `:8080`):\n"
            "- `POST /api/auth/register` / `POST /api/auth/login`\n"
            "- `GET /api/events/all` · `GET /api/events/paginated` · `GET /api/events/{id}`\n"
            "- `POST /api/events/create` · `DELETE /api/events/{id}` (organizer)\n"
            "- `POST /api/registrations` (book a ticket)\n"
            "- `GET /api/tickets/...` · `POST /api/payments/...`\n"
            "- `GET /api/admin/...` (admin only)"
        ),
    },
    {
        "intent": "image_upload",
        "keywords": [
            "image", "cover image", "upload image", "photo", "picture",
            "event image", "banner",
        ],
        "answer": (
            "Each event stores a cover image as a base64 string in the `image` column "
            "(`@Lob`). When creating an event, pick a file in the *Create Event* form and "
            "the frontend converts it to base64 before posting. The image appears on the "
            "event card and on the Event Details page."
        ),
    },
    {
        "intent": "logs",
        "keywords": [
            "log", "logs", "logging", "where are logs", "debug",
            "see logs", "log file",
        ],
        "answer": (
            "Backend services log to console (level controlled by `logging.level.*` in "
            "`application.properties`) and the DEMP service also writes to `backend/DEMP/logs/`. "
            "The chatbot logs are visible in the uvicorn terminal."
        ),
    },
    {
        "intent": "thanks",
        "keywords": ["thanks", "thank you", "thx", "ty"],
        "answer": "You're welcome! Happy event managing 🎉",
    },
    {
        "intent": "bye",
        "keywords": ["bye", "goodbye", "see you", "exit"],
        "answer": "Goodbye! Come back anytime you need help with DEMP.",
    },
]

FALLBACK = (
    "I'm not sure about that one. Try asking about:\n"
    "- *Creating an event*\n"
    "- *Booking a ticket*\n"
    "- *Payments / refunds*\n"
    "- *Organizer or Admin dashboard*\n"
    "- *Signing up or logging in*\n"
    "- *The tech stack or architecture*"
)
