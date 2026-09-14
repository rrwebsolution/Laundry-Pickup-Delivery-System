# Laundry Pickup and Delivery Management System

A desktop CRUD application for managing a laundry shop's customers, services,
orders, pickup/delivery scheduling, payments, and reports — built with plain
Java, Swing, and JDBC/MySQL. Built as a school project to demonstrate complete
Object-Oriented Programming and full CRUD operations through a GUI.

## Project Overview

Staff and administrators log in, then use a sidebar-navigated dashboard to:

1. Register customers
2. Create a laundry order (cost is calculated automatically)
3. Schedule a pickup
4. Move the order through its wash/dry/fold statuses
5. Mark it ready for delivery and schedule/assign a rider
6. Record the customer's payment
7. Mark the order delivered
8. Review everything from the Reports section

## Features

- **Login system** with three roles: Administrator, Staff, Rider
- **Dashboard** with live summary cards (customers, orders, pending pickups,
  in-process items, ready-for-delivery, completed orders, today's revenue)
- **Customer management** — full CRUD, search by name/contact, click-a-row-to-edit
- **Laundry service catalog** — full CRUD (Administrator only), per-kilogram or
  per-piece pricing
- **Order management** — full CRUD, automatic laundry cost/total computation,
  10-stage status workflow, search/filter by ID, customer, or status
- **Pickup & delivery scheduling** — full CRUD, rider assignment, status tracking
- **Payment recording** — full CRUD, automatic change calculation, automatic
  payment-status determination, negative change is never allowed
- **User management** (Administrator only) — full CRUD, hashed passwords never
  shown in the table
- **Reports** — daily orders, completed orders, pending orders, pickup/delivery
  records, payment records, daily revenue, monthly revenue
- Confirmation dialogs before every delete, and success/error dialogs after
  every create/update/delete

## Technologies

- Java (Swing for the GUI)
- [FlatLaf](https://www.formdev.com/flatlaf/) - modern flat look and feel (light theme, rounded components, visible input borders/focus/error outlines)
- [Ikonli](https://kordamp.org/ikonli/) (Material Design 2 icon pack) - real vector icons rendered through Swing's `Icon` interface, not emoji/Unicode glyphs
- JDBC (MySQL Connector/J, bundled in `lib/`)
- MySQL

No PHP, no JavaScript, no web framework — this is a pure desktop application.

## OOP Concepts Demonstrated

| Concept | Where |
|---|---|
| **Encapsulation** | Every model class (`Customer`, `LaundryOrder`, `Payment`, ...) keeps fields `private` with public getters/setters |
| **Inheritance** | `User` (abstract) → `Administrator`, `Staff`, `Rider` |
| **Abstraction** | `User.getRole()` / `getAccessDescription()` are abstract; `GenericDAO<T, ID>` is an interface hiding JDBC details from callers |
| **Polymorphism** | Code that holds a `User` reference (e.g. `MainDashboardFrame`) calls `getRole()`/`getAccessDescription()` and gets different behavior per concrete subclass; `UserFactory` returns the right subclass without callers knowing which one |
| **Constructors** | Every model/DAO has explicit constructors (default + parameterized) |
| **Interfaces** | `dao.GenericDAO<T, ID>` defines the CRUD contract implemented by every DAO |
| **Separation of concerns** | `model` (data), `dao` (JDBC/SQL), `service` (validation/business rules), `view` (Swing GUI), `database` (connection factory) are separate packages — Swing classes never contain SQL |

## Project Structure

```text
LaundrySystem/
├── src/
│   ├── Main.java                  # application entry point
│   ├── model/                     # User hierarchy, Customer, LaundryOrder, etc.
│   ├── dao/                       # JDBC CRUD classes (CustomerDAO, UserDAO, ...)
│   ├── service/                   # validation + business logic between GUI and DAO
│   ├── view/                      # Swing GUI (LoginFrame, MainDashboardFrame, panels)
│   │   └── components/            # Reusable UI building blocks (StyledTextField, SidebarButton,
│   │                              # IconButton, AppIcon, Card, Toast, Dialogs, ...)
│   ├── database/                  # DatabaseConnection (centralized JDBC config)
│   └── util/                      # PasswordUtil (SHA-256 hashing)
├── database/
│   └── laundry_system.sql         # schema + seed data
├── lib/
│   ├── mysql-connector-j-8.4.0.jar
│   ├── flatlaf-3.5.4.jar
│   ├── ikonli-core-12.4.0.jar
│   ├── ikonli-swing-12.4.0.jar
│   └── ikonli-materialdesign2-pack-12.4.0.jar
├── bin/                           # compiled .class output (created by javac)
└── README.md
```

## Database Setup

1. Make sure MySQL Server is running (e.g. via XAMPP's MySQL module, or a
   standalone MySQL installation).
2. Run the schema script. From a terminal with the `mysql` client on PATH:

   ```bash
   mysql -u root -p < database/laundry_system.sql
   ```

   Or open `database/laundry_system.sql` in phpMyAdmin / MySQL Workbench and
   execute it. This drops and recreates the `laundry_system` database, creates
   all six tables (`users`, `customers`, `laundry_services`, `laundry_orders`,
   `pickup_deliveries`, `payments`), and inserts sample data plus three
   default accounts.

## MySQL Configuration

Connection settings live in one place: [src/database/DatabaseConnection.java](src/database/DatabaseConnection.java).

```java
private static final String HOST = "localhost";
private static final String PORT = "3306";
private static final String DATABASE = "laundry_system";
private static final String USERNAME = "root";
private static final String PASSWORD = "";
```

Edit these constants to match your MySQL setup (for example, set `PASSWORD` if
your `root` account requires one).

## How to Compile

All required libraries are already included in `lib/` (MySQL Connector/J,
FlatLaf, and Ikonli's core/Swing/Material-Design-2 icon pack jars), so no
build tool (Maven/Gradle) is required — just the JDK.

**Windows (PowerShell / cmd):**

```powershell
cd "LaundrySystem"
javac -encoding UTF-8 -cp "lib/*" -d bin (Get-ChildItem -Recurse -Filter *.java src | ForEach-Object { $_.FullName })
```

**macOS / Linux:**

```bash
cd LaundrySystem
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -cp "lib/*" -d bin @sources.txt
```

## How to Run

**Windows:**

```powershell
java -cp "bin;lib/*" Main
```

**macOS / Linux:**

```bash
java -cp "bin:lib/*" Main
```

## Default Login Credentials

| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |
| Staff | `staff1` | `staff123` |
| Rider | `rider1` | `rider123` |

Passwords are never stored in plain text — the SQL seed data stores SHA-256
hashes, and the application hashes any password typed at login or entered in
User Management before comparing/storing it (see `util/PasswordUtil.java`).

## Validation Highlights

- Required fields (name, contact, address, etc.) cannot be blank
- Contact numbers must match a basic phone-number pattern
- Prices, weights/quantities, pickup/delivery fees, and amount paid cannot be negative
- Orders must reference an existing customer and service
- Pickup/delivery records must reference an existing order
- Payments cannot report a completed/paid status while owing a negative change
  (change is clamped to zero and status becomes "Partially Paid" or "Unpaid"
  until the full amount is covered)
- Usernames must be unique
