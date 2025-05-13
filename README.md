# Cabin Booking Backend – SPK

This is the backend API for the SPK company cabin booking system. It is built using **Java**, **Spring Boot**, and uses **MySQL** as the database. The backend is deployed to **Google Cloud Run** and uses **Firebase** for authentication.

## Features

- Cabin booking for employees (private or work-related)
- Firebase-based authentication (admin and user roles)
- Admin panel support
- Booking lottery system for private trips
- Point system and quarantine logic
- Waitlist handling
- Transaction logging

## Technologies

- Java 17
- Spring Boot
- MySQL (Cloud SQL)
- Firebase Admin SDK
- Google Cloud Run
- Docker

## CI/CD and Docker

The project includes GitHub Actions workflows (`.yml` files) for continuous deployment to Firebase Hosting. Docker is used to build and deploy the backend service to Google Cloud Run.

## Running Locally

Before running locally, make sure you update `application.properties`.

### Step 1: Update `src/main/resources/application.properties`

Replace this line:
spring.datasource.url=...

With:
spring.datasource.url=jdbc:mysql://localhost:8080
spring.datasource.username=root
spring.datasource.password=your_password


Make sure to remove or comment out any Cloud SQL connector settings.

### Step 2: Firebase Admin SDK

Place your Firebase service account JSON file in:
src/main/resources/firebase-service-account.json

### Step 3: Run the app

Using Maven:
- mvn clean install
- mvn spring-boot:run


## Authentication

- The backend expects an ID token from Firebase in the `Authorization` header.
- Example: Authorization: Bearer <firebase_id_token>
- Only users registered in the database can log in (SSO planned).
- Admins must be manually created in the database (e.g. `admin@admin.no`).

## Deployment

- Deployed to Google Cloud Run
- Admin frontend hosted on Firebase Hosting
- GitHub Actions handle Firebase deployments using secrets in admin frontend 
- In production, all secrets are handled using Secret Manager

## Notes

- Update `application.properties` for local development.
- Firebase token must be valid and belong to a known user in the system.

