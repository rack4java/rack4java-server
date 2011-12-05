package org.stringtree.nio;

import java.net.InetAddress;

public class NioServer extends NioNode {
	public NioServer(InetAddress host, int port, PacketHandler handler, String name) {
		super(name, true, host, port, handler);
	}
	
	public NioServer(InetAddress host, int port, PacketHandler handler) {
		this(host, port, handler, "server");
	}
}
