package com.example.grpcsample.data

import com.example.grpc.greeter.GreeterGrpcKt
import com.example.grpc.greeter.HelloReply
import com.example.grpc.greeter.helloRequest
import com.example.grpc.greeter.streamRequest
import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.util.concurrent.TimeUnit

/**
 * Wraps the gRPC channel and the generated coroutine stub.
 *
 * One ManagedChannel should be reused for the lifetime of the app/screen: it is
 * expensive to create and manages its own connection pool.
 */
class GreeterRepository(host: String, port: Int, useTls: Boolean = false) : Closeable {

    private val channel: ManagedChannel = OkHttpChannelBuilder
        .forAddress(host, port)
        .apply { if (useTls) useTransportSecurity() else usePlaintext() }
        .keepAliveTime(30, TimeUnit.SECONDS)
        .build()

    private val stub = GreeterGrpcKt.GreeterCoroutineStub(channel)

    /** Unary call. Throws io.grpc.StatusException on failure. */
    suspend fun sayHello(name: String): HelloReply = withContext(Dispatchers.IO) {
        stub.withDeadlineAfter(5, TimeUnit.SECONDS)
            .sayHello(helloRequest { this.name = name })
    }

    /** Server-streaming call exposed as a cold Flow. */
    fun streamGreetings(name: String, count: Int): Flow<HelloReply> =
        stub.withDeadlineAfter(30, TimeUnit.SECONDS)
            .streamGreetings(streamRequest {
                this.name = name
                this.count = count
            })
            .flowOn(Dispatchers.IO)

    override fun close() {
        channel.shutdown().awaitTermination(2, TimeUnit.SECONDS)
    }
}
