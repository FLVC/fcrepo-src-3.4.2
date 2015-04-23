/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.test.api;

import java.io.File;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.Map;

import org.apache.axis.types.NonNegativeInteger;

import org.apache.commons.io.FileUtils;

import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.fcrepo.common.Constants;

import org.fcrepo.server.access.FedoraAPIA;
import org.fcrepo.server.management.FedoraAPIM;
import org.fcrepo.server.types.gen.ComparisonOperator;
import org.fcrepo.server.types.gen.Condition;
import org.fcrepo.server.types.gen.FieldSearchQuery;
import org.fcrepo.server.types.gen.FieldSearchResult;

import org.fcrepo.test.FedoraServerTestCase;
import org.fcrepo.test.TemplatedResourceIterator;

/**
 * Test APIM based on templating of resource files
 *
 * @author Stephen Bayliss
 * @version $Id$
 */
public class TestAPIM2
        extends FedoraServerTestCase
        implements Constants {

    private FedoraAPIM apim;
    private FedoraAPIA apia;

    public static Test suite() {
        TestSuite suite = new TestSuite("TestAPIM2 TestSuite");
        suite.addTestSuite(TestAPIM2.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        apim = getFedoraClient().getAPIM();
        apia = getFedoraClient().getAPIA();


        Map<String, String> nsMap = new HashMap<String, String>();
        nsMap.put("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
        nsMap.put("dc", "http://purl.org/dc/elements/1.1/");
        nsMap.put("foxml", "info:fedora/fedora-system:def/foxml#");
        NamespaceContext ctx = new SimpleNamespaceContext(nsMap);
        XMLUnit.setXpathNamespaceContext(ctx);

        // not really necessary, but will cope with any junk left from other tests
        purgeDemoObjects();

    }

    @Override
    public void tearDown() throws Exception {
        // assumes all our test objects are in the demo namespace
        purgeDemoObjects();

        XMLUnit.setXpathNamespaceContext(SimpleNamespaceContext.EMPTY_CONTEXT);
    }

    public void testIngest() throws Exception {

        String resourceDirName = "src/test/resources/APIM2/foxml/";
        String[] resourceFilenames = new File(resourceDirName).list();

        // ingest resources, substituting from file "values"
        int count = 0; // count ingested objects
        for (String resourceFilename : resourceFilenames) {
            File resourceFile = new File(resourceDirName + resourceFilename);
            if (resourceFile.isFile()) {
                String resource = FileUtils.readFileToString(resourceFile, "UTF-8");
                TemplatedResourceIterator tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/values");
                while (tri.hasNext()) {
                    byte[] foxml = tri.next().getBytes("UTF-8");
                    apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");
                    count++;
                }
            }
        }

        assertEquals("Ingested object count", count, getDemoObjects().size());

        // ingest resources, substituting from file "valuesplain"
        for (String resourceFilename : resourceFilenames) {
            File resourceFile = new File(resourceDirName + resourceFilename);
            if (resourceFile.isFile()) {
                String resource = FileUtils.readFileToString(resourceFile, "UTF-8");
                TemplatedResourceIterator tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
                while (tri.hasNext()) {
                    byte[] foxml = tri.next().getBytes("UTF-8");
                    apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");
                    count++;
                }
            }
        }

        assertEquals("Ingested object count", count, getDemoObjects().size());

    }

    public void testFieldSearch() throws Exception {

        // get some sample objects ingested
        String resourceDirName = "src/test/resources/APIM2/foxml/";
        String[] resourceFilenames = new File(resourceDirName).list();

        for (String resourceFilename : resourceFilenames) {
            File resourceFile = new File(resourceDirName + resourceFilename);
            if (resourceFile.isFile()) {
                String resource = FileUtils.readFileToString(resourceFile, "UTF-8");
                TemplatedResourceIterator tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
                while (tri.hasNext()) {
                    byte[] foxml = tri.next().getBytes("UTF-8");
                    apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");
                }
            }
        }

        String[] resultFields = {"pid", "title"};
        NonNegativeInteger maxResults = new NonNegativeInteger("100");


        String termsTemplate = "$value$";
        TemplatedResourceIterator tri = new TemplatedResourceIterator(termsTemplate, "src/test/resources/APIM2/searchvalues");
        while (tri.hasNext()) {
            FieldSearchQuery query;
            FieldSearchResult res;

            // using conditions
            Condition[] conditions = {new Condition("pid", ComparisonOperator.fromString("eq"), tri.getAttributeValue("value"))};
            query = new FieldSearchQuery(conditions, null);
            try {
                res = apia.findObjects(resultFields, maxResults, query);
            } catch (RemoteException e) {
                if (!e.getMessage().startsWith("org.fcrepo.server.errors.QueryParseException"))
                    throw e;
            }

            String terms = tri.next();
            query = new FieldSearchQuery(null, terms);
            try {
                res = apia.findObjects(resultFields, maxResults, query);
            } catch (RemoteException e) {
                if (!e.getMessage().startsWith("org.fcrepo.server.errors.QueryParseException"))
                    throw e;
            }

        }

        purgeDemoObjects();

        for (String resourceFilename : resourceFilenames) {
            File resourceFile = new File(resourceDirName + resourceFilename);
            if (resourceFile.isFile()) {
                String resource = FileUtils.readFileToString(resourceFile, "UTF-8");
                tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
                while (tri.hasNext()) {
                    byte[] foxml = tri.next().getBytes("UTF-8");
                    apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");
                }
            }
        }

        purgeDemoObjects();

    }
    public void testObjectMethods() throws Exception {
        // test object
        String resfile = "src/test/resources/APIM2/foxml/demo_SmileyBeerGlass.xml";

        File resourceFile = new File(resfile);
        String resource = FileUtils.readFileToString(resourceFile, "UTF-8");
        TemplatedResourceIterator tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
        while (tri.hasNext()) {
            String label2 = tri.getAttributeValue("label2");
            byte[] foxml = tri.next().getBytes("UTF-8");
            String pid = apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");

            // update object label with new value
            apim.modifyObject(pid, null, label2, null, "updating object label");

        }

        purgeDemoObjects();

        String resourceDirName = "src/test/resources/APIM2/foxml/";
        String[] resourceFilenames = new File(resourceDirName).list();
        for (String resourceFilename : resourceFilenames) {
            resourceFile = new File(resourceDirName + resourceFilename);
            if (resourceFile.isFile()) {
                resource = FileUtils.readFileToString(resourceFile, "UTF-8");
                tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
                while (tri.hasNext()) {
                    byte[] foxml = tri.next().getBytes("UTF-8");
                    apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");
                }
            }
        }

        purgeDemoObjects();

    }
    public void testDatastreamMethods() throws Exception {
        // test object
        String resfile = "src/test/resources/APIM2/foxml/demo_SmileyBeerGlass.xml";

        File resourceFile = new File(resfile);
        String resource = FileUtils.readFileToString(resourceFile, "UTF-8");
        TemplatedResourceIterator tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
        while (tri.hasNext()) {
            String label2 = tri.getAttributeValue("label2");
            byte[] foxml = tri.next().getBytes("UTF-8");
            String pid = apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");

            // modify datastream label
            apim.modifyDatastreamByValue(pid, "DC", null, label2, null, null, null, null, null, "modify datastream label", false);

        }

        purgeDemoObjects();

        String resourceDirName = "src/test/resources/APIM2/foxml/";
        String[] resourceFilenames = new File(resourceDirName).list();
        for (String resourceFilename : resourceFilenames) {
            resourceFile = new File(resourceDirName + resourceFilename);
            if (resourceFile.isFile()) {
                resource = FileUtils.readFileToString(resourceFile, "UTF-8");
                tri = new TemplatedResourceIterator(resource, "src/test/resources/APIM2/valuesplain");
                while (tri.hasNext()) {
                    byte[] foxml = tri.next().getBytes("UTF-8");
                    apim.ingest(foxml, FOXML1_1.uri,"ingesting new foxml object");
                }
            }
        }

        purgeDemoObjects();

    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestAPIM2.class);
    }

}
