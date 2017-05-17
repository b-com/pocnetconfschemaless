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

public class JunOS implements HostnameAdapter {
    static final Logger LOG = LoggerFactory.getLogger(JunOS.class);

    public static final String JUNOS_NS = "http://xml.juniper.net/xnm/1.1/xnm";

    @Override
    public YangInstanceIdentifier getGetHostnameYIID() {
        return YangInstanceIdentifier.builder()
                .node(new NodeIdentifier(QName.create(JUNOS_NS, "configuration")))
                .node(new NodeIdentifier(QName.create(JUNOS_NS, "system")))
                .node(new NodeIdentifier(QName.create(JUNOS_NS, "host-name")))
                .build();
    }

    @Override
    public String lookForHostname(Node node) {
        return XmlUtils.getTextValueByTagName(node, "host-name");
    }

    /** Build an XML document to configure the hostname of a JunOS device,
     * starting just under the <system> element
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

            Element hostnameElement = doc.createElementNS(JUNOS_NS, "host-name");
            doc.appendChild(hostnameElement);
            hostnameElement.appendChild(doc.createTextNode(hostname));
        }
        catch (Exception e) {
            LOG.debug(String.format("Could not build XML document to " +
                    "configure hostname: %s", hostname), e);
            doc = null;
        }
        return doc;
    }

    @Override
    public String getSetHostnameNS() {
        return JUNOS_NS;
    }

    @Override
    public YangInstanceIdentifier getSetHostnameYIID() {
        return getGetHostnameYIID();
    }
}
