# Temperature Converter

## 1. Project overview and objectives

Temperature Converter is a Java desktop application for converting values between
Celsius, Fahrenheit, and Kelvin. It is intended for students, developers, and
other users who need a small, reliable temperature-conversion tool with a
history of the conversions stored in a relational database.

The application provides a JavaFX user interface where the user enters a
temperature, selects the source and target units, and starts the conversion.
Each successful conversion is written to the database together with both unit
identifiers. The project goals are to provide correct conversion formulas, a
simple graphical user experience, persistent conversion data, automated tests,
and a repeatable Maven/Jenkins/Docker delivery process.

## 2. Technology stack and dependencies

| Area | Technology |
| --- | --- |
| Language and runtime | Java 21 |
| Build tool | Apache Maven (the Jenkins pipeline uses the `Maven3` installation) |
| User interface | JavaFX 21.0.12 |
| Database | MariaDB, accessed through JDBC |
| Database driver | MariaDB Connector/J 3.5.6 |
| Test framework | JUnit Jupiter 5.14.0 |
| Test database | H2 2.3.232 in-memory database |
| Coverage | JaCoCo Maven Plugin 0.8.13 |
| Container | Docker, based on Ubuntu 24.04 with OpenJDK 21 and OpenJFX |
| Continuous integration | Jenkins |

The Maven project coordinates are
`org.example:OTP1_inclass1_assignment_Aaro1:1.0-SNAPSHOT`. Maven downloads
JavaFX, MariaDB Connector/J, JUnit, H2, and JaCoCo from Maven Central. No
localized resource bundles are currently used; the UI text is defined directly
in `Main.java` and is currently in English.

## 3. Design and development methodology

### Architecture

The application uses a small layered architecture:

* **Presentation layer:** `Main` extends JavaFX `Application` and handles the
  window, input controls, unit selection, conversion action, and result display.
* **Domain layer:** `TemperatureConverter` contains the conversion formulas and
  the extreme-temperature rule (below -40 °C or above 50 °C).
* **Data-access layer:** `TemperatureUnitDAO` reads unit identifiers and names,
  while `TempRecordDAO` persists conversion records.
* **Infrastructure layer:** `DBConnection` loads `database.properties` from the
  classpath and creates JDBC connections.

This separation keeps conversion logic independent from JavaFX and allows the
DAO classes to be tested with a separate H2 database. SQL statements use
prepared statements for values supplied to queries and inserts.

### UML and ER design

The logical component/class relationships are:

```text
Main --> TemperatureConverter
Main --> TemperatureUnitDAO --> DBConnection
Main --> TempRecordDAO      --> DBConnection
```

The relational model contains two tables:

```text
temperature_units
-----------------
id (PK)
name

temperature_records
-------------------
id (PK)
temperature
unit_id              -> temperature_units.id
converted_temperature
converted_unit_id    -> temperature_units.id
```

`temperature_units` is seeded with Celsius, Fahrenheit, and Kelvin. Each row in
`temperature_records` stores the original value and the converted value,
together with the source and destination unit IDs. The same
`temperature_units` table is referenced for both unit roles.

### Development process

Development follows an incremental, test-oriented workflow. The conversion
rules and database operations are implemented as separate classes, then
verified with JUnit tests. Maven provides the common build lifecycle, JaCoCo
generates coverage data, and Jenkins automates checkout, build, tests, coverage,
dependency packaging, Docker image creation, and Docker Hub publication.

## 4. Functional testing

Testing is automated with `mvn test` and is divided into two areas:

* `TemperatureConverterTest` verifies Fahrenheit/Celsius conversion in both
  directions, Kelvin-to-Celsius conversion, boundary values, and the
  `isExtremeTemperature` limits.
* `DatabaseDaoTest` creates isolated H2 tables before each test and verifies
  unit lookup, the error raised for an unknown unit, retrieval of all units,
  and insertion of a conversion record.

The tests use the same DAO classes as the application but override their
connection method to point to an in-memory H2 database. This avoids modifying
the MariaDB installation during tests and makes the test run reproducible.

After a test run, Maven Surefire XML results are available under
`target/surefire-reports/`. JaCoCo generates an HTML report at
`target/site/jacoco/index.html`.

## 5. Setup and execution instructions

### Prerequisites

Install the following:

* JDK 21
* Apache Maven 3.x
* MariaDB server (or another MariaDB-compatible installation)
* Docker, only if the container workflow is needed

Verify Java and Maven:

```bash
java -version
mvn -version
```

### Configure MariaDB

Create the application database and tables. The repository does not contain a
database migration, so run the following SQL in MariaDB:

```sql
CREATE DATABASE temperature_converter;
USE temperature_converter;

CREATE TABLE temperature_units (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO temperature_units (id, name) VALUES
    (1, 'Celsius'),
    (2, 'Fahrenheit'),
    (3, 'Kelvin');

CREATE TABLE temperature_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    temperature DOUBLE NOT NULL,
    unit_id INT NOT NULL,
    converted_temperature DOUBLE NOT NULL,
    converted_unit_id INT NOT NULL,
    CONSTRAINT fk_temperature_unit
        FOREIGN KEY (unit_id) REFERENCES temperature_units(id),
    CONSTRAINT fk_converted_temperature_unit
        FOREIGN KEY (converted_unit_id) REFERENCES temperature_units(id)
);
```

Create `src/main/resources/database.properties` locally. This file is ignored
by Git because it contains credentials:

```properties
db.url=jdbc:mariadb://localhost:3306/temperature_converter
db.user=<your-mariadb-user>
db.password=<your-mariadb-password>
```

When the application is run inside Docker on Docker Desktop, use
`host.docker.internal` instead of `localhost` in `db.url`, and make sure the
MariaDB server accepts the connection from the container.

### Build and test

Run these commands from the repository root:

```bash
mvn clean test
mvn jacoco:report
```

A successful test run produces the Surefire reports and the JaCoCo report
described in the testing section.

### Run the JavaFX application

```bash
mvn javafx:run
```

The application opens the Temperature Converter window. Enter a value, choose
the source and destination units, and press **Convert**. The result is shown in
the window and the conversion is saved to `temperature_records`.

### Build and run the Docker image

The Dockerfile expects compiled classes and copied Maven dependencies:

```bash
mvn clean package
mvn dependency:copy-dependencies -DoutputDirectory=target/dependency
docker build -t temperature-converter .
docker run --rm temperature-converter
```

The container starts the JavaFX application with software rendering. A
graphical display must be made available to the container for the window to be
visible; the exact display forwarding configuration depends on the host
operating system. The database configuration and MariaDB network access must
also be available to the container.

### Jenkins pipeline

`Jenkinsfile` defines the following stages:

1. Checkout the Git repository.
2. Build and install with `mvn clean install`.
3. Run tests with `mvn test`.
4. Generate and publish JaCoCo coverage.
5. Publish JUnit XML results.
6. Copy runtime dependencies to `target/dependency`.
7. Build the Docker image.
8. Push the image to Docker Hub.

The Jenkins agent must provide JDK 21 under the configured name `jdk-21.0.12`,
Maven under `Maven3`, Docker, and the Docker Hub credential identified by
`docker_ID`. The pipeline publishes the image
`aarohaavisto/temperature_converter:latest`.
