package org.stringtree.nio;

import java.nio.channels.SocketChannel;

public class Packet {
	public Sender sender;
	public SocketChannel socket;
	public byte[] data;
	public int offset;
	public int length;
	
	public Packet(Sender sender, SocketChannel socket, byte[] data, int offset, int length) {
		this.sender = sender;
		this.socket = socket;
		this.data = data;
		this.offset = offset;
		this.length = length;
	}
	
	@Override public String toString() {
		return "packet[sender=" + sender + ",socket=" + socket + ",data=" + data + ",offset=" + offset + ",length=" + length+ "]";
	}
}