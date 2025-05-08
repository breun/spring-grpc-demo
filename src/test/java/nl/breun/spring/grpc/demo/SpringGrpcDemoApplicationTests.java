package nl.breun.spring.grpc.demo;

import nl.breun.spring.grpc.demo.hello.v1.proto.HelloReply;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloRequest;
import nl.breun.spring.grpc.demo.hello.v1.proto.HelloV1Grpc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.test.AutoConfigureInProcessTransport;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureInProcessTransport
class SpringGrpcDemoApplicationTests {

	@Autowired
	private HelloV1Grpc.HelloV1BlockingStub stub;

	@Test
	@DirtiesContext
	void should_say_hello() {
		HelloReply response = stub.sayHello(request("Jack"));

		assertThat(response.getMessage()).isEqualTo("Hello Jack");
	}

	@Test
	@DirtiesContext
	void should_say_hello_five_times_when_streaming() {
        Iterator<HelloReply> responses = stub.streamHello(request("Alien"));

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

	private static HelloRequest request(String name) {
		return HelloRequest.newBuilder()
				.setName(name)
				.build();
	}
}
