
package org.fcrepo.server.security.xacml.test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fcrepo.server.security.xacml.pdp.data.PolicyStore;
import org.fcrepo.server.security.xacml.pdp.data.PolicyStoreFactory;
import org.fcrepo.server.security.xacml.util.AttributeBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestFindPolicies {

    private static final Logger logger =
            LoggerFactory.getLogger(TestFindPolicies.class);

    private static PolicyStore dbXmlPolicyDataManager;

    public static void main(String[] args) throws Exception {
        PolicyStoreFactory f = new PolicyStoreFactory();
        dbXmlPolicyDataManager = f.newPolicyStore();


        AttributeBean[] attributes = new AttributeBean[1];
        Set<String> value = null;
        value = new HashSet<String>();
        value.add("urn:fedora:names:fedora:2.1:action:id-findObjects");
        attributes[0] =
                new AttributeBean("urn:fedora:names:fedora:2.1:action:id",
                                  null,
                                  value);
        value = new HashSet<String>();
        value.add("student");
        attributes[0] =
                new AttributeBean("urn:fedora:names:fedora:2.1:subject:role",
                                  null,
                                  value);

        Map<String, byte[]> results = null;

        // TODO: move these tests to a PolicyDataQuery test
        /*
        results = dbXmlPolicyDataManager.findPolicies(attributes);
        for (String name : results.keySet()) {
            logger.info("Name: " + name);
        }

        results = dbXmlPolicyDataManager.findPolicies(attributes);
        for (String name : results.keySet()) {
            logger.info("Name: " + name);
        }

        results = dbXmlPolicyDataManager.findPolicies(attributes);
        for (String name : results.keySet()) {
            logger.info("Name: " + name);
        }
        */
    }
}
