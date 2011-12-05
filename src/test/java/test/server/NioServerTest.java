package test.server;

import java.io.IOException;

import junit.framework.TestCase;

import org.rack4java.context.MapContext;
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
	
	public void setUp() {
		buf = new StringBuilder();
		server = new NioServer(null, PORT, new RecordingHandler(buf, new EchoHandler()));
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
		RackRequestProcessor processor = new RackRequestProcessor("", new MapContext<String>());
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
