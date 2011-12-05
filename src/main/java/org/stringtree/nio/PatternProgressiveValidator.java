package org.stringtree.nio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternProgressiveValidator implements ProgressiveValidator {
	private static final int WHOLE = -1;
	private Map<Integer, List<String>> partialValues;

	public PatternProgressiveValidator(List<String> fullValues) {
		partialValues = new HashMap<Integer, List<String>>(fullValues.size()+1);
		for (String value : fullValues) {
			addParts(value);
		}
	}

	public PatternProgressiveValidator(String... fullValues) {
		partialValues = new HashMap<Integer, List<String>>(fullValues.length+1);
		for (String value : fullValues) {
			addParts(value);
		}
	}
	
	@Override public boolean isValidPart(StringBuilder buf) {
		int n = buf.length();
		if (0 == n) return true;
		if (!partialValues.containsKey(n)) return false;
		
		String part = buf.toString();
		List<String> parts = partialValues.get(n);
		return parts.contains(part);
	}

	@Override public boolean isValid(String whole) {
		List<String> parts = partialValues.get(WHOLE);
		return null != parts && parts.contains(whole);
	}
	
	protected void addParts(String whole) {
		for (int i = 1; i <= whole.length(); ++i) {
			addPart(i, whole.substring(0, i));
		}
		addPart(WHOLE, whole);
	}

	protected void addPart(int i, String part) {
		List<String> parts = partialValues.get(i);
		if (null == parts) {
			parts = new ArrayList<String>();
			partialValues.put(i, parts);
		}
		
		parts.add(part);
	}

	public void addValue(String string) {
		addParts(string);
	}
}