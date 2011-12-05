package org.stringtree.nio.http;

import java.util.Map;

import org.rack4java.Context;
import org.rack4java.context.BagContext;
import org.rack4java.context.MapContext;

public class HTTPAgent {
	private static final byte[] empty = new byte[0];
	
	public enum Direction { REQUEST, RESPONSE };
	private Context<String> commonHeaders;
	private Context<String> commonCookies;
	
	private static Map<Direction, String[]> defaults = new LiteralMap<HTTPAgent.Direction, String[]>(
			Direction.REQUEST, new String[] { "GET", "/", "HTTP/1.0" }, 
			Direction.RESPONSE, new String[] { "HTTP/1.0", "500", null } 
		);

	public HTTPAgent(Context<String> commonHeaders, Context<String> commonCookies) {
		this.commonHeaders = commonHeaders;
		this.commonCookies = commonCookies;
	}
	
	public HTTPAgent() {
		this(new BagContext<String>(), new MapContext<String>());
	}

	public byte[][] buildHTTPMessage(Tract payload, Direction direction, Context<String> headers) {
		String[] preamble = new String[] {
			extractOrDefault(payload, EmoConstants.PREAMBLE_PREFIX+0, defaults.get(direction)[0]),
			extractOrDefault(payload, EmoConstants.PREAMBLE_PREFIX+1, defaults.get(direction)[1]),
			extractOrDefault(payload, EmoConstants.PREAMBLE_PREFIX+2, defaults.get(direction)[2])
		};
		if (direction == Direction.RESPONSE && null == preamble[HTTPResponse.RESPONSE_MESSAGE]) {
			preamble[HTTPResponse.RESPONSE_MESSAGE] = defaultMessage(preamble[HTTPResponse.RESPONSE_CODE]);
		}
		return buildHTTPMessage(preamble, payload, headers);
	}

	private String defaultMessage(String code) {
		// TODO add different messages for different codes according to RFC 
		if (code.startsWith("5")) return "Error";
		return "OK";
	}
	
	private String extractOrDefault(Context<String> context, String key, String dfl) {
		String value = context.get(key);
		if (null != value) {
			context.remove(key);
		} else {
			value = dfl;
		}
		return value;
	}

	public byte[][] buildHTTPMessage(String[] preamble, Tract payload, Context<String> requestHeaders) {
		StringBuilder header = new StringBuilder();
		
		header.append(preamble[0]);
		header.append(" ");
		header.append(preamble[1]);
		header.append(" ");
		header.append(preamble[2]);
		header.append("\r\n");

        if (null != commonCookies) for (ContextEntry<String> cookie : commonCookies) {
			addHeader("Cookie", cookie.getValue(), header);
        }

        if (null != commonHeaders) for (ContextEntry<String> entry : commonHeaders) {
			String name = entry.getKey();
			if (!name.startsWith(EmoConstants.PREAMBLE_PREFIX)) {
				addHeader(name, entry.getValue(), header);
			}
        }

        if (null != requestHeaders) for (ContextEntry<String> entry : requestHeaders) {
			addHeader(entry.getKey(), entry.getValue(), header);
        }

        byte[] content = null;
        int length = 0;
        
        if (null != payload) {
        	for (ContextEntry<String> entry : payload) {
	        	if (entry.getKey().startsWith(EmoConstants.HEADER_PREFIX)) {
					String name = entry.getKey().substring(EmoConstants.HEADER_PREFIX.length());
	        		if (!isProhibitedHeader(name)) {
						addHeader(name, entry.getValue(), header);
					}
	        	}
	        }

			content = payload.getBodyAsBytes();
			length = content.length;
			String type = extractOrDefault(payload, EmoConstants.HEADER_CONTENT_TYPE, "text/plain");
	        
			if (length > 0) {
				addHeader(EmoConstants.RAW_HEADER_CONTENT_TYPE, type, header);
			}
        } else {
        	content = empty;
        	length = 0;
        }

		addHeader(EmoConstants.RAW_HEADER_CONTENT_LENGTH, length, header);
		header.append("\r\n");
		
		return new byte[][] { header.toString().getBytes(), content };
	}

	private boolean isProhibitedHeader(String name) {
		return 
			EmoConstants.RAW_HEADER_CONTENT_TYPE.equalsIgnoreCase(name) || 
			EmoConstants.RAW_HEADER_CONTENT_LENGTH.equalsIgnoreCase(name);
	}
	
	private void addHeader(String key, Object value, StringBuilder header) {
		if (key.startsWith(EmoConstants.HEADER_PREFIX)) key = key.substring(EmoConstants.HEADER_PREFIX.length());
		header.append(key);
		header.append(": ");
		header.append(value);
		header.append("\r\n");
	}
    
    public void setHeader(String name, String value) {
    	commonHeaders.put(name, value);
    }
    
    public void setCookie(String name, String value) {
       commonCookies.put(name, "$Version=0; " + name + "=" + value);
    }

}
