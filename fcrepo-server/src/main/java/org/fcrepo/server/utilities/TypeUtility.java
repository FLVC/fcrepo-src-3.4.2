/* The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.server.utilities;

import org.fcrepo.server.storage.types.MethodDef;
import org.fcrepo.server.storage.types.MethodParmDef;
import org.fcrepo.server.types.gen.Condition;
import org.fcrepo.server.types.gen.DatastreamProblem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * A utility class for converting back and forth from the internal Fedora type
 * classes in org.fcrepo.server.storage.types and the generated type classes
 * produced by the wsdl2java emitter in org.fcrepo.server.types.gen.
 *
 * @author Ross Wayland
 */
public abstract class TypeUtility {

    private static final Logger logger =
            LoggerFactory.getLogger(TypeUtility.class);

    public static org.fcrepo.server.types.gen.Datastream convertDatastreamToGenDatastream(
            org.fcrepo.server.storage.types.Datastream in) {
        org.fcrepo.server.types.gen.Datastream out =
                new org.fcrepo.server.types.gen.Datastream();
        String group = in.DSControlGrp;
        out.setControlGroup(org.fcrepo.server.types.gen.DatastreamControlGroup
                .fromValue(group));
        if (group.equals("R") || group.equals("E")) {
            // only given location if it's a redirect or external datastream
            out.setLocation(in.DSLocation);
        }
        out.setCreateDate(DateUtility.convertDateToString(in.DSCreateDT));
        out.setID(in.DatastreamID);
        out.setAltIDs(in.DatastreamAltIDs);
        out.setLabel(in.DSLabel);
        out.setVersionable(in.DSVersionable);
        out.setMIMEType(in.DSMIME);
        out.setFormatURI(in.DSFormatURI);
        out.setSize(in.DSSize);
        out.setState(in.DSState);
        out.setVersionID(in.DSVersionID);
        out.setChecksum(in.DSChecksum);
        out.setChecksumType(in.DSChecksumType);
        return out;
    }

    public static org.fcrepo.server.types.gen.FieldSearchResult convertFieldSearchResultToGenFieldSearchResult(
            org.fcrepo.server.search.FieldSearchResult result) {
        org.fcrepo.server.types.gen.FieldSearchResult ret =
                new org.fcrepo.server.types.gen.FieldSearchResult();
        ret
                .setResultList(convertSearchObjectFieldsListToGenObjectFieldsArray(result
                        .objectFieldsList()));
        if (result.getToken() != null) {
            org.fcrepo.server.types.gen.ListSession sess =
                    new org.fcrepo.server.types.gen.ListSession();
            sess.setToken(result.getToken());
            if (result.getCursor() > -1) {
                sess.setCursor(new org.apache.axis.types.NonNegativeInteger(""
                                                                            + result.getCursor()));
            }
            if (result.getCompleteListSize() > -1) {
                sess
                        .setCompleteListSize(new org.apache.axis.types.NonNegativeInteger(""
                                                                                          +
                                                                                          result.getCompleteListSize()));
            }
            if (result.getExpirationDate() != null) {
                sess.setExpirationDate(DateUtility.convertDateToString(result
                        .getExpirationDate()));
            }
            ret.setListSession(sess);
        }
        return ret;
    }

    public static org.fcrepo.server.search.FieldSearchQuery convertGenFieldSearchQueryToFieldSearchQuery(
            org.fcrepo.server.types.gen.FieldSearchQuery gen)
            throws org.fcrepo.server.errors.InvalidOperatorException,
                   org.fcrepo.server.errors.QueryParseException {
        if (gen.getTerms() != null) {
            return new org.fcrepo.server.search.FieldSearchQuery(gen.getTerms());
        } else {
            return new org.fcrepo.server.search.FieldSearchQuery(convertGenConditionArrayToSearchConditionList(gen
                    .getConditions()));
        }
    }

    public static java.util.List convertGenConditionArrayToSearchConditionList(
            org.fcrepo.server.types.gen.Condition[] genConditions)
            throws org.fcrepo.server.errors.InvalidOperatorException,
                   org.fcrepo.server.errors.QueryParseException {
        java.util.ArrayList list = new java.util.ArrayList();
        if (genConditions != null) {
            for (Condition c : genConditions) {
                list.add(new org.fcrepo.server.search.Condition(c.getProperty(), c
                        .getOperator().toString(), c.getValue()));
            }
        }
        return list;
    }

    public static org.fcrepo.server.types.gen.ObjectFields[] convertSearchObjectFieldsListToGenObjectFieldsArray(
            java.util.List sfList) {
        org.fcrepo.server.types.gen.ObjectFields[] genFields =
                new org.fcrepo.server.types.gen.ObjectFields[sfList.size()];
        for (int i = 0; i < sfList.size(); i++) {
            org.fcrepo.server.types.gen.ObjectFields gf =
                    new org.fcrepo.server.types.gen.ObjectFields();
            org.fcrepo.server.search.ObjectFields sf =
                    (org.fcrepo.server.search.ObjectFields) sfList.get(i);
            // Repository key fields
            if (sf.getPid() != null) {
                gf.setPid(sf.getPid());
            }
            if (sf.getLabel() != null) {
                gf.setLabel(sf.getLabel());
            }
            if (sf.getState() != null) {
                gf.setState(sf.getState());
            }
            if (sf.getOwnerId() != null) {
                gf.setOwnerId(sf.getOwnerId());
            }
            if (sf.getCDate() != null) {
                gf.setCDate(DateUtility.convertDateToString(sf.getCDate()));
            }
            if (sf.getMDate() != null) {
                gf.setMDate(DateUtility.convertDateToString(sf.getMDate()));
            }
            if (sf.getDCMDate() != null) {
                gf.setDcmDate(DateUtility.convertDateToString(sf.getDCMDate()));
            }
            // Dublin core fields
            if (sf.titles().size() != 0) {
                gf.setTitle(toStringArray(sf.titles()));
            }
            if (sf.creators().size() != 0) {
                gf.setCreator(toStringArray(sf.creators()));
            }
            if (sf.subjects().size() != 0) {
                gf.setSubject(toStringArray(sf.subjects()));
            }
            if (sf.descriptions().size() != 0) {
                gf.setDescription(toStringArray(sf.descriptions()));
            }
            if (sf.publishers().size() != 0) {
                gf.setPublisher(toStringArray(sf.publishers()));
            }
            if (sf.contributors().size() != 0) {
                gf.setContributor(toStringArray(sf.contributors()));
            }
            if (sf.dates().size() != 0) {
                gf.setDate(toStringArray(sf.dates()));
            }
            if (sf.types().size() != 0) {
                gf.setType(toStringArray(sf.types()));
            }
            if (sf.formats().size() != 0) {
                gf.setFormat(toStringArray(sf.formats()));
            }
            if (sf.identifiers().size() != 0) {
                gf.setIdentifier(toStringArray(sf.identifiers()));
            }
            if (sf.sources().size() != 0) {
                gf.setSource(toStringArray(sf.sources()));
            }
            if (sf.languages().size() != 0) {
                gf.setLanguage(toStringArray(sf.languages()));
            }
            if (sf.relations().size() != 0) {
                gf.setRelation(toStringArray(sf.relations()));
            }
            if (sf.coverages().size() != 0) {
                gf.setCoverage(toStringArray(sf.coverages()));
            }
            if (sf.rights().size() != 0) {
                gf.setRights(toStringArray(sf.rights()));
            }
            genFields[i] = gf;
        }
        return genFields;
    }

