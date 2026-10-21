# Movie Night API

A small Spring Boot REST API for a movie night. It manages two things: **Movies** and **Snacks**. Use it as a reference for how the layers of a Spring Boot app fit together.

## How a request flows

```
Client (Postman / browser)
   │  HTTP request
   ▼
Controller   → receives the request, returns JSON
   │
   ▼
Service      → business logic and validation
   │
   ▼
Repository   → talks to the database
   │
   ▼
MySQL
```

Each layer only talks to the one directly below it. The controller never touches the repository, and the repository never knows about HTTP.

## Project structure

```
src/main/java/com/example/movienight
├── MovieNightApplication.java      ← entry point (@SpringBootApplication)
├── movie/
│   ├── Movie.java                  ← Entity
│   ├── MovieRepository.java        ← Repository
│   ├── MovieService.java           ← Service
│   └── MovieController.java        ← Controller
└── snack/
    ├── Snack.java
    ├── SnackRepository.java
    ├── SnackService.java
    └── SnackController.java

src/main/resources
├── application.properties          ← DB connection + JPA settings
└── data.sql                        ← sample movies and snacks
```

All classes must live in `com.example.movienight` or a package below it. `@SpringBootApplication` only scans from its own package down, so a class outside it is never found.

## The four layers

| Layer | Annotation | Its job |
| --- | --- | --- |
| Entity | `@Entity` | A Java class that maps to a database table. Each field is a column. |
| Repository | (interface extending `JpaRepository`) | Database access. Spring writes the implementation for you. |
| Service | `@Service` | Business rules, e.g. a movie rating must be between 0 and 10. |
| Controller | `@RestController` | Maps URLs and HTTP methods to Java methods. Returns objects as JSON. |

A few things worth noticing in the code:

- **Free CRUD.** `JpaRepository<Movie, Long>` gives you `save`, `findAll`, `findById`, `deleteById` and `count` with no code.
- **Derived queries.** Spring builds SQL from a method name: `findByGenreIgnoreCase(String genre)` becomes `WHERE LOWER(genre) = LOWER(?)`.

## Running it

### Prerequisites

- Java 25
- Maven (or use the wrapper `./mvnw`)
- MySQL running locally on port 3306

### 1. Configure the database

Open `src/main/resources/application.properties` and set your own MySQL username and password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/movienight?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 2. Run the app

From your IDE, run `MovieNightApplication`, or from a terminal:

```bash
./mvnw spring-boot:run
```

The API is at `http://localhost:8080`.

## Endpoints

### Movies: `/api/movies`

| Method | URL | Body | Result |
| --- | --- | --- | --- |
| GET | `/api/movies` | none | All movies |
| GET | `/api/movies?genre=Comedy` | none | Movies in that genre |
| GET | `/api/movies/{id}` | none | One movie, or 404 |
| POST | `/api/movies` | Movie JSON | 201, the saved movie with its new id |

Example body:

```json
{
  "title": "Barbie",
  "genre": "Comedy",
  "releaseYear": 2023,
  "rating": 6.8
}
```

### Snacks: `/api/snacks`

| Method | URL | Body | Result |
| --- | --- | --- | --- |
| GET | `/api/snacks` | none | All snacks |
| GET | `/api/snacks?maxPrice=5` | none | Snacks at or under that price |
| GET | `/api/snacks/{id}` | none | One snack, or 404 |
| POST | `/api/snacks` | Snack JSON | 201, the saved snack |
| PUT | `/api/snacks/{id}` | Snack JSON | The updated snack |
| DELETE | `/api/snacks/{id}` | none | 204, no content |

Example body:

```json
{
  "name": "Buttered Popcorn",
  "type": "Salty",
  "price": 8.50,
  "shareable": true
}
```

JSON keys match the **Java field names** (`releaseYear`), not the database column names (`release_year`).

## Troubleshooting

| Problem | Likely cause |
| --- | --- |
| `Access denied for user` | Wrong username or password in `application.properties`. |
| `Communications link failure` | MySQL isn't running, or it's on a different port. |
| `Unknown database` | Check the URL for a typo, or keep `?createDatabaseIfNotExist=true`. |
| 404 on every endpoint | A class is outside the `com.example.movienight` package. |
| Response is `{}` | The entity is missing getters. Jackson reads fields through getters. |
| `No default constructor for entity` | Add an empty constructor to the entity. |
| `data.sql` didn't load | Check `spring.sql.init.mode=always` and `spring.jpa.defer-datasource-initialization=true`. |
| Port 8080 already in use | Stop the other running app, or set `server.port=8081`. |

