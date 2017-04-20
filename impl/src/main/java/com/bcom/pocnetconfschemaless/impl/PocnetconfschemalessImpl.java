/*
 * Copyright © 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.pocnetconfschemaless.rev170317.*;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import static org.opendaylight.yangtools.yang.common.RpcResultBuilder.success;

public class PocnetconfschemalessImpl implements PocnetconfschemalessService {
    private static final Logger LOG = LoggerFactory.getLogger(PocnetconfschemalessImpl.class);

    private final DOMMountPointService domMountPointService;

    public PocnetconfschemalessImpl(DOMMountPointService domMountPointService) {
        this.domMountPointService = domMountPointService;
    }

    @Override
    public Future<RpcResult<Void>> setHostname(SetHostnameInput input) {
        LOG.info(String.format("set-hostname(node-id=%s, hostname=%s)", input.getNodeId(), input.getHostname()));
        Hostname.setHostname(domMountPointService, input.getNodeId(), input.getHostname());
        Void nothing = null;
        return RpcResultBuilder.success(nothing).buildFuture();
    }

    @Override
    public Future<RpcResult<GetHostnameOutput>> getHostname(GetHostnameInput input) {
        LOG.info(String.format("get-hostname(node-id=%s, device-family=%s)",
                input.getNodeId(), input.getDeviceFamily()));
        String hostname = Hostname.getHostname(domMountPointService, input.getNodeId(), input.getDeviceFamily());
        LOG.info(String.format("hostname=%s", hostname));
        GetHostnameOutput output = new GetHostnameOutputBuilder().setHostname(hostname).build();
        return success(output).buildFuture();
    }
}
