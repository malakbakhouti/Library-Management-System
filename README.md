📚 Bibliothèque EMSI – Application Java de Gestion de Bibliothèque (JavaFX)

Bibliothèque EMSI is a desktop Java application built with JavaFX and MySQL, designed to automate library operations: book catalog management, loans, returns, late-fee calculation and role-based dashboards.

This project was developed as part of the **JavaFX module (4th year, Ingénierie Informatique et Réseaux)** at EMSI, supervised by Mme Nisrine DAD.

🚀 Main Features

🛡️ Admin
* Manage user accounts (create, edit, delete) with role assignment (ADMIN / LIBRARIAN / MEMBER)
* Search/filter users by name, email or role
* Read-only view of the book catalog and all loans
* Global statistics dashboard (total books, users, active loans, overdue loans, most borrowed books)

📖 Librarian
* Full CRUD on the book catalog (title, author, genre, year, quantity)
* Validate or reject pending loan requests
* Register book returns with automatic stock update
* Automatic late-fee calculation (5 MAD × days late) and overdue notifications
* Librarian statistics dashboard (loan status breakdown, monthly loan trends)

🎓 Member
* Browse the book catalog by genre, with availability status
* Request a loan (14-day duration, confirmation dialog)
* Track personal loans (pending / active / overdue / returned) with penalties
* Return a borrowed book
* Receive notifications (loan validation/rejection, overdue alerts)

🏗️ Architecture Overview

The application follows a strict **MVC (Modèle – Vue – Contrôleur)** architecture on top of a MySQL relational database, accessed via JDBC.

| Layer | Role |
|---|---|
| Model | Java classes mapping the database tables (`Book`, `Loan`, `UserAccount`, `Role`, `Settings`) |
| View | JavaFX FXML screens (Login, Admin/Librarian/Member dashboards, book CRUD & forms, catalogue) |
| Controller | Handles user actions from the View and coordinates updates between Model and View |
| DAO / Service | Data Access Objects execute SQL queries; Service classes hold business logic (loan validation, penalty calculation) |

📁 Modules – Usage Summary

**`src/model`**
* `Book`, `Loan`, `UserAccount`, `Role`, `Settings` — plain Java objects mapping the `books`, `loans`, `users`, `roles` and `settings` MySQL tables

**`src/dao`**
* `BookDAO`, `LoanDAO`, `UserDAO`, `SettingsDAO` — SQL access layer
* `DBConnection` — JDBC connection handling (MySQL via `mysql-connector-j`)

**`src/service`**
* `BookService`, `LoanService`, `UserService` — business logic (availability checks, loan validation, late-fee calculation: **5 MAD × jours de retard**)

**`src/controller`**
* `LoginController`, `SessionController` — authentication and role-based routing
* `AdminDashboardController`, `LibrarianDashboardController`, `MemberDashboardController` — role-specific dashboards
* `BookCrudController`, `BookFormController`, `CatalogueController`, `LoanController`, `UserCrudController` — feature controllers

**`resources/view`**
* FXML screens: `LoginView`, `AdminDashboard`, `LibrarianDashboard`, `MemberDashboard`, `BookCrud`, `BookForm`, `Catalogue`

🗄️ Database Schema (MySQL — `library_db`)

| Table | Description |
|---|---|
| `users` | id, full_name, email, password, role_id, avatar, created_at |
| `roles` | ADMIN / LIBRARIAN / MEMBER |
| `books` | id, title, author, publisher, publish_year, genre, quantity, cover_image |
| `loans` | id, user_id, book_id, loan_date, return_due, returned, returned_date, validated_by, penalty |

🔄 Key Usage Scenario — Loan Lifecycle
1. Member browses the catalogue and requests a loan → status **"en attente"**
2. Librarian validates or rejects the request
3. On validation: loan registered (14-day due date), book stock decreased
4. On return: librarian confirms → stock increased, `returned = true`
5. If overdue: penalty auto-calculated (5 MAD/jour) and a notification is shown to the member

▶️ Run the Project

**Requirements:** JDK, JavaFX SDK, MySQL/XAMPP with the `library_db` database imported.

```bash
# Configure your MySQL connection in src/dao/DBConnection.java, then:
./run.sh
```

Or open the project in Eclipse (`.classpath` / `.project` included) and run `src/app/Main.java`.

🔮 Future Improvements
* Barcode scanner integration for books and members
* Automated email/internal notifications for due dates and penalties
* Online reservation system for unavailable books
* Book cover images in the catalogue
* Personalized user avatars

🎤 Conclusion

Bibliothèque EMSI demonstrates a complete MVC desktop application built with Java, JavaFX and MySQL, providing role-based access (Admin, Librarian, Member) to automate book management, loans, returns and penalty tracking for a university library.
