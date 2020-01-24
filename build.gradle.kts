import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    application
    id("com.github.johnrengelman.shadow").version("4.0.3")
}

group = "biz.schroeders.mite"
version = "1.1"

application {
    mainClassName = "io.vertx.core.Launcher"
}

val mainVerticleName = "biz.schroeders.mite.MiteServer"
val watchForChange = "src/**/*.java"
val doOnChange = "$projectDir/gradlew classes"

tasks {
    test {
        useJUnitPlatform()
    }

    getByName<JavaExec>("run") {
        args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=${application.mainClassName}", "--on-redeploy=$doOnChange")
    }

    withType<ShadowJar> {
        archiveClassifier.set("fat")
        manifest {
            attributes["Main-Verticle"] = mainVerticleName
        }
        mergeServiceFiles {
            include("META-INF/services/io.vertx.core.spi.VerticleFactory")
        }
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    jcenter()
}

object Versions {
    const val slf4j = "1.7.25"
    const val logback = "1.2.1"
    const val vertx = "3.8.4"
    const val rxJava = "2.2.4"
    const val gson = "2.8.4"
    const val freemarker = "2.3.29"

    const val junit = "4.12"
    const val mockito = "2.8.9"
}

dependencies {
    implementation("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("ch.qos.logback:logback-classic:${Versions.logback}")
    implementation("com.google.code.gson:gson:${Versions.gson}")
    implementation("io.vertx:vertx-core:${Versions.vertx}")
    implementation("io.vertx:vertx-web:${Versions.vertx}")
    implementation("io.vertx:vertx-web-client:${Versions.vertx}")
    implementation("io.vertx:vertx-rx-java2:${Versions.vertx}")
    implementation("io.vertx:vertx-web-templ-freemarker:${Versions.vertx}")
    implementation("io.reactivex.rxjava2:rxjava:${Versions.rxJava}")
    implementation("org.freemarker:freemarker:${Versions.freemarker}")

    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("io.vertx:vertx-unit:${Versions.vertx}")
}
