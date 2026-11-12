package com.example.grpc.server

import com.example.grpc.greeter.GreeterGrpcKt
import com.example.grpc.greeter.HelloReply
import com.example.grpc.greeter.HelloRequest
import com.example.grpc.greeter.StreamRequest
import com.example.grpc.greeter.helloReply
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.protobuf.services.ProtoReflectionService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Implementation of the Greeter service using grpc-kotlin coroutine stubs. */
class GreeterService : GreeterGrpcKt.GreeterCoroutineImplBase() {

    override suspend fun sayHello(request: HelloRequest): HelloReply {
        val name = request.name.trim()
        if (name.isEmpty()) {
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("name must not be empty"))
        }
        println("SayHello <- $name")
        return helloReply {
            message = "Hello, $name! 👋"
            timestampMillis = System.currentTimeMillis()
        }
    }

    override fun streamGreetings(request: StreamRequest): Flow<HelloReply> = flow {
        val count = request.count.coerceIn(1, 20)
        println("StreamGreetings <- ${request.name} x$count")
        for (i in 1..count) {
            emit(helloReply {
                message = "Greeting $i of $count for ${request.name}"
                timestampMillis = System.currentTimeMillis()
            })
            delay(500) // simulate work so the streaming is visible in the UI
        }
    }
}

class GreeterServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(GreeterService())
        .addService(ProtoReflectionService.newInstance())
        .build()

    fun start() {
        server.start()
        println("Greeter server listening on port $port")
        Runtime.getRuntime().addShutdownHook(Thread {
            println("Shutting down gRPC server")
            server.shutdown()
        })
    }

    fun blockUntilShutdown() = server.awaitTermination()
}

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 50051
    GreeterServer(port).apply {
        start()
        blockUntilShutdown()
    }
}
