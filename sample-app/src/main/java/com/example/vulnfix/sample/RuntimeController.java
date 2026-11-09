package com.example.vulnfix.sample;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/runtime")
public class RuntimeController {
    public record Deployment(String buildVersion, Map<String, String> dependencies, boolean failHealthCheck) { }
    public record RuntimeInfo(String instanceName, String buildVersion, String deployedAt, Map<String, String> dependencies) { }

    private final String instanceName;
    private final AtomicReference<RuntimeInfo> runtime;
    private volatile boolean healthy = true;

    public RuntimeController(
            @Value("${SIM_INSTANCE_NAME:local-instance}") String instanceName,
            @Value("${SIM_BUILD_VERSION:baseline-1}") String buildVersion,
            @Value("${SIM_COMMONS_TEXT_VERSION:1.12.0}") String commonsTextVersion) {
        this.instanceName = instanceName;
        this.runtime = new AtomicReference<>(new RuntimeInfo(instanceName, buildVersion, Instant.now().toString(), Map.ofEntries(
            Map.entry("org.apache.commons:commons-text", commonsTextVersion),
            Map.entry("commons-io:commons-io", "2.18.0"),
            Map.entry("org.apache.httpcomponents:httpclient", "4.5.14"),
            Map.entry("org.apache.logging.log4j:log4j-core", "2.24.3"),
            Map.entry("org.yaml:snakeyaml", "2.3"),
            Map.entry("com.google.guava:guava", "33.4.0-jre"),
            Map.entry("com.google.code.gson:gson", "2.12.1"),
            Map.entry("org.postgresql:postgresql", "42.7.5"),
            Map.entry("org.hibernate.orm:hibernate-core", "6.4.4.Final"),
            Map.entry("io.netty:netty-handler", "4.1.118.Final"),
            Map.entry("org.apache.poi:poi-ooxml", "5.4.1"),
            Map.entry("org.bouncycastle:bcprov-jdk18on", "1.80"),
            Map.entry("com.thoughtworks.xstream:xstream", "1.4.21"),
            Map.entry("com.fasterxml.jackson.core:jackson-databind", "2.18.2"))));
    }

    @GetMapping("/info")
    public RuntimeInfo info() {
        return runtime.get();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        if (!healthy) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Injected health check failure");
        }
        return Map.of("status", "UP");
    }

    @PostMapping("/deploy")
    public RuntimeInfo deploy(@RequestBody Deployment deployment) {
        if (deployment.buildVersion() == null || deployment.dependencies() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Build version and dependencies are required");
        }
        healthy = !deployment.failHealthCheck();
        Map<String, String> dependencies = new LinkedHashMap<>(deployment.dependencies());
        runtime.set(new RuntimeInfo(instanceName, deployment.buildVersion(), Instant.now().toString(), dependencies));
        return runtime.get();
    }
}