package nl.breun.spring.grpc.demo;

import nl.breun.spring.grpc.demo.hello.v1.proto.HelloRequest;

final class TestUtils {

    static HelloRequest request(String name) {
        return HelloRequest.newBuilder()
                .setName(name)
                .build();
    }
}
