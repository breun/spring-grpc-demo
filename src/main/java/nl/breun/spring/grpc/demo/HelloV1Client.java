package nl.breun.spring.grpc.demo;

import nl.breun.spring.grpc.demo.hello.v1.proto.HelloReply;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloRequest;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloV1Grpc;
import com.google.common.collect.Iterators;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class HelloV1Client {

    private final HelloV1Grpc.HelloV1BlockingStub stub;

    public HelloV1Client(HelloV1Grpc.HelloV1BlockingStub stub) {
        this.stub = stub;
    }

    public String sayHello(String name) {
        HelloRequest request = request(name);
        HelloReply reply = stub.sayHello(request);
        return reply.getMessage();
    }

    public Iterator<String> streamHello(String name) {
        HelloRequest request = request(name);
        Iterator<HelloReply> iterator = stub.streamHello(request);
        return Iterators.transform(iterator, HelloReply::getMessage);
    }

    private static HelloRequest request(String name) {
        return HelloRequest.newBuilder()
                .setName(name)
                .build();
    }
}
