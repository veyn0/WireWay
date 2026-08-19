# WireWay

A Java library for sending typed messages over a byte-stream connection.

WireWay multiplexes several logical streams onto a single connection: payloads are
split into fragments, packed into length-prefixed frames, and reassembled on the
other side. On top of that it provides packet channels with request/response and
fire-and-forget delivery.

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

WireWay wireWay = new WireWay(
        SocketTransport.connect("localhost", 26656),
        new PacketRegistry(protocol),
        new ChannelSet(),
        64,   // max frame length
        32);  // max fragment length

PacketStream stream = wireWay.createPacketChannel(1);
stream.sendPacket(new HeartBeatPacket(), response -> { /* ... */ });

wireWay.sendPacketAsync(new HeartBeatPacket());
```

## Packages

| Package | Contents |
| --- | --- |
| `transport` | Byte-stream adapters (`SocketTransport`, `LoopbackTransport`) |
| `frame` | Frame and fragment encoding, channel abstraction |
| `frame.transmit` | Fragmentation, scheduling, reassembly |
| `protocol` | Packet interface, registries, id mapping |
| `service` | Entry point (`WireWay`) and packet streams |
| `util` | VarInt, buffers, id allocation |
