package nl.breun.spring.grpc.demo;

import nl.breun.spring.grpc.demo.hello.v1.proto.HelloReply;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloRequest;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloV1Grpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.stream.IntStream;

@Service
class HelloV1Service extends HelloV1Grpc.HelloV1ImplBase {

    private static final Logger log = LoggerFactory.getLogger(HelloV1Service.class);

    @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        String name = request.getName();
        log.info("Say hello: {}", name);

        String message = String.format("Hello %s", name);
        HelloReply reply = hello(message);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void streamHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        String name = request.getName();
        log.info("Stream hello: {}", name);

        IntStream.range(0, 5).forEach(index -> {
            String message = String.format("[#%d] Hello %s", index + 1, name);
            HelloReply reply = hello(message);
            responseObserver.onNext(reply);
            try {
                Thread.sleep(Duration.ofMillis(200));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                responseObserver.onError(e);
            }
        });

        responseObserver.onCompleted();
    }

    private static HelloReply hello(String message) {
        return HelloReply.newBuilder()
                .setMessage(message)
                .build();
    }
}
