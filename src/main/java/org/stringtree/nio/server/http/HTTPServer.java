package org.stringtree.nio.server.http;

import java.net.InetAddress;

import org.stringtree.nio.NioServer;
import org.stringtree.nio.SocketRoutingHandler;

public class HTTPServer extends NioServer {
	private HTTPRequestHandlerFactory factory;
	
	public HTTPServer(InetAddress host, int port, HTTPRequestHandlerFactory factory) {
		super(host, port, new SocketRoutingHandler(factory));
		this.factory = factory;
	}
}
