Backend Assignment 

Project Overview:-
This is a high-performance Spring Boot REST API designed to handle social media interactions (Posts and Comments) while implementing strict guardrails against automated bot spam. Built to survive high-concurrency environments, the system utilizes a hybrid storage architecture featuring PostgreSQL as the primary source of truth and Redis as a high-speed, thread-safe gateway.

Tech Stack:-
Framework: Java 21 / Spring Boot 3.3.4

Database: PostgreSQL 15

In-Memory Cache & Locks: Redis 7

Containerization: Docker & Docker Compose

Build Tool: Maven

Core Architecture:-
1. Hybrid Storage Strategy
To optimize database performance, the system acts as a two-tier filter.

Redis acts as the "Bouncer", handling all high-frequency operations (cooldowns, bot counting, virality scoring) entirely in-memory.

PostgreSQL acts as the "Vault". A database connection is only consumed after Redis has validated the request, protecting the database from crashing during a spam attack.

2. Thread Safety & The "Spam Test" (Phase 2)
To guarantee thread safety during the 200-concurrent-bot spam test, standard Java synchronized blocks and PostgreSQL row-level locks were intentionally avoided, as they cause massive performance bottlenecks. Instead, concurrency is handled via Redis atomic operations:

Horizontal Cap (100 Bots): Managed using Redis INCR (opsForValue().increment()). Because Redis is single-threaded, this operation is inherently atomic, preventing the "Lost Update" problem where multiple concurrent bots are incorrectly counted as one.

Vertical Cooldown (10 Minutes): Implemented using Redis SETNX logic (setIfAbsent). This ensures the check-and-lock operation happens in a single, uninterrupted step, preventing race conditions where a bot might sneak in two comments at the exact same millisecond.

3. Batched Notification Sweeper (Phase 3)
To prevent notification fatigue, the system uses a Time-Windowed Buffer:

The very first bot interaction triggers an immediate console notification and sets a 15-minute cooldown timer.

Any subsequent interactions within that 15-minute window are silently queued into a Redis List.

A background Scheduled Sweeper (@Scheduled) executes every 5 minutes, pops the Redis queue, and logs a single, summarized "Batched" notification, significantly reducing system overhead.

💻 Local Setup & Installation
Prerequisites
Docker and Docker Compose installed.

Java 21 and Maven installed (if running outside of a container).

Step 1: Spin up Infrastructure
A docker-compose.yml is included to instantly provision PostgreSQL and Redis. From the root directory, run:

Bash
docker-compose up -d
Step 2: Run the Application
The application is configured with ddl-auto=update and will automatically generate the required database tables on startup.

Bash
mvn spring-boot:run
(The API will be available at http://localhost:8080)

API Testing
A fully configured Postman collection (backend_assignment.json) is included in the root directory.

To test the API:

Open Postman.

Click Import and select the included JSON file.

The requests are ordered chronologically. Run the Create Post request first to establish a human post, and then use the subsequent Bot Comment requests to test the Cooldown, Limit, and Notification Guardrails.