## Try it yourself

Once it runs, extend it. Each of these touches more than one layer.

1. `GET /api/movies?minRating=8` returns only movies rated 8 or higher.
2. Creating a snack with a blank name returns `400 Bad Request`.
3. A movie has a `runtimeMinutes` field that shows up in the JSON and the database.
4. `GET /api/snacks/sweet` returns only sweet snacks.
5. A third entity of your choice (a `Friend` who's coming to movie night?) with full CRUD.


---
---
# Spring Boot Movie API: Snack CRUD Reference

Prepared October 21, 2026. This guide documents the snack feature in `Movie_Api_Example_Fall_2026` and provides a repeatable workflow for future APIs.

## 1. Evidence and confidence

**Verified against local files:** the entity, repository, service, controller, service tests, Maven configuration, main application, configuration property names, and existing Surefire report were inspected. The source project was located under the user's OneDrive Documents course folder. The source project was not modified.

**Verified test evidence:** the existing report `target/surefire-reports/com.movieapi.snack.snackServiceTest.txt` states:

```text
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

This is a saved test result, not a new test run performed while writing this guide. It proves the recorded service test run passed; it does not prove current database connectivity, controller behavior, or every feature in the Movie API.

**Conversation evidence:** the accessible conversation contains screenshots rather than a complete textual implementation history. They show an empty JSON list from a localhost snack GET request, a snack implementation commit, an initial GitHub DNS failure, and a later successful push. The retrieved conversation exposes no older-page cursor. Historical debugging details that cannot be reconstructed are presented as reusable lessons, not invented quotations or a precise timeline.

**Check before reusing:** endpoint paths below are confirmed in the inspected controller, but must be rechecked after changes. Database name, credentials, deployed host/port, physical table naming, current Git state, and current remote configuration require checking in the actual environment. Secret values are intentionally omitted. Git metadata could not be inspected from this session; screenshot evidence establishes the past push only.

## 2. Objective and completed feature

The Movie API is a Spring Boot application with movie and snack packages. The snack feature stores a snack's name, type, and price in MySQL and exposes HTTP operations to:

- Create a snack.
- List all snacks and retrieve one by ID.
- Search for snacks priced at or below a supplied amount.
- Update an existing snack.
- Delete a snack if it exists.

The implementation includes a JPA entity, a Spring Data repository, a service, a REST controller, and 11 isolated service tests. No relationship between movies and snacks is declared in the inspected snack entity.

## 3. Architecture and package structure

```text
HTTP client / PowerShell
       -> snackController: routes and HTTP responses
       -> snackService: operations and update/delete decisions
       -> snackRepository: persistence interface
       -> JPA/Hibernate -> MySQL
```

The entity describes persisted data and is passed through the current layers. It is not a separate request/response DTO.

```text
Movie_Api_Example_Fall_2026/
├── pom.xml
├── .gitignore
├── src/main/java/com/movieapi/
│   ├── MovieApp.java
│   ├── movie/
│   │   ├── Movie.java
│   │   ├── MovieRepository.java
│   │   ├── MovieService.java
│   │   └── MovieController.java
│   └── snack/
│       ├── snack.java
│       ├── snackRepository.java
│       ├── snackService.java
│       └── snackController.java
├── src/main/resources/application.properties
├── src/test/java/com/movieapi/snack/snackServiceTest.java
└── target/                         # generated; ignored by Git
    └── surefire-reports/
```

The actual snack class names begin with lowercase letters. Java permits this, but future projects should use `Snack`, `SnackRepository`, `SnackService`, and `SnackController`. Rename classes, files, constructors, imports, and tests together. The screenshot history displays `Snack.java`, while the inspected local file is `snack.java`; do not assume the historical filename casing equals the current checkout. Windows can obscure case-only rename problems.

`MovieApp` is in `com.movieapi` and has `@SpringBootApplication`. Keeping components in subpackages lets default component scanning discover them. A future module outside this package tree may require explicit scan configuration.

## 4. Entity and persistence model

The current entity is a mutable JPA class, not a Java record:

```java
@Entity
public class snack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snackId;
    private String name;
    private String type;
    private Double price;

    public snack() {}
    // Constructor for name/type/price, plus getters and setters.
}
```

| Member | Purpose |
|---|---|
| `snackId: Long` | Database-generated identifier; wrapper type allows an unset ID. |
| `name: String` | Snack name, such as Popcorn. |
| `type: String` | Category, such as Food or Drink. |
| `price: Double` | Current price representation. |

`@Entity` marks the class for persistence. `@Id` identifies the primary key. `IDENTITY` delegates ID generation to the database identity/auto-increment mechanism. The no-argument constructor supports entity creation by the persistence provider. These imports are from `jakarta.persistence`, matching this project's Spring generation.

There is no explicit `@Table` or `@Column` in the inspected entity. **Check actual schema/table/column names** rather than assuming that `snackId` appears unchanged in MySQL. There are no bean validation annotations, declared relationships, or custom uniqueness constraints in this entity.

For future projects, consider `BigDecimal` and an explicit database decimal precision/scale for money. This would be a coordinated API/schema/test change, not a description of the current code. A Java record can be useful as a request/response DTO; do not simply replace this mutable entity with a record.

## 5. Repository and derived queries

The repository extends `JpaRepository<snack, Long>`:

```java
@Repository
public interface snackRepository extends JpaRepository<snack, Long> {
    Iterable<snack> findSnackByPriceLessThanEqual(Double price);
}
```

The two generic parameters are the entity type and identifier type. The repository inherits operations such as `save`, `findById`, `findAll`, `existsById`, `deleteById`, and `count`. `JpaRepository` supplies JPA-oriented capabilities in addition to basic CRUD; extending both it and `CrudRepository` is unnecessary. The existing `CrudRepository` import is unused.

Spring Data interprets `findSnackByPriceLessThanEqual` as a query on the entity's `price` property with an inclusive upper bound. Conceptually:

```sql
-- Explanation only; physical names are environment-dependent.
SELECT * FROM snack WHERE price <= ?;
```

The `5.00` mentioned in source comments is an example, not a hardcoded limit. `/snacks/searchByPrice/8.50` passes 8.50 to the query. Method names reference Java properties, not arbitrary SQL column names. Misspelling a property can prevent repository initialization at startup. See the official [query method documentation](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods-details.html) and [operator mapping](https://docs.spring.io/spring-data/jpa/reference/3.5/jpa/query-methods.html).

## 6. Service behavior

`@Service` makes `snackService` a Spring component. The current code uses field injection with `@Autowired`; constructor injection is a useful future improvement for explicit dependencies.

| Service method | Current behavior |
|---|---|
| `saveSnack(snack)` | Calls `save` and returns `Snack saved successfully`. |
| `getSnackById(Long)` | Returns the repository's `Optional<snack>`. |
| `getAllSnacks()` | Delegates to `findAll`. |
| `findByPrice(Double)` | Delegates to the inclusive maximum-price query. |
| `deleteSnack(Long)` | Calls `deleteById` only when `existsById` is true. |
| `updateSnack(Long, snack)` | Finds the existing entity, copies name/type/price, saves it, and returns the saved entity. |

The critical update sequence is:

```java
snack existingSnack = snackRepository.findById(id)
    .orElseThrow(() -> new RuntimeException("Snack not found: " + id));
