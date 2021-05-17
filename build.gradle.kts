plugins {
    java
    id("org.springframework.boot") version "2.4.5"
    id ("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.freefair.lombok") version "5.3.0"
    id ("org.asciidoctor.convert") version "1.5.8"
}

group = "net.titanrealms.api"
version = "0.0.1-SNAPSHOT"

val snippetsDir by extra { file("build/generated-snippets") }

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation( "org.springframework.boot:spring-boot-starter-web")

    // github api client
    implementation("org.kohsuke:github-api:1.127")
    // lang formatting
    implementation("net.titanrealms.lang.formatter:lang-formatter:1.0-SNAPSHOT")
    // HOCON files
    implementation("com.typesafe:config:1.4.1")

    developmentOnly( "org.springframework.boot:spring-boot-devtools")
    runtimeOnly ("io.micrometer:micrometer-registry-influx")

    testImplementation( "org.springframework.boot:spring-boot-starter-test")
    testImplementation ("org.springframework.restdocs:spring-restdocs-mockmvc")
}

ext {
    set("snippetsDir", snippetsDir)
}

tasks {
    test {
        useJUnitPlatform()
    }
    val testTask = withType<Test> {
        outputs.dir(snippetsDir)
    }
    withType<org.asciidoctor.gradle.AsciidoctorTask> {
        dependsOn(testTask)
        inputs.dir(snippetsDir)
    }
}