    public static String[] toStringArray(java.util.List<DCField> l) {
        String[] ret = new String[l.size()];
        for (int i = 0; i < l.size(); i++) {
            ret[i] = l.get(i).getValue();
        }
        return ret;
    }

    /**
     * <p>
     * Converts an array of org.fcrepo.server.storage.types.MethodDef into an array
     * of org.fcrepo.server.types.gen.MethodDef.
     * </p>
     *
     * @param methodDefs
     *        An array of org.fcrepo.server.storage.types.MethodDef.
     * @return An array of org.fcrepo.server.types.gen.MethodDef.
     */
    /*
     * public static org.fcrepo.server.types.gen.MethodDef[]
     * convertMethodDefArrayToGenMethodDefArray(
     * org.fcrepo.server.storage.types.MethodDef[] methodDefs) { if (methodDefs !=
     * null) { org.fcrepo.server.types.gen.MethodDef[] genMethodDefs = new
     * org.fcrepo.server.types.gen.MethodDef[methodDefs.length]; for (int i=0; i<genMethodDefs.length;
     * i++) { org.fcrepo.server.types.gen.MethodDef genMethodDef = new
     * org.fcrepo.server.types.gen.MethodDef();
     * genMethodDef.setMethodLabel(methodDefs[i].methodLabel);
     * genMethodDef.setMethodName(methodDefs[i].methodName);
     * org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
     * methodDefs[i].methodParms; org.fcrepo.server.types.gen.MethodParmDef[]
     * genMethodParmDefs = new org.fcrepo.server.types.gen.MethodParmDef[0]; if
     * (methodParmDefs != null) { genMethodParmDefs = new
     * org.fcrepo.server.types.gen.MethodParmDef[methodParmDefs.length]; for (int
     * j=0; j<methodParmDefs.length; j++) { genMethodParmDefs[j] =
     * convertMethodParmDefToGenMethodParmDef(methodParmDefs[j]); } }
     * genMethodDef.setMethodParms(genMethodParmDefs); genMethodDefs[i] =
     * genMethodDef; } return genMethodDefs; } else { return null; } }
     */

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.storage.types.MethodDef into an
     * instance of org.fcrepo.server.types.gen.MethodDef.
     * </p>
     *
     * @param methodDef
     *        An instance of org.fcrepo.server.storage.types.MethodDef.
     * @return An instance of org.fcrepo.server.types.gen.MethodDef.
     */
    /*
     * public static org.fcrepo.server.types.gen.MethodDef
     * convertMethodDefToGenMethodDef( org.fcrepo.server.storage.types.MethodDef
     * methodDef) { if (methodDef != null ) { org.fcrepo.server.types.gen.MethodDef
     * genMethodDefs = new org.fcrepo.server.types.gen.MethodDef();
     * org.fcrepo.server.types.gen.MethodDef genMethodDef = new
     * org.fcrepo.server.types.gen.MethodDef();
     * genMethodDef.setMethodLabel(methodDef.methodLabel);
     * genMethodDef.setMethodName(methodDef.methodName);
     * org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
     * methodDef.methodParms; org.fcrepo.server.types.gen.MethodParmDef[]
     * genMethodParmDefs = new org.fcrepo.server.types.gen.MethodParmDef[0];
     * genMethodParmDefs = convertMethodParmDefArrayToGenMethodParmDefArray(
     * methodParmDefs); if (methodParmDefs != null) { genMethodParmDefs = new
     * org.fcrepo.server.types.gen.MethodParmDef[methodParmDefs.length]; for (int
     * j=0; j<methodParmDefs.length; j++) { genMethodParmDefs[j] =
     * convertMethodParmDefToGenMethodParmDef(methodParmDefs[j]); } }
     * genMethodDef.setMethodParms(genMethodParmDefs); return genMethodDefs; }
     * else { return null; } }
     */

    /**
     * <p>
     * Converts an array of org.fcrepo.server.types.gen.MethodDef into an array of
     * org.fcrepo.server.storage.types.MethodDef.
     * </p>
     *
     * @param genMethodDefs
     *        An array of org.fcrepo.server.types.gen.MethodDef.
     * @return An array of org.fcrepo.server.storage.types.MethodDef.
     */
    /*
     * public static org.fcrepo.server.storage.types.MethodDef[]
     * convertGenMethodDefArrayToMethodDefArray(
     * org.fcrepo.server.types.gen.MethodDef[] genMethodDefs) { if (genMethodDefs !=
     * null) { org.fcrepo.server.storage.types.MethodDef[] methodDefs = new
     * org.fcrepo.server.storage.types.MethodDef[genMethodDefs.length]; for (int
     * i=0; i<genMethodDefs.length; i++) {
     * org.fcrepo.server.storage.types.MethodDef methodDef = new
     * org.fcrepo.server.storage.types.MethodDef(); methodDef.methodLabel =
     * genMethodDefs[i].getMethodLabel(); methodDef.methodName =
     * genMethodDefs[i].getMethodName(); org.fcrepo.server.types.gen.MethodParmDef[]
     * genMethodParmDefs = genMethodDefs[i].getMethodParms();
     * org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs = new
     * org.fcrepo.server.storage.types.MethodParmDef[0]; if (genMethodParmDefs !=
     * null) { methodParmDefs = new org.fcrepo.server.storage.types.MethodParmDef[
     * genMethodParmDefs.length]; for (int j=0; j<genMethodParmDefs.length;
     * j++) { methodParmDefs[j] = convertGenMethodParmDefToMethodParmDef(
     * genMethodParmDefs[j]); } } methodDef.methodParms = methodParmDefs;
     * methodDefs[i] = methodDef; } return methodDefs; } else { return null; } }
     */

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.types.gen.MethodDef into an
     * instance of org.fcrepo.server.storage.types.MethodDef.
     * </p>
     *
     * @param genMethodDef
     *        An instance of org.fcrepo.server.types.gen.MethodDef.
     * @return An instance of org.fcrepo.server.storage.types.MethodDef.
     */
    /*
     * public static org.fcrepo.server.storage.types.MethodDef
     * convertGenMethodDefToMethodDef( org.fcrepo.server.types.gen.MethodDef
     * genMethodDef) { if (genMethodDef != null) {
     * org.fcrepo.server.storage.types.MethodDef methodDef = new
     * org.fcrepo.server.storage.types.MethodDef(); methodDef.methodLabel =
     * genMethodDef.getMethodLabel(); methodDef.methodName =
     * genMethodDef.getMethodName(); org.fcrepo.server.types.gen.MethodParmDef[]
     * genMethodParmDefs = genMethodDef.getMethodParms();
     * org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs = new
     * org.fcrepo.server.storage.types.MethodParmDef[0]; if (genMethodParmDefs !=
     * null) { methodParmDefs =
     * convertGenMethodParmDefArrayToMethodParmDefArray( genMethodParmDefs); }
     * methodDef.methodParms = methodParmDefs; return methodDef; } else { return
     * null; } }
     */

