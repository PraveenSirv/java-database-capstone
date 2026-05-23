Section 1: Architecture summary
The Smart Clinic Management System follows a three-tier architecture consisting of Presentation Layer, Application Layer, and Data Layer. The application is built using Spring Boot and combines both MVC and REST architectural styles to support different interaction patterns. Thymeleaf-based MVC controllers are used for server-side rendered interfaces such as Admin Dashboard and Doctor Dashboard, while REST APIs provide JSON-based communication for modules like Appointments, Patient Dashboard, and Patient Records.

The application routes all incoming requests through controller classes, which delegate business processing to a centralized service layer. The service layer acts as the core processing unit and contains validation rules, workflow management, and domain logic. Data persistence is abstracted through repository interfaces.

The system uses a polyglot persistence approach with two databases. MySQL stores structured relational data including Patients, Doctors, Appointments, and Admin entities using Spring Data JPA and entity mapping. MongoDB stores flexible document-oriented data such as Prescription records using Spring Data MongoDB. This design improves maintainability, scalability, and allows each database to leverage its strengths.

The architecture maintains a clean separation of concerns and supports future enhancements such as mobile clients, external integrations, containerized deployment, and CI/CD pipelines.

Section 2: Numbered flow of data and control
1. Users interact with the application through either Thymeleaf web dashboards (AdminDashboard and DoctorDashboard) or REST API clients such as Appointment and Patient modules.
2. Incoming user requests are routed through Spring Boot URL mappings to either MVC Controllers or REST Controllers depending on the request type.
3. Controllers validate incoming data and forward requests to the Service Layer for business processing.
4. The Service Layer executes business rules, coordinates workflows, applies validations, and determines which repositories should be accessed.
5. Repository components communicate with the appropriate database systems. MySQL repositories handle structured relational entities while MongoDB repositories manage document-based prescription data.
6. Retrieved database data is mapped into application model objects using JPA entities (@Entity) for MySQL and document classes (@Document) for MongoDB.
7. Processed models are returned to the presentation layer where MVC controllers render Thymeleaf HTML pages and REST controllers serialize responses into JSON for API consumers.
