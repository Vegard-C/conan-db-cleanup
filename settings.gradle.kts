rootProject.name = "conancleanup"

pluginManagement {
  val kotlinVersion: String by settings

  repositories {
    mavenCentral()
    gradlePluginPortal()
  }

  plugins {
    kotlin("jvm") version kotlinVersion
  }
}
