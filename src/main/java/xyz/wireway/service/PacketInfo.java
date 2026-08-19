package xyz.wireway.service;

import xyz.wireway.protocol.Packet;

record PacketInfo(boolean request, int referenceId, int endpointId, Packet packet) {}
