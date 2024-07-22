plugins {
    java
    id("org.springframework.boot") version "2.6.7"
    id("io.spring.dependency-management") version "1.1.6"
    id("io.freefair.lombok") version "6.4.3"
}

group = "net.titanrealms.api"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("redis.clients:jedis:4.2.2")

    // github api client
    implementation("org.kohsuke:github-api:1.306")
    // HOCON files
    implementation("com.typesafe:config:1.4.2")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    //runtimeOnly ("io.micrometer:micrometer-registry-influx")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}
