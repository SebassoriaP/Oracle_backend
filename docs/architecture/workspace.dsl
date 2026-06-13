workspace "OMI Backend" "C4 model for the OMI Smart project backend API" {

    !identifiers hierarchical

    model {
        user = person "User" "Project manager or team member using the OMI application."

        omiSystem = softwareSystem "OMI System" "Issue and project management platform with AI-assisted features." {
            frontend = container "Web Frontend" "React SPA" "Delivers the user interface."
            backend = container "Spring Boot API" "Java 17, Spring Boot 3" "REST API for projects, issues, sprints, KPIs and authentication." {
                authService = component "Authentication Service" "Handles login, registration, JWT and security filters." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/auth-service.puml"
                }
                userService = component "User Management" "CRUD operations for application users." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/user-service.puml"
                }
                roleService = component "Role Management" "Manages user roles and permissions." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/role-service.puml"
                }
                projectService = component "Project Management" "Creates and manages projects and members." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/project-service.puml"
                }
                sprintService = component "Sprint Management" "Manages sprints within projects." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/sprint-service.puml"
                }
                featureService = component "Feature Management" "Manages features linked to projects." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/feature-service.puml"
                }
                issueService = component "Issue Management" "CRUD issues, time logs and AI embeddings." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/issue-service.puml"
                }
                kpiService = component "KPI Service" "Aggregates hours and task metrics per user." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/kpi-service.puml"
                }
                overdueService = component "Overdue Report Service" "Generates and updates overdue task reports." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/overdue-service.puml"
                }
                healthService = component "Health Check" "Exposes liveness and readiness endpoints." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/health-service.puml"
                }
                configService = component "API Configuration" "CORS, OpenAPI and cross-cutting config." "Java and Spring Boot" {
                    url "https://github.com/SebassoriaP/Oracle_backend/blob/paco/docs/diagrams/config-service.puml"
                }
            }

            database = container "Oracle Autonomous DB" "Stores users, projects, issues, sprints and KPI data." "Oracle ADB" {
                tags "Database"
            }
        }

        user -> omiSystem.frontend "Uses"
        omiSystem.frontend -> omiSystem.backend "API calls" "HTTPS/JSON"
        omiSystem.backend -> omiSystem.database "Reads from and writes to" "JDBC"
    }

    views {
        systemContext omiSystem "SystemContext" {
            include *
            autolayout lr
        }

        container omiSystem "Containers" {
            include *
            autolayout lr
        }

        component omiSystem.backend "Components" {
            include *
            autolayout lr
        }

        styles {
            element "Person" {
                shape Person
            }
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Database" {
                shape Cylinder
                background #438dd5
                color #ffffff
            }
        }
    }
}
