package xyz.wireway.wire;

/** Signals malformed or out-of-contract bytes on the wire. */
public class ProtocolException extends RuntimeException {

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
