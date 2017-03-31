/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import javax.xml.transform.dom.DOMSource;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class HostnameXmlUtilsTest {
    Logger LOG = LoggerFactory.getLogger(HostnameXmlUtilsTest.class);

    @Test
    public void testBuildAnyXmlConfigSystemHostname() {
        LOG.trace("hostname document:");
        Document hostnameDocument = HostnameXmlUtils.createHostnameDocument("localhost-from-ut");
        XmlUtils.logNode(LOG, hostnameDocument);

        YangInstanceIdentifier hostnameYIID = YangInstanceIdentifier.builder()
                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.NS, "system")))
                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.NS, "hostname")))
                .build();

        AnyXmlNode anyXmlNode = Builders.anyXmlBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
                        QName.create(
                                HostnameXmlUtils.NS,
                                hostnameYIID.getLastPathArgument().getNodeType().getLocalName())))
                .withValue(new DOMSource(hostnameDocument.getDocumentElement()))
                .build();

        LOG.trace("AnyXml document:");
        XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());
    }

//    @Test
//    public void testBuildAnyXmlConfigSystemHostname2() {
//        LOG.trace("hostname document:");
//        Document hostnameDocument = HostnameXmlUtils.createHostnameDocument("localhost-from-ut");
//        XmlUtils.logNode(LOG, hostnameDocument);
//
//        YangInstanceIdentifier hostnameYIID = YangInstanceIdentifier.builder()
//                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.NS, "system")))
//                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.NS, "hostname")))
//                .build();
//
//        String localName = hostnameYIID.getLastPathArgument().getNodeType().getLocalName();  // <-- "hostname"
//
//        YangInstanceIdentifier emptyYIID = YangInstanceIdentifier.EMPTY;
//        YangInstanceIdentifier.PathArgument pathArgument = emptyYIID.getLastPathArgument();  // <-- pathArgument = null
//        QName nodeType = pathArgument.getNodeType();
//        localName = nodeType.getLocalName();
//
//        AnyXmlNode anyXmlNode = Builders.anyXmlBuilder()
//                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
//                        QName.create(
//                                HostnameXmlUtils.NS,
//                                emptyYIID.getLastPathArgument().getNodeType().getLocalName())))
//                .withValue(new DOMSource(hostnameDocument.getDocumentElement()))
//                .build();
//
//        LOG.trace("AnyXml document:");
//        XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());
//    }

}
