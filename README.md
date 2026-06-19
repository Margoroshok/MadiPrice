# 💊 MediPrice — Medicine Price Comparison System

A JavaFX desktop application for comparing medicine prices across pharmacies,
checking availability, and reserving medicines.

---

## Tech Stack
- **Java 17**
- **JavaFX 21** (GUI + FXML)
- **SQLite** (via JDBC + sqlite-jdbc)
- **BCrypt** (jBCrypt 0.4) — password hashing
- **Maven** — build tool
- **JUnit 5 + Mockito** — unit testing

---

## Project Structure

```
MediPrice/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/mediprice/
    │   │   ├── MediPriceApp.java          ← JavaFX entry point
    │   │   ├── controller/
    │   │   │   ├── LoginController.java
    │   │   │   ├── RegisterController.java
    │   │   │   ├── SearchController.java
    │   │   │   ├── UserPanelController.java
    │   │   │   └── AdminPanelController.java
    │   │   ├── dao/
    │   │   │   ├── UserDAO.java
    │   │   │   ├── MedicineDAO.java
    │   │   │   ├── PharmacyDAO.java
    │   │   │   ├── MedicinePriceDAO.java
    │   │   │   └── ReservationDAO.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Medicine.java
    │   │   │   ├── Pharmacy.java
    │   │   │   ├── MedicinePrice.java
    │   │   │   └── Reservation.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java
    │   │   │   ├── MedicineService.java
    │   │   │   ├── PharmacyService.java
    │   │   │   ├── ReservationService.java
    │   │   │   └── ServiceException.java
    │   │   └── util/
    │   │       ├── DatabaseConnection.java
    │   │       ├── PasswordUtil.java
    │   │       ├── ValidationUtil.java
    │   │       ├── SessionManager.java
    │   │       └── SceneManager.java
    │   └── resources/
    │       ├── css/styles.css
    │       ├── db/
    │       │   ├── schema.sql
    │       │   └── data.sql
    │       └── fxml/
    │           ├── Login.fxml
    │           ├── Register.fxml
    │           ├── Search.fxml
    │           ├── UserPanel.fxml
    │           └── AdminPanel.fxml
    └── test/
        └── java/com/mediprice/service/
            ├── AuthServiceTest.java
            ├── MedicineServiceTest.java
            ├── PharmacyServiceTest.java
            ├── ReservationServiceTest.java
            └── UtilsTest.java
```

---

## Build & Run

### Prerequisites
- Java 17+
- Maven 3.8+

### Build
```bash
mvn clean package -DskipTests
```

### Run with JavaFX Maven Plugin
```bash
mvn javafx:run
```

### Run Tests
```bash
mvn test
```

---

## Default Credentials

| Role  | Username | Password  |
|-------|----------|-----------|
| Admin | admin    | admin123  |
| User  | john_doe | user123   |
| User  | jane_smith | user123 |

The database (`mediprice.db`) is created automatically on first run in the working directory.

---

## Features by Role

### Guest (no login)
- Browse all medicines
- Search medicines by name (partial match)
- Compare prices across pharmacies
- View availability / stock levels

### Logged-in User
- All Guest features
- Reserve medicines (with stock check)
- View/cancel reservation history
- Find nearest pharmacy (Haversine GPS calculation)

### Administrator
- Full CRUD on medicines
- Full CRUD on pharmacies
- View and manage all reservations (confirm/complete/cancel)
- Access to search view

---

## Security Implementation

| Feature | Implementation |
|---------|---------------|
| Password hashing | BCrypt (12 rounds) |
| SQL Injection prevention | PreparedStatements only |
| Login bruteforce protection | Account locked for 15 min after 5 failed attempts |
| Input validation | `ValidationUtil` class — email regex, username pattern, etc. |
| Role-based access | `SessionManager` + controller-level checks |

---

## Database Schema

The SQLite database is auto-created from `schema.sql` on first launch.
Sample data is loaded from `data.sql` only if the `users` table is empty.

Tables:
- `users` — authentication, roles, failed attempts, lock
- `medicines` — drug catalogue
- `pharmacies` — locations with GPS coordinates
- `medicine_prices` — per-pharmacy price + quantity (unique constraint per medicine/pharmacy pair)
- `reservations` — user reservations with status lifecycle (PENDING → CONFIRMED → COMPLETED / CANCELLED)

---

## Nearest Pharmacy Algorithm

Uses the **Haversine formula** to calculate great-circle distance between two GPS points.
Results are sorted ascending by distance in km.

```java
PharmacyService.haversineDistance(userLat, userLon, pharmacyLat, pharmacyLon)
```

Warsaw centre example: lat=52.2297, lon=21.0122

---

## Architecture (MVC)

```
View (FXML)  ←→  Controller  ←→  Service  ←→  DAO  ←→  SQLite
```

- **Model** — POJOs: User, Medicine, Pharmacy, MedicinePrice, Reservation
- **View** — FXML files with CSS styling
- **Controller** — JavaFX controllers, handle UI events, call services
- **Service** — Business logic, validation, transactions
- **DAO** — Data access via PreparedStatements
- **Util** — DatabaseConnection, SessionManager, PasswordUtil, ValidationUtil, SceneManager
