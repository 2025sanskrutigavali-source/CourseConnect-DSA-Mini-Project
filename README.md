# CourseConnect — Student Course Enrollment System

> **College Data Structures and Algorithms (DSA) Mini-Project**  
> Built with Core Java, Java Built-In HTTP Server (`com.sun.net.httpserver.HttpServer`), File-Based JSON Persistence, and Vanilla Modern Web Technologies.

---

## 1. Project Overview

**CourseConnect** is a robust, full-stack university course enrollment application engineered specifically as an exemplary Data Structures and Algorithms mini-project. It implements an authoritative, friction-free academic SaaS environment allowing students to browse real-time course catalogues, enroll within semester credit caps, and track their degree loads, while institutional registrars manage offerings, audit student records, and monitor campus capacity utilization.

### Key Highlights
- **Zero External Server Dependencies:** Runs directly on Core Java without requiring Spring Boot, external database servers (MySQL/PostgreSQL), Maven, or complex container tooling.
- **Built-in Java HTTP Server:** Employs Java's standard library `com.sun.net.httpserver.HttpServer` with a multi-threaded thread pool.
- **Pure Core Java DSA:** Real, genuine implementations of **HashMap**, **HashSet**, **ArrayList**, **Searching**, and **Sorting** algorithms with time and space complexities explained.
- **Secure File-based JSON Persistence:** Stores and restores users, courses, and enrollment records across server restarts with atomic file synchronization.
- **Secure Authentication:** Never stores plaintext passwords; leverages **SHA-256 with secure random 16-byte salt**.
- **Role-Based Access Control:** Strict segregation between Student and Institution Registrar endpoints.
- **Polished Visual Design:** Clean, emerald-accented design system featuring responsive cards, tables, modal dialogs, and toast notifications.

---

## 2. System Architecture

```
CourseConnectSystem/
├── backend/
│   ├── src/
│   │   ├── Main.java                 # Entry point; boots HTTP server and registers shutdown hooks
│   │   ├── TestRunner.java           # Automated verification test suite for all 20 test cases
│   │   ├── HttpIntegrationTest.java  # End-to-end HTTP integration tests
│   │   ├── model/
│   │   │   ├── User.java             # User entity (Student / Institution Admin)
│   │   │   ├── Course.java           # Course offering entity with capacity metrics
│   │   │   ├── Enrollment.java       # Enrollment binding student to course
│   │   │   └── Session.java          # In-memory authenticated session with TTL
│   │   ├── repository/
│   │   │   └── DataStore.java        # File persistence & initial data seeder
│   │   ├── service/
│   │   │   ├── AuthService.java      # Password verification and session lifecycle
│   │   │   ├── CourseService.java    # Course DSA queries, searching, sorting, management
│   │   │   ├── EnrollmentService.java# Enrollment constraint enforcement & thread-safe operations
│   │   │   └── AdminService.java     # Institutional capacity metrics and aggregations
│   │   ├── server/
│   │   │   ├── HttpServerApp.java    # HttpServer configuration and context wiring
│   │   │   ├── ApiHandler.java       # REST routing and role access enforcement
│   │   │   └── StaticFileHandler.java# Serves frontend assets with path-traversal prevention
│   │   └── util/
│   │       ├── JsonUtil.java         # Zero-dependency, lightweight JSON parser/serializer
│   │       ├── PasswordUtil.java     # SHA-256 + Salt hashing utility
│   │       └── ResponseUtil.java     # Uniform REST JSON response helper
│   └── bin/                          # Compiled bytecode .class files
├── frontend/
│   ├── index.html                    # Single Page Application (Login, Student & Admin Dashboards)
│   ├── css/
│   │   └── styles.css                # Emerald design system, responsive grid, modal & toast styling
│   └── js/
│       ├── api.js                    # Fetch API client with Bearer token authentication
│       ├── components.js             # Modal dialogs, toast notifications, confirmation prompts
│       └── app.js                    # Router, view controllers, search/filter listeners
├── data/                             # Persisted JSON datastore (auto-seeded on first run)
│   ├── users.json                    # User credentials and role profiles
│   ├── courses.json                  # University course offerings and seat counts
│   └── enrollments.json              # Active student enrollment records
├── run.bat                           # 1-click Windows compilation & launch script
├── run.sh                            # 1-click Linux/macOS compilation & launch script
└── README.md                         # Project documentation and DSA guide
```

