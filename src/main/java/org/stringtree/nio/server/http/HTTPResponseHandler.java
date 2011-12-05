package org.stringtree.nio.server.http;

import org.stringtree.nio.http.HTTPPacketHandler;

public class HTTPResponseHandler extends HTTPPacketHandler {

	public HTTPResponseHandler(HTTPResponse response) {
		super(response);
	}

	public HTTPResponseHandler() {
		super(new HTTPResponse());
	}

}
