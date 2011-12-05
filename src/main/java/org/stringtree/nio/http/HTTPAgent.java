package org.stringtree.nio.http;

import java.util.HashMap;
import java.util.Map;

import org.rack4java.Context;
import org.rack4java.Rack;
import org.rack4java.context.BagContext;
import org.rack4java.context.MapContext;
import org.stringtree.nio.server.http.HTTPResponse;

public class HTTPAgent {
	private static final byte[] empty = new byte[0];
	
	public enum Direction { REQUEST, RESPONSE };
	private Context<String> commonHeaders;
	private Context<String> commonCookies;
	
	private static Map<Direction, String[]> defaults;
	static {
		defaults = new HashMap<HTTPAgent.Direction, String[]>();
		defaults.put(Direction.REQUEST, new String[] { "GET", "/", "HTTP/1.0" }); 
		defaults.put(Direction.RESPONSE, new String[] { "HTTP/1.0", "500", null }); 
	}

	public HTTPAgent(Context<String> commonHeaders, Context<String> commonCookies) {
		this.commonHeaders = commonHeaders;
		this.commonCookies = commonCookies;
	}
	
	public HTTPAgent() {
		this(new BagContext<String>(), new MapContext<String>());
	}

	public byte[][] buildHTTPMessage(HTTPMessage payload, Direction direction, Context<String> headers) {
		String[] preamble = new String[] {
			extractOrDefault(payload, 0, defaults.get(direction)[0]),
			extractOrDefault(payload, 1, defaults.get(direction)[1]),
			extractOrDefault(payload, 2, defaults.get(direction)[2])
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
	
	private String extractOrDefault(HTTPMessage message, int index, String dfl) {
		String ret = message.getPreamble(index);
		return null != ret ? ret : dfl;
	}

	public byte[][] buildHTTPMessage(String[] preamble, Tract payload, Context<String> requestHeaders) {
		StringBuilder header = new StringBuilder();
		
		header.append(preamble[0]);
		header.append(" ");
		header.append(preamble[1]);
		header.append(" ");
		header.append(preamble[2]);
		header.append("\r\n");

        if (null != commonCookies) for (Map.Entry<String,String> cookie : commonCookies) {
			addHeader("Cookie", cookie.getValue(), header);
        }

        if (null != commonHeaders) for (Map.Entry<String,String> entry : commonHeaders) {
			String name = entry.getKey();
			if (!name.startsWith(HTTPMessage.PREAMBLE_PREFIX)) {
				addHeader(name, entry.getValue(), header);
			}
        }

        if (null != requestHeaders) for (Map.Entry<String,String> entry : requestHeaders) {
			addHeader(entry.getKey(), entry.getValue(), header);
        }

        byte[] content = null;
        int length = 0;
        
        if (null != payload) {
        	for (Map.Entry<String,String> entry : payload) {
	        	if (entry.getKey().startsWith(Rack.HTTP_)) {
					String name = entry.getKey().substring(Rack.HTTP_.length());
	        		if (!isProhibitedHeader(name)) {
						addHeader(name, entry.getValue(), header);
					}
	        	}
	        }

			content = payload.getBodyAsBytes();
			length = content.length;
			String type = extractOrDefault(payload, Rack.HTTP_CONTENT_TYPE, "text/plain");
	        
			if (length > 0) {
				addHeader("Content-Type", type, header);
			}
        } else {
        	content = empty;
        	length = 0;
        }

		addHeader("Content-Length", length, header);
		header.append("\r\n");
		
		return new byte[][] { header.toString().getBytes(), content };
	}

	private boolean isProhibitedHeader(String name) {
		return 
			"Content-Type".equalsIgnoreCase(name) || 
			"Content-Length".equalsIgnoreCase(name);
	}
	
	private void addHeader(String key, Object value, StringBuilder header) {
		if (key.startsWith(Rack.HTTP_)) key = key.substring(Rack.HTTP_.length());
		header.append(key);
		header.append(": ");
		header.append(value);
		header.append("\r\n");
	}
    
    public void setHeader(String name, String value) {
    	commonHeaders.with(name, value);
    }
    
    public void setCookie(String name, String value) {
       commonCookies.with(name, "$Version=0; " + name + "=" + value);
    }

}
