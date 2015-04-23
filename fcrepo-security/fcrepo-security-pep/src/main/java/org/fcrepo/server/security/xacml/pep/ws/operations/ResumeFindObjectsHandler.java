/*
 * File: ResumeFindObjectsHandler.java
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


import org.apache.axis.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.fcrepo.common.Constants;
import org.fcrepo.server.security.xacml.pep.PEPException;
import org.fcrepo.server.security.xacml.util.LogUtil;

import com.sun.xacml.ctx.RequestCtx;


/**
 * @author nishen@melcoe.mq.edu.au
 */
public class ResumeFindObjectsHandler
        extends AbstractOperationHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(ResumeFindObjectsHandler.class);

    private FieldSearchResultHandler resultHandler = null;

    public ResumeFindObjectsHandler()
            throws PEPException {
        super();
        resultHandler = new FieldSearchResultHandler();
    }

    public RequestCtx handleResponse(MessageContext context)
            throws OperationHandlerException {
        if (logger.isDebugEnabled()) {
            logger.debug("ResumeFindObjectsHandler/handleResponse!");
        }
        return resultHandler.handleResponse(context);
    }

    public RequestCtx handleRequest(MessageContext context)
            throws OperationHandlerException {
        if (logger.isDebugEnabled()) {
            logger.debug("ResumeFindObjectsHandler/handleRequest!");
        }

        LogUtil.statLog(context.getUsername(), Constants.ACTION.FIND_OBJECTS
                .getURI().toASCIIString(), "FedoraRepository", null);

        return resultHandler.handleRequest(context);
    }
}