existingSnack.setName(updatedSnack.getName());
existingSnack.setType(updatedSnack.getType());
existingSnack.setPrice(updatedSnack.getPrice());
return snackRepository.save(existingSnack);
```

This preserves the existing ID and makes persistence explicit. Returning the submitted object without saving is not equivalent. The implementation replaces all three editable fields; it is not a partial PATCH. Missing JSON fields can become null and overwrite existing values because validation is not configured.

There is no explicit transaction annotation in this service. A future transactional service boundary can group read/update work, but should be designed and tested deliberately. The separate existence-check/delete sequence also has a race window if another request deletes the row between calls.

## 7. Actual HTTP contract

The controller uses `@RestController`, `@CrossOrigin`, and `@RequestMapping("/snacks")`. `@RequestBody` reads JSON and `@PathVariable` binds URL values. `ResponseEntity.ok(...)` explicitly returns HTTP 200 on each successful operation.

**Confirmed paths at inspection time; check `snackController.java` after any refactor.** `http://localhost:8080` is a local example supported by the screenshot, not a confirmed current running server.

| Method | Path | Body | Successful response |
|---|---|---|---|
| POST | `/snacks` | name, type, price JSON | 200; text `Snack saved successfully` |
| GET | `/snacks/getAllSnacks` | None | 200; JSON array, possibly `[]` |
| GET | `/snacks/{snackId}` | None | 200; snack JSON |
| GET | `/snacks/searchByPrice/{price}` | None | 200; JSON array with `price <=` supplied limit |
| PUT | `/snacks/{id}` | name, type, price JSON | 200; updated snack JSON |
| DELETE | `/snacks/{id}` | None | 200; text `Snack deleted successfully` |

