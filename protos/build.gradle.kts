// This module only packages the .proto files so that both the Android app and
// the server can generate code from the same contract via `protobuf(project(":protos"))`.
plugins {
    `java-library`
}

java {
    sourceSets.getByName("main").resources.srcDir("src/main/proto")
}
