plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "App"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.google.ortools:ortools-java:9.10.4067")
    implementation("org.openjfx:javafx-controls:23-ea+20")
    implementation("org.openjfx:javafx-fxml:23-ea+20")
    implementation("org.openjfx:javafx-swing:23-ea+20")
    implementation("org.scilab.forge:jlatexmath:1.0.7")


}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "23-ea+20"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass = "App.Launcher"
}