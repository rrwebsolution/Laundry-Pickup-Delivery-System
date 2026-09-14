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

## Getting Started (Setup Guide)

Follow these steps in order for a first-time setup. This assumes Windows with
XAMPP, but the Linux/macOS equivalents are noted where they differ.

### Prerequisites

- **JDK 17 or newer** (JDK 21 LTS recommended; this project was also
  built/tested on JDK 26 — any recent JDK works). See
  [0. Install Java JDK](#0-install-java-jdk--vs-code-first-time-setup) below
  if you don't have one yet.
- **MySQL Server**, running and reachable. The easiest option on Windows is
  [XAMPP](https://www.apachefriends.org/) — its bundled MySQL is all you need
  (you don't need Apache/PHP running, just the MySQL service).
- No IDE is required — a text editor plus the commands below are enough. VS
  Code is a good free option if you don't already have one set up — see below.

### 0. Install Java JDK + VS Code (first-time setup)

Skip this section if you already have a JDK and an editor/IDE installed and
working — jump straight to [Step 1](#step-1--get-the-project).

**a) Install the Java JDK**

1. Go to [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)
   and pick **Java 21 → Windows → x64 Installer**.
2. Download and run the installer. The default settings are fine for
   almost everyone — just keep clicking Next.

**b) Verify the installation**

Open Command Prompt (or the VS Code terminal) and run:

```powershell
java -version
javac -version
```

You should see output similar to:

```text
java version "21..."
javac 21...
```

If both commands print a version number like that, your JDK is installed
correctly. (If you see "not recognized as an internal or external command",
close and reopen your terminal first — if it still fails, the installer may
not have added Java to your PATH; reinstall and make sure that option is
checked, or add it manually.)

**c) Install VS Code**

If you already have VS Code, skip this. Otherwise, download and install it
from [code.visualstudio.com](https://code.visualstudio.com/).

**d) Install the Java extension pack for VS Code**

1. Open VS Code and press `Ctrl+Shift+X` to open the Extensions panel.
2. Search for **Extension Pack for Java**.
3. Install **Extension Pack for Java** by Microsoft.

This bundles the extensions you need for Java development, debugging, and
project management inside VS Code (you won't need to install them one by one).

**e) Try it out**

Create a folder for your projects, e.g. `C:\Projects\LaundrySystem`, then in
VS Code use **File → Open Folder...** and select it. Once your `LaundrySystem`
project files are inside that folder, you can either use VS Code's integrated
terminal to run the `javac`/`java` commands from [Step 5](#step-5--compile)
and [Step 6](#step-6--run) below, or install the Java extension pack's Run
button once the project is open — both work the same way.

### Step 1 — Get the project

Make sure you have the whole `LaundrySystem` folder, including the `src/`,
`database/`, and `lib/` subfolders (the `lib/` jars are required — the app
will not compile or run without them).

### Step 2 — Start MySQL

Start MySQL from the XAMPP Control Panel (click **Start** next to *MySQL*),
or start your standalone MySQL service if you're not using XAMPP. Leave it
running for as long as you use the app.

### Step 3 — Import the database

Run the bundled schema/seed script against your MySQL server:

```bash
mysql -u root -p < database/laundry_system.sql
```

(If your `root` account has no password — the XAMPP default — just omit
`-p`, or press Enter when prompted for a password.)

If you don't have the `mysql` command-line client on your PATH, open
**phpMyAdmin** (XAMPP's Control Panel → MySQL → Admin) or **MySQL Workbench**,
open `database/laundry_system.sql` there, and run/execute it instead.

This script drops and recreates the `laundry_system` database, creates all
six tables (`users`, `customers`, `laundry_services`, `laundry_orders`,
`pickup_deliveries`, `payments`), and inserts sample data plus three default
accounts (see [Default Login Credentials](#default-login-credentials) below).

### Step 4 — Check the database connection settings

Connection settings live in one place:
[src/database/DatabaseConnection.java](src/database/DatabaseConnection.java).

```java
private static final String HOST = "localhost";
private static final String PORT = "3306";
private static final String DATABASE = "laundry_system";
private static final String USERNAME = "root";
private static final String PASSWORD = "";
```

The defaults above match a stock XAMPP install, so most users can skip this
step. If your MySQL uses a different host/port/username, or your `root`
account has a password, edit these four constants to match, then re-save
the file (you'll compile it in the next step).

### Step 5 — Compile

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

This compiles every `.java` file under `src/` into `.class` files under `bin/`
(created automatically). Re-run this any time you change a `.java` file.

### Step 6 — Run

**Windows:**

```powershell
java -cp "bin;lib/*" Main
```

**macOS / Linux:**

```bash
java -cp "bin:lib/*" Main
```

The login window should appear within a couple of seconds.

### Step 7 — Log in

Use one of the [default accounts](#default-login-credentials) — e.g.
`admin` / `admin123` — then explore the sidebar (Dashboard, Customers,
Orders, Pickup & Delivery, Payments, Reports, Users, Settings).

### Troubleshooting

| Problem | Likely cause / fix |
|---|---|
| `Could not connect to the database` dialog on login | MySQL isn't running — start it from XAMPP's Control Panel — or the credentials in `DatabaseConnection.java` don't match your setup (Step 4). |
| `Unknown database 'laundry_system'` | The schema script (Step 3) wasn't run yet, or was run against a different MySQL server than the app is pointing at. |
| `Access denied for user 'root'@'localhost'` | Your MySQL `root` account has a password that isn't reflected in `PASSWORD` in `DatabaseConnection.java` (Step 4). |
| `package does not exist` / `cannot find symbol` when compiling | The `-cp "lib/*"` (or `lib/*` on macOS/Linux) flag is missing or misspelled, or a jar is missing from `lib/`. |
| `Error: Could not find or load main class Main` when running | You're running `java` from a different folder than the compiled `bin/` directory, or `-cp` doesn't include `bin`. Run the command from inside the `LaundrySystem` folder, as shown above. |
| Login window doesn't visually update after editing a `.java` file | You edited the source but didn't recompile — repeat Step 5, then Step 6. |
| Port 3306 already in use / MySQL won't start in XAMPP | Another MySQL instance (or a different app) is already using port 3306. Stop the other service, or change MySQL's port in XAMPP and update `PORT` in `DatabaseConnection.java` to match. |

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
