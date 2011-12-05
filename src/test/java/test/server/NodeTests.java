package test.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.stringtree.nio.NioClient;
import org.stringtree.nio.NioNode;
import org.stringtree.nio.NioServer;
import org.stringtree.nio.server.http.HTTPRequestHandler;
import org.stringtree.nio.server.http.HTTPResponseHandler;

import test.stubs.EchoHandler;
import test.stubs.RecordingHandler;

public class NodeTests extends TestCase {
	private InetAddress localhost;
	private static final int PORT = 24761;
	
	StringBuilder buf;
	HTTPRequestHandler requestHandler;
	HTTPResponseHandler responseHandler;
	
	public void setUp() throws UnknownHostException {
		localhost = InetAddress.getByName("localhost");
		buf = new StringBuilder();
//		HTTPRequestProcessor processor = new HTTPRequestProcessor("", new MapContext<String>());
//		requestHandler = new HTTPRequestHandler(processor);
		responseHandler = new HTTPResponseHandler();
	}
	
	public void testStartupShutdown() throws IOException, InterruptedException {
		NioNode node = new NioNode("node", true, localhost, PORT, new RecordingHandler(buf, requestHandler));
		assertTrue(node.activate(1000));
		assertTrue(node.deactivate(1000));
	}
	
	public void testSendExternal() throws IOException, InterruptedException {
		NioClient client = new NioClient(InetAddress.getByName("www.google.com"), 80, new RecordingHandler(buf, responseHandler));
		assertTrue(client.activate(1000));
		
		client.send("GET / HTTP/1.0\r\nContent-Length:0\r\n\r\n".getBytes());
		
		assertTrue(client.waitUntilHandled(1000));
		
		assertTrue(client.deactivate(1000));
//		System.err.println(buf);
	}
	
	public void testSendInternal() throws IOException, InterruptedException {
		StringBuilder sbuf = new StringBuilder();
		NioServer server = new NioServer(localhost, PORT, new RecordingHandler(sbuf, new EchoHandler()));
		assertTrue(server.activate(1000));

		StringBuilder cbuf = new StringBuilder();
		NioClient client = new NioClient(localhost, PORT, new RecordingHandler(cbuf, requestHandler));
		assertTrue(client.activate(1000));

		client.send("hello!".getBytes());
		assertTrue(client.waitUntilHandled(1000));
		
		assertTrue(client.deactivate(1000));
		assertTrue(server.deactivate(1000));
		
//		System.err.println("server buf: "  + sbuf);
//		System.err.println("client buf: "  + cbuf);
		assertTrue(sbuf.toString().contains("hello!"));
		assertTrue(cbuf.toString().contains("hello!"));
	}
}