---

## 3. Data Structures and Algorithms (DSA) Implementation

The backend explicitly showcases fundamental computer science data structures and algorithms in action:

| DSA Concept | Component & Location | Purpose & Operational Role | Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- | :--- |
| **HashMap** | `DataStore.courseMap`<br>`CourseService` | Direct mapping of `courseId` $\rightarrow$ `Course` object. Enables instantaneous course lookups during detail queries, enrollment checks, updates, and seat decrements. | **$O(1)$** Average Lookup<br>**$O(1)$** Insert/Delete | **$O(N)$** |
| **HashSet** | `DataStore.enrollmentIndex`<br>`EnrollmentService` | Stores unique composite keys in format `"studentId#courseId"`. Enables instantaneous duplicate enrollment detection before modifying database. | **$O(1)$** Average Check | **$O(E)$** where $E$ is enrollments |
| **ArrayList** | `DataStore.enrollments`<br>`CourseService.queryCourses` | Dynamic sequential collections for cataloguing courses, maintaining chronological enrollment records, and supporting iteration and filtering. | **$O(1)$** Append<br>**$O(N)$** Traversal | **$O(N)$** |
| **Searching** | `CourseService.queryCourses` | Linear scan evaluating case-insensitive substring matching against `courseId`, `courseName`, and `instructor`, combined with department and credit predicates. | **$O(N)$** Linear Search | **$O(K)$** where $K \le N$ |
| **Sorting** | `CourseService.getComparator` | Java `Collections.sort` leveraging TimSort (hybrid Merge Sort and Insertion Sort) with custom Comparators for Course Code, Title, Credits, and Available Seats. | **$O(K \log K)$** | **$O(K)$** auxiliary |
| **Concurrency / Mutex** | `EnrollmentService.ENROLLMENT_MUTEX` | Thread-safe synchronization preventing race conditions and simultaneous over-subscription of limited course capacity. | **$O(1)$** Lock acquisition | **$O(1)$** |

### DSA Explanations for College Viva / Examination:

1. **Why use HashMap for Courses?**
   - In a university with hundreds of courses, looking up a course by ID using a linear list takes $O(N)$ comparisons. A HashMap computes a hash of the `courseId` string and achieves near-instantaneous $O(1)$ lookup time.
2. **Why use HashSet for Duplicate Enrollment Prevention?**
   - When a student attempts to enroll, checking all historical enrollment records sequentially would require iterating through every record ($O(E)$ time). By maintaining a composite key `"student01#CS-201"` in a HashSet, duplicate checking executes in $O(1)$ time.
3. **How does Sorting work?**
   - The user can toggle sorting by Course Title, Course Code, Credits, or Available Seats. Java uses TimSort on the candidate ArrayList, dividing elements into sorted runs and merging them in $O(K \log K)$ worst-case time without performance degradation.

---

## 4. Academic Enrollment Business Rules

The backend strictly enforces 10 institutional academic constraints:

