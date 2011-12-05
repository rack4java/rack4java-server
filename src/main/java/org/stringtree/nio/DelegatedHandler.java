package org.stringtree.nio;

public abstract class DelegatedHandler implements PacketHandler {
	protected PacketHandler handler;

	public DelegatedHandler(PacketHandler handler) {
		this.handler = handler;
	}

	public DelegatedHandler() {
		this(null);
	}

	public PacketHandler getHandler() {
		return handler;
	}

	public void setHandler(PacketHandler handler) {
		this.handler = handler;
	}

	@Override public boolean handle(Packet packet) {
		return null != handler ? handler.handle(packet) : false;
	}
}
