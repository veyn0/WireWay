# WireWay

A Java library for sending typed messages over a byte-stream connection.

WireWay multiplexes several logical streams onto a single connection: payloads are
split into fragments, packed into length-prefixed frames, and reassembled on the
other side. On top of that it provides packet channels with request/response and
fire-and-forget delivery. The wire format is documented in `spec/`.

## Status

`0.1-ALPHA` — early development, the API is not stable.

## Requirements

Java 21, Maven.

## Build

```
mvn package
```

## Usage

```java
Protocol protocol = new Protocol();
protocol.register(HeartBeatPacket.class);
PacketRegistry packets = new PacketRegistry(protocol);

WireWay wireWay = new WireWay(
        SocketTransport.connect("localhost", 26656),
        packets,
        64,   // max frame length
        32);  // max fragment length

PacketStream stream = wireWay.openStream(1);
stream.setListener(request -> new HeartBeatPacket());          // answer requests
stream.send(new HeartBeatPacket(), response -> { /* ... */ }); // request/response

wireWay.sendPacket(new HeartBeatPacket());                     // fire-and-forget
```

## Packages

| Package | Contents |
| --- | --- |
| `transport` | Byte-stream adapters (`SocketTransport`, `LoopbackTransport`) |
| `wire` | Wire format: varints, length prefixes, frames, fragments, stream headers |
| `channel` | `DataSource`/`DataSink` abstraction and channel type registry |
| `transmit` | Outbound scheduling and frame assembly, inbound decoding and demultiplexing |
| `protocol` | Packet interface, registries, id mapping |
| `service` | Entry point (`WireWay`) and packet streams |
| `util` | Buffers, id allocation, name hashing |