1. **Duplicate Prevention:** A student cannot enroll in the same course twice (verified via HashSet in $O(1)$ time).
2. **Capacity Enforcement:** A student cannot enroll when all seats are filled (`availableSeats <= 0`).
3. **Capacity Cap Integrity:** A course cannot exceed its defined capacity.
4. **Existence Verification:** A student cannot enroll in a nonexistent course (verified via HashMap in $O(1)$).
5. **Legitimate Drop Verification:** A student cannot drop a course in which they are not currently enrolled.
6. **Student Isolation:** A student can only view and manage their own enrollments. They cannot drop courses for another student.
7. **Atomic Seat Allocation:** Enrolling immediately increments `enrolledCount` and decrements `availableSeats`.
8. **Seat Release on Drop:** Dropping a course releases a seat and increments `availableSeats`.
9. **Maximum Credit Cap:** Students are limited to a maximum of **18 semester credits**. Any enrollment request exceeding 18 credits is rejected with a descriptive message.
10. **Concurrency Safety:** Enrollment and drop operations are synchronized on a shared mutex lock to prevent race conditions during high-concurrency registration windows.

---

## 5. Demonstration Accounts

The application includes pre-seeded demonstration accounts for local evaluation:

| Portal Role | Username | Password | Full Name | Academic Department / Role |
| :--- | :--- | :--- | :--- | :--- |
| **Student** | `student01` | `student123` | Alex Mercer | Computer Science & Eng. (3rd Year) |
| **Student** | `student02` | `student123` | Sophia Chen | Electrical Engineering (2nd Year) |
| **Institution** | `admin01` | `admin123` | Dr. Eleanor Vance | Academic Registrar Office |

> **Security Note:** Passwords are stored in `data/users.json` as cryptographic hashes (`<16-byte-salt-hex>:<sha-256-hash-hex>`). Plaintext passwords are never persisted.

---

## 6. How to Run the Application Locally

### Prerequisites
- Installed JDK 17, 21, or 25.
- Ensure `java` and `javac` are available in your system `PATH`.

### Option A: Using the Windows Batch Script (Recommended for Windows)
1. Double-click `run.bat` or open PowerShell in the project directory:
   ```cmd
   .\run.bat
   ```
2. The script will verify Java, check port 8082 availability, compile all source files into `backend\bin`, start the server at `http://localhost:8082`, and open your default web browser.

### Option B: Using the Linux / macOS Shell Script
1. Make the script executable and run:
   ```bash
   chmod +x run.sh
   ./run.sh
   ```

### Option C: Manual Command-Line Compilation & Execution
1. Compile all Java sources:
   ```cmd
   javac -d backend/bin backend/src/model/*.java backend/src/util/*.java backend/src/repository/*.java backend/src/service/*.java backend/src/server/*.java backend/src/Main.java
   ```
2. Start the HTTP server on Port 8082:
   ```cmd
   java -cp backend/bin Main --port 8082 --frontend frontend
   ```
3. Open your browser and navigate to:
   ```
   http://localhost:8082
   ```

### Stopping the Server
Press `Ctrl + C` in the terminal where the server is running. The shutdown hook will cleanly terminate the HTTP server and release all port bindings.

---

## 7. Port Allocation & Multi-Service Compatibility

CourseConnect is pre-configured to run on **Port 8082** to coexist alongside other local development services:
- **Port 8080:** Reserved for Jenkins CI/CD.
- **Port 8081:** Reserved for earlier CourseConnect project versions.
- **Port 8082:** Dedicated for this CourseConnect release.

### Built-In Port Safety & Availability Check
Before opening the server socket, CourseConnect performs a non-destructive socket probe. If port 8082 is already occupied:
1. CourseConnect **never terminates or interferes** with other active processes.
2. It reports that the port is occupied and sequentially searches for the next free port (e.g., 8083, 8084), displaying exact commands to start the server without conflict.

---

## 7. REST API Endpoints

All endpoints exchange uniform JSON payloads: `{ "success": true|false, "message": "...", "data": ... }`.

### Authentication Routes
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Public | Registers a Student or Institution account; persists a salted password hash. |
| `POST` | `/api/auth/login` | Public | Authenticates user; returns bearer token and profile. |
| `POST` | `/api/auth/logout` | Authenticated | Invalidates the active session token. |
| `GET` | `/api/auth/me` | Authenticated | Retrieves current authenticated session details. |

