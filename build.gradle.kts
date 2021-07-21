buildscript {
  repositories {
    mavenCentral()
  }
}

val sqlliteVersion: String by project
val kotlinVersion: String by project

plugins {
  kotlin("jvm")
}

group = "com." + project.name
version = "0.0.1"

java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
}


dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
  implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
  implementation("org.xerial:sqlite-jdbc:$sqlliteVersion")
}