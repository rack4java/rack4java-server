package org.stringtree.nio.server.http;

import org.stringtree.nio.ProgressiveValidator;
import org.stringtree.nio.http.HTTPMessage;
import org.stringtree.nio.http.HTTPMethodValidator;

public class HTTPRequest extends HTTPMessage {
	public static final int REQUEST_METHOD = 0;
	public static final int REQUEST_RESOURCE = 1;
	public static final int REQUEST_PROTOCOL = 2;
	
	private static final ProgressiveValidator methodValidator = new HTTPMethodValidator();
	
	public HTTPRequest() {
		preambleValidator[0] = methodValidator;
		preambleValidator[2] = protocolValidator;
	}

	public String getMethod() {
		return getPreamble(REQUEST_METHOD);
	}

	public String getResource() {
		return getPreamble(REQUEST_RESOURCE);
	}

	public String getProtocol() {
		return getPreamble(REQUEST_PROTOCOL);
	}
}
