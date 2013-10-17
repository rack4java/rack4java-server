package test.stubs;

import java.util.Map;

import org.rack4java.Context;
import org.rack4java.Rack;
import org.rack4java.RackResponse;

public class LocalServerApplication implements Rack {
	private boolean verbose;
	
	public LocalServerApplication(boolean verbose) {
		this.verbose = verbose;
	}
	
	public LocalServerApplication() {
		this(false);
	}

	private RackResponse index() {
		if (verbose) System.err.println("in LocalServerApplication.index");
		return new RackResponse(200).withBody("hello");
	}

	private RackResponse ugh() {
		if (verbose) System.err.println("in LocalServerApplication.ugh");
		return new RackResponse(200).withBody("zot");
	}

	@Override public RackResponse call(Context<String> environment) throws Exception {
System.err.println("LocalServerApplication call env=" + dumpContext(environment));
		String path = (String) environment.get(PATH_INFO);
		if (path.endsWith("ugh")) return ugh();
		return index();
	}

	private String dumpContext(Context<String> environment) {
		StringBuilder ret = new StringBuilder("{ ");
		for (Map.Entry<String, Object> entry : environment) {
			ret.append(entry.getKey());
			ret.append("=");
			ret.append(entry.getValue());
			ret.append(" ");
		}
		ret.append("}");
		return ret.toString();
	}
}
