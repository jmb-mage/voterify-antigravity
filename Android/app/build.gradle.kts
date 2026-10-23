import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun loadFirestoreConfig(fileName: String): Properties {
    val props = Properties()
    val file = rootProject.file("config/$fileName")
    if (file.exists()) {
        file.inputStream().use { props.load(it) }
    }
    return props
}

val debugProps = loadFirestoreConfig("firestore-debug.properties")
val releaseProps = loadFirestoreConfig("firestore-release.properties")

android {
    namespace = "com.voterify.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.voterify.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        // Shared Firestore & OAuth properties from gradle.properties
        buildConfigField("String", "FIRESTORE_API_VERSION", "\"${project.findProperty("FIRESTORE_API_VERSION") ?: "v1"}\"")
        buildConfigField("String", "FIRESTORE_DEFAULT_REGION", "\"${project.findProperty("FIRESTORE_DEFAULT_REGION") ?: "nam5"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_USERS", "\"${project.findProperty("FIRESTORE_COLLECTION_USERS") ?: "users"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_VOTERS", "\"${project.findProperty("FIRESTORE_COLLECTION_VOTERS") ?: "voters"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_ELECTIONS", "\"${project.findProperty("FIRESTORE_COLLECTION_ELECTIONS") ?: "elections"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_CANDIDATES", "\"${project.findProperty("FIRESTORE_COLLECTION_CANDIDATES") ?: "candidates"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_REGIONS", "\"${project.findProperty("FIRESTORE_COLLECTION_REGIONS") ?: "regions"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_PARTIES", "\"${project.findProperty("FIRESTORE_COLLECTION_PARTIES") ?: "parties"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_PERMISSIONS", "\"${project.findProperty("FIRESTORE_COLLECTION_PERMISSIONS") ?: "permissions"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_VOTES", "\"${project.findProperty("FIRESTORE_COLLECTION_VOTES") ?: "votes"}\"")
        buildConfigField("String", "FIRESTORE_COLLECTION_BLOCKCHAIN", "\"${project.findProperty("FIRESTORE_COLLECTION_BLOCKCHAIN") ?: "blockchain_ledger"}\"")

        buildConfigField("String", "OAUTH_REDIRECT_SCHEME", "\"${project.findProperty("OAUTH_REDIRECT_SCHEME") ?: "voterify"}\"")
        buildConfigField("String", "OAUTH_APPLE_SERVICE_ID", "\"${project.findProperty("OAUTH_APPLE_SERVICE_ID") ?: "com.voterify.app.auth"}\"")
        buildConfigField("String", "OAUTH_GOOGLE_CLIENT_ID", "\"${project.findProperty("OAUTH_GOOGLE_CLIENT_ID") ?: "100000000000-voterifygoogleclient.apps.googleusercontent.com"}\"")
        buildConfigField("String", "OAUTH_FACEBOOK_APP_ID", "\"${project.findProperty("OAUTH_FACEBOOK_APP_ID") ?: "100000000000000"}\"")
        buildConfigField("String", "OAUTH_TWITTER_CLIENT_ID", "\"${project.findProperty("OAUTH_TWITTER_CLIENT_ID") ?: "voterify_twitter_oauth2_client_id"}\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "FIRESTORE_ENVIRONMENT", "\"${debugProps.getProperty("FIRESTORE_ENVIRONMENT", "development")}\"")
            buildConfigField("String", "FIRESTORE_PROJECT_ID", "\"${debugProps.getProperty("FIRESTORE_PROJECT_ID", "voterify-dev")}\"")
            buildConfigField("String", "FIRESTORE_DATABASE_ID", "\"${debugProps.getProperty("FIRESTORE_DATABASE_ID", "(default)")}\"")
            buildConfigField("String", "FIRESTORE_HOST", "\"${debugProps.getProperty("FIRESTORE_HOST", "10.0.2.2:8080")}\"")
            buildConfigField("boolean", "FIRESTORE_USE_SSL", debugProps.getProperty("FIRESTORE_USE_SSL", "false"))
            buildConfigField("boolean", "FIRESTORE_USE_EMULATOR", debugProps.getProperty("FIRESTORE_USE_EMULATOR", "true"))
            buildConfigField("String", "FIRESTORE_API_KEY", "\"${debugProps.getProperty("FIRESTORE_API_KEY", "AIzaSyDevelopmentVoterifyFirestoreKey00000")}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "FIRESTORE_ENVIRONMENT", "\"${releaseProps.getProperty("FIRESTORE_ENVIRONMENT", "production")}\"")
            buildConfigField("String", "FIRESTORE_PROJECT_ID", "\"${releaseProps.getProperty("FIRESTORE_PROJECT_ID", "voterify-production")}\"")
            buildConfigField("String", "FIRESTORE_DATABASE_ID", "\"${releaseProps.getProperty("FIRESTORE_DATABASE_ID", "(default)")}\"")
            buildConfigField("String", "FIRESTORE_HOST", "\"${releaseProps.getProperty("FIRESTORE_HOST", "firestore.googleapis.com")}\"")
            buildConfigField("boolean", "FIRESTORE_USE_SSL", releaseProps.getProperty("FIRESTORE_USE_SSL", "true"))
            buildConfigField("boolean", "FIRESTORE_USE_EMULATOR", releaseProps.getProperty("FIRESTORE_USE_EMULATOR", "false"))
            buildConfigField("String", "FIRESTORE_API_KEY", "\"${releaseProps.getProperty("FIRESTORE_API_KEY", "AIzaSyProductionVoterifyFirestoreKey99999")}\"")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        managedDevices {
            localDevices {
                create("pixel9ProApi35") {
                    device = "Pixel 9 Pro"
                    apiLevel = 35
                    systemImageSource = "google-atd"
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)
}
