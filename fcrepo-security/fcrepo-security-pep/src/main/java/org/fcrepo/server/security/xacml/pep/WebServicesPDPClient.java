/*
 * File: WebServicesPDPClient.java
 *
 * Copyright 2007 Macquarie E-Learning Centre Of Excellence
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.server.security.xacml.pep;

import java.net.URL;
import java.util.Map;


import org.apache.axis.client.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fcrepo.server.security.xacml.pdp.client.MelcoePDPSOAP11BindingStub;

/**
 * This is the Web Services based client for the MelcoePDP. It uses the classes
 * in org.fcrepo.server.security.xacml.pdp.client which are the Web Service client stubs.
 * 
 * @author nishen@melcoe.mq.edu.au
 */
public class WebServicesPDPClient
        implements PDPClient {

    private static final Logger logger =
            LoggerFactory.getLogger(WebServicesPDPClient.class);

    private MelcoePDPSOAP11BindingStub client = null;

    /**
     * Initialises the WebServicesPEPClient class.
     * 
     * @param options
     *        a Map of options for this class
     * @throws PEPException
     */
    public WebServicesPDPClient(Map<String, String> options)
            throws PEPException {
        try {
            String serviceEndpoint = options.get("ServiceEndpoint");
            if (serviceEndpoint == null || "".equals(serviceEndpoint)) {
                throw new PEPException("The serviceEnpoint option has not been set in the configuration file.");
            }
            client =
                    new MelcoePDPSOAP11BindingStub(new URL(serviceEndpoint),
                                                   new Service());
        } catch (Exception e) {
            logger.error("Could not initialise the PEP Client.");
            throw new PEPException("Could not initialise the PEP Client.", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.fcrepo.server.security.xacml.pep.PEPClient#evaluate(java.lang.String)
     */
    public String evaluate(String request) throws PEPException {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving String request:\n" + request);
        }

        String response = null;
        try {
            response = client.evaluate(request);
        } catch (Exception e) {
            logger.error("Error evaluating request.", e);
            throw new PEPException("Error evaluating request", e);
        }

        return response;
    }

    /*
     * (non-Javadoc)
     * @see org.fcrepo.server.security.xacml.pep.PEPClient#evaluateBatch(java.lang.String[])
     */
    public String evaluateBatch(String[] request) throws PEPException {
        if (logger.isDebugEnabled()) {
            logger.debug("Resolving request batch (" + request.length
                    + " requests)");
        }

        String response = null;
        try {
            response = client.evaluateBatch(request);
        } catch (Exception e) {
            logger.error("Error evaluating request.", e);
            throw new PEPException("Error evaluating request", e);
        }

        return response;
    }
}
