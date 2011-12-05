package org.stringtree.nio.http;

import org.stringtree.nio.PatternProgressiveValidator;

public class HTTPMethodValidator extends PatternProgressiveValidator {
	public HTTPMethodValidator() {
		super("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS", "TRACE", "CONNECT");
	}
}