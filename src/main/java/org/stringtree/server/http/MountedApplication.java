package org.stringtree.server.http;

public class MountedApplication {
	public final String prefix;
	public final Object application;
	
	public MountedApplication(String prefix, Object application) {
		this.prefix = prefix;
		this.application = application;
	}
}
