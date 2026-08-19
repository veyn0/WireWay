package xyz.wireway.channel;

/** Creates the inbound sink for one incoming stream of a channel type. */
@FunctionalInterface
public interface SinkFactory {

    DataSink create(int endpointId);
}
