package org.stringtree.nio.server.http;

import org.rack4java.Rack;
import org.stringtree.nio.Packet;
import org.stringtree.nio.PacketHandler;
import org.stringtree.nio.PacketHandlerFactory;
import org.stringtree.server.rack4java.RackRequestProcessor;

public class HTTPRequestHandlerFactory implements PacketHandlerFactory {
	
	private RackRequestProcessor processor;

	public HTTPRequestHandlerFactory(Rack application) {
		this.processor = new RackRequestProcessor(application);
	}

	// TODO consider pooling these?
	@Override public PacketHandler create(Packet request) {
		return new HTTPRequestHandler(processor);
	}
}