### Course Catalogue Routes
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/courses` | Public / Student | Lists courses with query parameters: `search`, `department`, `credits`, `sort`. If student token is supplied, marks `isEnrolled: true/false`. |
| `GET` | `/api/courses/{id}` | Public | Retrieves detailed course syllabus, schedule, and room. |
| `POST` | `/api/courses` | Institution | Creates a new course offering (validates positive capacity and credits). |
| `PUT` | `/api/courses/{id}` | Institution | Updates existing course (prevents setting capacity below enrolled count). |
| `DELETE` | `/api/courses/{id}` | Institution | Deletes course (strictly prevented if active enrollments exist). |

### Student Portal Routes
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/student/enrollments` | Student Only | Returns active enrolled courses and total credit tally. |
| `POST` | `/api/student/enroll` | Student Only | Enrolls in a course `{ "courseId": "CS-201" }`. |
| `POST` | `/api/student/drop` | Student Only | Drops an enrolled course `{ "courseId": "CS-201" }`. |
| `GET` | `/api/student/profile` | Student Only | Returns student profile and credit progress. |

### Institution Administrator Routes
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/stats` | Institution Only | Real-time calculations: total courses, students, enrollments, capacity %. |
| `GET` | `/api/admin/enrollments` | Institution Only | Searchable, filterable audit log of all student enrollments. |
| `GET` | `/api/admin/students` | Institution Only | Directory of registered undergraduate students. |

---

## 8. Verification & Automated Test Results

The project includes an automated test suite (`TestRunner.java`) and an integration test runner (`HttpIntegrationTest.java`).

### Running the Test Suite:
```cmd
javac -cp backend/bin -d backend/bin backend/src/TestRunner.java
java -cp backend/bin TestRunner
```

### Verified Test Cases:
- [x] **Test 1:** Valid student login (`student01` / `student123`).
- [x] **Test 2:** Valid institution login (`admin01` / `admin123`).
- [x] **Test 3:** Invalid password rejection with error message.
- [x] **Test 4:** Student account blocked from institution endpoints (403 Forbidden).
- [x] **Test 5:** Search course by title ("Data Structures").
- [x] **Test 6:** Search course by unique code ("CS-250").
- [x] **Test 7:** Successful enrollment decrements available seats.
- [x] **Test 8:** Duplicate enrollment rejection via HashSet ($O(1)$).
- [x] **Test 9:** Enrollment rejection when course reaches maximum capacity.
- [x] **Test 10:** Successful course drop releases seat and updates credit load.
- [x] **Test 11:** Rejection when enrolling in a nonexistent course ID.
- [x] **Test 12:** Institution adds a new course offering.
- [x] **Test 13:** Institution updates course attributes.
- [x] **Test 14:** Input validation blocks invalid courses (0 credits, negative capacity).
- [x] **Test 15:** Institution deletes a course with 0 enrollments.
- [x] **Test 16:** Deletion of course with active enrollments is blocked.
- [x] **Test 17:** Capacity cannot be set lower than currently enrolled students.
- [x] **Test 18:** Stored JSON data survives server restart.
- [x] **Test 19:** Logout invalidates session token.
- [x] **Test 20:** Maximum credit cap (18 credits) enforced on backend.
- [x] **Test 21:** Multi-threaded concurrency test (race condition prevented on limited seats).

---

## 9. Troubleshooting & FAQ

- **Q: "Port 8082 already in use" error?**  
  You can run the server on a custom port using `--port <number>`:
  ```cmd
  java -cp backend/bin Main --port 8083 --frontend frontend
  ```
  Then access `http://localhost:8083`. CourseConnect automatically suggests the next available port if 8082 is occupied without terminating running processes.

- **Q: How do I reset the data to initial demo state?**  
  Simply delete the `data/` directory or the JSON files inside it. Upon the next startup, `DataStore` will automatically detect the absence of files and re-seed the fresh demo dataset.

- **Q: Are external libraries or Maven required?**  
  No. Everything is written in standard Core Java without external `.jar` files or complex build tools.
