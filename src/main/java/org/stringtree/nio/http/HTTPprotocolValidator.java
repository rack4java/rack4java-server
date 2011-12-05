package org.stringtree.nio.http;

import org.stringtree.nio.PatternProgressiveValidator;

public class HTTPprotocolValidator extends PatternProgressiveValidator {
	public HTTPprotocolValidator() {
		super("HTTP/1.0", "HTTP/1.1");
	}
}