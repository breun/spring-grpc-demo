package nl.breun.spring.grpc.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HelloV1ClientTest {

    @Autowired
    private HelloV1Client client;

    @Test
    void should_say_hello() {
        String hello = client.sayHello("James Bond");

        assertThat(hello).isEqualTo("Hello James Bond");
    }

    @Test
    void should_stream_hello() {
        Iterator<String> hellos = client.streamHello("007");

        assertThat(hellos)
                .toIterable()
                .containsExactly(
                        "[#1] Hello 007",
                        "[#2] Hello 007",
                        "[#3] Hello 007",
                        "[#4] Hello 007",
                        "[#5] Hello 007"
                );
    }
}
