/**
 * Framing and stream multiplexing between the transport and channels.
 *
 * <p>Threading contract: all inbound decoding runs on the transport's receive thread;
 * all outbound assembly runs on the single transmit-loop thread. Data sources drained
 * by the loop must synchronize internally when producers write from other threads.
 * Application callbacks must be dispatched off the receive thread (see the service layer).
 */
package xyz.wireway.transmit;
