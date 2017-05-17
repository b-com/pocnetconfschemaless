/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XmlUtils {
    static final Logger LOG = LoggerFactory.getLogger(XmlUtils.class);
    static final DocumentBuilderFactory BUILDERFACTORY;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        factory.setIgnoringElementContentWhitespace(true);
        factory.setIgnoringComments(true);
        BUILDERFACTORY = factory;
    }

    /**
     * Creates an empty XML Document for use with the DOM parser
     *
     * @return The empty document. null if it cannot be created.
     */
    public static Document newDocument() {
        DocumentBuilder builder = null;
        try {
            builder = BUILDERFACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            LOG.debug("newDocument(): ", e);
            return null;
        }
        Document doc = builder.newDocument();
        return doc;
    }

    public static String nodeTypeToString(short nodeType) {
        String nodeTypeStr;
        switch (nodeType) {
            case Node.ELEMENT_NODE:
                nodeTypeStr = "Element";
                break;
            case Node.ATTRIBUTE_NODE:
                nodeTypeStr = "Attr";
                break;
            case Node.TEXT_NODE:
                nodeTypeStr = "Text";
                break;
            case Node.CDATA_SECTION_NODE:
                nodeTypeStr = "CDATASection";
                break;
            case Node.ENTITY_REFERENCE_NODE:
                nodeTypeStr = "EntityReference";
                break;
            case Node.ENTITY_NODE:
                nodeTypeStr = "Entity";
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                nodeTypeStr = "ProcessingInstruction";
                break;
            case Node.COMMENT_NODE:
                nodeTypeStr = "Comment";
                break;
            case Node.DOCUMENT_NODE:
                nodeTypeStr = "Document";
                break;
            case Node.DOCUMENT_TYPE_NODE:
                nodeTypeStr = "DocumentType";
                break;
            case Node.DOCUMENT_FRAGMENT_NODE:
                nodeTypeStr = "DocumentFragment";
                break;
            case Node.NOTATION_NODE:
                nodeTypeStr = "Notation";
                break;
            default:
                nodeTypeStr = "" + nodeType;
        }
        return nodeTypeStr;
    }

    public static void logNode(Logger log, Node node) {
        try {
            logNode(log, node, 0);
        }
        catch (Exception e) {
            log.debug("Failed to log node: ", e);
        }
    }

    private static void logNode(Logger log, Node node, int level) {
        String nodeName = node.getNodeName();
        String prefix = "";
        for (int i=0; i<level; i++) {
            prefix = prefix + "--";
        }
        log.trace(prefix + " node name: " + nodeName);
        short nodeType = node.getNodeType();
        log.trace(prefix + " node type: " + nodeTypeToString(nodeType));
        String namespaceURI = node.getNamespaceURI();
        if (namespaceURI != null) {
            log.trace(prefix + " node namespace URI: " + namespaceURI);
        }

        if (Node.DOCUMENT_NODE == nodeType) {
            Element root = ((Document) node).getDocumentElement();
            logNode(log, root, level + 1);
        }
        else if (Node.ELEMENT_NODE == nodeType) {
            NamedNodeMap attrs = node.getAttributes();  /* Only Element nodes have attributes */
            for (int i = 0; i < attrs.getLength(); i++) {
                Node attr = attrs.item(i);
                logNode(log, attr, level + 1);
            }

            NodeList nodeList = node.getChildNodes();
            for (int i=0; i<nodeList.getLength(); i++) {
                Node child = nodeList.item(i);
                logNode(log, child, level + 1);
            }
        }
        else if (Node.TEXT_NODE == nodeType) {
            log.trace(prefix + " text data: " + node.getNodeValue());
        }
    }

    /** Given a XML DOM node, look for the value of a text element identified
     *  by its tag name.
     *
     * This method is meant to be used in the reply of a NETCONF get-config
     * operation, so it will check that the tag is contained in a <data> tag.
     *
     * @param node The starting point for the search in the XML document.
     * @param tagName The name of the tag that contains the text element.
     *
     * @return The first text text value found as a String. null if no value can
     * be found.
     */

    static public String getTextValueByTagName(Node node, String tagName) {
        String textValue = null;

        try {
            Element root = ((Document) node).getDocumentElement();

            if (! root.getNodeName().equals("data"))
                return null;

            NodeList hostnameNodes = root.getElementsByTagName(tagName);
            Node hostnameNode = hostnameNodes.item(0);
            NodeList childs = hostnameNode.getChildNodes();
            for (int i=0; i<childs.getLength(); i++) {
                Node child = childs.item(i);
                if (child instanceof Text) {
                    textValue = child.getNodeValue();
                    break;
                }
            }
        }
        catch (Exception e) {
            LOG.debug("Could not find hostname in Node: ", e);
        }

        return textValue;
    }
}
