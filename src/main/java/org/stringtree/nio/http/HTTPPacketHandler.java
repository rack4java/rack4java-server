package org.stringtree.nio.http;

import org.stringtree.nio.Packet;
import org.stringtree.nio.PacketHandler;

public class HTTPPacketHandler implements PacketHandler {

	protected HTTPMessage message;

	public HTTPPacketHandler(HTTPMessage message) {
		this.message = message;
	}

	@Override public boolean handle(Packet packet) {
		if (!message.isComplete()) {
			this.message.append(packet.data, packet.offset, packet.length);
		}
		return message.isComplete();
	}

	public HTTPMessage getMessage() {
		return message;
	}
	
	public void setVerbose(boolean verbose) {
		message.setVerbose(verbose);
	}
}
