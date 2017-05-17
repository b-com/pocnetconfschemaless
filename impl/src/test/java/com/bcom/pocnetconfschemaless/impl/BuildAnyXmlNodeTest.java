/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import com.bcom.pocnetconfschemaless.device.NetconfTesttool;
import com.bcom.pocnetconfschemaless.utils.XmlUtils;
import javax.xml.transform.dom.DOMSource;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class BuildAnyXmlNodeTest {
    Logger LOG = LoggerFactory.getLogger(BuildAnyXmlNodeTest.class);

    @Test
    public void testBuildAnyXmlForNetconfTesttoolSetHostname() {
        LOG.trace("hostname document:");

        NetconfTesttool nttAdapter = new NetconfTesttool();

        Document hostnameDocument = nttAdapter.createSetHostnameDocument("localhost-from-ut");
        XmlUtils.logNode(LOG, hostnameDocument);

        YangInstanceIdentifier hostnameYIID = nttAdapter.getSetHostnameYIID();

        AnyXmlNode anyXmlNode = Builders.anyXmlBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
                        QName.create(
                                nttAdapter.getSetHostnameNS(),
                                hostnameYIID.getLastPathArgument().getNodeType().getLocalName())))
                .withValue(new DOMSource(hostnameDocument.getDocumentElement()))
                .build();

        LOG.trace("AnyXml document:");
        XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());
    }
}
