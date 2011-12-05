package org.stringtree.nio.server.http;

import java.util.regex.Pattern;

import org.stringtree.nio.ProgressiveValidator;
import org.stringtree.nio.http.HTTPMessage;

class HTTPStatusCodeValidator implements ProgressiveValidator {
	private static final Pattern pattern = Pattern.compile("\\d{3}");
	
	@Override public boolean isValidPart(StringBuilder buf) {
		int len = buf.length();
		if (0 == len) return true;
		if (len > 3) return false;
		
		char[] chars = buf.toString().toCharArray();
		for (char c : chars) {
			if (!Character.isDigit(c)) return false;
		}
		
		return true;
	}

	@Override public boolean isValid(String whole) {
		return pattern.matcher(whole).matches();
	}
}

public class HTTPResponse extends HTTPMessage {
	public static final int RESPONSE_PROTOCOL = 0;
	public static final int RESPONSE_CODE = 1;
	public static final int RESPONSE_MESSAGE = 2;

	private static final ProgressiveValidator statusCodeValidator = new HTTPStatusCodeValidator();

	public HTTPResponse() {
		preambleValidator[0] = protocolValidator;
		preambleValidator[1] = statusCodeValidator;
	}
	
	private HTTPResponse(int code, String message) {
		preamble[RESPONSE_CODE] = Integer.toString(code);
		preamble[RESPONSE_MESSAGE] = message;
	}

	public String getProtocol() {
		return getPreamble(RESPONSE_PROTOCOL);
	}

	public String getCode() {
		return getPreamble(RESPONSE_CODE);
	}

	public String getMessage() {
		return getPreamble(RESPONSE_MESSAGE);
	}

	public static HTTPResponse createExceptionResponse(String message) {
		return new HTTPResponse(500, message);
	}
}
