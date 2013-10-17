package test.server;

import java.io.IOException;

import junit.framework.TestCase;

import org.rack4java.Context;
import org.rack4java.Rack;
import org.rack4java.RackResponse;
import org.stringtree.nio.NioClient;
import org.stringtree.nio.NioServer;
import org.stringtree.nio.server.http.HTTPRequestHandler;
import org.stringtree.server.rack4java.RackRequestProcessor;

import test.stubs.EchoHandler;
import test.stubs.RecordingHandler;

public class NioServerTest extends TestCase {
	private static final int PORT = 24761;
	StringBuilder buf;
	NioServer server;
	Rack application;
	
	public void setUp() {
		buf = new StringBuilder();
		server = new NioServer(null, PORT, new RecordingHandler(buf, new EchoHandler()));
		application = new Rack() {
			@Override public RackResponse call(Context<String> environment) throws Exception {
				return new RackResponse(200).withBody("OK");
			}
		};
	}
	
	public void testBigBufferSmallText() throws IOException {
		server.setBufferSize(1024);
		call("Hello World!");
	}
	
	public void testExactBufferMultiple() throws IOException {
		server.setBufferSize(16);
		call( 
				"To be, or not to be, that is the question:\n" +
				"Wh!");
	}
	
	public void testSmallBufferBigText() throws IOException {
		server.setBufferSize(16);
		call(
				"To be, or not to be, that is the question:\n" +
				"Whether 'tis nobler in the mind to suffer\n" +
				"The slings and arrows of outrageous fortune,\n" +
				"Or to take arms against a sea of troubles!");
	}

	private void call(String text) throws IOException {
		assertTrue(server.activate(1000));
System.err.println("server started on port " + PORT + "...");

		StringBuilder cbuf = new StringBuilder();
		RackRequestProcessor processor = new RackRequestProcessor(application);
		NioClient client = new NioClient(null, PORT, new RecordingHandler(cbuf, new HTTPRequestHandler(processor)));
		assertTrue(client.activate(1000));
		
		client.send(NioHTTPServerTest.simpleRequest("GET", "/", text));
		assertTrue(client.waitUntilHandled(1000));
		
		assertTrue(client.deactivate(1000));
		assertTrue(server.deactivate(1000));
		
System.err.println("server stopped");
		assertTrue(cbuf.toString().endsWith(text));
	}
}
