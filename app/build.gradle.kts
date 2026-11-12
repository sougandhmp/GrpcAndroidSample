import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.protobuf)
}

android {
    namespace = "com.example.grpcsample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.grpcsample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Default server address. 10.0.2.2 is the host machine as seen from the Android emulator.
        buildConfigField("String", "GRPC_HOST", "\"10.0.2.2\"")
        buildConfigField("int", "GRPC_PORT", "50051")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    protobuf(project(":protos"))

    // gRPC (lite runtime is the recommended choice on Android)
    implementation(libs.grpc.okhttp)
    implementation(libs.grpc.protobuf.lite)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.protobuf.kotlin.lite)
    implementation(libs.kotlinx.coroutines.android)

    // Android / Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
}

protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }
    plugins {
        id("grpc") { artifact = libs.protoc.gen.grpc.java.get().toString() }
        id("grpckt") { artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar" }
    }
    generateProtoTasks {
        all().configureEach {
            builtins {
                id("java") { option("lite") }
                id("kotlin") { option("lite") }
            }
            plugins {
                id("grpc") { option("lite") }
                id("grpckt") { option("lite") }
            }
        }
    }
}
