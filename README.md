# FindIt — Smart Campus Lost & Found

A Java Spring Boot web application. Spring Boot serves the browser UI and REST API from one deployable service. Reports and claims are stored in a relational database; uploaded images are stored as database binary data.

## Run locally

Requirements: Java 17+ and Maven 3.8+.

```bash
mvn spring-boot:run
```

Open http://localhost:8080. The default local database is an H2 file database at `./data/findit`.

To build a runnable JAR:

```bash
mvn clean package
java -jar target/findit-campus-1.0.0.jar
```

## Deploy on Railway

1. Push the contents of this folder to a GitHub repository.
2. In Railway, create a project and choose **Deploy from GitHub repo**.
3. Railway detects the Maven/Spring Boot project. Set the service start command if asked: `java -jar target/findit-campus-1.0.0.jar`.
4. Add a Railway PostgreSQL database to the project.
5. In the Java service, set these variables using the values shown in the PostgreSQL service's Connect/Variables panel:
   - `SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}`
   - `SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}`
   - `SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}`
   Replace `Postgres` in the variable references if your database service has a different name.
6. Set `ADMIN_KEY` to a long, random secret. Do not use the sample default in a public deployment.
7. Deploy, then open **Settings → Networking → Generate Domain**. Railway generates the public HTTPS URL.
8. Open the generated URL and test: create a report with a photo, refresh, submit a claim, sign in to Review desk using the `ADMIN_KEY`, approve/reject the claim, and confirm handover.

Railway's dashboard labels can change. Follow the current UI if a menu has moved.

## Deploy on Render (alternative)

Create a Web Service from the GitHub repository. Build command: `./mvnw clean package` if you add a Maven wrapper, otherwise `mvn clean package` (Render build environment must have Maven). Start command: `java -jar target/findit-campus-1.0.0.jar`. Add a managed PostgreSQL database and configure the same three datasource variables plus `ADMIN_KEY`. Configure a persistent database; do not rely on an ephemeral local filesystem for data.

## Security and project scope

- Public listing responses do not expose the reporter's contact information or the report's private identifying detail.
- Claim evidence and reporter contact details are available only through the admin-key-protected review endpoints.
- A claim is never auto-approved. A reviewer must compare evidence and arrange a safe in-person handover.
- The admin key is a simple shared-secret gate for a student demonstration, not a full institutional login system. It does not verify student identity, provide individual reviewer accounts, or provide audit-grade access control. Use a proper authentication/authorization system before real campus production use.
- Do not upload passwords, government ID numbers, or other sensitive personal data. Keep public item photos free of private details.
- For a real campus rollout, add verified institutional accounts, rate limiting, audit logs, CSRF protections where applicable, retention rules, moderation, and an agreed handover desk process.
