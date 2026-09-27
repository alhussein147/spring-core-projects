---
tags: [java, spring, spring-core, annotations, teaching, documentation]
aliases: [Spring Annotations, Spring Core Annotations]
---

# Spring Core Annotations

> [!NOTE]
> This document assumes everything from [[Spring Core]] and [[Bean Definition — All XML Attributes & Sub-Elements|Bean Definition — All XML Attributes]]. Every annotation below is shown **side-by-side with the XML it replaces**, since that's the fastest way to understand what it actually does.

---

## Table of Contents

1. [[#1. Why Annotations Exist]]
2. [[#2. Stereotype Annotations]]
3. [[#3. Enabling Component Scanning]]
4. [[#4. Java-Based Configuration]]
5. [[#5. Dependency Injection Annotations]]
6. [[#6. Scope & Laziness]]
7. [[#7. Lifecycle Annotations]]
8. [[#8. Conditional & Environment-Based Annotations]]
9. [[#9. Ordering & Miscellaneous Annotations]]
10. [[#10. Full Comparison Table — Annotation vs. XML]]
11. [[#Summary]]
12. [[#Key Takeaways]]
13. [[#Common Interview Questions]]

---

## 1. Why Annotations Exist

Every XML concept you've learned so far — `<bean>`, `<property>`, `<constructor-arg>`, `scope`, `depends-on`, `init-method` — has an annotation-based equivalent. Annotations don't introduce new capabilities; they let you express the **same configuration directly on the Java class**, instead of in a separate XML file.

```xml
<!-- XML: the bean's identity and dependencies live OUTSIDE the class -->
<bean id="orderService" class="com.example.OrderService">
    <constructor-arg ref="paymentService" />
</bean>
```

```java
// Annotations: the same information lives INSIDE the class
@Service
public class OrderService {
    private final PaymentService paymentService;

    @Autowired
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

> [!NOTE]
> Neither approach is "more powerful" — they configure the exact same IoC container. Many real projects mix both, though most modern Spring code favors annotations for day-to-day beans and reserves XML (or Java `@Configuration` classes) for third-party beans that can't be annotated directly.

---

## 2. Stereotype Annotations

Stereotype annotations mark a class as **a candidate for automatic bean registration** via [[#3. Enabling Component Scanning|component scanning]] — instead of writing a `<bean class="...">` entry yourself.

### 2.1 `@Component`

The generic, base stereotype. Any class annotated with it becomes a Spring-managed bean.

```java
@Component
public class EmailNotifier {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (auto-generated) | The bean's `id`/name. If omitted, Spring generates one from the class name (`emailNotifier`, lowercase first letter). |

```java
@Component("primaryNotifier") // equivalent to id="primaryNotifier" in XML
public class EmailNotifier {
}
```

**XML equivalent:**
```xml
<bean id="emailNotifier" class="com.example.EmailNotifier" />
```

### 2.2 `@Service`

A specialization of `@Component`, used to mark **business/service-layer** classes. Functionally identical to `@Component` — the different name exists purely for **readability and intent**, so a developer scanning the codebase immediately knows this class holds business logic.

```java
@Service
public class OrderService {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (auto-generated) | Same as `@Component` |

### 2.3 `@Repository`

A specialization of `@Component` for **data-access-layer** classes. Unlike `@Service`, this one has a real functional difference: Spring automatically wraps methods on `@Repository` beans with **exception translation** — converting database-specific exceptions into Spring's unified `DataAccessException` hierarchy.

```java
@Repository
public class OrderRepository {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (auto-generated) | Same as `@Component` |

### 2.4 `@Controller`

A specialization of `@Component` for **Spring MVC web controllers** — classes that handle incoming HTTP requests and typically return a **view name** to render.

```java
@Controller
public class OrderPageController {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (auto-generated) | Same as `@Component` |

### 2.5 `@RestController`

A combination of `@Controller` **and** `@ResponseBody` — every method's return value is written directly into the HTTP response body (typically as JSON), instead of being resolved as a view name.

```java
@RestController
public class OrderApiController {
    // methods here return data (e.g. a JSON object), not a view name
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (auto-generated) | Same as `@Component` |

### 2.6 Stereotype Comparison

| Annotation | Layer | Extra Behavior Beyond `@Component`? |
|---|---|---|
| `@Component` | Generic | None |
| `@Service` | Business/service | None — naming only |
| `@Repository` | Data access | ✅ Automatic exception translation |
| `@Controller` | Web (view-returning) | Enables MVC request-handling behavior |
| `@RestController` | Web (data-returning) | `@Controller` + `@ResponseBody` combined |

---

## 3. Enabling Component Scanning

Stereotype annotations alone don't do anything — Spring needs to be **told where to look** for them.

### 3.1 `@ComponentScan`

Placed on a `@Configuration` class, tells Spring which package(s) to scan for stereotype-annotated classes.

```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` / `basePackages` | `String[]` | `{}` (current package) | Package(s) to scan |
| `basePackageClasses` | `Class<?>[]` | `{}` | Type-safe alternative — scans the packages containing these classes |
| `includeFilters` | `Filter[]` | `{}` | Additional classes to include, beyond the default stereotype annotations |
| `excludeFilters` | `Filter[]` | `{}` | Classes to exclude from scanning |
| `lazyInit` | `boolean` | `false` | Whether beans found by this scan should default to lazy initialization |

**XML equivalent:**
```xml
<context:component-scan base-package="com.example" />
```

---

## 4. Java-Based Configuration

### 4.1 `@Configuration`

Marks a class as a **source of bean definitions** — the annotation-based replacement for an entire `applicationContext.xml` file.

```java
@Configuration
public class AppConfig {
    // @Bean methods go here
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (auto-generated) | The bean name for the configuration class itself (rarely used) |
| `proxyBeanMethods` | `boolean` | `true` | If `true`, calling one `@Bean` method from another within the same class returns the **same singleton instance** (CGLIB proxying). If `false`, each call creates a plain method invocation — faster, but breaks inter-bean singleton references. |

**XML equivalent:** the XML file itself (`applicationContext.xml`).

### 4.2 `@Bean`

Placed on a method **inside** an `@Configuration` class — marks that method's return value as a Spring-managed bean, identified by the method name (by default).

```java
@Configuration
public class AppConfig {

    @Bean
    public PaymentService paymentService() {
        return new CreditCardPaymentService();
    }
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` / `name` | `String[]` | `{}` (method name) | The bean's `id`/name (can supply multiple aliases) |
| `initMethod` | `String` | `""` | Method to call after construction — equivalent to XML's `init-method` |
| `destroyMethod` | `String` | `"(inferred)"` | Method to call on shutdown — equivalent to XML's `destroy-method`; by default, Spring tries to auto-detect a `close()`/`shutdown()` method |
| `autowireCandidate` | `boolean` | `true` | Equivalent to XML's `autowire-candidate` |

```java
@Bean(name = "paySvc", initMethod = "init", destroyMethod = "cleanup")
public PaymentService paymentService() {
    return new CreditCardPaymentService();
}
```

**XML equivalent:**
```xml
<bean id="paySvc" class="com.example.CreditCardPaymentService"
      init-method="init" destroy-method="cleanup" />
```

---

## 5. Dependency Injection Annotations

### 5.1 `@Autowired`

Tells Spring to **automatically inject** a matching bean — can be placed on a constructor, a setter method, or directly on a field.

```java
// Constructor injection (recommended)
@Service
public class OrderService {
    private final PaymentService paymentService;

    @Autowired // optional on a single constructor in modern Spring, shown for clarity
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

```java
// Setter injection
@Autowired
public void setPaymentService(PaymentService paymentService) {
    this.paymentService = paymentService;
}
```

```java
// Field injection
@Autowired
private PaymentService paymentService;
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `required` | `boolean` | `true` | If `true`, Spring throws an exception when no matching bean is found. If `false`, the dependency is left `null` when nothing matches. |

```java
@Autowired(required = false)
private ReportingService optionalReportingService;
```

**XML equivalent:** `autowire="byType"` / `autowire="constructor"`, or explicit `<constructor-arg ref="..."/>` / `<property ref="..."/>`.

### 5.2 `@Qualifier`

Resolves ambiguity when `@Autowired` finds **more than one** matching bean by type — specifies exactly which one to use, by name.

```java
public interface PaymentService { void pay(double amount); }

@Service("creditCardPayment")
public class CreditCardPaymentService implements PaymentService { }

@Service("payPalPayment")
public class PayPalPaymentService implements PaymentService { }

@Service
public class OrderService {
    private final PaymentService paymentService;

    @Autowired
    public OrderService(@Qualifier("creditCardPayment") PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | (required) | The specific bean name to inject |

**XML equivalent:** `<qualifier value="..."/>`, or simply an explicit `ref="creditCardPayment"`.

### 5.3 `@Primary`

Marks one bean as the **default choice** among multiple candidates of the same type, avoiding the need for `@Qualifier` at every injection point.

```java
@Service
@Primary
public class CreditCardPaymentService implements PaymentService { }

@Service
public class PayPalPaymentService implements PaymentService { }
```

- No parameters — it's a marker annotation.

**XML equivalent:** `primary="true"`.

### 5.4 `@Value`

Injects a **literal value** — a hardcoded constant, a Spring Expression Language (SpEL) expression, or a value pulled from a properties file via `${...}` placeholder syntax.

```java
@Component
public class AppInfo {

    @Value("MyApplication")
    private String appName;

    @Value("${server.port}") // reads from a properties file / environment
    private int port;

    @Value("#{2 * 10}") // SpEL expression
    private int computedValue;
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | (required) | A literal string, a `${...}` property placeholder, or a `#{...}` SpEL expression |

**XML equivalent:** `<property value="..."/>` / `<constructor-arg value="..."/>`.

### 5.5 `@Resource`

A JSR-250 standard alternative to `@Autowired`. The key behavioral difference: `@Resource` resolves **by name first**, falling back to type only if no name match is found — the reverse priority of `@Autowired` + `@Qualifier`.

```java
@Resource(name = "creditCardPayment")
private PaymentService paymentService;
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `name` | `String` | `""` (defaults to field/property name) | The bean name to resolve by |
| `type` | `Class<?>` | `Object.class` (unset) | Optionally restrict by type as well |

**XML equivalent:** `autowire="byName"`.

### 5.6 `@Inject`

The JSR-330 standard equivalent of `@Autowired` (requires the `jakarta.inject`/`javax.inject` dependency). Functionally very similar, with one notable difference: `@Inject` has **no `required` parameter** — a dependency it can't resolve always throws an exception.

```java
@Inject
private PaymentService paymentService;
```

- No parameters.

**XML equivalent:** same as `@Autowired`.

### 5.7 DI Annotation Comparison

| Annotation | Standard | Resolves By | Has `required`? | Typical Use |
|---|---|---|---|---|
| `@Autowired` | Spring-specific | Type (then `@Qualifier` for name) | ✅ Yes | Default choice in Spring code |
| `@Qualifier` | Spring-specific | Name (used alongside `@Autowired`) | N/A | Disambiguating multiple beans of one type |
| `@Resource` | JSR-250 | Name (then type) | ❌ No | When name-first resolution is wanted, or for portability |
| `@Inject` | JSR-330 | Type (then `@Qualifier` from `javax.inject`) | ❌ No | Framework-agnostic code (not Spring-specific) |

---

## 6. Scope & Laziness

### 6.1 `@Scope`

Sets a bean's scope — the annotation equivalent of the `scope` attribute.

```java
@Component
@Scope("prototype")
public class ShoppingCart {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` / `scopeName` | `String` | `"singleton"` | `"singleton"`, `"prototype"`, `"request"`, `"session"`, `"application"`, `"websocket"`, or a custom scope name |
| `proxyMode` | `ScopedProxyMode` | `DEFAULT` | Controls whether a proxy is created for injecting a shorter-lived-scoped bean into a longer-lived one (e.g., a `request`-scoped bean into a `singleton`) — options: `NO`, `INTERFACES`, `TARGET_CLASS`, `DEFAULT` |

**XML equivalent:** `scope="..."`.

### 6.2 `@Lazy`

Delays a bean's creation until first use.

```java
@Component
@Lazy
public class ReportGenerator {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `boolean` | `true` | `true` = lazy, `false` = eager (useful for explicitly overriding a class-level `@Lazy` at an injection point) |

**XML equivalent:** `lazy-init="true"`.

---

## 7. Lifecycle Annotations

### 7.1 `@PostConstruct`

A JSR-250 annotation placed on a method that Spring should call **right after construction and dependency injection** are complete.

```java
@Component
public class ConnectionPool {

    @PostConstruct
    public void initialize() {
        System.out.println("Connection pool initialized");
    }
}
```

- No parameters. The method must take no arguments.

**XML equivalent:** `init-method="..."`.

### 7.2 `@PreDestroy`

A JSR-250 annotation placed on a method Spring should call when the container is **shutting down** (singleton beans only).

```java
@Component
public class ConnectionPool {

    @PreDestroy
    public void shutdown() {
        System.out.println("Connection pool shut down");
    }
}
```

- No parameters. The method must take no arguments.

**XML equivalent:** `destroy-method="..."`.

---

## 8. Conditional & Environment-Based Annotations

### 8.1 `@Profile`

Registers a bean **only if** a specific environment profile is currently active (e.g., `"dev"`, `"test"`, `"prod"`).

```java
@Service
@Profile("dev")
public class MockPaymentService implements PaymentService {
}

@Service
@Profile("prod")
public class RealPaymentService implements PaymentService {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String[]` | (required) | Profile name(s) this bean should be active for. Can prefix with `!` to mean "NOT this profile" (e.g., `"!prod"`) |

**XML equivalent:**
```xml
<beans profile="dev">
    <bean id="mockPaymentService" class="com.example.MockPaymentService" />
</beans>
```

### 8.2 `@Conditional`

A more general-purpose version of `@Profile` — registers a bean only if a custom `Condition` implementation evaluates to `true`.

```java
@Service
@Conditional(OnWindowsCondition.class)
public class WindowsSpecificService {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `Class<? extends Condition>[]` | (required) | One or more `Condition` implementations to evaluate |

- No direct XML equivalent — this is annotation-only functionality.

### 8.3 `@PropertySource`

Loads a `.properties` (or `.yml`, with extra configuration) file into Spring's `Environment`, so its values become accessible via `@Value("${...}")`.

```java
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String[]` | (required) | Location(s) of the properties file(s) |
| `name` | `String` | `""` (auto-generated) | A name for the resulting property source |
| `ignoreResourceNotFound` | `boolean` | `false` | If `true`, doesn't fail if the file is missing |
| `encoding` | `String` | `""` (platform default) | Character encoding of the file |

**XML equivalent:**
```xml
<context:property-placeholder location="classpath:application.properties" />
```

---

## 9. Ordering & Miscellaneous Annotations

### 9.1 `@DependsOn`

Forces one or more other beans to be created first, even without a direct dependency relationship.

```java
@Component
@DependsOn("databaseInitializer")
public class OrderRepository {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String[]` | (required) | Bean name(s) that must be created first |

**XML equivalent:** `depends-on="..."`.

### 9.2 `@Lookup`

Implements Lookup Method Injection — the annotation equivalent of XML's `<lookup-method>`. Placed on an (often abstract) method that Spring overrides to return a fresh bean (typically `prototype`-scoped) on every call.

```java
@Component
public abstract class CommandManager {
    public Object process() {
        Command command = createCommand();
        return command.execute();
    }

    @Lookup("myCommand")
    protected abstract Command createCommand();
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `String` | `""` (resolved by return type if omitted) | The bean name to return on each call |

**XML equivalent:** `<lookup-method name="..." bean="..."/>`.

### 9.3 `@Import`

Placed on an `@Configuration` class, pulls in bean definitions from one or more other `@Configuration` classes.

```java
@Configuration
@Import({ DatabaseConfig.class, SecurityConfig.class })
public class AppConfig {
}
```

| Parameter | Type | Default | Meaning |
|---|---|---|---|
| `value` | `Class<?>[]` | (required) | The `@Configuration` class(es) to import |

**XML equivalent:**
```xml
<import resource="databaseConfig.xml" />
<import resource="securityConfig.xml" />
```

---

## 10. Full Comparison Table — Annotation vs. XML

| Annotation | Parameters | XML Equivalent |
|---|---|---|
| `@Component` | `value` | `<bean class="..."/>` |
| `@Service` | `value` | `<bean class="..."/>` (semantic label only) |
| `@Repository` | `value` | `<bean class="..."/>` + auto exception translation |
| `@Controller` | `value` | `<bean class="..."/>` (MVC-aware) |
| `@RestController` | `value` | `@Controller` + `@ResponseBody` |
| `@ComponentScan` | `basePackages`, `basePackageClasses`, `includeFilters`, `excludeFilters`, `lazyInit` | `<context:component-scan base-package="..."/>` |
| `@Configuration` | `value`, `proxyBeanMethods` | An entire XML config file |
| `@Bean` | `name`, `initMethod`, `destroyMethod`, `autowireCandidate` | `<bean id="..." class="..." init-method="..." destroy-method="..."/>` |
| `@Autowired` | `required` | `autowire="byType"` / `<constructor-arg ref="..."/>` |
| `@Qualifier` | `value` | `<qualifier value="..."/>` / explicit `ref="..."` |
| `@Primary` | *(none)* | `primary="true"` |
| `@Value` | `value` | `<property value="..."/>` |
| `@Resource` | `name`, `type` | `autowire="byName"` |
| `@Inject` | *(none)* | Same as `@Autowired` |
| `@Scope` | `value`/`scopeName`, `proxyMode` | `scope="..."` |
| `@Lazy` | `value` | `lazy-init="true"` |
| `@PostConstruct` | *(none)* | `init-method="..."` |
| `@PreDestroy` | *(none)* | `destroy-method="..."` |
| `@Profile` | `value` | `<beans profile="...">` |
| `@Conditional` | `value` | No direct equivalent |
| `@PropertySource` | `value`, `name`, `ignoreResourceNotFound`, `encoding` | `<context:property-placeholder location="..."/>` |
| `@DependsOn` | `value` | `depends-on="..."` |
| `@Lookup` | `value` | `<lookup-method name="..." bean="..."/>` |
| `@Import` | `value` | `<import resource="..."/>` |

---

## Summary

- **Stereotype annotations** (`@Component`, `@Service`, `@Repository`, `@Controller`, `@RestController`) mark classes for automatic bean registration via component scanning
- **`@ComponentScan`** and **`@Configuration`**/**`@Bean`** are the annotation-based replacements for an entire XML configuration file
- **DI annotations** (`@Autowired`, `@Qualifier`, `@Primary`, `@Value`, `@Resource`, `@Inject`) replace `<constructor-arg>`/`<property>` wiring, `autowire="..."`, and `primary="..."`
- **`@Scope`** and **`@Lazy`** replace the `scope` and `lazy-init` attributes
- **`@PostConstruct`** and **`@PreDestroy`** replace `init-method`/`destroy-method`
- **`@Profile`**, **`@Conditional`**, and **`@PropertySource`** handle environment-specific and externalized configuration
- **`@DependsOn`**, **`@Lookup`**, and **`@Import`** replace their exact XML namesakes (`depends-on`, `<lookup-method>`, `<import>`)

---

## Key Takeaways

- Every annotation in this document has a direct, learnable XML equivalent — nothing here is a brand-new concept, only a new syntax for concepts already covered in [[Spring Core]] and [[Bean Definition — All XML Attributes & Sub-Elements]]
- `@Autowired`, `@Resource`, and `@Inject` all perform dependency injection but differ in **resolution priority** (type-first vs. name-first) and which standard they come from (Spring-specific vs. JSR-250 vs. JSR-330)
- `@Primary` and `@Qualifier` solve the same ambiguity problem from two different angles: `@Primary` sets a default, `@Qualifier` picks explicitly at the injection point
- `@Service` and `@Repository` behave identically to `@Component` except for one detail each: `@Repository` adds exception translation, `@Service`/`@Controller` are naming-only
- Most parameters across these annotations default to sensible values (auto-generated bean names, `required = true`, `singleton` scope), so a huge amount of real code uses these annotations with **zero parameters** at all

---

## Common Interview Questions

1. What is the difference between `@Component`, `@Service`, and `@Repository`? Is there any *functional* difference, or just naming?
2. What does `@ComponentScan` do, and what happens if you forget to include it (or point it at the wrong package)?
3. What is the difference between `@Autowired` and `@Resource` in terms of how they resolve a dependency?
4. How do `@Primary` and `@Qualifier` interact when both are present in the same application?
5. What does `@Value("${server.port}")` do, and where does that value actually come from?
6. What is the difference between `@PostConstruct`/`@PreDestroy` and simply writing that logic in a constructor?
7. What does `proxyBeanMethods` on `@Configuration` control, and what would break if it were set to `false` in a config class where one `@Bean` method calls another?
8. How would you register two different implementations of the same interface as beans, and control which one is injected by default, using only annotations?
9. What is the annotation equivalent of XML's `<lookup-method>`, and what problem does it solve?
10. Why does `@Repository` provide automatic exception translation but `@Service` and `@Controller` don't add any special behavior?

---

## Related Topics

- [[Spring Core]]
- [[Bean Definition — All XML Attributes & Sub-Elements]]
- [[Dependency Injection]]
- [[Inversion of Control]]
