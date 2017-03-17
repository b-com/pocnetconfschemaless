/*
 * Copyright © 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.bcom.pocnetconfschemaless.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PocnetconfschemalessProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PocnetconfschemalessProvider.class);

    private final DataBroker dataBroker;

    public PocnetconfschemalessProvider(final DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        LOG.info("PocnetconfschemalessProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        LOG.info("PocnetconfschemalessProvider Closed");
    }
}