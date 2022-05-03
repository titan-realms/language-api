plugins {
    java
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.freefair.lombok") version "5.3.3.3"
}

group = "net.titanrealms.api"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // github api client
    implementation("org.kohsuke:github-api:1.127")
    // HOCON files
    implementation("com.typesafe:config:1.4.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    //runtimeOnly ("io.micrometer:micrometer-registry-influx")

    // API Documentation
    annotationProcessor("io.swagger.core.v3:swagger-annotations:2.2.0")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    // Display API documentation
    developmentOnly("org.springdoc:springdoc-openapi-ui:1.5.2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}
