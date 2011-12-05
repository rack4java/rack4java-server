package test.server;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class NioTests extends TestCase {
    
    public static TestSuite suite() {
        TestSuite ret = new TestSuite();

        ret.addTestSuite(NodeTests.class);
        ret.addTestSuite(WrappedHTTPServerTest.class);
        ret.addTestSuite(NioHTTPServerTest.class);
        ret.addTestSuite(NioServerTest.class);
        return ret;
    }

}
