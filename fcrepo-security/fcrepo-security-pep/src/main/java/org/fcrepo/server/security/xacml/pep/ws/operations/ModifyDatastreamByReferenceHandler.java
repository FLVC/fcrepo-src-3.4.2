/*
 * File: ModifyDatastreamByReferenceHandler.java
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

package org.fcrepo.server.security.xacml.pep.ws.operations;

import java.net.URI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.xacml.attr.AnyURIAttribute;
import com.sun.xacml.attr.AttributeValue;
import com.sun.xacml.attr.StringAttribute;
import com.sun.xacml.ctx.RequestCtx;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fcrepo.common.Constants;

import org.fcrepo.server.security.xacml.pdp.data.FedoraPolicyStore;
import org.fcrepo.server.security.xacml.pep.PEPException;
import org.fcrepo.server.security.xacml.util.LogUtil;

/**
 * @author nishen@melcoe.mq.edu.au
 */
public class ModifyDatastreamByReferenceHandler
        extends AbstractOperationHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(ModifyDatastreamByReferenceHandler.class);

    public ModifyDatastreamByReferenceHandler()
            throws PEPException {
        super();
    }

    public RequestCtx handleResponse(MessageContext context)
            throws OperationHandlerException {
        return null;
    }

    public RequestCtx handleRequest(MessageContext context)
            throws OperationHandlerException {
        logger.debug("ModifyDatastreamByReferenceHandler/handleRequest!");

        RequestCtx req = null;
        List<Object> oMap = null;

        String pid = null;
        String dsID = null;
        // String[] altIDs = null;
        // String dsLabel = null;
        String mimeType = null;
        String formatURI = null;
        String dsLocation = null;
        String checksumType = null;
        String checksum = null;
        // String logMessage = null;
        // Boolean force = null;

        try {
            oMap = getSOAPRequestObjects(context);
            logger.debug("Retrieved SOAP Request Objects");
        } catch (AxisFault af) {
            logger.error("Error obtaining SOAP Request Objects", af);
            throw new OperationHandlerException("Error obtaining SOAP Request Objects",
                                                af);
        }

        try {
            pid = (String) oMap.get(0);
            dsID = (String) oMap.get(1);
            // altIDs = (String[]) oMap.get(2);
            // dsLabel = (String) oMap.get(3);
            mimeType = (String) oMap.get(4);
            formatURI = (String) oMap.get(5);
            dsLocation = (String) oMap.get(6);
            checksumType = (String) oMap.get(7);
            checksum = (String) oMap.get(8);
            // logMessage = (String) oMap.get(9);
            // force = (Boolean) oMap.get(10);
        } catch (Exception e) {
            logger.error("Error obtaining parameters", e);
            throw new OperationHandlerException("Error obtaining parameters.",
                                                e);
        }

        logger.debug("Extracted SOAP Request Objects");

        Map<URI, AttributeValue> actions = new HashMap<URI, AttributeValue>();
        Map<URI, AttributeValue> resAttr = new HashMap<URI, AttributeValue>();

        try {
            if (pid != null && !"".equals(pid)) {
                resAttr.put(Constants.OBJECT.PID.getURI(),
                            new StringAttribute(pid));
            }
            if (pid != null && !"".equals(pid)) {
                resAttr.put(new URI(XACML_RESOURCE_ID),
                            new AnyURIAttribute(new URI(pid)));
            }
            if (dsID != null && !"".equals(dsID)) {
                resAttr.put(Constants.DATASTREAM.ID.getURI(),
                            new StringAttribute(dsID));
            }
            if (mimeType != null && !"".equals(mimeType)) {
                resAttr.put(Constants.DATASTREAM.NEW_MIME_TYPE.getURI(),
                            new StringAttribute(mimeType));
            }
            if (formatURI != null && !"".equals(formatURI)) {
                resAttr.put(Constants.DATASTREAM.NEW_FORMAT_URI.getURI(),
                            new AnyURIAttribute(new URI(formatURI)));
            }
            if (dsLocation != null && !"".equals(dsLocation)) {
                resAttr.put(Constants.DATASTREAM.NEW_LOCATION.getURI(),
                            new AnyURIAttribute(new URI(dsLocation)));
            }
            if (checksumType != null && !"".equals(checksumType)) {
                resAttr.put(Constants.DATASTREAM.NEW_CHECKSUM_TYPE.getURI(),
                            new StringAttribute(checksumType));
            }
            if (checksum != null && !"".equals(checksum)) {
                resAttr.put(Constants.DATASTREAM.NEW_CHECKSUM.getURI(),
                            new StringAttribute(checksum));
            }

            actions
                    .put(Constants.ACTION.ID.getURI(),
                         new StringAttribute(Constants.ACTION.MODIFY_DATASTREAM_BY_REFERENCE
                                 .getURI().toASCIIString()));
            actions.put(Constants.ACTION.API.getURI(),
                        new StringAttribute(Constants.ACTION.APIM.getURI()
                                .toASCIIString()));
            // modifying the FeSL policy datastream requires policy management permissions
            if (dsID != null && dsID.equals(FedoraPolicyStore.POLICY_DATASTREAM)) {
                actions.put(Constants.ACTION.ID.getURI(),
                            new StringAttribute(Constants.ACTION.MANAGE_POLICIES.getURI().toASCIIString()));

            }


            req =
                    getContextHandler().buildRequest(getSubjects(context),
                                                     actions,
                                                     resAttr,
                                                     getEnvironment(context));

            LogUtil.statLog(context.getUsername(),
                            Constants.ACTION.MODIFY_DATASTREAM_BY_REFERENCE
                                    .getURI().toASCIIString(),
                            pid,
                            dsID);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new OperationHandlerException(e.getMessage(), e);
        }

        return req;
    }
}
