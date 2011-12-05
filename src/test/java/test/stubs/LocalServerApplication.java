package test.stubs;

public class LocalServerApplication {
	private boolean verbose;
	
	public LocalServerApplication(boolean verbose) {
		this.verbose = verbose;
	}
	
	public LocalServerApplication() {
		this(false);
	}

	public String index() {
		if (verbose) System.err.println("in LocalServerApplication.index");
		return "hello";
	}

	public String ugh() {
		if (verbose) System.err.println("in LocalServerApplication.ugh");
		return "zot";
	}
}
