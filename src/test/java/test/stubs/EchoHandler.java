package test.stubs;

import java.io.IOException;

import org.stringtree.nio.Packet;
import org.stringtree.nio.PacketHandler;

public class EchoHandler implements PacketHandler {

	private StringBuilder buf;
	
	public EchoHandler() {
		this.buf = new StringBuilder();
	}

	@Override public boolean handle(Packet request) {
		if (request.length > 0) {
			String string = new String(request.data, request.offset, request.length);
			buf.append(string);
		}
		
		String body = buf.toString();

		if (body.endsWith("!")) {
			String header = "HTTP/1.0 200 OK\r\n" +
				"Content-Type: text/plain\r\n" +
				"Content-Length: " + body.length() + "\r\n" +
				"\r\n";
			byte[] data = (header + body).getBytes();
			try {
				request.sender.send(request.socket, data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		
		return false;
	}
}
