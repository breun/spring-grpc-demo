package nl.breun.spring.grpc.demo;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloReply;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloRequest;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloV1Grpc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.grpc.test.autoconfigure.LocalGrpcServerPort;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;
import java.util.function.Consumer;

import static nl.breun.spring.grpc.demo.TestUtils.request;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing with a running server: https://docs.spring.io/spring-boot/reference/io/grpc.html#io.grpc.testing.local-server-port
 */
@SpringBootTest(properties = "spring.grpc.server.port=0")
class IntegrationTests {

    @LocalGrpcServerPort
    private int port;

    @Test
    void sayHello() {
        testHelloStub(stub -> {
            HelloRequest request = request("John");
            HelloReply helloReply = stub.sayHello(request);

            assertThat(helloReply.getMessage()).isEqualTo("Hello John");
        });
    }

    @Test
    void streamHello() {
        testHelloStub(stub -> {
            HelloRequest request = request("Alien");
            Iterator<HelloReply> responses = stub.streamHello(request);

            assertThat(responses)
                    .toIterable()
                    .extracting(HelloReply::getMessage)
                    .containsExactly(
                            "[#1] Hello Alien",
                            "[#2] Hello Alien",
                            "[#3] Hello Alien",
                            "[#4] Hello Alien",
                            "[#5] Hello Alien"
                    );
        });
    }

    private void testHelloStub(Consumer<HelloV1Grpc.HelloV1BlockingStub> consumer) {
        String target = "localhost:%s".formatted(this.port);
        ManagedChannel channel = NettyChannelBuilder.forTarget(target).usePlaintext().build();
        try {
            HelloV1Grpc.HelloV1BlockingStub hello = HelloV1Grpc.newBlockingStub(channel);
            consumer.accept(hello);
        } finally {
            channel.shutdown();
        }
    }
}
