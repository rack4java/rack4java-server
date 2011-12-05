package org.stringtree.nio;

import java.net.InetAddress;

public class NioClient extends NioNode {
	public NioClient(InetAddress host, int port, PacketHandler handler, String name) {
		super(name, false, host, port, handler);
	}

	public NioClient(InetAddress host, int port, PacketHandler handler) {
		this(host, port, handler, "client");
	}
}
