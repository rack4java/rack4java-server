package org.stringtree.nio;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SocketRoutingHandler implements PacketHandler {
	
	private PacketHandlerFactory factory;
	private Map<Socket, PacketHandler> handlers;

	public SocketRoutingHandler(PacketHandlerFactory factory) {
		this.factory = factory;
		this.handlers = new HashMap<Socket, PacketHandler>();
	}

	public SocketRoutingHandler(Map<Socket, PacketHandler>handlers) {
		this.factory = null;
		this.handlers = handlers;
	}

	public SocketRoutingHandler() {
		this.factory = null;
		this.handlers = Collections.synchronizedMap(new HashMap<Socket,PacketHandler>());
	}

	@Override public boolean handle(Packet request) {
		Socket key = request.socket.socket();
		if (null == key) {
			throw new UnsupportedOperationException("SocketRoutingHandler can only route with a non-null socket");
		}
		
		PacketHandler handler = handlers.get(key);
		if (null == handler && null != factory) {
			handler = factory.create(request);
			handlers.put(key, handler);
		}
		
		if (null != handler) {
			boolean handled = handler.handle(request);
			if (true == handled) {
				handlers.remove(key);
			}
			return handled;
		}
		
		return false;
	}
	
	public void addHandler(Socket socket, PacketHandler handler) {
		handlers.put(socket, handler);
	}

}
