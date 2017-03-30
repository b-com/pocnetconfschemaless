/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

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

    /**
     * Add an attribute to a given element of a XML document.
     *
     * @param doc		The document to edit
     * @param element	The element to edit
     * @param attrName	The attribute to add
     * @param attrValue	The added attribute value
     */
    public static void setAttr(Document doc, Element element, String attrName, String attrValue) {
        Attr attr = doc.createAttribute(attrName);
        attr.appendChild(doc.createTextNode(attrValue));
        element.setAttributeNode(attr);
    }

    /**
     * Create a child text element and append it to a given father element of a XML document.
     *
     * @param doc 			The document to edit
     * @param father		The element in which the new element should be added
     * @param elementName	The new element name
     * @param elementValue	The new element value
     */
    public static void appendTextElement(Document doc, Element father, String elementName, String elementValue) {
        Element child = doc.createElement(elementName);
        father.appendChild(child);
        child.appendChild(doc.createTextNode(elementValue));
    }

    /**
     * Create a child element, append it to a given father and return the newly created element.
     *
     * @param doc 			The document to edit
     * @param father		The element (father) in which the new element (child) should be added
     * @param elementName	The child element name
     * @return The created child element.
     */
    public static Element appendPlaceholderElement(Document doc, Element father, String elementName) {
        Element child = doc.createElement(elementName);
        father.appendChild(child);
        return child;
    }

    /**
     * Create an empty child element and append it to a given father .
     *
     * @param doc 			The document to edit
     * @param father		The element (father) in which the new element (child) should be added
     * @param elementName	The child element name
     */
    public static void appendEmptyElement(Document doc, Element father, String elementName) {
        father.appendChild(doc.createElement(elementName));
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
}
