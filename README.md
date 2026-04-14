# Relay — Microblogging Platform

A full-stack microblogging application inspired by Twitter/X. Users can post short messages, follow each other, like and comment on posts, and manage their profile.

**Live demo:** _coming soon_

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, React Router, Vite |
| Backend | Java 21, Spring Boot 3, Spring Data JPA |
| Database | MariaDB (production), H2 (tests) |
| Auth | Session-based (HTTP-only cookies) with BCrypt password hashing |
| API Docs | OpenAPI / Swagger |
| Containerisation | Docker, Docker Compose |

---

## Features

- Register, log in, and log out
- Create posts (max 280 characters)
- Like and unlike posts (cannot like your own)
- Comment on posts, delete your own comments
- Follow and unfollow users
- Aggregated timeline of followed users' posts
- User search
- Edit your own profile (name, email, bio)
- View other users' public profiles and follower / following counts
- Dark / Light mode toggle
- Responsive design (mobile and desktop)
- Accessible: skip-to-content link, ARIA attributes, keyboard navigation, speech-to-text input (Chrome / Edge)

---

## Running Locally

### Prerequisites

- Node.js 18+
- Java 21
- Docker & Docker Compose (easiest option)

### Option A — Docker (recommended)

```bash
docker-compose up --build
```

- Frontend: http://localhost
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Option B — Manual

**Backend**

```bash
cd relay-backend
# Copy .env.example to .env and fill in the values, then:
export DB_URL=jdbc:mariadb://localhost:3306/relay?createDatabaseIfNotExist=true
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
./mvnw spring-boot:run
```

**Frontend**

```bash
cd relay-frontend
npm install
npm run dev
```

App runs at http://localhost:5173 — the Vite dev server proxies `/api` to the backend automatically.

---

## Environment Variables

Copy `.env.example` to `.env` and fill in the values before running.

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | MariaDB JDBC connection string | `jdbc:mariadb://localhost:3306/relay` |
| `DB_USERNAME` | Database user | `root` |
| `DB_PASSWORD` | Database password | _(none)_ |
| `COOKIE_SECURE` | Set `true` when running behind HTTPS | `false` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `http://localhost` |
| `VITE_API_URL` | Backend API base URL used by the frontend build | `/api` |

---

## Testing

```bash
cd relay-backend
./mvnw test
```

73 tests covering controllers, services, and authentication flows.

---

## Security

- Passwords hashed with **BCrypt** (work factor 12, unique salt per hash)
- Session cookies are `HttpOnly` — not readable by JavaScript
- `Secure` flag enabled in production via the `COOKIE_SECURE` env var
- No secrets committed to version control — all sensitive config via environment variables

---

## License

Copyright © 2025 Malek Alsibai
