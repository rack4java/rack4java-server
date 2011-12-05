package test.stubs;

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

	@Override public RackResponse call(Context<Object> environment) throws Exception {
		String path = (String) environment.get(PATH_INFO);
		if (path.endsWith("ugh")) return ugh();
		return index();
	}
}
