/*
 * Copyright (c) 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;


import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;


/** Network device abstraction layer for the set-hostname and get-hostname
 * RPCs.
 */

public interface HostnameAdapter {
    YangInstanceIdentifier getGetHostnameYIID();
    String lookForHostname(Node node);

    Document createSetHostnameDocument(String hostname);
    String getSetHostnameNS();
    YangInstanceIdentifier getSetHostnameYIID();
}
