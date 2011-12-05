package org.stringtree.nio;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

public interface Sender {
	void send(SelectableChannel channel, byte[]... data) throws IOException;
}
