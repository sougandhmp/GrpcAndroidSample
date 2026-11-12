import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf)
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    protobuf(project(":protos"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.grpc.protobuf)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.services)       // server reflection (handy with grpcurl)
    implementation(libs.grpc.kotlin.stub)
    implementation(libs.protobuf.kotlin)
}

protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }
    plugins {
        id("grpc") { artifact = libs.protoc.gen.grpc.java.get().toString() }
        id("grpckt") { artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar" }
    }
    generateProtoTasks {
        all().configureEach {
            plugins {
                id("grpc")
                id("grpckt")
            }
            builtins {
                id("kotlin")
            }
        }
    }
}

application {
    mainClass.set("com.example.grpc.server.GreeterServerKt")
}
