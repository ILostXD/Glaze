import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("app.cash.sqldelight")
}

compose.resources {
    packageOfResClass = "com.glaze.shared.resources"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("app.cash.sqldelight:runtime:2.3.2")
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation("com.composables:icons-material-symbols-rounded-cmp:2.2.1")
            implementation("com.composables:icons-material-symbols-rounded-filled-cmp:2.2.1")
            implementation("com.github.skydoves:cloudy:0.6.1")
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("io.ktor:ktor-client-core:3.3.1")
        }
        androidMain.dependencies {
            implementation("androidx.activity:activity-compose:1.11.0")
            implementation("app.cash.sqldelight:android-driver:2.3.2")
            implementation("io.ktor:ktor-client-okhttp:3.3.1")
        }
        androidUnitTest.dependencies {
            implementation(kotlin("test"))
            implementation("app.cash.sqldelight:sqlite-driver:2.3.2")
            implementation("io.ktor:ktor-client-mock:3.3.1")
        }
    }
}

sqldelight {
    databases {
        register("HistoryDatabase") {
            packageName.set("com.glaze.shared.db")
        }
    }
}

android {
    namespace = "com.glaze.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
