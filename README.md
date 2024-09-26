# WebApp Application

This repository contains the source code for the project 'webapp', a cloud-based backend web application developed for the course CSYE 6225: Network Structures and Cloud Computing, offered by Northeastern University and taken in Fall 2024.

## Languages and Technologies Used

The application is built using the following technologies, frameworks, and tools:
- **Java JDK**: Version 17
- **Spring Boot**: Version 3.3.3
- **PostgreSQL**: Version 16
- **Spring Data JPA** (Hibernate ORM)
- **Gradle**: Build tool
- **Postman**: To build and test APIs

## How to Run the Application

To run the application, follow these steps:

1. **Ensure prerequisites are installed**:
    - Java (JDK version 17)
    - PostgreSQL (version 16)

2. **Run the application**:
    - Navigate to the project directory in the command line.
    - Execute the following command to start the application:
      ```bash
      ./gradlew bootRun
      ```

3. **Build the application** (optional):
    - To build the application and generate a jar file, run the following command:
      ```bash
      ./gradlew clean build
      ```
    - This command will clean old artifacts, run test cases, and generate a jar file in the `/build/libs` directory.

4. **Run the jar file**:
    - If you wish to run the application from the generated jar file, execute the following command:
      ```bash
      java -jar <PATH_TO_JAR_FILE/JAR_FILE_NAME>
      ```
    - If your terminal is already in the correct directory, the `<PATH_TO_JAR_FILE>` can be omitted.

## API Endpoints

While running locally, you can access the application at `http://localhost:8080`. Below are the available endpoints:

### Health Check Endpoint

**GET**: `/healthz`
- Does **not** require authentication.
- Returns `200 OK` if the connection to the database is healthy.
- Returns `503 Service Unavailable` if the connection to the database is unsuccessful.
- Returns `400 Bad Request` if an invalid request parameter or request body is provided.

**POST**: `/healthz`
- Returns `405 Method Not Allowed`

**PUT**: `/healthz`
- Returns `405 Method Not Allowed`

**DELETE**: `/healthz`
- Returns `405 Method Not Allowed`

**PATCH**: `/healthz`
- Returns `405 Method Not Allowed`

## Additional Information

- This application uses **Spring Data JPA** for database interactions, with **PostgreSQL** serving as the backend database.
- API testing can be performed using **Postman** by hitting the listed endpoints.
