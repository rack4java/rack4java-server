package org.stringtree.nio;

public interface PacketHandlerFactory {
	PacketHandler create(Packet request);
}
