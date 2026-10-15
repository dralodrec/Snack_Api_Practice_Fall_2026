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

- **Constructor injection.** Services and controllers never use `new` to create their dependencies. Spring sees the constructor parameter and passes in the right object (a "bean").
- **Free CRUD.** `JpaRepository<Movie, Long>` gives you `save`, `findAll`, `findById`, `deleteById` and `count` with no code.
- **Derived queries.** Spring builds SQL from a method name: `findByGenreIgnoreCase(String genre)` becomes `WHERE LOWER(genre) = LOWER(?)`.
- **Errors.** The service throws `ResponseStatusException` with a status like `404 NOT_FOUND` or `400 BAD_REQUEST`, and Spring turns it into that HTTP response.

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
| PUT | `/api/movies/{id}` | Movie JSON | The updated movie |
| DELETE | `/api/movies/{id}` | none | 204, no content |

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