plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.0.14"
    application
}

group = "org.sandbox.starbat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.openjfx:javafx-swing:21")
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml", "javafx.swing")
}

application {
    mainClass.set("org.sandbox.starbat.Launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Zip> {
    enabled = false
}

tasks.withType<Tar> {
    enabled = false
}