Important limitations:

- GET by a missing ID and PUT by a missing ID throw plain `RuntimeException`. There is no custom 404 mapping in the inspected snack code; under ordinary default handling these become server errors. Check global handlers before relying on an exact error body/status.
- DELETE returns the same success message when the ID is absent, because the service quietly skips deletion.
- POST returns neither the created entity nor its ID and does not explicitly return 201.
- GET `/snacks` is not the list route in this implementation. Use `/snacks/getAllSnacks`.
- The snack controller does not declare authentication. `@CrossOrigin` is a browser cross-origin policy annotation, not access control. Check application-wide security before deployment.
- Restrict allowed browser origins and introduce validated DTOs and consistent errors in a future hardened version.

## 8. MySQL setup and safe configuration

Install and start MySQL, then create a development database and a dedicated application account. The following is a **template**, not the project's verified database name or credentials. Replace the password marker privately before execution; never commit the completed SQL.

```sql
CREATE DATABASE movie_api_dev CHARACTER SET utf8mb4;
CREATE USER 'movie_api_app'@'localhost' IDENTIFIED BY '<GENERATE_A_PRIVATE_PASSWORD>';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
ON movie_api_dev.* TO 'movie_api_app'@'localhost';
```

Those grants support ordinary CRUD and common development schema updates; a particular migration may require additional privileges. Use a separately managed migration account and narrower runtime grants in production. The account host must match how the application connects.

The inspected configuration has datasource URL/username/password properties, MySQL driver configuration, `ddl-auto=update`, `show-sql=true`, and a bean-definition-overriding property. Actual connection values are omitted. Do not copy local credentials into documentation.

A safe, reusable tracked `application.properties` template is:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Development convenience only; use reviewed migrations in production.
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:validate}
spring.jpa.show-sql=${SHOW_SQL:false}
server.port=${PORT:8080}
```

Spring Boot supports environment variables and placeholders for external configuration; see [Externalized Configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html). The example defaults to schema validation. For an empty development database, explicitly select `update` or run migrations first. `validate` does not create tables, and `update` is not a production migration strategy.

Set values in the same PowerShell session that launches Maven:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/movie_api_dev'
$env:DB_USERNAME = 'movie_api_app'
$secret = Read-Host 'Database password' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $secret).Password
$env:DDL_AUTO = 'update'     # local development only
$env:SHOW_SQL = 'false'
```

This keeps the typed password out of literal command history, although the application process still needs access to it. Do not print environment variables or include them in screenshots. IDE-launched applications need the same variables configured in their run settings. A `.env` file is not automatically loaded by this example.

After stopping the app, clear the password from this shell if appropriate:

```powershell
Remove-Item Env:DB_PASSWORD
Remove-Variable secret
```

If a real credential was previously committed, rotate it. Replacing or ignoring the current file does not remove secrets from old commits. Keep a secret-free template tracked; if using private override files, ignore those specific files and ensure the application actually loads them. Git ignore rules do not untrack already tracked files.


Both run MySQL local database on port :3306 while Postman API test endpoint on port :8080
If your application.properties contains:
spring.datasource.password=${DB_PASSWORD}

Run:
```powershell
if ($env:DB_PASSWORD) {
    "Password variable is configured"
} else {
    "Password variable is missing"
}
```

If it is missing, set it again:
```powershell
$secure = Read-Host "MySQL password" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $secure).Password
```

The password must be configured in the same PowerShell session where you launch Spring Boot.

## 9. Maven, Java, and Spring Boot startup

The inspected `pom.xml` declares:

| Item | Inspected value |
|---|---|
| Coordinates | `com.movie:Movie_Api_Example_Fall_2026:1.0-SNAPSHOT` |
| Java compiler source/target | 25 |
| Spring Boot parent | 4.1.1 |
| Mockito core | Explicit version 5.20.0, test scope |
| Other explicit libraries | bcrypt 0.10.2; springdoc WebMVC UI 3.1.1 |
| Core starters | web, data-jpa, test, webmvc-test |
| Database driver | MySQL Connector/J, runtime scope |
| Build plugins | Spring Boot Maven plugin; Maven Surefire |

These are observed project declarations, not a recommendation to copy these versions into every future project. JUnit Jupiter and Mockito integration are supplied through the test dependencies; inspect the resolved dependency tree for exact transitive versions. The extra WebMVC test starter and bcrypt dependency do not establish snack controller test coverage or snack authentication.

Open PowerShell in the directory containing `pom.xml`. Replace the following path with your actual checkout:

