package test.stubs;

import org.stringtree.nio.DelegatedHandler;
import org.stringtree.nio.Packet;
import org.stringtree.nio.PacketHandler;

public class RecordingHandler extends DelegatedHandler {

	private StringBuilder buf;

	public RecordingHandler(StringBuilder buf, PacketHandler handler) {
		super(handler);
		this.buf = buf;
	}
	
	public byte[] empty = new byte[0];

	@Override public boolean handle(Packet request) {
		if (null == handler) {
			throw new IllegalStateException("can forward to null handler");
		}
//System.err.println("RecordingHandler about to pass on packet: " + request);
		boolean done = handler.handle(request);
		if (request.length > 0) {
			String string = new String(request.data, request.offset, request.length);
//System.err.println("recording [" + string + "] handler (" + handler.getClass().getSimpleName() + ") reports done=" + done);
			buf.append(string);
		} else {
//System.err.println("recording empty packet handler reports done=" + done);
		}
		return done;
	}

}
