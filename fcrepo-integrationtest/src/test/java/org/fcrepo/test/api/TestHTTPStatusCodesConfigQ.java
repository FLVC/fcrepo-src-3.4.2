/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.test.api;

import org.fcrepo.test.FedoraServerTestCase;

import junit.framework.Test;
import junit.framework.TestSuite;

import static org.fcrepo.test.api.TestHTTPStatusCodes.RI_SEARCH_PATH;
import static org.fcrepo.test.api.TestHTTPStatusCodes.checkError;


/**
 * HTTP status code tests to be run when API-A authentication is off and the
 * resource index is disabled.
 *
 * @author Chris Wilper
 */
public class TestHTTPStatusCodesConfigQ
        extends FedoraServerTestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite("TestHTTPStatusCodesConfigQ TestSuite");
        suite.addTestSuite(TestHTTPStatusCodesConfigQ.class);
        return suite;
    }

    //---
    // API-A Lite: riSearch
    //---

    public void testRISearch_Disabled() throws Exception {
        checkError(RI_SEARCH_PATH);
    }

}
