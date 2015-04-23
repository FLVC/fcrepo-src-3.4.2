/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.rest;

import org.fcrepo.common.Constants;
import org.fcrepo.server.Context;
import org.fcrepo.server.access.ObjectProfile;
import org.fcrepo.server.rest.RestUtil.RequestContent;
import org.fcrepo.server.rest.param.DateTimeParam;
import org.fcrepo.server.storage.types.Validation;
import org.fcrepo.server.utilities.DateUtility;
import org.fcrepo.server.utilities.StreamUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;


/**
 * Implement /objects/pid/* REST API
 *
 * @author cuong.tran@yourmediashelf.com
 * @version $Id$
 */
@Path("/{pid}")
public class FedoraObjectResource extends BaseRestResource {
    private final String FOXML1_1 = "info:fedora/fedora-system:FOXML-1.1";
    private final String ATOMZIP1_1 = "info:fedora/fedora-system:ATOMZip-1.1";

    private static final Logger logger =
            LoggerFactory.getLogger(FedoraObjectResource.class);

    @Path("/validate")
    @GET
    @Produces({XML})
    public Response doObjectValidation(
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam(RestParam.AS_OF_DATE_TIME)
            String dateTime) {
        try {
            Context context = getContext();
            Date asOfDateTime = DateUtility.parseDateOrNull(dateTime);
            MediaType mediaType = TEXT_XML;

            Validation validation = apiMService.validate(context, pid, asOfDateTime);

            String xml = getSerializer(context).objectValidationToXml(validation);
            return Response.ok(xml, mediaType).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }


    /**
     * Exports the entire digital object in the specified XML format
     * ("info:fedora/fedora-system:FOXML-1.1" or
     * "info:fedora/fedora-system:METSFedoraExt-1.1"), and encoded appropriately
     * for the specified export context ("public", "migrate", or "archive").
     * <p/>
     * GET /objects/{pid}/export ? format context encoding
     */
    @Path("/export")
    @GET
    @Produces({XML, ZIP})
    public Response getObjectExport(
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam(RestParam.FORMAT)
            @DefaultValue(FOXML1_1)
            String format,
            @QueryParam(RestParam.EXPORT_CONTEXT)
            String exportContext,
            @QueryParam(RestParam.ENCODING)
            @DefaultValue(DEFAULT_ENC)
            String encoding) {

        try {
            Context context = getContext();
            InputStream is = apiMService.export(context, pid, format, exportContext, encoding);
            MediaType mediaType = TEXT_XML;
            if (format.equals(ATOMZIP1_1)) {
                mediaType = MediaType.valueOf(ZIP);
            }
            return Response.ok(is, mediaType).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Gets a list of timestamps indicating when components changed in an
     * object. This is a set of timestamps indicating when a datastream or
     * disseminator was created or modified in the object. These timestamps can
     * be used to request a timestamped dissemination request to view the object
     * as it appeared at a specific point in time.
     * <p/>
     * GET /objects/{pid}/versions ? format
     */
    @Path("/versions")
    @GET
    public Response getObjectHistory(
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam(RestParam.FORMAT)
            @DefaultValue(HTML)
            String format) {

        try {
            Context context = getContext();
            String[] objectHistory = apiAService.getObjectHistory(context, pid);
            String xml = getSerializer(context).objectHistoryToXml(objectHistory, pid);
            MediaType mime = RestHelper.getContentType(format);

            if (TEXT_HTML.isCompatible(mime)) {
                CharArrayWriter writer = new CharArrayWriter();
                transform(xml, "access/viewObjectHistory.xslt", writer);
                xml = writer.toString();
            }

            return Response.ok(xml, mime).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Gets a profile of the object which includes key metadata fields and URLs
     * for the object Dissemination Index and the object Item Index. Can be
     * thought of as a default view of the object.
     * <p/>
     * GET /objects/{pid}/objectXML
     */
    @Path("/objectXML")
    @GET
    @Produces(XML)
    public Response getObjectXML(
            @PathParam(RestParam.PID)
            String pid) {

        try {
            Context context = getContext();
            InputStream is = apiMService.getObjectXML(context, pid, DEFAULT_ENC);

            return Response.ok(is, TEXT_XML).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Gets a profile of the object which includes key metadata fields and URLs
     * for the object Dissemination Index and the object Item Index. Can be
     * thought of as a default view of the object.
     * <p/>
     * GET /objects/{pid} ? format asOfDateTime
     */
    @GET
    @Produces({HTML, XML})
    public Response getObjectProfile(
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam(RestParam.AS_OF_DATE_TIME)
            String dateTime,
            @QueryParam(RestParam.FORMAT)
            @DefaultValue(HTML)
            String format) {

        try {
            Date asOfDateTime = DateUtility.parseDateOrNull(dateTime);
            Context context = getContext();
            ObjectProfile objProfile = apiAService.getObjectProfile(context, pid, asOfDateTime);
            String xml = getSerializer(context).objectProfileToXML(objProfile, asOfDateTime);

            MediaType mime = RestHelper.getContentType(format);

            if (TEXT_HTML.isCompatible(mime)) {
                CharArrayWriter writer = new CharArrayWriter();
                transform(xml, "access/viewObjectProfile.xslt", writer);
                xml = writer.toString();
            }

            return Response.ok(xml, mime).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Permanently removes an object from the repository.
     * <p/>
     * DELETE /objects/{pid} ? logMessage
     */
    @DELETE
    public Response deleteObject(
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam("logMessage")
            String logMessage) {
        try {
            Context context = getContext();
            Date d = apiMService.purgeObject(context, pid, logMessage);
            return Response.ok(DateUtility.convertDateToXSDString(d), MediaType.TEXT_PLAIN_TYPE).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Create/Update a new digital object. If no xml given in the body, will
     * create an empty object.
     * <p/>
     * POST /objects/{pid} ? label logMessage format encoding namespace ownerId state
     */
    @POST
    @Consumes({XML, FORM})
    public Response createObject(
            @javax.ws.rs.core.Context
            HttpHeaders headers,
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam(RestParam.LABEL)
            String label,
            @QueryParam(RestParam.LOG_MESSAGE)
            String logMessage,
            @QueryParam(RestParam.FORMAT)
            @DefaultValue(FOXML1_1)
            String format,
            @QueryParam(RestParam.ENCODING)
            @DefaultValue(DEFAULT_ENC)
            String encoding,
            @QueryParam(RestParam.NAMESPACE)
            String namespace,
            @QueryParam(RestParam.OWNER_ID)
            String ownerID,
            @QueryParam(RestParam.STATE)
            @DefaultValue("A")
            String state,
            @QueryParam(RestParam.IGNORE_MIME)
            @DefaultValue("false")
            boolean ignoreMime) {
        try {
            Context context = getContext();

            InputStream is = null;

            // Determine if content is provided
            RestUtil restUtil = new RestUtil();
            RequestContent content =
                    restUtil.getRequestContent(servletRequest, headers);
            if (content != null && content.getContentStream() != null) {
                if (ignoreMime) {
                    is = content.getContentStream();
                } else {
                    // Make sure content is XML
                    String contentMime = content.getMimeType();
                    if (contentMime != null &&
                        TEXT_XML.isCompatible(MediaType.valueOf(contentMime))) {
                        is = content.getContentStream();
                    }
                }
            }

            // If no content is provided, use a FOXML template
            if (is == null) {
                if (pid == null || pid.equals("new")) {
                    pid = apiMService.getNextPID(context, 1, namespace)[0];
                }

                ownerID = context.getSubjectValue(Constants.SUBJECT.LOGIN_ID.uri);
                is = new ByteArrayInputStream(getFOXMLTemplate(pid, label, ownerID, encoding).getBytes());
            } else {

                if (namespace != null && !namespace.equals("")) {
                    logger.warn("The namespace parameter is only applicable when object " +
                                "content is not provided, thus the namespace provided '" +
                                namespace + "' has been ignored.");
                }
            }

            pid = apiMService.ingest(context, is, logMessage, format, encoding, pid);

            URI createdLocation = uriInfo.getRequestUri().resolve(URLEncoder.encode(pid, DEFAULT_ENC));
            return Response.created(createdLocation).entity(pid).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    /**
     * Update (modify) digital object.
     * <p>PUT /objects/{pid} ? label logMessage ownerId state lastModifiedDate</p>
     *
     * @param pid              the persistent identifier
     * @param label
     * @param logMessage
     * @param ownerID
     * @param state
     * @param lastModifiedDate Optional XSD dateTime to guard against concurrent
     *                         modification. If provided (i.e. not null), the request will fail with an
     *                         HTTP 409 Conflict if lastModifiedDate is earlier than the object's
     *                         lastModifiedDate.
     * @return The timestamp for this modification (as an XSD dateTime string)
     * @see org.fcrepo.server.management.Management#modifyObject(org.fcrepo.server.Context, String, String, String, String, String)
     */
    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    public Response updateObject(
            @PathParam(RestParam.PID)
            String pid,
            @QueryParam(RestParam.LABEL)
            String label,
            @QueryParam(RestParam.LOG_MESSAGE)
            String logMessage,
            @QueryParam(RestParam.OWNER_ID)
            String ownerID,
            @QueryParam(RestParam.STATE)
            String state,
            @QueryParam(RestParam.LAST_MODIFIED_DATE)
            DateTimeParam lastModifiedDate) {
        try {
            Context context = getContext();
            Date requestModDate = null;
            if (lastModifiedDate != null) {
                requestModDate = lastModifiedDate.getValue();
            }
            Date lastModDate =
                    apiMService.modifyObject(context, pid, state, label, ownerID, logMessage, requestModDate);
            return Response.ok().entity(DateUtility.convertDateToXSDString(lastModDate)).build();
        } catch (Exception ex) {
            return handleException(ex);
        }
    }

    private static String getFOXMLTemplate(
            String pid,
            String label,
            String ownerId,
            String encoding) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>\n");
        xml.append("<foxml:digitalObject VERSION=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
        xml.append("    xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\"\n");
        xml.append(
                "           xsi:schemaLocation=\"" + Constants.FOXML.uri + " " + Constants.FOXML1_1.xsdLocation + "\"");
        if (pid != null && pid.length() > 0) {
            xml.append("\n           PID=\"" + StreamUtility.enc(pid) + "\">\n");
        } else {
            xml.append(">\n");
        }
        xml.append("  <foxml:objectProperties>\n");
        xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>\n");
        xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#label\" VALUE=\""
                   + StreamUtility.enc(label) + "\"/>\n");
        xml.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#ownerId\" VALUE=\""
                   + ownerId + "\"/>\n");
        xml.append("  </foxml:objectProperties>\n");
        xml.append("</foxml:digitalObject>");

        return xml.toString();
    }
}
