plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("org.jetbrains.compose") version "1.6.10"
}

group = "com.watermelon.music"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.foundation)
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    
    // Networking (Retrofit/OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Supabase & Ktor (for Desktop JVM)
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.1.4")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.1.4")
    implementation("io.ktor:ktor-client-cio:2.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Coil (Image Loading)
    implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.0-rc01")

    // JavaFX for Audio Playback
    implementation("org.openjfx:javafx-base:17.0.2:win")
    implementation("org.openjfx:javafx-graphics:17.0.2:win")
    implementation("org.openjfx:javafx-media:17.0.2:win")
    
    // NewPipe Extraction
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.26.2")
    
    // JSON
    implementation("org.json:json:20231013")
}

compose.desktop {
    application {
        mainClass = "com.watermelon.music.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe)
            packageName = "Watermelon"
            packageVersion = "1.0.0"
            windows {
                iconFile.set(project.file("src/main/resources/watermelon.ico"))
                menuGroup = "Watermelon"
                upgradeUuid = "3d4a2c1e-5b6f-7890-abcd-ef1234567890"
            }
        }
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}
