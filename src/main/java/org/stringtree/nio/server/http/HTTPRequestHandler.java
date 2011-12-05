package org.stringtree.nio.server.http;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.stringtree.nio.Packet;
import org.stringtree.nio.Sender;
import org.stringtree.nio.http.HTTPAgent;
import org.stringtree.nio.http.HTTPMessage;
import org.stringtree.nio.http.HTTPPacketHandler;
import org.stringtree.server.rack4java.RackRequestProcessor;

public class HTTPRequestHandler extends HTTPPacketHandler {
	
	private RackRequestProcessor processor;
	private HTTPAgent agent;
	
	public HTTPRequestHandler(RackRequestProcessor processor) {
		super(new HTTPRequest());
		this.processor = processor;
		this.agent = new HTTPAgent();
	}

	@Override public boolean handle(Packet packet) {
		boolean finished = super.handle(packet);
		if (finished) {
//System.err.println("parsed request " + request);
			try {
				respond(packet.sender, packet.socket); 
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.message = new HTTPRequest();
		}

		return finished;
	}

	public void respond(Sender sender, SocketChannel socket) throws IOException {
		HTTPMessage response = processor.request(
				message.getPreamble(HTTPRequest.REQUEST_METHOD), 
				message.getPreamble(HTTPRequest.REQUEST_RESOURCE), 
				message.getPreamble(HTTPRequest.REQUEST_PROTOCOL), 
				message);
		
		if (null != response) {
			sender.send(socket, agent.buildHTTPMessage(response, HTTPAgent.Direction.RESPONSE, null));
		}
	}

}
