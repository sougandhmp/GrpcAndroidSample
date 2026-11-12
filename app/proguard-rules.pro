# Keep generated protobuf-lite message fields (accessed reflectively by the lite runtime).
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# gRPC / OkHttp optional dependencies that are not present on Android.
-dontwarn com.google.common.**
-dontwarn javax.naming.**
-dontwarn okio.**
-dontwarn com.squareup.okhttp.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
