# gRPC Android Sample

A minimal Android app (Kotlin + Jetpack Compose) that calls a gRPC service, plus a local Kotlin gRPC server to talk to.

```
GrpcAndroidSample/
├── protos/   shared contract: protos/src/main/proto/greeter/greeter.proto
├── server/   Kotlin/JVM gRPC server (Netty) on port 50051
└── app/      Android client (OkHttp transport, protobuf-lite, coroutine stubs)
```

The `Greeter` service demonstrates two call types:

| RPC               | Type             | What the app does                                                                    |
|-------------------|------------------|--------------------------------------------------------------------------------------|
| `SayHello`        | Unary            | "Say hello" button → one reply                                                       |
| `StreamGreetings` | Server streaming | "Stream x5" button → replies arrive every 0.5 s and are collected as a Kotlin `Flow` |

## Requirements

- Android Studio (Ladybug 2024.2 or newer) with Android SDK 35
- JDK 17+

## Run it

1. **Open the project** in Android Studio (File → Open → `GrpcAndroidSample`). Let Gradle sync — Studio will create the Gradle wrapper if prompted.
2. **Start the server** (from the project root, or the Gradle panel → `server › application › run`):
   ```bash
   ./gradlew :server:run
   ```
   You should see `Greeter server listening on port 50051`.
3. **Run the `app` configuration** on an Android **emulator**. It connects to `10.0.2.2:50051`, which is the emulator's alias for your computer.

### Using a physical device

Either forward the port over USB:
```bash
adb reverse tcp:50051 tcp:50051
```
and change `GRPC_HOST` in `app/build.gradle.kts` to `"127.0.0.1"`, **or** set `GRPC_HOST` to your computer's LAN IP (e.g. `192.168.1.20`) and add that IP to `app/src/main/res/xml/network_security_config.xml`.

## How the client works

- `data/GreeterRepository.kt` builds one `ManagedChannel` with `OkHttpChannelBuilder` and creates the generated `GreeterGrpcKt.GreeterCoroutineStub`. Unary calls are `suspend` functions; streaming calls return a `Flow`. Each call sets a deadline.
- `ui/GreeterViewModel.kt` calls the repository from `viewModelScope`, maps `StatusException` to an error message, and closes the channel in `onCleared()`.
- `ui/GreeterScreen.kt` is the Compose UI.

Code generation is handled by the `com.google.protobuf` Gradle plugin. On Android it uses the **lite** runtime (`option("lite")`), which is smaller and is what gRPC recommends for mobile. The server uses the full runtime. Generated sources land in `app/build/generated/source/proto/`.

## Test the server without the app (optional)

The server enables reflection, so [grpcurl](https://github.com/fullstorydev/grpcurl) works:
```bash
grpcurl -plaintext -d '{"name":"World"}' localhost:50051 greeter.Greeter/SayHello
grpcurl -plaintext -d '{"name":"World","count":3}' localhost:50051 greeter.Greeter/StreamGreetings
```

## Going to production

- Use TLS: create the repository with `useTls = true` (calls `useTransportSecurity()`), and remove the cleartext entries from `network_security_config.xml`.
- Add auth with a `ClientInterceptor` that attaches a token to `Metadata`, or with `CallCredentials`.
- Keep a single channel per backend for the app's lifetime (e.g. provided by DI) instead of one per ViewModel.

## Adding your own RPCs

1. Edit or add `.proto` files under `protos/src/main/proto/`.
2. Rebuild — the stubs regenerate for both `app` and `server`.
3. Implement the new method in `server/.../GreeterServer.kt` and call it from `GreeterRepository`.

## Versions

Pinned in `gradle/libs.versions.toml`: AGP 8.7.3, Kotlin 2.1.0, gRPC-Java 1.68.1, gRPC-Kotlin 1.4.1, Protobuf 4.28.3, protobuf Gradle plugin 0.9.4. These are known to work together; bump them together if you upgrade.
