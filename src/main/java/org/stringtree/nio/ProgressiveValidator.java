package org.stringtree.nio;

public interface ProgressiveValidator {
	public boolean isValidPart(StringBuilder buf);
	public boolean isValid(String whole);
}