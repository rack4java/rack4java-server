package org.stringtree.nio.server.http;

import java.net.InetAddress;
import java.util.List;

import org.rack4java.Context;
import org.stringtree.nio.NioServer;
import org.stringtree.nio.SocketRoutingHandler;
import org.stringtree.server.http.MountedApplication;

public class HTTPServer extends NioServer {
	private HTTPRequestHandlerFactory factory;
	
	public HTTPServer(InetAddress host, int port, HTTPRequestHandlerFactory factory) {
		super(host, port, new SocketRoutingHandler(factory));
		this.factory = factory;
	}

	public HTTPServer(InetAddress host, int port, String mount, Context<String> templates, List<MountedApplication> applications) {
		this(host, port, new HTTPRequestHandlerFactory(mount, templates, applications));
	}

	public HTTPServer(InetAddress host, int port, String mount, Context<String> templates) {
		this(host, port, new HTTPRequestHandlerFactory(mount, templates));
	}

	public HTTPServer(int port, String mount, Context<String> templates) {
		this(null, port, new HTTPRequestHandlerFactory(mount, templates));
	}
	
	public void mountApplication(String prefix, Object application) {
		factory.mountApplication(prefix, application);
	}
}
