package nl.breun.spring.grpc.demo;

import nl.breun.spring.grpc.demo.hello.v1.proto.HelloReply;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloRequest;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloV1Grpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.grpc.test.autoconfigure.AutoConfigureTestGrpcTransport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.ImportGrpcClients;

import java.util.Iterator;

import static nl.breun.spring.grpc.demo.TestUtils.request;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * <a href="https://docs.spring.io/spring-boot/reference/io/grpc.html#io.grpc.testing.test-transport">Using In-Process Test Transport</a>
 */
@SpringBootTest
@AutoConfigureTestGrpcTransport
@ImportGrpcClients(types = HelloV1Grpc.HelloV1BlockingStub.class)
class InProcessTests {

	@Autowired
	private HelloV1Grpc.HelloV1BlockingStub stub;

	@Test
	void should_say_hello() {
		HelloRequest request = request("Jack");
		HelloReply response = stub.sayHello(request);

		assertThat(response.getMessage()).isEqualTo("Hello Jack");
	}

	@Test
	void should_say_hello_five_times_when_streaming() {
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
	}
}
