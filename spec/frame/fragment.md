# Fragment

One chunk of one stream inside a frame:

```
[streamId : VarInt] [flags : byte] [dataLength : VarInt] [data : bytes]
```

Flags:

| Bit | Meaning |
| --- | --- |
| 0x02 | start — first fragment of the stream |
| 0x01 | end — last fragment of the stream |

- `streamId` is allocated by the sender and only unique per direction. It is
  released after the end fragment is written and may then be reused; the
  receiver drops its mapping when the end fragment arrives.
- A fragment for a stream id that is not open (no start fragment seen) is a
  protocol error, as is a start fragment for an id that is still open.

## Stream header

The first bytes of every stream's data (possibly split across fragments) are:

```
[channelTypeId : VarInt] [endpointId : VarInt]
```

`channelTypeId` selects the channel type; ids are derived on both peers by
sorting the 64-bit SHA-256 name hashes of all registered channel names.
`endpointId` is the application-level address within that channel type.
