plugins {
    id("java")
}

group = "de.ricosw.orbismod"
version = "1.0.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
    maven {
        name = "hytale-prerelease"
        url = uri("https://maven.hytale.com/pre-release")
    }
    maven {
        name = "orbismanager"
        url = uri("https://maven.ricosw.de/orbismanager")
    }
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:2026.03.26-89796e57b")
    compileOnly("de.ricosw.orbismanager:mod-api:1.0.0")
}


tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from("src/main/resources") {
        include("manifest.json")
    }
}

tasks.test {
    useJUnitPlatform()
}