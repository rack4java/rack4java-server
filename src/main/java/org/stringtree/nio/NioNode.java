package org.stringtree.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.LinkedList;

public class NioNode implements Runnable, Sender {
	protected static final int DFL_BUFFERSIZE = 8192;
	
	public static final int INACTIVE = 0; 
	public static final int ACTIVE = 16;
	
	public static final int STOPPED = INACTIVE + 1;
	public static final int STOPPING = INACTIVE + 2;
	public static final int STARTING = ACTIVE + 1;
	public static final int RUNNING = ACTIVE + 2;

	protected PacketHandler handler;

	private String name;
	private boolean server;
	private InetSocketAddress endpoint;
	private int bufferSize;
	private volatile int state;

	private Selector selector;
	private SelectableChannel channel;
	
	private LinkedList<ByteBuffer> outQueue;
	private ByteBuffer readBuffer;
	private boolean handled;

	private Thread thread;

	public NioNode(String name, boolean server, InetAddress host, int port, PacketHandler handler) {
		this.name = name;
		this.server = server;
		this.endpoint = new InetSocketAddress(host, port);
		this.handler = handler;
		this.bufferSize = DFL_BUFFERSIZE;
		
		setState(STOPPED);
	}
	
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public boolean activate(int timeout) throws IOException {
		setState(STARTING);
		
		outQueue = new LinkedList<ByteBuffer>();
		handled = false;

		readBuffer = ByteBuffer.allocate(bufferSize);
		selector = SelectorProvider.provider().openSelector();

		thread = new Thread(this, name);

		if (server) {
			channel = ServerSocketChannel.open();
			channel.configureBlocking(false);
			ServerSocket socket = ((ServerSocketChannel)channel).socket();

			// sometimes under repeated startup.shutdown load this reports as already bound
			// what can we do about this?
			socket.bind(endpoint);
			channel.register(selector, SelectionKey.OP_ACCEPT);
			setState(RUNNING);
		} else {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			((SocketChannel)channel).connect(endpoint);
			channel.register(selector, SelectionKey.OP_CONNECT);
			thread.setDaemon(true);
		}
		
		thread.start();
		
		return waitUntilReady(timeout);
	}
	
	public boolean deactivate(int timeout) {
		setState(STOPPING);
		thread.interrupt();
		try {
			synchronized (channel) {
				channel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return waitUntilStopped(timeout);
	}
	
	private synchronized void setState(int state) {
		this.state = state;
		notifyAll();
	}

	private synchronized void setHandled(boolean handled) {
		this.handled = handled;
		notifyAll();
	}

	public void send(SelectableChannel channel, byte[]... data) throws IOException {
		for (byte[] block : data) {
			outQueue.add(ByteBuffer.wrap(block));
		}
		
		SelectionKey key = channel.keyFor(selector);
		channel.register(selector, SelectionKey.OP_WRITE);
		synchronized(key.channel()) {
			write(key);
		}
		selector.wakeup();
	}

	public void send(byte[]... data) throws IOException {
		send(channel, data);
	}

	public void run() {
		while (state > ACTIVE) {
			try {
				setMode();
				
				selector.select();

				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					synchronized(key.channel()) {
						if (!key.isValid()) {
							continue;
						}

						if (key.isAcceptable()) {
							accept(key);
						} else if (key.isConnectable()) {
							connect(key);
						} else if (key.isReadable()) {
							read(key);
						} else if (key.isWritable()) {
							write(key);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		try {
			selector.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setState(STOPPED);
	}

	private void setMode() {
		SelectionKey key = channel.keyFor(selector);
		if (!outQueue.isEmpty()) {
			expressInterest(key, SelectionKey.OP_WRITE);
		}
	}

	private void connect(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			socketChannel.finishConnect();
			int ops = key.interestOps();
			ops &= ~SelectionKey.OP_CONNECT;
			key.interestOps(ops);
			setState(RUNNING);
		} catch (IOException e) {
			System.out.println(e);
			key.cancel();
		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ);
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			int nbytes = 1;
			while (nbytes >= 0) {
				nbytes = socketChannel.read(readBuffer);
				if (nbytes > 0) { 
					byte[] data = readBuffer.array();
					boolean result = handler.handle(new Packet(this, socketChannel, data, 0, nbytes));
					setHandled(result);
					readBuffer.clear();
					if (result) {
						key.cancel();
						break;
					}
				}
			}
			
		} catch (ClosedByInterruptException e) {
			socketChannel.close();
			key.cancel();
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		synchronized (outQueue) {
			while (!outQueue.isEmpty()) {
				ByteBuffer buf = outQueue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					return;
				}

				outQueue.remove(0);
			}
		}
		
		expressInterest(key, SelectionKey.OP_READ);
	}

	private void expressInterest(SelectionKey key, int next) {
		if (next != key.interestOps()) key.interestOps(next);
	}

	public boolean waitUntilReady(int timeout) {
		return waitForState(RUNNING, timeout);
	}

	public boolean waitUntilStopped(int timeout) {
		return waitForState(STOPPED, timeout);
	}
	
	public synchronized boolean waitForState(int desired, int timeout) {
		long end = System.currentTimeMillis() + timeout;
		while (state != desired) {
			long togo = end - System.currentTimeMillis();
			if (togo <= 0) break;

			try {
				wait(togo);
			} catch (InterruptedException e) {
				break;
			}
		}
		
		return state == desired;
	}
	
	public synchronized boolean waitUntilHandled(int timeout) {
		long end = System.currentTimeMillis() + timeout;
		while (!handled) {
			if (end <= System.currentTimeMillis()) return false;;

			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		
		return true;
	}
}