```powershell
Set-Location 'C:\path\to\Movie_Api_Example_Fall_2026'
java -version
mvn -version
mvn test
mvn spring-boot:run
```

Use a JDK capable of compiling the configured Java 25 target. `mvn -version` reveals Maven's actual Java runtime, which may differ from the IDE JDK. This checkout has no inspected Maven wrapper; use `mvn` from an installed Maven distribution. In future projects that include a wrapper, use `./mvnw.cmd test` on Windows instead.

Wait for the application-started message and the embedded server's port announcement before testing HTTP. Stop the server with Ctrl+C. A successful Maven test run does not mean an HTTP server is running.

Alternative packaged execution:

```powershell
mvn package
java -jar .\target\Movie_Api_Example_Fall_2026-1.0-SNAPSHOT.jar
```

**Check the generated filename and successful packaging first.** Do not run both launch methods simultaneously on the same port. If a command is run outside the project root, use `mvn -f 'C:\path\to\project\pom.xml' test`.

## 10. Complete PowerShell CRUD verification

Run this against a local development database with disposable data after startup. It creates and deletes its own sample row. No fixed ID is assumed. Because POST currently returns text, the script locates the created record through the list endpoint using a unique name.

```powershell
$base = 'http://localhost:8080'

# GET all: [] is a valid successful result for an empty database.
Invoke-RestMethod -Method Get -Uri "$base/snacks/getAllSnacks"

# POST: do not supply snackId for a new row.
$name = 'GuidePopcorn-' + [guid]::NewGuid().ToString('N')
$body = @{ name = $name; type = 'Food'; price = 4.99 } | ConvertTo-Json
Invoke-RestMethod -Method Post -Uri "$base/snacks" `
    -ContentType 'application/json' -Body $body

# Discover the ID produced by MySQL.
$all = @(Invoke-RestMethod -Method Get -Uri "$base/snacks/getAllSnacks")
$created = @($all | Where-Object { $_.name -eq $name })
if ($created.Count -ne 1) { throw 'Expected exactly one newly created snack.' }
$id = $created[0].snackId

# GET one.
$one = Invoke-RestMethod -Method Get -Uri "$base/snacks/$id"
if ($one.name -ne $name) { throw 'Create/read verification failed.' }

# GET by inclusive price threshold.
$matches = @(Invoke-RestMethod -Method Get -Uri "$base/snacks/searchByPrice/5.00")
if (@($matches | Where-Object { $_.price -gt 5.00 }).Count -gt 0) {
    throw 'Price filter returned an out-of-range result.'
}
if (@($matches | Where-Object { $_.snackId -eq $id }).Count -ne 1) {
    throw 'Expected the created snack in the price-filter results.'
}

# PUT: send every editable field.
$update = @{ name = "$name-Large"; type = 'Food'; price = 6.99 } | ConvertTo-Json
$updated = Invoke-RestMethod -Method Put -Uri "$base/snacks/$id" `
    -ContentType 'application/json' -Body $update
if ($updated.snackId -ne $id -or $updated.price -ne 6.99) {
    throw 'Update response verification failed.'
}

# Read again to verify the update persisted.
$saved = Invoke-RestMethod -Method Get -Uri "$base/snacks/$id"
if ($saved.name -ne "$name-Large" -or $saved.price -ne 6.99) {
    throw 'Persisted update verification failed.'
}

# DELETE, then verify absence using the list endpoint.
Invoke-RestMethod -Method Delete -Uri "$base/snacks/$id"
$remaining = @(Invoke-RestMethod -Method Get -Uri "$base/snacks/getAllSnacks")
if (@($remaining | Where-Object { $_.snackId -eq $id }).Count -ne 0) {
    throw 'Delete verification failed.'
}
'CRUD verification passed.'
```

`Invoke-RestMethod` parses JSON; `Invoke-WebRequest` is useful when inspecting HTTP status and raw content. Avoid relying on `curl` because Windows PowerShell may map that name to another command. Use decimal points in URL price values. PowerShell continuation backticks must be the last character on a line, with no trailing spaces.

The walkthrough verifies successful CRUD and one price threshold. It does not establish authentication, validation, concurrency behavior, or all negative cases. To check that the threshold is inclusive, additionally create a disposable snack priced exactly 5.00 and confirm it is returned for `/searchByPrice/5.00`.

## 11. JUnit 5 and Mockito: the 11 passing tests

`snackServiceTest` uses `@ExtendWith(MockitoExtension.class)`, `@Mock` for the repository, and `@InjectMocks` for the service. `@BeforeEach` creates a Popcorn/Food/4.99 snack with ID `1L`. No Spring application context or MySQL connection is started by these tests.

