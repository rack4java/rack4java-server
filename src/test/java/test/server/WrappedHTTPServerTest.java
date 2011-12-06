package test.server;

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.rack4java.Rack;
import org.stringtree.nio.NioClient;
import org.stringtree.nio.server.http.HTTPRequestHandlerFactory;
import org.stringtree.nio.server.http.HTTPResponse;
import org.stringtree.nio.server.http.HTTPResponseHandler;
import org.stringtree.nio.server.http.HTTPServer;

import test.stubs.LocalServerApplication;
import test.stubs.RecordingHandler;

public class WrappedHTTPServerTest extends TestCase {
	protected static final int PORT = 24761;
	
	HTTPServer server;
	
	public void setUp() throws IOException {
		Rack application = new LocalServerApplication();
		
		server = new HTTPServer(null, PORT, new HTTPRequestHandlerFactory(application));
	}
	
	public void testRequest() throws IOException {
		call("Hello World!");
	}
	
	private void call(String text) throws IOException {
		assertTrue(server.activate(1000));
System.err.println("server started on port " + PORT + "...");

		StringBuilder cbuf = new StringBuilder();
		HTTPResponse response = new HTTPResponse();
		HTTPResponseHandler responseHandler = new HTTPResponseHandler(response);
		NioClient client = new NioClient(InetAddress.getLocalHost(), PORT, new RecordingHandler(cbuf, responseHandler));
//responseHandler.setVerbose(true);
		assertTrue(client.activate(1000));
		
		client.send(NioHTTPServerTest.simpleRequest("GET", "/", text));
		
		assertTrue(client.waitUntilHandled(1000));
		assertTrue(client.deactivate(2000));
		assertTrue(server.deactivate(2000));

		System.err.println("server stopped");
		System.err.println("client buf: "  + cbuf);
		assertTrue(cbuf.toString().endsWith("hello"));
	}

}
