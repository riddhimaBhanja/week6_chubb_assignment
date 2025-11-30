**Flight Booking WebFlux**

A fully reactive Spring Boot service for managing flights, checking availability, booking tickets, and accessing booking history.
Built using **Spring WebFlux**, **Reactive MongoDB**, **Reactor**, **JUnit 5**, **Mockito**, and **JaCoCo** for test coverage.

## Features

* Add flight inventory
* Search flights by route and date
* Book tickets
* Retrieve ticket details using PNR
* View booking history
* Cancel tickets
* Global exception handling
* Reactive Non-blocking I/O
* Comprehensive unit tests (Service + Controller)
* JaCoCo test coverage reports

---

##  Tech Stack

* **Spring Boot 3**
* **Spring WebFlux**
* **Reactive MongoDB**
* **Project Reactor**
* **Lombok**
* **JUnit 5**, **Mockito**, **WebTestClient**
* **JaCoCo**
* **Maven**

---

##  API Endpoints

### **Add Flight Inventory**

`POST /api/v1.0/flight/airline/inventory`

### **Search Flights**

`POST /api/v1.0/flight/search`

### **Book Ticket**

`POST /api/v1.0/flight/booking/{flightId}`

### **Get Ticket by PNR**

`GET /api/v1.0/flight/ticket/{pnr}`

### **Booking History**

`GET /api/v1.0/flight/booking/history/{emailId}`

### **Cancel Ticket**

`DELETE /api/v1.0/flight/cancel/{pnr}`

---

