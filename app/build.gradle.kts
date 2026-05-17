plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("jacoco")
}

android {
    namespace = "com.example.xolotl"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.xolotl"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            // Activa la cobertura para pruebas locales (src/test)
            enableUnitTestCoverage = true
            // Activa la cobertura para pruebas en el emulador (src/androidTest)
            enableAndroidTestCoverage = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        unitTests {
            // Para encuentrar los IDs de los botones
            isIncludeAndroidResources = true
        }

        unitTests.all {
            val testTask = this as? org.gradle.api.tasks.testing.Test
            testTask?.apply {
                maxHeapSize = "3072m"
                setForkEvery(1L)

                // --- PROPIEDADES DE RENDIMIENTO ---
                systemProperty("robolectric.graphicsMode", "LEGACY")
                systemProperty("robolectric.pixelCopyRenderMode", "off")
                jvmArgs("-XX:+UseG1GC", "-XX:TieredStopAtLevel=1", "-Xss2m")
            }
        }
    }
}

// Configuración corregida para Kotlin DSL
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("createDebugCoverageReport")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*")

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
    }
    val kotlinTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    sourceDirectories.setFrom(files("${project.projectDir}/src/main/java"))
    classDirectories.setFrom(files(debugTree, kotlinTree))
    executionData.setFrom(fileTree(layout.buildDirectory.get()) {
        include("**/*.ec")
    })
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Dependencia para Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Dependencias de Firebase
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.1.0")
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    // Almacenamiento de fotos
    implementation("com.google.firebase:firebase-storage:21.0.0")
    //Dependencia para Sweet Alert
    implementation("com.github.f0ris.sweetalert:library:1.6.2")
    implementation(libs.play.services.location)
    implementation(libs.androidx.ink.geometry.android)
    //Dependencias de pruebas
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    // --- INFRAESTRUCTURA DE PRUEBAS UNITARIAS (Carpeta 'test') ---
    testImplementation("junit:junit:4.13.2")
    // Mockito para simular objetos de Firebase
    testImplementation("org.mockito:mockito-core:5.5.0")
    // Soporte de Mockito para Kotlin (evita errores de nullability)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    // Para probar lógica que use hilos o corrutinas
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    // Roboelectric para pruebas unnitarias sin emulador Android
    testImplementation("org.robolectric:robolectric:4.11.1")

    // --- INFRAESTRUCTURA DE PRUEBAS DE INTEGRACIÓN/UI (Carpeta 'androidTest') ---
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    // Soporte para probar RecyclerViews y DatePickers
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    // --- PARA LOS INTENTS ---
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    // Reglas de JUnit para lanzar Activities automáticamente
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    // Mockito para Android (necesario para mocks dentro del emulador)
    androidTestImplementation("org.mockito:mockito-android:5.5.0")
    // Asegurar que el emulador tenga la versión correcta disponible
    androidTestImplementation("com.google.protobuf:protobuf-javalite:3.25.1")

    // Librería para el tour visual (Onboarding)
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
}

// Bloqueo de versión antigua de Protobuf que interfiere con Firestore en Espresso
configurations.all {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
}




