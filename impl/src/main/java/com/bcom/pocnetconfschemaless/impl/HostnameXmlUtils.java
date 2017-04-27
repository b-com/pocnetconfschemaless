/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class HostnameXmlUtils {
    private static final Logger log = LoggerFactory.getLogger(HostnameXmlUtils.class);

    public static final String NS = "urn:opendaylight:hostname";
    public static final String JUNOS_NS = "http://xml.juniper.net/xnm/1.1/xnm";
    public static final String NOKIA_CONF_NS = "urn:nokia.com:sros:ns:yang:sr:conf";
    public static final String NOKIA_CONF_SYSTEM_NS = "urn:nokia.com:sros:ns:yang:sr:conf-system";

    public static final QName QNAME = QName.create(NS, "system").intern();

    /** Find the hostname in an XML document.
     *
     * @param node the starting point for the search in the XML document
     *
     * @return The hostname as a String. null if no hostname can be found
     */

    static String lookForHostname(Node node, String deviceFamily) {
        String hostname = null;

        try {
            Element root = ((Document) node).getDocumentElement();

            if (! root.getNodeName().equals("data"))
                return null;

            String hostnameTag = "hostname";
            if (deviceFamily.equals("junos")) {
                hostnameTag = "host-name";
            }
            else if (deviceFamily.equals("nokia")) {
                hostnameTag = "name";
            }

            NodeList hostnameNodes = root.getElementsByTagName(hostnameTag);
            Node hostnameNode = hostnameNodes.item(0);
            NodeList childs = hostnameNode.getChildNodes();
            for (int i=0; i<childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child instanceof Text) {
                    hostname = child.getNodeValue();
                    break;
                }
            }
        }
        catch (Exception e) {
            log.debug("Could not find hostname in Node: ", e);
        }

        return hostname;
    }

    /** Build an XML document to configure the hostname of a device according to the hostname@2017-03-17.yang schema,
     * starting from the root of the configuration.
     *
     * @param hostname The name of the host
     *
     * @return The XML document for use with the DOM parser. null in case of problem.
     */

    static Document createSystemHostnameDocument(String hostname) {
        Document doc = null;
        try {
            doc = XmlUtils.newDocument();

            Element systemElement = doc.createElementNS(NS, "system");
            doc.appendChild(systemElement);

            Element hostnameElement = doc.createElementNS(NS, "hostname");
            systemElement.appendChild(hostnameElement);
            hostnameElement.appendChild(doc.createTextNode(hostname));
        }
        catch (Exception e) {
            log.debug(String.format("Could not build XML document to configure hostname: %s", hostname), e);
            doc = null;
        }
        return doc;
    }

    /** Build an XML document to configure the hostname of a device according to the hostname@2017-03-17.yang schema,
     * starting just under the <system> element
     *
     * @param hostname The name of the host
     *
     * @return The XML document for use with the DOM parser. null in case of problem.
     */

    static Document createHostnameDocument(String hostname) {
        Document doc = null;
        try {
            doc = XmlUtils.newDocument();

            Element hostnameElement = doc.createElementNS(NS, "hostname");
            doc.appendChild(hostnameElement);
            hostnameElement.appendChild(doc.createTextNode(hostname));
        }
        catch (Exception e) {
            log.debug(String.format("Could not build XML document to configure hostname: %s", hostname), e);
            doc = null;
        }
        return doc;
    }

    /** Build an XML document to configure the hostname of a JunOS device,
     * starting just under the <system> element
     *
     * @param hostname The name of the host
     *
     * @return The XML document for use with the DOM parser. null in case of problem.
     */

    static Document createHostnameDocumentJunOS(String hostname) {
        Document doc = null;
        try {
            doc = XmlUtils.newDocument();

            Element hostnameElement = doc.createElementNS(JUNOS_NS, "host-name");
            doc.appendChild(hostnameElement);
            hostnameElement.appendChild(doc.createTextNode(hostname));
        }
        catch (Exception e) {
            log.debug(String.format("Could not build XML document to configure hostname: %s", hostname), e);
            doc = null;
        }
        return doc;
    }

    /** Build an XML document to configure the hostname of a Nokia device,
     * starting just under the <system> element
     *
     * @param hostname The name of the host
     *
     * @return The XML document for use with the DOM parser. null in case of problem.
     */

    static Document createHostnameDocumentNokia(String hostname) {
        Document doc = null;
        try {
            doc = XmlUtils.newDocument();

            Element hostnameElement = doc.createElementNS(NOKIA_CONF_SYSTEM_NS, "name");
            doc.appendChild(hostnameElement);
            hostnameElement.appendChild(doc.createTextNode(hostname));
        }
        catch (Exception e) {
            log.debug(String.format("Could not build XML document to configure hostname: %s", hostname), e);
            doc = null;
        }
        return doc;
    }
}
