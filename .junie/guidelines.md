# Junie Guidelines

These guidelines define the technical and architectural conventions for this project. They serve as the primary source
of truth. Follow them strictly when generating, modifying, or refactoring code.

## Implementation and Code Quality

- Implementation needs to follow best practices in the highest standard possible.

- Implementation needs to be:
    - **Maintainable**
    - **Scalable**
    - **Professional**
    - **Production-ready**
    - **Enterprise-level**
    - **Testable**

- Before implementing a new helper method, utility, or logic, search the codebase to see if a solution already exists.
- If a specific problem (like Pagination or Filtering) has already been solved in one feature, follow the same pattern
  in new features to maintain a consistent developer experience.

## Project Structure

- This project is a monorepo (bff-pattern) currently including two Spring Boot applications:

```
bff-pattern
├── authorization-server
│   └── 
└── resource-server
    └── 
```

### Spring Boot applications

- Java 21
- Spring Boot 4.0.2
- Gradle
- application.yaml
- **Package-by-Feature** structure.
- All Spring Boot applications that require database connectivity use Spring Data JDBC.
