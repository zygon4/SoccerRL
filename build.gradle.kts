/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java project to get you started.
 * For more details take a look at the Java Quickstart chapter in the Gradle
 * User Manual available at https://docs.gradle.org/6.5/userguide/tutorial_java_projects.html
 */

plugins {
    // Apply the java plugin to add support for Java
    java

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    implementation("org.hexworks.zircon:zircon.core-jvm:2020.1.6-HOTFIX")
    implementation("org.hexworks.zircon:zircon.jvm.swing:2020.1.6-HOTFIX")

/*
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.hexworks.zircon/zircon.core-jvm "2020.0.1-PREVIEW"]
                 [org.hexworks.zircon/zircon.jvm.swing "2020.0.1-PREVIEW"]
                 [org.jetbrains.kotlinx/kotlinx-collections-immutable-jvm "0.3"]
                 [org.clojure/tools.logging "0.6.0"]]
  :repositories [["jitpack.io" "https://jitpack.io"]
                 ["KotlinX" "https://dl.bintray.com/kotlin/kotlinx/"]]
*/



    // Use JUnit test framework
    testImplementation("junit:junit:4.13")
}

application {
    // Define the main class for the application.
    mainClassName = "com.zygon.rl.soccer.ui.Runner"
}
