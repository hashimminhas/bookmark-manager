plugins {
    id("java")
    id("application")
}

group = "com.hashim"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Javalin web framework
    implementation("io.javalin:javalin:6.1.3")
    
    // SQLite database
    implementation("org.xerial:sqlite-jdbc:3.45.0.0")
    
    // JSON processing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.hashim.Main")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.hashim.Main"
    }
    // Include all dependencies in the JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}