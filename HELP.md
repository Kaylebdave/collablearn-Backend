# Getting Started

## Local Configuration

The base properties remain environment-based. For this workspace, local Atlas and Cloudinary settings are in the ignored root-level `application-local.properties`; it is imported when present and is not copied into the application artifact. For another checkout, create that file from the example, set its MongoDB URI, and use an SMTP username and password if OTP delivery is needed. Empty mail credentials do not prevent startup.

```powershell
Copy-Item src/main/resources/application-local.properties.example application-local.properties
.\mvnw.cmd spring-boot:run
```

For deployment or the default profile, provide these required environment variables:

- `MONGODB_URI`: MongoDB connection URI.
- `MAIL_USERNAME` and `MAIL_PASSWORD`: SMTP credentials (Brevo SMTP credentials for the hosted setup).
- `PORT`: optional; defaults to `8080`.
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET`: optional until Cloudinary uploads are used.

For local SMTP overrides, `MAIL_HOST`, `MAIL_PORT`, `MAIL_SMTP_AUTH`, and `MAIL_SMTP_STARTTLS` are also supported. This service sends through SMTP; a Brevo HTTP API key alone does not configure OTP delivery. Spring Boot does not load `.env` files automatically; export variables in the process environment or your hosting provider's settings. `.env` and `application-local.properties` are excluded from Git.

## Render Environment

Set `MONGODB_URI`, `MAIL_USERNAME`, and `MAIL_PASSWORD` in the Render service's environment settings. Render supplies `PORT`. Set `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, and `CLOUDINARY_API_SECRET` when enabling Cloudinary uploads. Use Brevo SMTP credentials for the mail variables, not the Brevo HTTP API key.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.1.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.1.1/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.1.1/reference/web/servlet.html)
* [Spring Security](https://docs.spring.io/spring-boot/4.1.1/reference/web/spring-security.html)
* [Validation](https://docs.spring.io/spring-boot/4.1.1/reference/io/validation.html)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/4.1.1/reference/data/nosql.html#data.nosql.mongodb)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

