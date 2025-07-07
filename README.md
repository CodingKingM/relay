# Microblogging Application – ICS WTP Relay

## Overview

This is a microblogging application developed for the Web Technology Project.  
It allows users to register, log in, create posts, follow/unfollow users, like/unlike posts, comment, edit their 
own profile and many more. 
The project was developed individually, with progress tracked using a self-made Notion template.

---

## Technologies Used

- **Frontend:** React (Vite), Custom CSS
- **Backend:** Spring Boot (Java 21), JPA, MariaDB, H2 (for tests)
- **API Documentation:** OpenAPI/Swagger
- **Testing:** JUnit (backend)
- **Containerization:** Docker, Docker Compose

---

## Features

### Core Features
- User registration, login, and logout
- Unique username enforcement
- User search with follow/unfollow functionality
- Create, view, and delete posts (max 280 characters)
- Like posts (cannot like own post or like multiple times)
- View own posts and timeline (most recent first)
- Aggregated timeline of followed users' posts

### Extra Features
- **Un-liking posts:** Users can remove their like from posts they previously liked.
- **Commenting on posts:** Users can add comments to posts.
- **Deleting one's own comments:** Users can delete their own comments from posts.
- **Deleting one's own posts:** Users can delete their own posts.
- **Managing one's own user profile:** Users can update their profile information (full name, email, biography).
- **Displaying the user profile of a different user:** Users can view the public profile of other users.
- **Dark/Light mode:** The application supports both dark and light themes for better accessibility and user preference.
- **Accessibility features:** Includes Skip to Main Content link, visible focus styles, ARIA attributes for interactive elements, ARIA roles and attributes, keyboard accessibility, and speech-to-text input for posts and comments, following best practices for software for the global market.
- **Responsive design:** Custom CSS for mobile and desktop.
- **Comprehensive error handling:** User-friendly error messages throughout the UI.
- **Test infrastructure:** JUnit test base class with session management and database cleaning.
- **API utilities:** Centralized API client for frontend communication.
- **Improved UX design:** Modern user experience and interface design, following best practices for global software.

---

## Project Management

Progress was tracked using a self-made Notion template, which helped organize weekly goals, Scrum-style updates, and keep a log of obstacles and solutions.  
This ensured consistent progress and clear documentation for each project milestone.

[View my Notion project management template here.](https://www.notion.so/Relay-Project-Tracker-1f044e974cbb80f19c28ff14e56dc354?source=copy_link)

---

## AI Usage Disclosure

AI tools (Claude) were used to assist in the development of the following files and features:

- `PostItem.jsx` – Post display and like/unlike logic
- `PostService.java` – Backend post business logic
- `PostControllerTest.java` – Backend test for post controller
- `RelayControllerTestBase.java` – Backend test infrastructure
- `UserControllerTest.java` – Backend test for user controller
- `FollowersFollowing.jsx` – Followers/following UI and logic
- `useAuth.js` – Authentication hook
- `MyProfilePage.jsx` – Profile page logic/UI
- `httpClient.js` – API communication utilities
- `App.css` – Custom CSS styling

**In each of these files, a comment at the top specifies the use of AI and the prompt or purpose.  
All AI-generated code was reviewed, tested, and adapted to fit the project requirements.**

---

## Setup Instructions

### Prerequisites

- Node.js (v18+)
- Java 21
- Docker & Docker Compose

### Backend

1. `cd relay-backend`
2. Copy or edit `src/main/resources/application.properties` for your MariaDB setup.
3. Run with Maven:
   ```
   ./mvnw spring-boot:run
   ```
4. API docs available at: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Frontend

1. `cd relay-frontend`
2. Install dependencies:
   ```
   npm install
   ```
3. Start development server:
   ```
   npm run dev
   ```
4. The app will be available at: [http://localhost:5173](http://localhost:5173)

### Docker (Full Stack)

1. From the project root:
   ```
   docker-compose up --build
   ```
2. The frontend will be at [http://localhost](http://localhost) and the backend at [http://localhost:8080](http://localhost:8080).

---

## Testing

- Backend:  
  ```
  cd relay-backend
  ./mvnw test
  ```
- Frontend:  
  (Add instructions if you have frontend tests)

---

## License
This project is copyright © Malek Alsibai 2025.  

