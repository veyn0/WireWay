# Frame

The transport byte stream is a sequence of frames:

```
[payloadLength : VarInt] [fragments : bytes]
```

- `payloadLength` counts the fragment bytes only, not its own prefix.
- The payload consists of one or more fragments (see `fragment.md`) that end
  exactly on the frame boundary; a fragment overrunning it is a protocol error.
- Declared lengths are bounded by `LengthPrefixed.MAX_RECORD_BYTES` (1 MiB);
  anything larger is a protocol error.

VarInts are unsigned LEB128, at most 5 bytes for a 32-bit value.
