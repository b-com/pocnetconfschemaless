/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class HostnameXmlUtils {
    private static final Logger log = LoggerFactory.getLogger(HostnameXmlUtils.class);

    /** Find the hostname in an XML document.
     *
     * @param node the starting point for the search in the XML document
     *
     * @return The hostname as a String. null if no hostname can be found
     */

    static String lookForHostname(Node node ) {
        String hostname = null;

        try {
            Element root = ((Document) node).getDocumentElement();

            if (! root.getNodeName().equals("data"))
                return null;

            NodeList hostnameNodes = root.getElementsByTagName("hostname");
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
}
