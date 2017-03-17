/*
 * Copyright © 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPoint;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

class Hostname {

    static YangInstanceIdentifier getMountPointId(String nodeId) {
        YangInstanceIdentifier mountPointId = YangInstanceIdentifier.builder()
                // network-topology:network-topology
                .node(NetworkTopology.QNAME)
                // .../topology/topology-netconf/
                .node(Topology.QNAME)
                .nodeWithKey(Topology.QNAME, QName.create(Topology.QNAME, "topology-id").intern(), TopologyNetconf.QNAME.getLocalName())
                // .../node/netconf-testtool/
                .node(Node.QNAME)
                .nodeWithKey(Node.QNAME, QName.create(Node.QNAME, "node-id").intern(), nodeId)
                .build();
        return mountPointId;
    }

    /** Get the mount point object for the specified NETCONF node.
     *
     * Equivalent to '.../restconf/<config | operational>network-topology:network-topology/topology/topology-netconf/node/netconf-testtool/yang-ext:mount/'
     *
     * That same mount point can be used to read both config and operational data
     *
     * @param domMountPointService Mount point service to retrieve the mount point
     * @param nodeId Name of the NETCONF mount point
     *
     * @return The hostname
     *
     * @throws java.lang.IllegalArgumentException: Unable to locate mountpoint (not mounted yet or not configured)
     */

    private static DOMMountPoint getMountPoint(final DOMMountPointService domMountPointService, String nodeId) {
        YangInstanceIdentifier mountPointId = getMountPointId(nodeId);

        final Optional<DOMMountPoint> nodeOptional = domMountPointService.getMountPoint(mountPointId);

        Preconditions.checkArgument(nodeOptional.isPresent(),
                "Unable to locate mountpoint: %s, not mounted yet or not configured", nodeId);

        DOMMountPoint mountPoint = nodeOptional.get();
        return mountPoint;
    }

    /** Get the device hostname from its configuration
     *
     * @param nodeId Name of the NETCONF mount point
     *
     * @return the hostname
     */

    static String getHostname(final DOMMountPointService domMountPointService, String nodeId) {
        final DOMMountPoint mountPoint = getMountPoint(domMountPointService, nodeId);

        final DOMDataBroker dataBroker = mountPoint.getService(DOMDataBroker.class).get();

        DOMDataReadOnlyTransaction rtx = dataBroker.newReadOnlyTransaction();

        // The YANG Instance Identifier is relative to the mountpoint.
        // When we set it to 'null', we read the whole device configuration

        rtx.read(LogicalDatastoreType.CONFIGURATION, null);

        return "stub";
    }
}
