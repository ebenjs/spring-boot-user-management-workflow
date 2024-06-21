# User management workflow
A simple ready to use user management workflow for your spring boot application.

## Features
- User registration
- User login (JWT, OAuth2)
- User profile update
- User password update
- User password reset
- User account verification
- User account activation

## Technologies
- Spring Boot
- Spring Security
- Spring Data JPA
- Spring Mail
- Spring Web

## Getting started
1. Clone the repository

```bash
git clone https://github.com/ebenjs/spring-boot-user-management-workflow.git
```
2. Open the project in your favorite IDE

    You can import the project as a maven project in your favorite IDE. The best way of
using this library is to add it as a dependency in your main project.

```xml
<dependency>
    <groupId>com.ebenj</groupId>
    <artifactId>user-management-workflow</artifactId>
    <version>1.0.0</version>
</dependency>
```
Copy values from `application.properties` to your `application.properties` file and adjust them to your needs.

```properties
spring.application.name=application
app.jwt.secret=9c3791c32879fa20b91587cae1855ea171f23c0b6d1c0cg83f2a432a553b6174
app.base.url=http://localhost:8080/
app.base.front.url=http://localhost:5173/
app.api.prefix=api/v1

spring.data.mongodb.uri=mongodb://localhost:27017
spring.data.mongodb.database=dbname

spring.jpa.hibernate.ddl-auto=update
spring.data.mongodb.auto-index-creation=true

spring.profiles.active=dev

spring.security.oauth2.client.registration.github.client-id=
spring.security.oauth2.client.registration.github.client-secret=
spring.security.oauth2.client.registration.github.scope=user:email,read:user
spring.security.oauth2.client.registration.github.client-name=GitHub

#spring.security.oauth2.client.provider.github.authorization-uri=https://github.com/login/oauth/authorize
#spring.security.oauth2.client.provider.github.token-uri=https://github.com/login/oauth/access_token
#spring.security.oauth2.client.provider.github.user-info-uri=https://api.github.com/user

spring.security.oauth2.client.registration.google.client-id=
spring.security.oauth2.client.registration.google.client-secret=
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.client-name=Google

spring.security.oauth2.client.registration.linkedin.client-id=
spring.security.oauth2.client.registration.linkedin.client-secret=
spring.security.oauth2.client.registration.linkedin.scope=profile,email
spring.security.oauth2.client.registration.linkedin.client-name=LinkedIn
spring.security.oauth2.client.registration.linkedin.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.linkedin.redirect-uri=http://localhost:8080/login/oauth2/code/linkedin

spring.security.oauth2.client.provider.linkedin.authorization-uri=https://www.linkedin.com/oauth/v2/authorization
spring.security.oauth2.client.provider.linkedin.token-uri=https://www.linkedin.com/oauth/v2/accessToken
spring.security.oauth2.client.provider.linkedin.user-info-uri=https://api.linkedin.com/v2/me
spring.security.oauth2.client.provider.linkedin.user-name-attribute=id


```
Annotate your main class with `@SpringBootApplication` and `@EnableMongoRepositories` annotation.

```java
@SpringBootApplication
@EnableMongoRepositories(basePackages = {"com.ebenjs"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
3. Run the project

    Now you can run the project by executing the main class.


4. Open your browser and navigate to `http://localhost:8080`. All endpoints are prefixed with `api/v1`.
You can customize anythig you want by editing the source code directly as you want.
You have access to :
- Controllers
- Services
- Repositories
- Models
- Entities
- Enums
- SecurityConfigurations
- ... and more

Note that by default the persistence layer is MongoDB. You can change it to any other database of your choice.
If you have any request or suggestion, feel free to open an issue or a pull request or contact me directly at `nikaboue10@gmail.com`.