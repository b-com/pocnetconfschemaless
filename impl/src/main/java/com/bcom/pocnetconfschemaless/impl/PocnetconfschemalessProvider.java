/*
 * Copyright © 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package com.bcom.pocnetconfschemaless.impl;

import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pocnetconfschemaless.rev170317.PocnetconfschemalessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PocnetconfschemalessProvider {

    private static final Logger LOG = LoggerFactory.getLogger(PocnetconfschemalessProvider.class);

    private final RpcProviderRegistry rpcProviderRegistry;
    private final DOMMountPointService domMountPointService;
    private final DOMDataBroker domDataBroker;

    private BindingAwareBroker.RpcRegistration<PocnetconfschemalessService> serviceRegistration;

    public PocnetconfschemalessProvider(final RpcProviderRegistry rpcProviderRegistry,
                                        final DOMMountPointService domMountPointService,
                                        final DOMDataBroker domDataBroker) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.domMountPointService = domMountPointService;
        this.domDataBroker = domDataBroker;
    }

    /**
     * Method called when the blueprint container is created.
     */
    public void init() {
        serviceRegistration = rpcProviderRegistry.addRpcImplementation(
                PocnetconfschemalessService.class, new PocnetconfschemalessImpl(domMountPointService));
        LOG.info("PocnetconfschemalessProvider Session Initiated");
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        serviceRegistration.close();
        LOG.info("PocnetconfschemalessProvider Closed");
    }
}
