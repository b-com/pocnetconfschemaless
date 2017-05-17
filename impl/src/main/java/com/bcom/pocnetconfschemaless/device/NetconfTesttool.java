/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.device;

import com.bcom.pocnetconfschemaless.impl.HostnameAdapter;
import com.bcom.pocnetconfschemaless.utils.XmlUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class NetconfTesttool implements HostnameAdapter{
    static final Logger LOG = LoggerFactory.getLogger(NetconfTesttool.class);
    public static final String NS = "urn:opendaylight:hostname";

    @Override
    public YangInstanceIdentifier getGetHostnameYIID() {
        return YangInstanceIdentifier.EMPTY;
    }

    @Override
    public String lookForHostname(Node node) {
        return XmlUtils.getTextValueByTagName(node, "hostname");
    }

    /** Build an XML document to configure the hostname of a device according
     * to the hostname@2017-03-17.yang schema, starting just under the <system> element
     *
     * @param hostname The name of the host
     *
     * @return The XML document for use with the DOM parser. null in case of problem.
     */

    @Override
    public Document createSetHostnameDocument(String hostname) {
        Document doc = null;
        try {
            doc = XmlUtils.newDocument();

            Element hostnameElement = doc.createElementNS(NetconfTesttool.NS, "hostname");
            doc.appendChild(hostnameElement);
            hostnameElement.appendChild(doc.createTextNode(hostname));
        }
        catch (Exception e) {
            LOG.debug(String.format("Could not build XML document to configure hostname: %s", hostname), e);
            doc = null;
        }
        return doc;
    }

    @Override
    public String getSetHostnameNS() {
        return NS;
    }

    @Override
    public YangInstanceIdentifier getSetHostnameYIID() {
        return YangInstanceIdentifier.builder()
                .node(new NodeIdentifier(QName.create(NS, "system")))
                .node(new NodeIdentifier(QName.create(NS, "hostname")))
                .build();
    }
}
