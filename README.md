# Vulnerable SaaS Sample App

A standalone Spring Boot sample application with runtime inventory, deployment, and health endpoints. It is the sample application from the Vulnfix project; this repository contains only the app and the files required to build and run it.

## Requirements

- JDK 25
- Maven 3.9 or later
- Docker, if using the container instructions

Spring Boot 3.5.7 supports Java 25. Maven uses the standard root `pom.xml`; no parent repository or sibling modules are required.

## Build and Test

From the repository root, run:

```bash
mvn -B -ntp test
mvn -B -ntp package
```

## Run

Start the packaged application with:

```bash
java -jar target/vulnerable-saas-demo.jar
```

The app listens on port 8080. The runtime endpoints are:

- `GET /api/runtime/info` returns the instance name, build version, deployment timestamp, and dependency inventory.
- `GET /api/runtime/health` returns `{"status":"UP"}` while healthy, or HTTP 503 after a deployment requests an injected health-check failure.
- `POST /api/runtime/deploy` updates the runtime build and dependency inventory. The JSON body requires `buildVersion` and `dependencies`; `failHealthCheck` controls the injected health state.

For example:

```bash
curl http://localhost:8080/api/runtime/health
curl http://localhost:8080/api/runtime/info
curl -X POST http://localhost:8080/api/runtime/deploy \
  -H 'Content-Type: application/json' \
  -d '{"buildVersion":"build-2","dependencies":{"org.apache.commons:commons-text":"1.13.0"},"failHealthCheck":false}'
```

The initial instance name, build version, and Commons Text version can be set with `SIM_INSTANCE_NAME`, `SIM_BUILD_VERSION`, and `SIM_COMMONS_TEXT_VERSION`.

## Docker

Build and run the standalone image from the repository root:

```bash
docker build -t vulnerable-saas-demo .
docker run --rm -p 8080:8080 vulnerable-saas-demo
```