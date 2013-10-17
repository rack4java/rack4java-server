package org.stringtree.server.rack4java;

import java.io.IOException;
import java.util.Arrays;

import org.rack4java.Context;
import org.rack4java.Rack;
import org.rack4java.context.FallbackContext;
import org.rack4java.context.MapContext;
import org.stringtree.nio.http.HTTPMessage;
import org.stringtree.nio.server.http.HTTPResponse;

public class RackRequestProcessor {
	private static final Context<String> commonEnvironment = new MapContext<String>()
	    .with(Rack.RACK_VERSION, Arrays.asList(0, 2))
	    .with(Rack.RACK_ERRORS, System.err)
	    .with(Rack.RACK_MULTITHREAD, true)
	    .with(Rack.RACK_MULTIPROCESS, true)
	    .with(Rack.RACK_RUN_ONCE, false);
	
	private final Rack application;

	public RackRequestProcessor(Rack application) {
		this.application = application;
	}

	public HTTPResponse request(String method, String path, String protocol, HTTPMessage request) throws IOException {
		// TODO add headers and other stuff for a complete Rack request
		Context<String> topcontext = new MapContext<String>()
			.with(Rack.REQUEST_METHOD, method)
			.with(Rack.PATH_INFO, path);
		
		@SuppressWarnings("unchecked") Context<String> environment = new FallbackContext<String>(topcontext, commonEnvironment);
		HTTPResponse ret;
		try {
			Context<String> response = application.call(environment);
			ret = createHTTPResponse(response);
		} catch (Exception e) {
			e.printStackTrace();
			ret = HTTPResponse.createExceptionResponse(e.getMessage());
		}
		
		return ret;
	}

	private HTTPResponse createHTTPResponse(Context<String> response) {
		return new HTTPResponse();
	}

}