| # | Test | What it checks |
|---|---|---|
| 1 | `testSaveSnack` | Repository save called; expected success text returned. |
| 2 | `testSaveAnotherSnack` | Another snack saved; result is non-null. |
| 3 | `testGetSnackByIdFound` | Present Optional and expected name. |
| 4 | `testGetSnackByIdNotFound` | Empty Optional propagated. |
| 5 | `testGetAllSnacks` | Two mocked items returned. |
| 6 | `testGetAllSnacksEmpty` | Empty collection handled. |
| 7 | `testFindByPrice` | Mocked query result returned for a threshold. |
| 8 | `testFindByPriceNoResults` | Empty query result handled. |
| 9 | `testDeleteSnack` | Existing ID triggers deletion. |
| 10 | `testUpdateSnack` | Name/price changed, ID retained, existing entity saved. |
| 11 | `testDeleteSnackNotFound` | Missing ID does not trigger deletion. |

The price tests stub repository output; they do not execute the derived SQL query or prove MySQL filtering. The update test does not currently assert every field or test the missing-ID exception. Controller responses, JSON mapping, validation, and database integration need separate tests.

### Why the delete test needed `existsById`

Mockito returns false for an unstubbed boolean method. Since deletion is guarded by `existsById`, this setup is required for the existing-record branch:

```java
when(snackRepository.existsById(1L)).thenReturn(true);
snackService.deleteSnack(1L);
verify(snackRepository).deleteById(1L);
```

For the missing-record branch, return false and verify `never().deleteById(99L)`. A failure saying a delete invocation was wanted but never happened often means the guard was not arranged.

### Why the update test stubs `save`

Unlike a real repository, an unstubbed mock object-returning method commonly returns null. The test uses:

```java
when(snackRepository.findById(1L)).thenReturn(Optional.of(testSnack));
when(snackRepository.save(any(snack.class)))
    .thenAnswer(invocation -> invocation.getArgument(0));
```

This lets the service return the entity it saved. Assertions should check the resulting state and important interactions rather than only that a method ran.

Run all tests or just the snack service suite:

```powershell
mvn test
mvn '-Dtest=snackServiceTest' test
Get-Content .\target\surefire-reports\com.movieapi.snack.snackServiceTest.txt
```

