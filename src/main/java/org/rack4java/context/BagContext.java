package org.rack4java.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.rack4java.Context;
import org.rack4java.context.ContextEntry;

public class BagContext<T> implements Context<T> {
	private Collection<Map.Entry<String,T>> entries;
	
	public BagContext() {
		this.entries = new ArrayList<Map.Entry<String,T>>(); 
	}

	@Override public T get(String key) {
		for (Map.Entry<String,T> entry : entries) {
			if (entry.getKey().equals(key)) return entry.getValue();
		}
		return null;
	}

	@Override public Context<T> with(String key, T value) {
		entries.add(new ContextEntry<T>(key,value));
		return this;
	}

	@Override public Iterator<Map.Entry<String, T>> iterator() {
		return entries.iterator();
	}

	@Override public T remove(String key) {
		Map.Entry<String,T> found = null;
		for (Map.Entry<String,T> entry : entries) {
			if (entry.getKey().equals(key)) {
				found = entry;
				break;
			}
		}
		
		if (null != found) {
			entries.remove(found);
			return found.getValue();
		}
		
		return null;
	}
	
	@Override public String toString() {
		return entries.toString();
	}
}
