# Spring gRPC Demo

**_Note: Spring gRPC will be added to Spring Boot via [!47288](https://github.com/spring-projects/spring-boot/pull/47288)._** 

This service is a demo for the (currently still experimental) [Spring gRPC](https://docs.spring.io/spring-grpc/reference/index.html), based on Spring Boot.
gRPC was created by Google and is open source.
gRPC has official implementations for many programming languages, including Java and Go.

## But we already have API technologies like REST and GraphQL

gRPC can be seen as a competitor to API technologies like REST and GraphQL, which already have some form of support in the Spring ecosystem.

Why gRPC seems interesting:

* gRPC provides an API first experience with code generation provided out of the box.
* gRPC has very good performance. It uses an optimized binary protocol on the wire (Protobuf) and HTTP/2 features. Some figures claim up to ~10x better performance than other API technologies.
* gRPC is a rich protocol: next to unary request/response it also supports server streaming, client streaming, bidirectional streaming, deadlines/timeouts and cancellations (so the other side knows they can stop their work).

A sweet spot for gRPC seems to be service-to-service communication.
Due to its dependency on HTTP/2, it currently cannot be used directly from a browser without a proxy.

## gRPC is not new, right?

No, gRPC itself has been around since 2016, when it was released as a successor to Google's previous RPC infrastructure ("Stubby") to connect the large number of Google's microservices.
Prior to Spring gRPC, there were already two other third party efforts to provide gRPC support for Spring Boot ([net.devh:grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter) and [io.github.lognet:grpc-spring-boot-starter](https://github.com/LogNet/grpc-spring-boot-starter)), but these never attracted large communities.
The former has archived its GitHub repository already by now, and the latter hasn't seen a release since September 2023.
I definitely have a lot more trust in the longevity of the Spring gRPC project, because it's an official Spring offering.

## Code Generation

Spring gRPC generates Java code at build time from the [`hello-v1.proto`](src/main/proto/hello-v1.proto) Protobuf definition file in `src/main/proto`.
The Java package that gets used for the generated code is configured in this file, like this:

```
option java_package = "nl.breun.spring.grpc.demo.hello.v1.proto";
```

This option makes sure that classes will be generated in multiple separate files:

```
option java_multiple_files = true;
```

See `target/generated-sources/protobuf` after a build to see the code that was generated from the [`hello-v1.proto`](src/main/proto/hello-v1.proto) file.

```
target/
  generated-sources/
    protobuf/
      grpc-java/
        com/bol/spring/grpc/demo/hello/v1/proto/
          HelloGrpc.java
      java/
        com/bol/spring/grpc/demo/hello/v1/proto/
          HelloReply.java
          HelloReplyOrBuilder.java
          HelloRequest.jav
          HelloRequestOrBuilder.java
          Hello.java
```

Note any comments in the Protobuf file also get used in the Javadoc for the generated classes and methods.

An application developer would then create an implementation of the service based on this generated code, which includes service stubs and model classes.
See the [`HelloService`](src/main/java/nl/breun/spring/grpc/demo/HelloV1Service.java) class in this demo application for an example.

## Demo

Build and start the service:

```
❯ ./mvnw verify spring-boot:run
```

Explore the service, for instance using [grpcurl](https://github.com/fullstorydev/grpcurl):

```
❯ grpcurl -plaintext localhost:9090 describe
(...)
HelloV1 is a service:
service HelloV1 {
  rpc SayHello ( .HelloRequest ) returns ( .HelloReply );
  rpc StreamHello ( .HelloRequest ) returns ( stream .HelloReply );
}
```

Want to know what a `HelloRequest` looks like?

```
❯ grpcurl -plaintext localhost:9090 describe HelloRequest
HelloV1Request is a message:
message HelloRequest {
  string name = 1;
}
```

We now know enough to make our first request:

```
❯ grpcurl -plaintext -d '{"name":"Nils"}' localhost:9090 HelloV1/SayHello
{
  "message": "Hello Nils"
}
```

Let's also try the streaming use case, which emits a value every second:

```
❯ grpcurl -plaintext -d '{"name":"Nils"}' localhost:9090 HelloV1/StreamHello
{
  "message": "Hello(1) Nils"
}
(...1 new message every 200 ms...)
{
  "message": "Hello(5) Nils"
}
```

## Testing / Client

See [SpringGrpcDemoApplicationTests](src/test/java/com/bol/spring/grpc/demo/SpringGrpcDemoApplicationTests.java) for an example of integration tests, which use the generated stub to connect to the service.

## gRPC on Tomcat instead of Netty

Spring gRPC uses Netty for gRPC services by default, although it also possible to run gRPC services on Tomcat.
If you want to run on Tomcat, add the `org.springframework.boot:spring-boot-starter-web` and `io.grpc:grpc-servlet-jakarta` dependencies.

## Workflow for communication between services

Some options, from lo-fi to hi-fi:

1. Consumer copies the server's `proto` file into its repository. (AFAIK A lot of bol teams work this way.)
2. Consumer fetches the `proto` file from somewhere else.
   1. The service provider provides the `proto` file in a JAR and shares it, e.g. via Artifactory. (This only helps JVM consumers.)
   2. The service provider publishes their `proto` file to a [Protobuf registry](https://www.google.com/search?q=protobuf+registry).
3. Consumer depends on an artifact which contains pre-generated client code ("stubs" in Protobuf speak).
   1. Maintained by the consumer team.
   2. Provided by the service provider. This does require the service provider providing pre-generated client code artifacts for all of the programming languages used by the consumers of the service.
   3. Provided by a central service. This does require the central service to provide pre-generated client code artifacts for all of the programming languages that consumers may want to use. (This is similar to the approach used by RAGE's [API registry](https://gitlab.bol.io/api-1st/api-registry).)

## Known issues

### Workaround for known issue with gRPC on Tomcat with `grpcurl` for streaming use cases

When using Tomcat (not the default), you'll need to customize Tomcat like this to avoid issues with `grpcurl` in streaming use cases:

```
@Bean
public TomcatConnectorCustomizer customizer() {
	return connector -> {
		for (UpgradeProtocol protocol : connector.findUpgradeProtocols()) {
			if (protocol instanceof Http2Protocol http2Protocol) {
				// Workaround for grpcurl sending lots of WINDOW_UPDATE frames, which Tomcat finds suspicious
				// See https://github.com/spring-projects-experimental/spring-grpc/issues/97#issuecomment-2604822164
				// This workaround is not necessary for the grpc-java client
				http2Protocol.setOverheadWindowUpdateThreshold(0);
			}
		}
	};
}
```