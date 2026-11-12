# Vulnerable SaaS Sample App

A multi-module Maven fixture with a Spring Boot sample application, runtime inventory, deployment, and health endpoints. The dependency-remediation scenario is synthetic and makes no claim about a real CVE.

## Requirements

- JDK 25
- Maven 3.9 or later
- Docker, if using the container instructions

Spring Boot 3.5.7 supports Java 25. The root `pom.xml` is the Maven parent and aggregator; shared dependency versions are declared there and consumed by the `sample-app` module.

## Build and Test

From the repository root, run:

```bash
mvn -B -ntp -pl sample-app -am test
mvn -B -ntp -pl sample-app -am package
```

## Run

Start the packaged application with:

```bash
java -jar sample-app/target/vulnerable-saas-demo.jar
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

## Synthetic HttpClient Migration Fixture

The sample uses Apache HttpClient 4 as its baseline. `LegacyHttpClient` makes a real `GET` request, checks the response status, reads the entity as UTF-8, and closes both the response and client. This is a synthetic dependency-remediation exercise, not a report of a real vulnerability.

To migrate this feature to HttpClient 5, update the shared version property and dependency in `pom.xml` files as follows, choosing a target version appropriate to the exercise:

```xml
<!-- Parent pom.xml -->
<httpclient5.version>5.x.y</httpclient5.version>

<!-- sample-app/pom.xml -->
<dependency>
  <groupId>org.apache.httpcomponents.client5</groupId>
  <artifactId>httpclient5</artifactId>
  <version>${httpclient5.version}</version>
</dependency>
```

Remove the HttpClient 4 dependency and its `httpclient4.version` property. The Java APIs are not source-compatible. For `LegacyHttpClient`, change imports from `org.apache.http.*` to the corresponding `org.apache.hc.client5.*` and `org.apache.hc.core5.*` packages. Use `org.apache.hc.client5.http.classic.methods.HttpGet`, `org.apache.hc.client5.http.impl.classic.CloseableHttpClient`, `org.apache.hc.client5.http.impl.classic.HttpClients`, and `org.apache.hc.core5.http.io.entity.EntityUtils`. The response handler form below replaces the HttpClient 4 response type and status accessors:

```java
try (CloseableHttpClient client = HttpClients.createDefault()) {
  return client.execute(new HttpGet(url), response -> {
    int statusCode = response.getCode();
    if (statusCode < 200 || statusCode >= 300) {
      throw new IOException("HTTP GET failed with status " + statusCode);
    }
    return response.getEntity() == null
        ? ""
        : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
  });
}
```

The repository intentionally remains on HttpClient 4 until such a migration is applied.

## Docker

Build and run the standalone image from the repository root:

```bash
docker build -t vulnerable-saas-demo .
docker run --rm -p 8080:8080 vulnerable-saas-demo
```