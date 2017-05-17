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

public class NokiaR14 implements HostnameAdapter{
    static final Logger LOG = LoggerFactory.getLogger(NokiaR14.class);

    public static final String NOKIA_CONF_NS = "urn:nokia.com:sros:ns:yang:sr:conf";
    public static final String NOKIA_CONF_SYSTEM_NS = "urn:nokia.com:sros:ns:yang:sr:conf-system";

    @Override
    public YangInstanceIdentifier getGetHostnameYIID() {
        return YangInstanceIdentifier.builder()
                .node(new NodeIdentifier(QName.create(NOKIA_CONF_NS, "configure")))
                .node(new NodeIdentifier(QName.create(NOKIA_CONF_SYSTEM_NS, "system")))
                //.node(new NodeIdentifier(QName.create(NOKIA_CONF_SYSTEM_NS, "name")))
                // => cannot filter on a leaf element with Nokia R14
                .build();
    }

    @Override
    public String lookForHostname(Node node) {
        return XmlUtils.getTextValueByTagName(node, "name");
    }

    /** Build an XML document to configure the hostname of a Nokia device,
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

            Element hostnameElement = doc.createElementNS(NOKIA_CONF_SYSTEM_NS, "name");
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
        return NOKIA_CONF_SYSTEM_NS;
    }

    @Override
    public YangInstanceIdentifier getSetHostnameYIID() {
        return YangInstanceIdentifier.builder()
                .node(new NodeIdentifier(QName.create(NOKIA_CONF_NS, "configure")))
                .node(new NodeIdentifier(QName.create(NOKIA_CONF_SYSTEM_NS, "system")))
                .node(new NodeIdentifier(QName.create(NOKIA_CONF_SYSTEM_NS, "name")))
                .build();
    }
}