Surefire writes test reports under `target/surefire-reports`; see the [official Surefire introduction](https://maven.apache.org/components/surefire-archives/surefire-LATEST/maven-surefire-plugin/). Read both test counts and failure/error counts. The inspected plugin also supplies `-XX:+EnableDynamicAgentLoading -Xshare:off`; these JVM flags are not the cause of passing assertions and should not be copied without understanding their role.

## 12. Debugging lessons and troubleshooting

The source and user-requested topics support the following reusable troubleshooting guide. Except where screenshot/report evidence is explicitly identified, this is guidance rather than a verified sequence of past incidents.

| Symptom | Likely check and corrective action |
|---|---|
| `cannot find symbol` | Check exact class/method spelling, package declaration, and imports. Add the correct `java.util.Optional`, Spring, Jakarta persistence, JUnit, or Mockito import. Verify dependencies exist. |
| IDE suggests the wrong annotation | Use `jakarta.persistence.Entity/Id` for this project and JUnit Jupiter's `Test`; do not mix old `javax.persistence` or JUnit 4 imports accidentally. |
| Update appears successful but data stays unchanged | Retrieve the existing entity, copy fields onto it, save it, then GET again. Confirm the app connects to the database you inspected. |
| Update test returns null | Stub repository `save` to return the saved entity and arrange `findById`. |
| Mockito says delete was never called | Stub `existsById(id)` as true for the existing-ID test; verify the ID matches. |
| Missing bean / repository | Keep classes in the application scan tree and confirm stereotypes and repository generics. Avoid masking duplicate bean definitions with a blanket override setting. |
| Derived-query initialization failure | Match the method's property segment to `price`; inspect the deepest startup exception. |
| Access denied from MySQL | Check environment variables, account password, database privileges, and matching user host. Do not print credentials while debugging. |
| Communications link failure / connection refused | Start MySQL; confirm host, TCP port, service status, and JDBC URL. `Test-NetConnection localhost -Port 3306` checks reachability only, not login. |
| Unknown database | Create the configured database or correct the URL; schema auto-update does not substitute for provisioning the database itself. |
| Schema validation fails | Apply migrations, or explicitly use `update` only in a disposable development environment. |
| `mvn` is not recognized | Install/configure Maven on PATH or use an existing wrapper. Open a new terminal after changing PATH. |
| No POM / wrong directory | Run from the directory containing `pom.xml`, or supply `-f` with its full path. |
| Invalid target release / class version error | Align the IDE, `JAVA_HOME`, Maven runtime, and compiler target with Java 25. |
| `spring-boot` command not recognized | It is a Maven goal: use `mvn spring-boot:run`, not a standalone shell command. |
| Port 8080 in use | Stop the earlier application instance or set another port and update the test base URL. |
| GET list returns `[]` | The request can be successful with no data. POST a sample row and check which database/profile is in use. This empty-list result appears in the conversation screenshot. |
| Missing ID gives server error | Current code uses generic exceptions. Add deliberate 404 handling in a future change; do not document 404 as existing behavior. |
| PUT clears fields | Current PUT copies every editable field. Send a complete body or implement a separately designed partial-update operation. |
| Git cannot resolve `github.com` | Check network/DNS connectivity and retry. This failure appeared before the successful push in the screenshots. |

### Missing and unused imports

Resolve compilation errors first; changing logic will not fix an unresolved type. Use compiler output to locate the file and line. Remove copied imports after refactoring: the current service includes unused Movie/HTTP-related imports, and the repository includes an unused `CrudRepository` import. Unused imports are usually cleanup rather than the cause of runtime persistence failures.

### Windows `target` directory locks

`target` contains compiled classes, packaged artifacts, and reports. `mvn clean` removes generated output; it may fail if Windows has a file open.

1. Stop this project's running application, tests, and IDE build process.
2. Close views or tools holding files under this project's `target` directory.
3. If OneDrive is synchronizing the build folder, allow it to finish or move a future checkout to a development folder outside sync.
4. Retry `mvn clean test` from the verified project root.

Do not indiscriminately kill all Java processes or delete folders outside the project. If manual deletion becomes necessary, first verify the absolute target is precisely this project's generated `target` folder. `mvn test` can help distinguish a clean-step lock from a test failure, but stale output may remain; complete a clean build once the lock is resolved.

## 13. GitHub fork, commit, and push workflow

A fork is your GitHub-hosted copy of another repository. A clone is the local checkout. Conventionally `origin` points to your fork and `upstream` points to the original project. **Check actual remotes before changing or pushing them.**

For a new project, fork through GitHub, clone your fork, and optionally add upstream:

```powershell
git clone https://github.com/YOUR_ACCOUNT/YOUR_FORK.git
Set-Location YOUR_FORK
git remote -v
git remote add upstream https://github.com/ORIGINAL_OWNER/ORIGINAL_REPOSITORY.git
git switch -c feature/snack-crud
```

All uppercase names are placeholders. Do not repeat `remote add upstream` if it already exists. The screenshots show a successful push to `https://github.com/dralodrec/Movie_Api_Example_Fall_2026.git`, on branch `main`, setting tracking to `origin/main`. They do not establish the original upstream URL or prove that GitHub marks this repository as a fork.

Review changes, test, and stage intentional files:

```powershell
git status
git diff --stat
mvn test
git add src/main/java/com/movieapi/snack/
git add src/test/java/com/movieapi/snack/
git diff --cached --stat
git diff --cached
git commit -m 'Complete Snack API CRUD implementation and unit tests'
git push -u origin feature/snack-crud
git status
```

Inspect diffs locally for accidental secrets before sharing output. Stage configuration only after converting it to a safe template. The historical screenshot shows commit `88e923b` with the snack CRUD/unit-test message and a later successful `git push -u origin main`. The final status screenshot still shows `application.properties` modified and unstaged. Therefore, a successful push did not mean every local file was committed or the worktree was clean.

For that existing historical main-branch workflow, `git push -u origin main` was the command shown. For future collaborative work, a feature branch and pull request provide a review point. Do not force-push to fix an ordinary rejection; fetch and inspect the branch history first. Use GitHub's supported authentication or a credential manager rather than placing tokens in remote URLs.

Useful checks:

```powershell
git branch --show-current
git status
git log -1 --oneline
git remote -v
```

`git status` describes local files and locally known tracking state; fetch before treating it as evidence of the latest remote state. `target/` is already ignored in this project. Do not commit generated classes or test reports as a substitute for reproducible tests.

## 14. Common pitfalls and improvements

| Current pattern / pitfall | Future-project improvement |
|---|---|
| Lowercase Java type names | Follow PascalCase class/interface conventions. |
| Direct entity request binding | Use DTOs to define accepted fields and protect identity fields. |
| POST accepts an entity with a writable ID | Reject client-supplied IDs for create; otherwise `save` may update/merge rather than create. |
| `Double` money | Use `BigDecimal` and deliberate schema precision. |
| Generic exceptions for missing resources | Map a domain exception to 404 using controlled error handling. |
| No input validation | Add validation dependency/annotations and `@Valid`, with negative tests. |
| Every success is 200 | Deliberately choose 201/Location for create and 204 or another documented delete response. |
| Field injection | Prefer constructor injection. |
| Mock-only service tests | Add repository integration and controller tests, then a real startup/CRUD smoke check. |
| `ddl-auto=update` everywhere | Use migrations and schema validation for deployed systems. |
| Unrestricted cross-origin annotation | Configure browser origins for the actual client and environment. |
| Local properties left modified after push | Decide which settings belong in a tracked template versus external environment variables. |

These are proposed improvements, not changes performed while preparing this document.

## 15. Future-project checklist

### Define and implement

- [ ] Write the resource's purpose, fields, constraints, and expected HTTP contract.
- [ ] Choose supported Java/Spring/Maven versions and verify Maven's actual JDK.
- [ ] Keep package names beneath the main application package.
- [ ] Create an entity with an ID strategy and no-argument constructor.
- [ ] Create the repository with correct entity/ID generics.
- [ ] Verify derived query names against entity properties.
- [ ] Put operation decisions in a service; use clear dependency injection.
- [ ] Implement update as find, validate, copy allowed fields, persist, return.
- [ ] Define missing-resource and invalid-input responses deliberately.
- [ ] Confirm actual controller paths rather than guessing from method names.

### Configure and verify

- [ ] Provision a development database and dedicated account.
- [ ] Keep credentials external; commit only safe templates.
- [ ] Choose schema migration/update behavior explicitly per environment.
- [ ] Unit-test success, empty, missing, and failure branches.
- [ ] Stub Mockito guard methods and return values that the service uses.
- [ ] Inspect test counts and failures, not only a green IDE icon.
- [ ] Start the app and verify startup/database logs.
- [ ] Exercise POST, all GET routes, PUT, and DELETE with disposable data.
- [ ] Read updated data again and verify deletion by absence.
- [ ] Test threshold boundaries and negative inputs.

### Preserve and share

- [ ] Review staged changes and remove credentials/generated output.
- [ ] Check fork, branch, origin, and upstream before pushing.
- [ ] Run required tests and record the date and scope of results.
- [ ] Commit intentional files and push the intended branch.
- [ ] Confirm remaining local modifications with `git status`.
- [ ] Update the README whenever paths, payloads, statuses, or setup change.
- [ ] Validate setup from a clean checkout before treating the guide as reproducible.

## 16. Quick reference

```text
Start:       mvn spring-boot:run
Tests:       mvn test
Clean tests: mvn clean test
List:        GET    /snacks/getAllSnacks
Read:        GET    /snacks/{snackId}
Filter:      GET    /snacks/searchByPrice/{price}
Create:      POST   /snacks
Update:      PUT    /snacks/{id}
Delete:      DELETE /snacks/{id}
JSON fields: name, type, price
ID field:    snackId (database-generated for new snacks)
```



# Snack API Testing with Postman

## Prerequisites
- MySQL is running and the application database is configured.
- The Spring Boot Movie API is running on port `8080`.
- Postman is installed.

**Base URL:** `http://127.0.0.1:8080`

## Test CRUD

1. **Get all snacks** — Send `GET http://127.0.0.1:8080/snacks/getAllSnacks`.

2. **Create a snack** — Send `POST http://127.0.0.1:8080/snacks`. In **Body → raw → JSON**, enter:
   ```json
   {
    "name": "Popcorn",
    "type": "Salty",
    "price": 5.99
   }
   ```
   Use `Content-Type: application/json`.

3. **Update a snack** — Send `PUT http://127.0.0.1:8080/snacks/{id}` with the same JSON fields and your updated values. Use **Body → raw → JSON** and `Content-Type: application/json`.
   ```json
   {
    "name": "Large Popcorn",
    "type": "Food",
    "price": 6.99
   }
   ```
   Use `Content-Type: application/json`.

4. **Delete a snack** — Send `DELETE http://127.0.0.1:8080/snacks/{id}`.

After each POST, PUT, or DELETE, repeat the GET request to confirm the change. Replace `{id}` with an actual snack ID from the response or GET results. **IDs vary between databases and test runs.**