    /**
     * <p>
     * Converts an array of org.fcrepo.server.storage.types.MethodParmDef into an
     * array of org.fcrepo.server.types.gen.MethodParmDef.
     * </p>
     *
     * @param methodParmDefs An array of org.fcrepo.server.storage.types.MethodParmDef.
     * @return An array of org.fcrepo.server.types.gen.MethodParmDef.
     */
    public static org.fcrepo.server.types.gen.MethodParmDef[] convertMethodParmDefArrayToGenMethodParmDefArray(
            org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs) {
        if (methodParmDefs != null) {
            org.fcrepo.server.types.gen.MethodParmDef[] genMethodParmDefs =
                    new org.fcrepo.server.types.gen.MethodParmDef[methodParmDefs.length];
            for (int i = 0; i < genMethodParmDefs.length; i++) {
                org.fcrepo.server.types.gen.MethodParmDef genMethodParmDef =
                        new org.fcrepo.server.types.gen.MethodParmDef();
                genMethodParmDef =
                        convertMethodParmDefToGenMethodParmDef(methodParmDefs[i]);
                genMethodParmDefs[i] = genMethodParmDef;
            }
            return genMethodParmDefs;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.storage.types.MethodParmDef into an
     * instance of org.fcrepo.server.types.gen.MethodParmDef.
     * </p>
     *
     * @param methodParmDef An instance of org.fcrepo.server.storage.types.MethodParmDef.
     * @return An instance of org.fcrepo.server.types.gen.MethodParmDef.
     */
    public static org.fcrepo.server.types.gen.MethodParmDef convertMethodParmDefToGenMethodParmDef(
            org.fcrepo.server.storage.types.MethodParmDef methodParmDef) {
        if (methodParmDef != null) {
            org.fcrepo.server.types.gen.MethodParmDef genMethodParmDef =
                    new org.fcrepo.server.types.gen.MethodParmDef();
            genMethodParmDef.setParmName(methodParmDef.parmName);
            genMethodParmDef.setParmLabel(methodParmDef.parmLabel);
            genMethodParmDef
                    .setParmDefaultValue(methodParmDef.parmDefaultValue);
            genMethodParmDef
                    .setParmDomainValues(methodParmDef.parmDomainValues);
            genMethodParmDef.setParmRequired(methodParmDef.parmRequired);
            genMethodParmDef.setParmType(methodParmDef.parmType);
            genMethodParmDef.setParmPassBy(methodParmDef.parmPassBy);
            return genMethodParmDef;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an array of org.fcrepo.server.types.gen.MethodParmDef into an array
     * of org.fcrepo.server.storage.types.MethodParmDef.
     * </p>
     *
     * @param genMethodParmDefs An array of org.fcrepo.server.types.gen.MethodParmDef.
     * @return An array of org.fcrepo.server.storage.types.MethodParmDef.
     */
    public static org.fcrepo.server.storage.types.MethodParmDef[] convertGenMethodParmDefArrayToMethodParmDefArray(
            org.fcrepo.server.types.gen.MethodParmDef[] genMethodParmDefs) {
        if (genMethodParmDefs != null) {
            org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
                    new org.fcrepo.server.storage.types.MethodParmDef[genMethodParmDefs.length];
            for (int i = 0; i < genMethodParmDefs.length; i++) {
                org.fcrepo.server.storage.types.MethodParmDef methodParmDef =
                        new org.fcrepo.server.storage.types.MethodParmDef();
                methodParmDef =
                        convertGenMethodParmDefToMethodParmDef(genMethodParmDefs[i]);
                methodParmDefs[i] = methodParmDef;
            }
            return methodParmDefs;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.storage.types.MethodParmDef into an
     * instance of org.fcrepo.server.types.gen.MethodParmDef.
     * </p>
     *
     * @param genMethodParmDef An instance of org.fcrepo.server.storage.types.MethodParmDef.
     * @return An instance of org.fcrepo.server.types.gen.MethodParmDef.
     */
    public static org.fcrepo.server.storage.types.MethodParmDef convertGenMethodParmDefToMethodParmDef(
            org.fcrepo.server.types.gen.MethodParmDef genMethodParmDef) {
        if (genMethodParmDef != null) {
            org.fcrepo.server.storage.types.MethodParmDef methodParmDef =
                    new org.fcrepo.server.storage.types.MethodParmDef();
            methodParmDef.parmName = genMethodParmDef.getParmName();
            methodParmDef.parmLabel = genMethodParmDef.getParmLabel();
            methodParmDef.parmDefaultValue =
                    genMethodParmDef.getParmDefaultValue();
            methodParmDef.parmDomainValues =
                    genMethodParmDef.getParmDomainValues();
            methodParmDef.parmRequired = genMethodParmDef.isParmRequired();
            methodParmDef.parmType = genMethodParmDef.getParmType();
            methodParmDef.parmPassBy = genMethodParmDef.getParmPassBy();
            return methodParmDef;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.storage.types.MIMETypedStream into
     * an instance of org.fcrepo.server.types.gen.MIMETypedStream.
     * </p>
     *
     * @param mimeTypedStream An instance of org.fcrepo.server.storage.types.MIMETypedStream.
     * @return An instance of org.fcrepo.server.types.gen.MIMETypedStream.
     */
    public static org.fcrepo.server.types.gen.MIMETypedStream convertMIMETypedStreamToGenMIMETypedStream(
            org.fcrepo.server.storage.types.MIMETypedStream mimeTypedStream) {
        if (mimeTypedStream != null) {
            org.fcrepo.server.types.gen.MIMETypedStream genMIMETypedStream =
                    new org.fcrepo.server.types.gen.MIMETypedStream();
            genMIMETypedStream.setMIMEType(mimeTypedStream.MIMEType);
            org.fcrepo.server.storage.types.Property[] header =
                    mimeTypedStream.header;
            genMIMETypedStream
                    .setHeader(convertPropertyArrayToGenPropertyArray(header));
            ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
            InputStream is = mimeTypedStream.getStream();
            int byteStream = 0;
            try {
                byte[] buffer = new byte[255];
                while ((byteStream = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, byteStream);
                }
            } catch (IOException ioe) {
                logger.error("Error converting types", ioe);
            }
            genMIMETypedStream.setStream(baos.toByteArray());
            mimeTypedStream.close();
            mimeTypedStream.setStream(new ByteArrayInputStream(baos
                    .toByteArray()));
            return genMIMETypedStream;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.types.gen.MIMETypedStream into an
     * instance of org.fcrepo.server.storage.types.MIMETypedStream.
     * </p>
     *
     * @param genMIMETypedStream An instance of org.fcrepo.server.types.gen.MIMETypedStream.
     * @return an instance of org.fcrepo.server.storage.types.MIMETypedStream.
     */
    public static org.fcrepo.server.storage.types.MIMETypedStream convertGenMIMETypedStreamToMIMETypedStream(
            org.fcrepo.server.types.gen.MIMETypedStream genMIMETypedStream) {
        if (genMIMETypedStream != null) {
            InputStream is =
                    new ByteArrayInputStream(genMIMETypedStream.getStream());
            org.fcrepo.server.types.gen.Property[] header =
                    genMIMETypedStream.getHeader();
            org.fcrepo.server.storage.types.MIMETypedStream mimeTypedStream =
                    new org.fcrepo.server.storage.types.MIMETypedStream(genMIMETypedStream
                            .getMIMEType(),
                                                                        is,
                                                                        convertGenPropertyArrayToPropertyArray(header));
            return mimeTypedStream;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an array of org.fcrepo.server.types.gen.ObjectMethodsDef into an
     * array of org.fcrepo.server.storage.types.ObjectMethodsDef.
     * </p>
     *
     * @param genObjectMethodDefs An array of org.fcrepo.server.types.gen.ObjectMethodsDef.
     * @return An array of org.fcrepo.server.storage.types.ObjectMethodsDef.
     */
    public static org.fcrepo.server.storage.types.ObjectMethodsDef[] convertGenObjectMethodsDefArrayToObjectMethodsDefArray(
            org.fcrepo.server.types.gen.ObjectMethodsDef[] genObjectMethodDefs) {
        if (genObjectMethodDefs != null) {
            org.fcrepo.server.storage.types.ObjectMethodsDef[] objectMethodDefs =
                    new org.fcrepo.server.storage.types.ObjectMethodsDef[genObjectMethodDefs.length];
            for (int i = 0; i < genObjectMethodDefs.length; i++) {
                org.fcrepo.server.storage.types.ObjectMethodsDef objectMethodDef =
                        new org.fcrepo.server.storage.types.ObjectMethodsDef();
                objectMethodDef.PID = genObjectMethodDefs[i].getPID();
                objectMethodDef.sDefPID =
                        genObjectMethodDefs[i].getServiceDefinitionPID();
                objectMethodDef.methodName =
                        genObjectMethodDefs[i].getMethodName();

                org.fcrepo.server.types.gen.MethodParmDef[] genMethodParmDefs =
                        genObjectMethodDefs[i].getMethodParmDefs();
                org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
                        new org.fcrepo.server.storage.types.MethodParmDef[0];
                if (genMethodParmDefs != null) {
                    methodParmDefs =
                            new org.fcrepo.server.storage.types.MethodParmDef[genMethodParmDefs.length];
                    for (int j = 0; j < genMethodParmDefs.length; j++) {
                        methodParmDefs[j] =
                                convertGenMethodParmDefToMethodParmDef(genMethodParmDefs[j]);
                    }
                }
                objectMethodDef.methodParmDefs = methodParmDefs;
                objectMethodDefs[i] = objectMethodDef;
            }
            return objectMethodDefs;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.types.gen.ObjectMethodsDef into an
     * instance of org.fcrepo.server.storage.types.ObjectMethodsDef.
     * </p>
     *
     * @param genObjectMethodDef An instance of org.fcrepo.server.types.gen.ObjectMethodsDef.
     * @return An instance of org.fcrepo.server.storage.types.ObjectMethodsDef.
     */
    public static org.fcrepo.server.storage.types.ObjectMethodsDef convertGenObjectMethodsDefToObjectMethodsDef(
            org.fcrepo.server.types.gen.ObjectMethodsDef genObjectMethodDef) {
        if (genObjectMethodDef != null) {
            org.fcrepo.server.storage.types.ObjectMethodsDef objectMethodDef =
                    new org.fcrepo.server.storage.types.ObjectMethodsDef();
            objectMethodDef.PID = genObjectMethodDef.getPID();
            objectMethodDef.sDefPID =
                    genObjectMethodDef.getServiceDefinitionPID();
            objectMethodDef.methodName = genObjectMethodDef.getMethodName();
            org.fcrepo.server.types.gen.MethodParmDef[] genMethodParmDefs =
                    genObjectMethodDef.getMethodParmDefs();
            org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
                    new org.fcrepo.server.storage.types.MethodParmDef[0];
            if (genMethodParmDefs != null) {
                methodParmDefs =
                        convertGenMethodParmDefArrayToMethodParmDefArray(genMethodParmDefs);
            }
            objectMethodDef.methodParmDefs = methodParmDefs;
            return objectMethodDef;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an array of org.fcrepo.server.storage.types.ObjectMethodsDef into an
     * array of org.fcrepo.server.types.gen.ObjectMethodsDef.
     * </p>
     *
     * @param objectMethodDefs An array of org.fcrepo.server.storage.types.ObjectMethodsDef.
     * @return An array of org.fcrepo.server.types.gen.ObjectMethodsDef.
     */
    public static org.fcrepo.server.types.gen.ObjectMethodsDef[] convertObjectMethodsDefArrayToGenObjectMethodsDefArray(
            org.fcrepo.server.storage.types.ObjectMethodsDef[] objectMethodDefs) {
        if (objectMethodDefs != null) {
            org.fcrepo.server.types.gen.ObjectMethodsDef[] genObjectMethodDefs =
                    new org.fcrepo.server.types.gen.ObjectMethodsDef[objectMethodDefs.length];
            for (int i = 0; i < objectMethodDefs.length; i++) {
                org.fcrepo.server.types.gen.ObjectMethodsDef genObjectMethodDef =
                        new org.fcrepo.server.types.gen.ObjectMethodsDef();
                genObjectMethodDef.setPID(objectMethodDefs[i].PID);
                genObjectMethodDef
                        .setServiceDefinitionPID(objectMethodDefs[i].sDefPID);
                genObjectMethodDef
                        .setMethodName(objectMethodDefs[i].methodName);
                org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
                        objectMethodDefs[i].methodParmDefs;
                org.fcrepo.server.types.gen.MethodParmDef[] genMethodParmDefs =
                        new org.fcrepo.server.types.gen.MethodParmDef[0];
                if (methodParmDefs != null) {
                    genMethodParmDefs =
                            new org.fcrepo.server.types.gen.MethodParmDef[methodParmDefs.length];
                    for (int j = 0; j < methodParmDefs.length; j++) {
                        genMethodParmDefs[j] =
                                convertMethodParmDefToGenMethodParmDef(methodParmDefs[j]);
                    }
                }
                genObjectMethodDef.setMethodParmDefs(genMethodParmDefs);
                genObjectMethodDefs[i] = genObjectMethodDef;
            }
            return genObjectMethodDefs;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.storage.types.ObjectMethodsDef into
     * an instance of org.fcrepo.server.types.gen.ObjectMethodsDef.
     * </p>
     *
     * @param objectMethodDef An instance of org.fcrepo.server.storage.types.ObjectMethodsDef.
     * @return An instance of org.fcrepo.server.types.gen.ObjectMethodsDef.
     */
    public static org.fcrepo.server.types.gen.ObjectMethodsDef convertObjectMethodsDefToGenObjectMethodsDef(
            org.fcrepo.server.storage.types.ObjectMethodsDef objectMethodDef) {
        if (objectMethodDef != null) {
            org.fcrepo.server.types.gen.ObjectMethodsDef genObjectMethodDef =
                    new org.fcrepo.server.types.gen.ObjectMethodsDef();
            genObjectMethodDef.setPID(objectMethodDef.PID);
            genObjectMethodDef.setServiceDefinitionPID(objectMethodDef.sDefPID);
            genObjectMethodDef.setMethodName(objectMethodDef.methodName);
            return genObjectMethodDef;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.types.gen.ObjectProfile into an
     * instance of org.fcrepo.server.access.ObjectProfile.
     * </p>
     *
     * @param genObjectProfile An instance of org.fcrepo.server.types.gen.ObjectProfile.
     * @return An instance of org.fcrepo.server.access.ObjectProfile.
     */
    public static org.fcrepo.server.access.ObjectProfile convertGenObjectProfileToObjectProfile(
            org.fcrepo.server.types.gen.ObjectProfile genObjectProfile) {
        if (genObjectProfile != null) {
            org.fcrepo.server.access.ObjectProfile objectProfile =
                    new org.fcrepo.server.access.ObjectProfile();
            objectProfile.PID = genObjectProfile.getPid();
            objectProfile.objectLabel = genObjectProfile.getObjLabel();
            objectProfile.objectCreateDate =
                    DateUtility.convertStringToDate(genObjectProfile
                            .getObjCreateDate());
            objectProfile.objectLastModDate =
                    DateUtility.convertStringToDate(genObjectProfile
                            .getObjLastModDate());
            objectProfile.objectModels =
                    new HashSet<String>(Arrays.asList(genObjectProfile
                            .getObjModels()));
            objectProfile.dissIndexViewURL =
                    genObjectProfile.getObjDissIndexViewURL();
            objectProfile.itemIndexViewURL =
                    genObjectProfile.getObjItemIndexViewURL();
            return objectProfile;
        } else {
            return null;
        }
    }

    public static org.fcrepo.server.types.gen.ObjectProfile convertObjectProfileToGenObjectProfile(
            org.fcrepo.server.access.ObjectProfile objectProfile) {
        if (objectProfile != null) {
            org.fcrepo.server.types.gen.ObjectProfile genObjectProfile =
                    new org.fcrepo.server.types.gen.ObjectProfile();
            genObjectProfile.setPid(objectProfile.PID);
            genObjectProfile.setObjLabel(objectProfile.objectLabel);

            genObjectProfile.setObjModels(objectProfile.objectModels
                    .toArray(new String[0]));
            genObjectProfile.setObjCreateDate(DateUtility
                    .convertDateToString(objectProfile.objectCreateDate));
            genObjectProfile.setObjLastModDate(DateUtility
                    .convertDateToString(objectProfile.objectLastModDate));
            genObjectProfile
                    .setObjDissIndexViewURL(objectProfile.dissIndexViewURL);
            genObjectProfile
                    .setObjItemIndexViewURL(objectProfile.itemIndexViewURL);
            return genObjectProfile;
        } else {
            return null;
        }
    }

    public static org.fcrepo.server.types.gen.RepositoryInfo convertReposInfoToGenReposInfo(
            org.fcrepo.server.access.RepositoryInfo repositoryInfo) {
        if (repositoryInfo != null) {
            org.fcrepo.server.types.gen.RepositoryInfo genRepositoryInfo =
                    new org.fcrepo.server.types.gen.RepositoryInfo();
            genRepositoryInfo.setRepositoryName(repositoryInfo.repositoryName);
            genRepositoryInfo
                    .setRepositoryBaseURL(repositoryInfo.repositoryBaseURL);
            genRepositoryInfo
                    .setRepositoryVersion(repositoryInfo.repositoryVersion);
            genRepositoryInfo
                    .setRepositoryPIDNamespace(repositoryInfo.repositoryPIDNamespace);
            genRepositoryInfo
                    .setDefaultExportFormat(repositoryInfo.defaultExportFormat);
            genRepositoryInfo.setOAINamespace(repositoryInfo.OAINamespace);
            genRepositoryInfo.setAdminEmailList(repositoryInfo.adminEmailList);
            genRepositoryInfo.setSamplePID(repositoryInfo.samplePID);
            genRepositoryInfo
                    .setSampleOAIIdentifier(repositoryInfo.sampleOAIIdentifer);
            genRepositoryInfo
                    .setSampleSearchURL(repositoryInfo.sampleSearchURL);
            genRepositoryInfo
                    .setSampleAccessURL(repositoryInfo.sampleAccessURL);
            genRepositoryInfo.setSampleOAIURL(repositoryInfo.sampleOAIURL);
            genRepositoryInfo.setRetainPIDs(repositoryInfo.retainPIDs);
            return genRepositoryInfo;
        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an array of org.fcrepo.server.types.gen.Property into an array of
     * org.fcrepo.server.storage.types.Property.
     * </p>
     *
     * @param genProperties An array of org.fcrepo.server.types.gen.Property.
     * @return An array of org.fcrepo.server.storage.types.Property.
     */
    public static org.fcrepo.server.storage.types.Property[] convertGenPropertyArrayToPropertyArray(
            org.fcrepo.server.types.gen.Property[] genProperties) {
        if (genProperties != null) {
            org.fcrepo.server.storage.types.Property[] properties =
                    new org.fcrepo.server.storage.types.Property[genProperties.length];
            for (int i = 0; i < genProperties.length; i++) {
                org.fcrepo.server.storage.types.Property property =
                        new org.fcrepo.server.storage.types.Property();
                property = convertGenPropertyToProperty(genProperties[i]);
                properties[i] = property;
            }
            return properties;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.types.gen.Property into an instance
     * of org.fcrepo.server.storage.types.Property.
     * </p>
     *
     * @param genProperty An instance of org.fcrepo.server.types.gen.Property.
     * @return An instance of org.fcrepo.server.storage.types.Property.
     */
    public static org.fcrepo.server.storage.types.Property convertGenPropertyToProperty(
            org.fcrepo.server.types.gen.Property genProperty) {
        org.fcrepo.server.storage.types.Property property =
                new org.fcrepo.server.storage.types.Property();
        if (genProperty != null) {
            property.name = genProperty.getName();
            property.value = genProperty.getValue();
        }
        return property;
    }

    /**
     * <p>
     * Converts an array of org.fcrepo.server.storage.types.Property into an array
     * of org.fcrepo.server.types.gen.Property.
     * </p>
     *
     * @param properties An array of org.fcrepo.server.storage.typesProperty.
     * @return An array of org.fcrepo.server.types.gen.Property.
     */
    public static org.fcrepo.server.types.gen.Property[] convertPropertyArrayToGenPropertyArray(
            org.fcrepo.server.storage.types.Property[] properties) {
        if (properties != null) {
            org.fcrepo.server.types.gen.Property[] genProperties =
                    new org.fcrepo.server.types.gen.Property[properties.length];
            for (int i = 0; i < properties.length; i++) {
                org.fcrepo.server.types.gen.Property genProperty =
                        new org.fcrepo.server.types.gen.Property();
                genProperty = convertPropertyToGenProperty(properties[i]);
                genProperties[i] = genProperty;
            }
            return genProperties;

        } else {
            return null;
        }
    }

    /**
     * <p>
     * Converts an instance of org.fcrepo.server.storage.types.Property into an
     * instance of org.fcrepo.server.types.gen.Property.
     * </p>
     *
     * @param property An instance of org.fcrepo.server.storage.types.Property.
     * @return An instance of org.fcrepo.server.types.gen.Property.
     */
    public static org.fcrepo.server.types.gen.Property convertPropertyToGenProperty(
            org.fcrepo.server.storage.types.Property property) {
        org.fcrepo.server.types.gen.Property genProperty =
                new org.fcrepo.server.types.gen.Property();
        if (property != null) {
            genProperty.setName(property.name);
            genProperty.setValue(property.value);
        }
        return genProperty;
    }

    public static org.fcrepo.server.types.gen.RelationshipTuple convertRelsTupleToGenRelsTuple(
            org.fcrepo.server.storage.types.RelationshipTuple in) {
        if (in == null) {
            return null;
        }
        org.fcrepo.server.types.gen.RelationshipTuple out =
                new org.fcrepo.server.types.gen.RelationshipTuple();
        out.setSubject(in.subject);
        out.setPredicate(in.predicate);
        out.setObject(in.object);
        out.setIsLiteral(in.isLiteral);
        out.setDatatype(in.datatype);
        return out;
    }

    public static org.fcrepo.server.types.gen.DatastreamDef convertDatastreamDefToGenDatastreamDef(
            org.fcrepo.server.storage.types.DatastreamDef in) {
        org.fcrepo.server.types.gen.DatastreamDef out =
                new org.fcrepo.server.types.gen.DatastreamDef();
        out.setID(in.dsID);
        out.setLabel(in.dsLabel);
        out.setMIMEType(in.dsMIME);

        return out;
    }

    public static org.fcrepo.server.storage.types.DatastreamDef convertGenDatastreamDefToDatastreamDef(
            org.fcrepo.server.types.gen.DatastreamDef genDatastreamDef) {
        if (genDatastreamDef == null) {
            return new org.fcrepo.server.storage.types.DatastreamDef(null,
                                                                     null,
                                                                     null);
        } else {
            return new org.fcrepo.server.storage.types.DatastreamDef(genDatastreamDef
                    .getID(),
                                                                     genDatastreamDef
                                                                             .getLabel(),
                                                                     genDatastreamDef
                                                                             .getMIMEType());
        }
    }

    //    public static org.fcrepo.server.types.gen.DatastreamBindingMap
    //            convertDSBindingMapToGenDatastreamBindingMap(
    //            org.fcrepo.server.storage.types.DSBindingMap in)
    //    {
    //        org.fcrepo.server.types.gen.DatastreamBindingMap out=
    //                new org.fcrepo.server.types.gen.DatastreamBindingMap();
    //        org.fcrepo.server.types.gen.DatastreamBinding datastreamBinding =
    //            new org.fcrepo.server.types.gen.DatastreamBinding();
    //        out.setDsBindings(convertDSBindingArrayToGenDatastreamBindingArray(in.dsBindings));
    //        out.setDsBindMapID(in.dsBindMapID);
    //        out.setDsBindMapLabel(in.dsBindMapLabel);
    //        out.setDsBindMechanismPID(in.dsBindMechanismPID);
    //        out.setState(in.state);
    //
    //        return out;
    //    }

    //    public static org.fcrepo.server.storage.types.DSBindingMap
    //        convertGenDatastreamBindingMapToDSBindingMap(
    //        org.fcrepo.server.types.gen.DatastreamBindingMap genDatastreamBindingMap)
    //    {
    //      org.fcrepo.server.storage.types.DSBindingMap dsBindingMap =
    //            new org.fcrepo.server.storage.types.DSBindingMap();
    //      if (genDatastreamBindingMap != null)
    //      {
    //        dsBindingMap.dsBindings = convertGenDatastreamBindingArrayToDSBindingArray(genDatastreamBindingMap.getDsBindings());
    //        dsBindingMap.dsBindMapID = genDatastreamBindingMap.getDsBindMapID();
    //        dsBindingMap.dsBindMapLabel = genDatastreamBindingMap.getDsBindMapLabel();
    //        dsBindingMap.dsBindMechanismPID = genDatastreamBindingMap.getDsBindMechanismPID();
    //        dsBindingMap.state = genDatastreamBindingMap.getState();
    //      }
    //      return dsBindingMap;
    //  }

    //    public static org.fcrepo.server.types.gen.DatastreamBinding
    //            convertDSBindingToGenDatastreamBinding(
    //            org.fcrepo.server.storage.types.DSBinding in)
    //    {
    //        org.fcrepo.server.types.gen.DatastreamBinding out=
    //                new org.fcrepo.server.types.gen.DatastreamBinding();
    //        out.setBindKeyName(in.bindKeyName);
    //        out.setBindLabel(in.bindLabel);
    //        out.setDatastreamID(in.datastreamID);
    //        out.setSeqNo(in.seqNo);
    //        return out;
    //    }
    //
    //    public static org.fcrepo.server.storage.types.DSBinding
    //        convertGenDatastreamBindingToDSBinding(
    //        org.fcrepo.server.types.gen.DatastreamBinding genDatastreamBinding)
    //    {
    //
    //      org.fcrepo.server.storage.types.DSBinding dsBinding =
    //            new org.fcrepo.server.storage.types.DSBinding();
    //      if (genDatastreamBinding != null)
    //      {
    //        dsBinding.bindKeyName = genDatastreamBinding.getBindKeyName();
    //        dsBinding.bindLabel = genDatastreamBinding.getBindLabel();
    //        dsBinding.datastreamID = genDatastreamBinding.getDatastreamID();
    //        dsBinding.seqNo = genDatastreamBinding.getSeqNo();
    //      }
    //      return dsBinding;
    //  }
    //
    //    public static org.fcrepo.server.types.gen.DatastreamBinding[]
    //        convertDSBindingArrayToGenDatastreamBindingArray(
    //        org.fcrepo.server.storage.types.DSBinding[] dsBindings)
    //    {
    //
    //      if (dsBindings != null)
    //      {
    //        org.fcrepo.server.types.gen.DatastreamBinding[] genDatastreamBindings =
    //            new org.fcrepo.server.types.gen.DatastreamBinding[dsBindings.length];
    //        for (int i=0; i<genDatastreamBindings.length; i++)
    //        {
    //          org.fcrepo.server.types.gen.DatastreamBinding genDatastreamBinding =
    //                   new org.fcrepo.server.types.gen.DatastreamBinding();
    //          genDatastreamBindings[i] =
    //              convertDSBindingToGenDatastreamBinding(dsBindings[i]);
    //        }
    //        return genDatastreamBindings;
    //
    //      } else
    //      {
    //        return null;
    //      }
    //  }

    public static org.fcrepo.server.types.gen.DatastreamDef[] convertDatastreamDefArrayToGenDatastreamDefArray(
            org.fcrepo.server.storage.types.DatastreamDef[] dsDefs) {

        if (dsDefs != null) {
            org.fcrepo.server.types.gen.DatastreamDef[] genDatastreamDefs =
                    new org.fcrepo.server.types.gen.DatastreamDef[dsDefs.length];
            for (int i = 0; i < genDatastreamDefs.length; i++) {
                org.fcrepo.server.types.gen.DatastreamDef genDatastreamDef =
                        new org.fcrepo.server.types.gen.DatastreamDef();
                genDatastreamDefs[i] =
                        convertDatastreamDefToGenDatastreamDef(dsDefs[i]);
            }
            return genDatastreamDefs;

        } else {
            return null;
        }
    }

    public static org.fcrepo.server.types.gen.Validation convertValidationToGenValidation(
            org.fcrepo.server.storage.types.Validation validation) {

        if (validation == null) {
            return null;
        }
        org.fcrepo.server.types.gen.Validation genvalid = new org.fcrepo.server.types.gen.Validation();
        genvalid.setValid(validation.isValid());
        genvalid.setPid(validation.getPid());
        genvalid.setObjModels(validation.getContentModels().toArray(new String[0]));
        genvalid.setObjProblems(validation.getObjectProblems().toArray(new String[0]));

        Map<String, List<String>> dsprobs = validation.getDatastreamProblems();

        DatastreamProblem[] genDatastreamProblems = new DatastreamProblem[dsprobs.keySet().size()];
        int i = 0;
        for (String key : dsprobs.keySet()) {
            genDatastreamProblems[i] = new DatastreamProblem(key, dsprobs.get(key).toArray(new String[0]));
            i++;
        }
        genvalid.setDatastreamProblems(genDatastreamProblems);
        return genvalid;
    }


    //  public static org.fcrepo.server.storage.types.DSBinding[]
    //      convertGenDatastreamBindingArrayToDSBindingArray(
    //      org.fcrepo.server.types.gen.DatastreamBinding[] genDatastreamBindings)
    //  {
    //
    //    if (genDatastreamBindings != null)
    //    {
    //      org.fcrepo.server.storage.types.DSBinding[] dsBindings =
    //          new org.fcrepo.server.storage.types.DSBinding[genDatastreamBindings.length];
    //      for (int i=0; i<genDatastreamBindings.length; i++)
    //      {
    //        org.fcrepo.server.storage.types.DSBinding dsBinding =
    //                 new org.fcrepo.server.storage.types.DSBinding();
    //        dsBinding =
    //            convertGenDatastreamBindingToDSBinding(genDatastreamBindings[i]);
    //        dsBindings[i] = dsBinding;
    //      }
    //      return dsBindings;
    //
    //    } else
    //    {
    //      return null;
    //    }
    //  }

    public static org.fcrepo.server.storage.types.DatastreamDef[] convertGenDatastreamDefArrayToDatastreamDefArray(
            org.fcrepo.server.types.gen.DatastreamDef[] genDatastreamDefs) {

        if (genDatastreamDefs != null) {
            org.fcrepo.server.storage.types.DatastreamDef[] dsDefs =
                    new org.fcrepo.server.storage.types.DatastreamDef[genDatastreamDefs.length];
            for (int i = 0; i < genDatastreamDefs.length; i++) {
                dsDefs[i] =
                        convertGenDatastreamDefToDatastreamDef(genDatastreamDefs[i]);
            }
            return dsDefs;

        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        try {
            org.fcrepo.server.storage.types.MethodParmDef methodParmDef1 =
                    new org.fcrepo.server.storage.types.MethodParmDef();
            methodParmDef1.parmName = "parm_name1";
            methodParmDef1.parmLabel = "parm_label1";
            methodParmDef1.parmDefaultValue = "parm_default_value1";
            methodParmDef1.parmRequired = true;
            org.fcrepo.server.storage.types.MethodParmDef methodParmDef2 =
                    new org.fcrepo.server.storage.types.MethodParmDef();
            methodParmDef2.parmName = "parm_name2";
            methodParmDef2.parmLabel = "parm_label2";
            methodParmDef2.parmDefaultValue = "parm_default_value2";
            methodParmDef2.parmRequired = false;
            org.fcrepo.server.storage.types.MethodDef methodDef1 =
                    new org.fcrepo.server.storage.types.MethodDef();
            methodDef1.methodName = "method_name1";
            methodDef1.methodLabel = "method_label1";
            org.fcrepo.server.storage.types.MethodParmDef[] methodParmDefs =
                    new org.fcrepo.server.storage.types.MethodParmDef[2];
            methodParmDefs[0] = methodParmDef1;
            methodParmDefs[1] = methodParmDef2;
            methodDef1.methodParms = methodParmDefs;
            org.fcrepo.server.storage.types.MethodDef methodDef2 =
                    new org.fcrepo.server.storage.types.MethodDef();
            methodDef2.methodName = "method_name2";
            methodDef2.methodLabel = "method_label2";
            methodDef2.methodParms = null;

            org.fcrepo.server.storage.types.MethodDef[] methodDef =
                    new org.fcrepo.server.storage.types.MethodDef[2];
            methodDef[0] = methodDef1;
            methodDef[1] = methodDef2;
            org.fcrepo.server.storage.types.Property[] properties =
                    new org.fcrepo.server.storage.types.Property[2];
            org.fcrepo.server.storage.types.Property prop1 =
                    new org.fcrepo.server.storage.types.Property();
            org.fcrepo.server.storage.types.Property prop2 =
                    new org.fcrepo.server.storage.types.Property();
            prop1.name = "prop1_name";
            prop1.value = "prop1_value";
            prop2.name = "prop2_name";
            prop2.value = "prop2_value";
            properties[0] = prop1;
            properties[1] = prop2;

            org.fcrepo.server.storage.types.ObjectMethodsDef[] objectMethods =
                    new org.fcrepo.server.storage.types.ObjectMethodsDef[2];
            org.fcrepo.server.storage.types.ObjectMethodsDef objectMethod1 =
                    new org.fcrepo.server.storage.types.ObjectMethodsDef();
            org.fcrepo.server.storage.types.ObjectMethodsDef objectMethod2 =
                    new org.fcrepo.server.storage.types.ObjectMethodsDef();
            objectMethod1.PID = "PID1";
            objectMethod1.sDefPID = "sDefPID1";
            objectMethod1.methodName = "method1";
            objectMethod2.PID = "PID2";
            objectMethod2.sDefPID = "sDefPID2";
            objectMethod2.methodName = "method2";
            objectMethods[0] = objectMethod1;
            objectMethods[1] = objectMethod2;

            System.out.println("\n----- Started with these values:");
            for (MethodDef element : methodDef) {
                System.out.println("name: " + element.methodName + "\nlabel: "
                                   + element.methodLabel + "\nparms:\n");
                org.fcrepo.server.storage.types.MethodParmDef[] methodParmDef =
                        null;
                methodParmDef = element.methodParms;
                if (methodParmDef != null) {
                    methodParmDef = element.methodParms;
                    for (MethodParmDef element2 : methodParmDef) {
                        System.out.println("parmname: " + element2.parmName
                                           + "\nparmLabel: " + element2.parmLabel
                                           + "\nparmDefault: " + element2.parmDefaultValue
                                           + "\nparmrequired: " + element2.parmRequired);
                    }
                }
            }

            System.out.println("\nObjectMethod  values:");
            for (int i = 0; i < objectMethods.length; i++) {
                System.out.println("ObjectMethod[" + i + "] = " + "\nPID = "
                                   + objectMethods[i].PID + "\nsDefPID = "
                                   + objectMethods[i].sDefPID + "\nmethod = "
                                   + objectMethods[i].methodName);
            }

            System.out.println("\nProperty  values:");
            for (int i = 0; i < properties.length; i++) {
                System.out.println("Prop[" + i + "] = " + "\nname = "
                                   + properties[i].name + "\nvalue = "
                                   + properties[i].value);
            }

            System.out.println("\n----- Starting with MIMETypedStream of:");
            String text = "this is some text for the bytestream";
            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            java.io.PrintWriter pw = new java.io.PrintWriter(baos);
            pw.println(text);
            pw.close();
            byte[] stream = baos.toByteArray();
            InputStream is = new ByteArrayInputStream(stream);
            org.fcrepo.server.storage.types.MIMETypedStream mimeTypedStream =
                    new org.fcrepo.server.storage.types.MIMETypedStream("text/plain",
                                                                        is,
                                                                        null);
            System.out.println("MIMEType: " + mimeTypedStream.MIMEType);
            int byteStream = 0;
            byte[] buffer = new byte[255];
            while ((byteStream = is.read()) >= 0) {
                System.out.write(buffer, 0, byteStream);
            }
            System.out.println("\n----- Converting GenMIMETypedStream to "
                               + " MIMETypedStream");
            org.fcrepo.server.types.gen.MIMETypedStream genMIMETypedStream =
                    TypeUtility
                            .convertMIMETypedStreamToGenMIMETypedStream(mimeTypedStream);
            System.out.println("MIMEType: " + genMIMETypedStream.getMIMEType());
            ByteArrayInputStream bais = null;
            bais = new ByteArrayInputStream(genMIMETypedStream.getStream());
            byteStream = 0;
            while ((byteStream = bais.read(buffer)) >= 0) {
                System.out.write(buffer, 0, byteStream);
            }
            System.out.println("\n----- Converting MIMETypedStream to "
                               + " GenMIMETypedStream");
            mimeTypedStream =
                    TypeUtility
                            .convertGenMIMETypedStreamToMIMETypedStream(genMIMETypedStream);
            System.out.println("MIMEType: " + mimeTypedStream.MIMEType);
            is = mimeTypedStream.getStream();
            byteStream = 0;
            while ((byteStream = is.read(buffer)) >= -1) {
                System.out.write(buffer, 0, byteStream);
            }
            mimeTypedStream.close();

            System.out.println("\n----- Converting ObjectMethodsDefArray to "
                               + "GenObjectMethodsDefArray");
            org.fcrepo.server.types.gen.ObjectMethodsDef[] genObjectMethods = null;
            genObjectMethods =
                    TypeUtility
                            .convertObjectMethodsDefArrayToGenObjectMethodsDefArray(objectMethods);
            for (int i = 0; i < genObjectMethods.length; i++) {
                System.out.println("GenProp[" + i + "]: " + "\nPID = "
                                   + genObjectMethods[i].getPID() + "\nsDefPID = "
                                   + genObjectMethods[i].getServiceDefinitionPID()
                                   + "\nmethod = " + genObjectMethods[i].getMethodName());
            }
            System.out
                    .println("\n----- Converting GenObjectMethodsDefArray to "
                             + "ObjectMethodsDefArray");
            objectMethods =
                    TypeUtility
                            .convertGenObjectMethodsDefArrayToObjectMethodsDefArray(genObjectMethods);
            for (int i = 0; i < objectMethods.length; i++) {
                System.out.println("ObjectMethods[" + i + "]: " + "\nPID = "
                                   + objectMethods[i].PID + "\nsDefPID = "
                                   + objectMethods[i].sDefPID + "\nmethod = "
                                   + objectMethods[i].methodName);
            }

            System.out.println("\n----- Converting PropertyArray to "
                               + "GenPropertyArray");
            org.fcrepo.server.types.gen.Property[] genProperties =
                    TypeUtility
                            .convertPropertyArrayToGenPropertyArray(properties);
            for (int i = 0; i < genProperties.length; i++) {
                System.out.println("GenProp[" + i + "]: " + "\nname = "
                                   + genProperties[i].getName() + "\nvalue = "
                                   + genProperties[i].getValue());
            }
            System.out.println("\n----- Converting GenPropertyArray to "
                               + "PropertyArray");
            properties =
                    TypeUtility
                            .convertGenPropertyArrayToPropertyArray(genProperties);
            for (int i = 0; i < properties.length; i++) {
                System.out.println("Prop[" + i + "]: " + "\nname = "
                                   + properties[i].name + "\nvalue = "
                                   + properties[i].value);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
