# Spring Boot + DynamoDB Local + Nginx API Gateway (Dockerized)

A simple local development stack for a Spring Boot REST API that connects to **DynamoDB Local**, with **Nginx** acting as a basic API gateway, everything running via Docker Compose.

## Build the Spring Boot JAR
  `./mvnw clean package`

## Start the stack
  `docker compose up --build -d`

## Access the API

- Through Nginx gateway (recommended):
  http://localhost:8080/api/greeting
- Direct to Spring Boot (for debugging):
  http://localhost:8081/api/greeting
- Accessing DynamoDB Local:

  Install AWS CLI and run:
  ```
  aws configure
  aws dynamodb list-tables --endpoint-url http://localhost:8000 --region us-east-1
  ```

  Alternatively, install NoSQL Workbench for DynamoDB and go to Operation Builder to connect. 
  The web-based shell for DynamoDB Local has been deprecated. 

## Stop everything
    docker compose down
