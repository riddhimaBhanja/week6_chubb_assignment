
---

#  Flight Booking Microservices

A modular and scalable microservices-based flight booking system built with Spring Boot, Spring Cloud, and reactive programming.

##  Architecture

```
┌──────────────────────────┐
│        API Gateway       │ (8080)
└────────────┬─────────────┘
             │
 ┌───────────▼────────────┐     ┌─────────────────────────┐
 │     Flight Service      │     │     Booking Service     │
 │       (8081)            │◄────┤         (8082)          │
 │ - Search                │ Feign│ - Book / Cancel / Hist │
 │ - Inventory             │ CB   │ - Email Notification    │
 └───────────┬────────────┘     └────────────┬────────────┘
             │                                │
      ┌──────▼──────┐                 ┌───────▼────────┐
      │ flight_db   │                 │  booking_db     │
      └─────────────┘                 └─────────────────┘
                     ┌──────────────────────────────┐
                     │      RabbitMQ (Async Mail)   │
                     └──────────────────────────────┘

           ┌──────────────────────────────┐
           │         Eureka Server        │ (8761)
           └──────────────────────────────┘
```

---

##  Highlight Features

### **Microservice Best Practices**

* Independent services with **separate databases**
* **Eureka-based service discovery**
* **API Gateway** for routing and centralized entry
* **Reactive programming** using Spring WebFlux
* **Circuit Breaker (Resilience4j)** to prevent cascading failures
* **OpenFeign** for inter-service communication
* **RabbitMQ** for asynchronous email notifications

### **Engineering Excellence**

* Complete **JaCoCo coverage reports**
* **SonarQube** integration for code quality
* **JMeter** load testing and Postman automation support
* Clean, modular directory structure with scripts for testing and startup

---

##  Technology Stack

| Category         | Tools                                        |
| ---------------- | -------------------------------------------- |
| Language         | Java 17                                      |
| Framework        | Spring Boot 3.x, Spring WebFlux              |
| Cloud Components | Eureka, API Gateway, OpenFeign, Resilience4j |
| Databases        | MongoDB (Reactive)                           |
| Messaging        | RabbitMQ                                     |
| DevOps & QA      | Maven, JaCoCo, SonarQube, JMeter, Newman     |

---

## Project Structure

```
flight-microservices/
│
├── eureka-server/
├── api-gateway/
├── flight-service/
├── booking-service/
│
└── testing/
    ├── jmeter/
    ├── postman/
    └── scripts/
```

---

##  Building the Project

```bash
mvn clean install
```

---

##  Running the Services

Run in sequence:

```bash
cd eureka-server && mvn spring-boot:run
cd api-gateway && mvn spring-boot:run
cd flight-service && mvn spring-boot:run
cd booking-service && mvn spring-boot:run
```

A startup script can automate all service launches.

---

##  Service Endpoints

| Service         | URL                                              |
| --------------- | ------------------------------------------------ |
| Eureka Server   | [http://localhost:8761](http://localhost:8761)   |
| API Gateway     | [http://localhost:8080](http://localhost:8080)   |
| Flight Service  | [http://localhost:8081](http://localhost:8081)   |
| Booking Service | [http://localhost:8082](http://localhost:8082)   |
| RabbitMQ UI     | [http://localhost:15672](http://localhost:15672) |

---

##  Core APIs

### **Flight Service**

```
POST /api/v1/flight/inventory
POST /api/v1/flight/search
```

### **Booking Service**

```
POST   /api/v1/booking/book/{flightId}
GET    /api/v1/booking/{pnr}
GET    /api/v1/booking/history/{email}
DELETE /api/v1/booking/cancel/{pnr}
```

---

## Testing & Quality

### **Run Unit Tests with Coverage**

```bash
mvn clean verify
```

View report in:

```
target/site/jacoco/index.html
```

### **Run SonarQube Analysis**

```bash
./testing/scripts/run-sonar.sh
```

### **Load Testing (JMeter)**

```bash
./testing/scripts/run-jmeter.sh
```

### **Postman Automation (Newman)**

```bash
./testing/scripts/run-postman.sh
```

---

##  Observability

### Health Checks

```
/actuator/health
```

### Circuit Breaker Monitoring

```
/actuator/circuitbreakers
/actuator/circuitbreakerevents
```

---

##  Migration from Monolith

Key changes:

* Decomposed into **Flight** and **Booking** services
* Added **API Gateway + Eureka**
* Introduced **Reactive Feign**, **Circuit Breakers**, and **RabbitMQ**
* Separate **MongoDB** databases for true isolation
* Structured CI/CD-ready architecture

---

##  Future Enhancements

* Distributed tracing (Zipkin/Sleuth)
* JWT-based Authentication
* Docker & Kubernetes deployment
* Redis caching
* Swagger/OpenAPI documentation

---


