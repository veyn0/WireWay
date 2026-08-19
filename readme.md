# Wireway

A Java library for sending typed messages over a byte-stream connection.

WireWay multiplexes several logical streams onto a single connection: payloads are
split into fragments, packed into length-prefixed frames, and reassembled on the
other side. On top of that it provides packet channels with request/response and
fire-and-forget delivery.

## Status

early development, the API is not stable.