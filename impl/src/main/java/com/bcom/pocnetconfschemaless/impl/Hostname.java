/*
 * Copyright © 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import javax.xml.transform.dom.DOMSource;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPoint;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

class Hostname {
    static Logger LOG = LoggerFactory.getLogger(Hostname.class);

    static private RpcResult<Void> SUCCESS = RpcResultBuilder.<Void>success().build();

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
     * @param deviceFamily Family of the NETCONF device (junos, netconf-testtool)
     *
     * @return the hostname
     */

    static String getHostname(final DOMMountPointService domMountPointService, String nodeId, String deviceFamily) {
        final DOMMountPoint mountPoint = getMountPoint(domMountPointService, nodeId);

        final DOMDataBroker dataBroker = mountPoint.getService(DOMDataBroker.class).get();

        DOMDataReadOnlyTransaction rtx = dataBroker.newReadOnlyTransaction();

        // The YANG Instance Identifier is relative to the mountpoint.
        // (note: When we set it to 'null', we read the whole device configuration, but the netconf stack fails to
        // analyze the response: the special value YangInstanceIdentifier.EMPTY must be used instead)

        YangInstanceIdentifier yiid = YangInstanceIdentifier.EMPTY;
        if ((deviceFamily != null) && (deviceFamily.equals("junos"))) {
            yiid = YangInstanceIdentifier.builder()
                    .node(new NodeIdentifier(QName.create(HostnameXmlUtils.JUNOS_NS, "configuration")))
                    .node(new NodeIdentifier(QName.create(HostnameXmlUtils.JUNOS_NS, "system")))
                    .node(new NodeIdentifier(QName.create(HostnameXmlUtils.JUNOS_NS, "host-name")))
                    .build();
        }

        String hostname;
        try {
            Optional<NormalizedNode<?, ?>> opt = rtx.read(LogicalDatastoreType.CONFIGURATION, yiid).checkedGet();

            if (opt.isPresent()) {
                final AnyXmlNode anyXmlData = (AnyXmlNode) opt.get();
                org.w3c.dom.Node node = anyXmlData.getValue().getNode();
                XmlUtils.logNode(LOG, node);
                hostname = HostnameXmlUtils.lookForHostname(node, deviceFamily);
                if (null == hostname) {
                    hostname = "[noname4]";
                }
            } else {
                hostname = "[noname1]";
            }

        } catch (ReadFailedException e) {
            LOG.warn("Failed to read operational datastore: {}", e);
            hostname = "[noname2]";
        } catch (Exception e) {
            LOG.warn("Failed to read operational datastore: {}", e);
            hostname = "[noname3]";
        } finally {
            rtx.close();
        }

        return hostname;
    }

    static void editConfig(final DOMMountPointService domMountPointService, String nodeId,
                           YangInstanceIdentifier yangInstanceIdentifier, AnyXmlNode anyXmlNode) {
        final DOMMountPoint mountPoint = getMountPoint(domMountPointService, nodeId);
        final DOMDataBroker dataBroker = mountPoint.getService(DOMDataBroker.class).get();
        DOMDataWriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        LOG.trace("editConfig(): XML document passed as a AnyXmlNode:");
        XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());

        // invoke edit-config
        writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, yangInstanceIdentifier, anyXmlNode);

        // commit asynchronously
        final CheckedFuture<Void, TransactionCommitFailedException> submit = writeTransaction.submit();
        Futures.transform(submit, new Function<Void, RpcResult<Void>>() {
            @Override
            public RpcResult<Void> apply(final Void result) {
                LOG.info("config writtent to '{}'", nodeId);
                return SUCCESS;
            }
        });

        // rem: in the real world, the future should be returned (see NcmountProvider.writeRoutes)
    }

    static void setHostnameJunOS(final DOMMountPointService domMountPointService,
                                 String nodeId, String hostname) {

        Document hostnameDocument = HostnameXmlUtils.createHostnameDocumentJunOS(hostname);

        LOG.trace("setHostnameJunOS(): hand-made XML document:");
        XmlUtils.logNode(LOG, hostnameDocument);

        YangInstanceIdentifier yangInstanceIdentifier = YangInstanceIdentifier.builder()
                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.JUNOS_NS, "configuration")))
                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.JUNOS_NS, "system")))
                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.JUNOS_NS, "host-name")))
                .build();

        AnyXmlNode anyXmlNode = Builders.anyXmlBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
                        QName.create(
                                HostnameXmlUtils.JUNOS_NS,
                                yangInstanceIdentifier.getLastPathArgument().getNodeType().getLocalName())))
                .withValue(new DOMSource(hostnameDocument.getDocumentElement()))
                .build();

        LOG.trace("setHostnameJunOS(): XML document after conversion to AnyXmlNode:");
        XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());

        editConfig(domMountPointService, nodeId, yangInstanceIdentifier, anyXmlNode);
    }

    static void setHostname(final DOMMountPointService domMountPointService,
                            String nodeId, String deviceFamily, String hostname) {

        if ((deviceFamily != null) && (deviceFamily.equals("junos"))) {
            setHostnameJunOS(domMountPointService, nodeId, hostname);
            return;
        }

        final DOMMountPoint mountPoint = getMountPoint(domMountPointService, nodeId);
        final DOMDataBroker dataBroker = mountPoint.getService(DOMDataBroker.class).get();
        DOMDataWriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

        /* This works, but let's try another approach. Commented out for history reasons: */
//        Document hostnameDocument = HostnameXmlUtils.createSystemHostnameDocument(hostname);
//        LOG.trace("setHostname(): hand-made XML document:");
//        XmlUtils.logNode(LOG, hostnameDocument);
//        YangInstanceIdentifier systemYIID = YangInstanceIdentifier.builder()
//                .node(new NodeIdentifier(QName.create(HostnameXmlUtils.NS, "system")))
//                //.node(new NodeIdentifier(QName.create(HostnameXmlUtils.NS, "hostname")))
//                .build();
//        AnyXmlNode anyXmlNode = Builders.anyXmlBuilder()
//                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
//                        QName.create(
//                                HostnameXmlUtils.NS,
//                                systemYIID.getLastPathArgument().getNodeType().getLocalName())))
//                .withValue(new DOMSource(hostnameDocument.getDocumentElement()))
//                .build();
//        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, systemYIID, anyXmlNode);

        Document hostnameDocument = HostnameXmlUtils.createHostnameDocument(hostname);

        LOG.trace("setHostname(): hand-made XML document:");
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

        LOG.trace("setHostname(): XML document after conversion to AnyXmlNode:");
        XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());

        // invoke edit-config, merge the config
        //
        // note: writeTransaction.put() is in my experience the preferred method
        //       to use with netconf-testtool.

        // writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY, anyXmlNode);
        //writeTransaction.put(LogicalDatastoreType.CONFIGURATION, YangInstanceIdentifier.EMPTY, anyXmlNode);
        writeTransaction.put(LogicalDatastoreType.CONFIGURATION, hostnameYIID, anyXmlNode);
        //writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, systemYIID, anyXmlNode);

        // commit
        //writeTransaction.submit();

        final CheckedFuture<Void, TransactionCommitFailedException> submit = writeTransaction.submit();
        Futures.transform(submit, new Function<Void, RpcResult<Void>>() {
            @Override
            public RpcResult<Void> apply(final Void result) {
                LOG.info("hostname '{}' writtent to '{}'", hostname, nodeId);
                return SUCCESS;
            }
        });

        // rem: in the real world, the future should be returned (see NcmountProvider.writeRoutes)
    }
}
