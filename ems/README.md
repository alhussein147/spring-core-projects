# Spring Core Annotations — Runnable Demos (Plain Spring, No Spring Boot)

A single Maven project — **plain Spring Framework only** (`spring-context` + `spring-web`, no Spring Boot starter, no embedded server) — with one small, runnable, self-contained demo per annotation covered in `spring-core-annotations.md`.

## Requirements

- Java 17+
- [Maven](https://maven.apache.org/)
- Internet connection the first time you build (Maven downloads Spring + a couple of small JSR API jars)

## How to Run Any Demo

```bash
mvn compile exec:java -Dexec.mainClass="<fully-qualified-class-name>"
```

## Demo Index

| Annotation(s) | Class to Run |
|---|---|
| `@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`, `@ComponentScan`, `@Configuration` | `com.example.stereotypes.StereotypesDemo` |
| `@Bean` (name, initMethod, destroyMethod) | `com.example.beanconfig.BeanConfigDemo` |
| `@Autowired` (constructor, field, setter, `required=false`) | `com.example.autowired.AutowiredDemo` |
| `@Qualifier`, `@Primary` | `com.example.qualifierprimary.QualifierPrimaryDemo` |
| `@Value` (literal, `${...}`, SpEL, defaults), `@PropertySource` | `com.example.valueannotation.ValueDemo` |
| `@Resource`, `@Inject` | `com.example.resourceinject.ResourceInjectDemo` |
| `@Scope`, `@Lazy` | `com.example.scopelazy.ScopeLazyDemo` |
| `@PostConstruct`, `@PreDestroy` | `com.example.lifecycle.LifecycleDemo` |
| `@Profile` | `com.example.profile.ProfileDemo` |
| `@Conditional` | `com.example.conditional.ConditionalDemo` |
| `@DependsOn` | `com.example.dependson.DependsOnDemo` |
| `@Lookup` | `com.example.lookup.LookupDemo` |
| `@Import` | `com.example.importdemo.ImportDemo` |

### Example

```bash
mvn compile exec:java -Dexec.mainClass="com.example.scopelazy.ScopeLazyDemo"
```

Expected output:
```
Creating context...
>>> EagerBean CREATED (eagerly, at context startup)

--- @Scope("prototype") ---
Same instance? false

--- @Lazy ---
Requesting ReportGenerator now...
>>> ReportGenerator bean CREATED
Generating report...
```

## Notes on Specific Demos

- **`@RestController`/`@Controller`**: these demos only prove the bean registers correctly. There's no `DispatcherServlet` or embedded Tomcat here (that would require Spring Boot or a real servlet container per [[Servlets and Spring]]) — the methods are called directly in Java, not over HTTP.
- **`@Value` + `@PropertySource`**: in plain Spring (no Boot), you must explicitly declare a `PropertySourcesPlaceholderConfigurer` `@Bean` for `${...}` placeholders to resolve — Spring Boot does this for you automatically, which is why this step is easy to forget when moving between the two.
- **`@Profile`**: since there's no Spring Boot `application.properties: spring.profiles.active=...` shortcut, profiles are activated manually on the context's `Environment` before calling `.refresh()` — see `ProfileDemo` for the exact pattern.
- **`@Lookup`**: requires CGLIB (included as a transitive dependency of `spring-context`, but declared explicitly is not necessary here) to generate a subclass overriding the abstract method at runtime.
- **`constructor-arg name="..."` style name matching** isn't used here (that was the XML demos project) — irrelevant to this annotation-only project.

## Project Structure

```
spring-annotations-demos/
  pom.xml
  spring-core-annotations.md   <- the full reference doc, bundled for convenience
  src/main/java/com/example/
      stereotypes/     -> @Component, @Service, @Repository, @Controller, @RestController, @ComponentScan, @Configuration
      beanconfig/      -> @Bean
      autowired/       -> @Autowired
      qualifierprimary/-> @Qualifier, @Primary
      valueannotation/ -> @Value, @PropertySource
      resourceinject/  -> @Resource, @Inject
      scopelazy/       -> @Scope, @Lazy
      lifecycle/       -> @PostConstruct, @PreDestroy
      profile/         -> @Profile
      conditional/     -> @Conditional
      dependson/       -> @DependsOn
      lookup/          -> @Lookup
      importdemo/      -> @Import
  src/main/resources/
      application.properties   -> used by the @Value / @PropertySource demo
```

Each package is fully independent — no shared classes between demos — so you can open, run, and modify any single one without needing the others.

## Employee Management Demo

The root `com.example.MainApp` is a small employee-management demonstration that can be run with:

```bash
mvn compile exec:java -Dexec.mainClass="com.example.MainApp"
```

It activates the `dev` profile, validates employees before they are saved or raised, sends all three notifications, enforces `raise.max-percentage` from `application.properties`, and closes the application context so singleton lifecycle callbacks are visible.

`AuditLogger` is a prototype-scoped bean. `EmployeeServiceImpl` is a singleton, so it receives an `ObjectProvider<AuditLogger>` instead of an `AuditLogger` directly. Calling `getObject()` inside each service operation asks Spring for a new logger at that moment; direct injection would resolve one logger during singleton construction and accidentally reuse it for the application's lifetime.
