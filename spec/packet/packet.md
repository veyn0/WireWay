# Packet record

```
[payloadLength : VarInt] [packetId : VarInt] [body : bytes]
```

- `payloadLength` counts `packetId` plus `body`.
- `packetId` is derived like channel type ids: sorted 64-bit SHA-256 hashes of
  the registered packet names.

## Sequential packet channel

Each record on the sequential (request/response) channel is:

```
[request : byte] [referenceId : VarInt] [packet record]
```

- `request` bit 0x01: 1 = request, 0 = response.
- `referenceId` links a response to its request; 0 means no response expected.

## Async packet channel

Plain packet records, one short-lived stream per send.
