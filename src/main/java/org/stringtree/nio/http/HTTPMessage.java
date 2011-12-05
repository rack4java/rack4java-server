package org.stringtree.nio.http;

import java.util.Arrays;
import java.util.LinkedList;

import org.rack4java.context.BagContext;
import org.stringtree.nio.Block;
import org.stringtree.nio.ProgressiveValidator;

public class HTTPMessage extends BagContext<Object> {
	private static final char NO_CHAR = (char) -1;
	private static int SMALL = 65536;
	private static enum State { P1, P2, P3, NAME, VALUE, BLANKLINE, BODY, COMPLETE, ERROR }
	
	protected static final ProgressiveValidator protocolValidator = new HTTPprotocolValidator();
	
	protected String[] preamble = new String[3];
	protected ProgressiveValidator[] preambleValidator = new ProgressiveValidator[3];
	protected int length = -1;
	
	private byte[] bodybuffer;
	private int bodyCursor = 0;
	
	private State state = State.P1;
	private char c = NO_CHAR;
	private StringBuilder buf;
	private LinkedList<Block> data;
	private String headerName;
	
	private boolean verbose = false;
	
	public HTTPMessage(String headerPrefix) {
		data = new LinkedList<Block>();
		buf = new StringBuilder();
		this.headerPrefix = headerPrefix;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void append(byte[] block) {
		if (block.length > 0) {
			data.add(new Block(block));
			advance();
		}
	}

	public void append(byte[] block, int offset, int length) {
		if (block.length > 0) {
			data.add(new Block(block, offset, length));
			advance();
		}
	}

	private void advance() {
		while (!data.isEmpty()) {
			Block current = data.getFirst();
			if (verbose) System.err.println("advance: got block " + current);
			if (verbose) System.err.println("advance state=" + state);
			if (State.P1 == state) {
				if (NO_CHAR == c) c = (char) current.next();
				preamble[0] = extractPreamble(' ', State.P2, 0);
			} else if (State.P2 == state) {
				preamble[1] = extractPreamble(' ', State.P3, 1);
			} else if (State.P3 == state) {
				preamble[2] = extractPreamble('\n', State.NAME, 2);
			} else if (State.NAME == state) {
				c = (char) current.next();
				if (c == '\r') {
					state = State.BLANKLINE;
				} else {
					headerName = extractWord(':', State.VALUE, null).trim();
				}
			} else if (State.VALUE == state) {
				String headerValue = extractWord('\n', State.NAME, null).trim();
				with(headerPrefix+headerName, headerValue);
				if ("Content-Length".equalsIgnoreCase(headerName)) {
					length = Integer.parseInt(headerValue);
					if (verbose) System.err.println("HTTPMessage read content-length " + length);
				}
			} else if (State.BLANKLINE == state) {
				c = (char) current.next();
				state = State.BODY;
			} else if (State.BODY == state) {
				if (length <= 0) {
					state = State.COMPLETE;
				} else if (length <= SMALL) {
					if (null == bodybuffer) {
						bodybuffer = new byte[length];
						bodyCursor = 0;
					}
					int available = current.remaining();
					int toCopy = Math.min(available, length);
					current.copy(bodybuffer, bodyCursor, toCopy);
					bodyCursor += toCopy;
					if (bodyCursor >= length) {
						setBody(bodybuffer);
						state = State.COMPLETE;
					}
					data.removeFirst();
				} else if (length > SMALL) {
					// TODO memory-mapped io?
					throw new UnsupportedOperationException();
				}
				break;
			} else if (State.COMPLETE == state || State.ERROR == state) {
				break;
			} else {
				System.err.println("Oops - unknown request parser state " + state);
				break;
			}
			if (verbose) System.err.println(" advance, c=" + c + " nblocks=" + data.size() + " buf=" + buf + " state=" + state + " pre=" + Arrays.toString(preamble) + " hname=" + headerName);
		}
		if (state == State.COMPLETE) {
			data.clear();
		}
		if (verbose) System.err.println("advanced, c=" + c + " nblocks=" + data.size() + " buf=" + buf + " state=" + state + " pre=" + Arrays.toString(preamble));
	}
	
	private String extractPreamble(char end, State next, int index) {
		String ret = extractWord(end, next, preambleValidator[index]);
		if (null != ret) put(EmoConstants.PREAMBLE_PREFIX + index, ret);
		return ret;
	}

	private String extractWord(char end, State next, ProgressiveValidator validator) {
		Block current = data.getFirst();

		int scooped = 1;
		String ret = null;
		if (c == NO_CHAR) c = (char) current.next();
		scooped += scoop(current, end);
		if (null != validator && scooped > 0) {
			if (!validator.isValidPart(buf)) {
				state = State.ERROR;
				next = State.ERROR;
			}
		}
		
		if (end == c) {
			ret = buf.toString();
			if (null != validator && state != State.ERROR && !validator.isValid(ret)) {
				state = State.ERROR;
			} else {
				state = next;
			}
			buf.setLength(0);
			c = NO_CHAR;
		}

		return ret;
	}

	private int scoop(Block current, char end) {
		int scooped = 0;
		while(c != end) {
			if (verbose) System.err.println("considering [" + c + "]");
			if (c != '\r') {
				buf.append(c);
				++scooped;
			}
			if (!current.hasMore()) {
				c = NO_CHAR;
				break;
			}
			c = (char) current.next();
		}
		if (!current.hasMore()) {
			data.removeFirst();
		}
		
		return scooped;
	}

	public boolean isComplete() {
		return state == State.COMPLETE || state == State.ERROR;
	}

	public int getlength() {
		return length;
	}
	
	public String getPreamble(int item) {
		return item >= 0 && item < 3 ? preamble[item] : null;
	}
	
	public boolean isValid() {
		return state != State.ERROR;
	}
}
