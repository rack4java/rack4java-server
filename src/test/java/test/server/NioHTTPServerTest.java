package test.server;

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.rack4java.Context;
import org.rack4java.context.MapContext;
import org.stringtree.nio.NioClient;
import org.stringtree.nio.NioServer;
import org.stringtree.nio.server.http.HTTPRequestHandler;
import org.stringtree.nio.server.http.HTTPServer;
import org.stringtree.server.http.HTTPRequestProcessor;

import test.stubs.LocalServerApplication;

public class NioHTTPServerTest extends TestCase {
	InetAddress localhost;
	static final int PORT = 24761;
	
	HTTPServer server;
	
	public void setUp() throws IOException {
		localhost = InetAddress.getByName("localhost");
		Context<String> templates = new MapContext<String>();
		
		server = new HTTPServer(localhost, PORT, "", templates);
		server.mountApplication("/", new LocalServerApplication());

		assertTrue(server.activate(1000));
		System.err.println("server started on port " + PORT + "...");
	}
	
	public void tearDown() {
		assertTrue(server.deactivate(1000));
		System.err.println("server stopped");
	}
	
	public void testSyncRequest() throws IOException {
		call(server, "Hello World!");
	}
	
	private void call(NioServer nioServer, String text) throws IOException {
		StringBuilder cbuf = new StringBuilder();
		HTTPRequestProcessor processor = new HTTPRequestProcessor("", new MapContext<String>());
		NioClient client = new NioClient(localhost, PORT, new RecordingHandler(cbuf, new HTTPRequestHandler(processor)));
		assertTrue(client.activate(1000));
		
		client.send(simpleRequest("GET", "/", text));

		assertTrue(client.waitUntilHandled(1000));

		assertTrue(client.deactivate(1000));
		String returned = cbuf.toString();
		assertTrue(returned.endsWith("hello"));
	}

	public static byte[][] simpleRequest(String method, String path, String body, String type) {
		byte[] bodyBytes = body.getBytes();
		String header = buildHeader(method, path, bodyBytes.length, type);
		return new byte[][] { header.getBytes(), bodyBytes };
	}

	public static byte[][] simpleRequest(String method, String path, String body) {
		return simpleRequest(method, path, body, "text/plain");
	}

	public static byte[][] simpleRequest(String method, String path, byte[] bodyBytes, String type) {
		String header = buildHeader(method, path, bodyBytes.length, type);
		return new byte[][] { header.getBytes(), bodyBytes };
	}

	public static byte[][] simpleRequest(String method, String path, byte[] bodyBytes) {
		return simpleRequest(method, path, bodyBytes, "text/plain");
	}

	public static String buildHeader(String method, String path, int length, String type) {
		StringBuilder buf = new StringBuilder();
		buf.append(method.toUpperCase());
		buf.append(" ");
		buf.append(path);
		buf.append(" ");
		buf.append("HTTP/1.0\r\n");
		buf.append(EmoConstants.RAW_HEADER_CONTENT_TYPE);
		buf.append(": ");
		buf.append(type);
		buf.append("\r\n");
		buf.append(EmoConstants.RAW_HEADER_CONTENT_LENGTH);
		buf.append(": ");
		buf.append(length);
		buf.append("\r\n\r\n");
		String header = buf.toString();
		return header;
	}
	
	public static void main(String[] args) throws IOException {
		NioHTTPServerTest ss = new NioHTTPServerTest();
		ss.setUp();
	}
}
