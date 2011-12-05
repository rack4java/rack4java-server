package org.stringtree.nio.server.http;

import java.util.ArrayList;
import java.util.List;

import org.rack4java.Context;
import org.stringtree.nio.Packet;
import org.stringtree.nio.PacketHandler;
import org.stringtree.nio.PacketHandlerFactory;
import org.stringtree.server.http.HTTPRequestProcessor;
import org.stringtree.server.http.MountedApplication;

public class HTTPRequestHandlerFactory implements PacketHandlerFactory {
	
	private HTTPRequestProcessor processor;

	public HTTPRequestHandlerFactory(String mount, Context<String> templates, List<MountedApplication> applications) {
		this.processor = new HTTPRequestProcessor(mount, templates, applications);
	}

	public HTTPRequestHandlerFactory(String mount, Context<String> templates) {
		this.processor = new HTTPRequestProcessor(mount, templates, new ArrayList<MountedApplication>());
	}

	public void mountApplication(String prefix, Object application) {
		processor.mountApplication(prefix, application);
	}

	// TODO consider pooling these?
	@Override public PacketHandler create(Packet request) {
		return new HTTPRequestHandler(processor);
	}
}
