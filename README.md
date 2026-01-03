# Task Automation System

A comprehensive personal task automation system built with Spring Boot, featuring JWT authentication, Quartz scheduling, rule-based task execution, and a modern web interface.

## Features

- **User Authentication**: Secure JWT-based authentication with login and registration
- **Task Management**: Full CRUD operations for personal tasks with priority levels and due dates
- **Automated Scheduling**: Quartz-powered task execution based on schedules and rules
- **Rule Engine**: Configurable rules for automated task actions and notifications
- **Email Notifications**: Automated email alerts for task events
- **Web Interface**: Responsive HTML/CSS/JavaScript frontend for easy task management
- **Database Support**: H2 (development) and PostgreSQL (production) databases
- **RESTful API**: Complete REST API for integration with other systems

## Technologies Used

### Backend
- **Java 17** - Programming language
- **Spring Framework 6.1.0** - Core framework
- **Spring Security 6.2.0** - Authentication and authorization
- **Spring MVC** - Web framework
- **JWT (JSON Web Tokens)** - Token-based authentication
- **Quartz Scheduler** - Task scheduling and execution
- **HikariCP** - Database connection pooling
- **Jakarta Mail** - Email functionality

### Database
- **H2 Database** - In-memory database for development
- **PostgreSQL** - Production database

### Frontend
- **HTML5/CSS3** - Markup and styling
- **JavaScript (ES6+)** - Client-side functionality
- **Responsive Design** - Mobile-friendly interface

### Build & Deployment
- **Maven** - Dependency management and build tool
- **Tomcat Embedded** - Application server
- **Logback** - Logging framework
- **SLF4J** - Logging facade

## Prerequisites

Before running this application, make sure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+** (included in the project)
- **PostgreSQL** (for production) or use H2 (default for development)

## Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd TaskAutomation
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Package the application:**
   ```bash
   mvn package
   ```

## Configuration

### Database Configuration

The application supports both H2 (development) and PostgreSQL (production) databases.

**Default Configuration (H2):**
- The application uses H2 in-memory database by default
- No additional setup required for development

**PostgreSQL Configuration:**
Update `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/taskautomation
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

### Email Configuration

Configure email settings in `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### JWT Configuration

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-here
jwt.expiration=86400000
```

### Quartz Scheduler

Quartz configuration is handled in `QuartzConfig.java`. Default settings are suitable for most use cases.

## Usage

### Running the Application

1. **Development Mode:**
   ```bash
   mvn spring-boot:run
   ```

2. **Production Mode:**
   ```bash
   java -jar target/TaskAutomation-0.0.1-SNAPSHOT.jar
   ```

3. **Custom Port:**
   ```bash
   java -jar target/TaskAutomation-0.0.1-SNAPSHOT.jar --server.port=9090
   ```

The application will start on `http://localhost:8080` (or your configured port).

### Web Interface

- Navigate to `http://localhost:8080`
- Register a new account or login with existing credentials
- Create, view, update, and manage your tasks
- Tasks can be marked as complete

### API Usage

The application provides a RESTful API. Here are the main endpoints:

#### Authentication Endpoints

**POST /auth/login**
```json
{
  "username": "your_username",
  "password": "your_password"
}
```

**POST /auth/register**
```json
{
  "username": "your_username",
  "email": "your_email@example.com",
  "password": "your_password"
}
```

**GET /auth/validate**
- Requires Authorization header with JWT token

#### Task Management Endpoints

All task endpoints require JWT authentication.

**GET /tasks** - Get all tasks for authenticated user

**GET /tasks/{id}** - Get specific task by ID

**POST /tasks** - Create a new task
```json
{
  "title": "Task Title",
  "description": "Task Description",
  "dueDate": "2024-12-31",
  "priority": "HIGH"
}
```

**PUT /tasks/{id}** - Update existing task

**DELETE /tasks/{id}** - Delete a task

**PATCH /tasks/{id}/complete** - Mark task as complete

### Example API Usage with cURL

```bash
# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password"}'

# Create a task (replace TOKEN with actual JWT token)
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"title":"New Task","description":"Task description","priority":"MEDIUM"}'
```

## Project Structure

```
src/
├── main/
│   ├── java/Project/
│   │   ├── TaskAutomation/
│   │   │   └── MainApplication.java          # Application entry point
│   │   ├── config/                           # Configuration classes
│   │   │   ├── AppConfig.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── QuartzConfig.java
│   │   │   └── WebConfig.java
│   │   ├── controller/                       # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── TaskController.java
│   │   │   └── IndexController.java
│   │   ├── dao/                              # Data access objects
│   │   │   ├── TaskDao.java
│   │   │   └── UserDao.java
│   │   ├── dto/                              # Data transfer objects
│   │   │   ├── ApiResponse.java
│   │   │   ├── AuthRequest.java
│   │   │   └── AuthResponse.java
│   │   ├── executor/                         # Task execution logic
│   │   │   └── ActionExecutor.java
│   │   ├── model/                            # Entity models
│   │   │   ├── Task.java
│   │   │   └── User.java
│   │   ├── rules/                            # Rule engine
│   │   │   └── RuleEngine.java
│   │   ├── scheduler/                        # Quartz jobs
│   │   │   └── TaskExecutionJob.java
│   │   ├── service/                          # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── TaskService.java
│   │   ├── util/                             # Utilities
│   │   │   ├── JwtUtil.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   └── config/WebAppInitializer.java
│   ├── resources/                            # Application resources
│   │   ├── application.properties
│   │   ├── logback.xml
│   │   ├── quartz.properties
│   │   └── schema.sql
│   └── webapp/                               # Web application
│       ├── index.html
│       ├── css/
│       │   └── styles.css
│       └── js/
│           └── app.js
└── test/                                     # Test classes
    └── java/Project/TaskAutomation/
        └── MainApplicationTest.java
```

## Testing

Run the tests using Maven:

```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue on the GitHub repository.

