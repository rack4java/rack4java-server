package org.stringtree.nio;

public class Block {
	public final byte[] data;
	public final int offset;
	public final int length;
	public int cursor;
	
	public Block(byte[] data, int offset, int length) {
		this.data = data;
		this.offset = offset;
		this.length = length;
		this.cursor = 0;
	}
	
	public Block(byte[] data) {
		this(data, 0, data.length);
	}
	
	public byte next() {
		return cursor >= length ? -1 : data[offset + (cursor++)];
	}

	public boolean hasMore() {
		return cursor < length;
	}

	public int remaining() {
		return length - cursor;
	}

	public void copy(byte[] dest, int destOffset, int length) {
		System.arraycopy(data, cursor, dest, destOffset, length);
	}
	
	@Override public String toString() {
		return "Block[buf-len=" + data.length + ",offset=" + offset + ",len=" + length + ",cursor=" + cursor + "]";
	}
}