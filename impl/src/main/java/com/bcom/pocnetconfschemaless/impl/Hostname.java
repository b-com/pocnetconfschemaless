/*
 * Copyright © 2017 b-com and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.bcom.pocnetconfschemaless.impl;

import java.util.concurrent.Future;
import javax.xml.transform.dom.DOMSource;

import com.bcom.pocnetconfschemaless.device.JunOS;
import com.bcom.pocnetconfschemaless.device.NetconfTesttool;
import com.bcom.pocnetconfschemaless.device.NokiaR14;
import com.bcom.pocnetconfschemaless.utils.NetconfMountPoint;
import com.bcom.pocnetconfschemaless.utils.XmlUtils;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.controller.md.sal.dom.api.DOMDataBroker;
import org.opendaylight.controller.md.sal.dom.api.DOMDataReadOnlyTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMDataWriteTransaction;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPoint;
import org.opendaylight.controller.md.sal.dom.api.DOMMountPointService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcError.ErrorType;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

class Hostname {
    static Logger LOG = LoggerFactory.getLogger(Hostname.class);

    static private RpcResult<Void> SUCCESS = RpcResultBuilder.<Void>success().build();

    static HostnameAdapter makeHostnameAdaptor(String deviceFamily) {
        if (deviceFamily != null) {
            if (deviceFamily.equals("junos")) {
                return new JunOS();
            } else if (deviceFamily.equals("nokia")) {
                return new NokiaR14();
            }
        }
        return new NetconfTesttool();  // default device adaptor
     }

    /** Get the hostname of a NETCONF device
     *
     * @param domMountPointService Mount point service
     * @param nodeId Name of the NETCONF mount point
     * @param deviceFamily Family of the NETCONF device. Can be "junos",
     *                     "nokia", "netconf-testtool" or null. If null,
     *                     defaults to "netconf-testtool".
     *
     * @return the hostname in case of success, or a special string "[noname#]"
     *         in case of error.
     */

    static String getHostname(final DOMMountPointService domMountPointService,
                                String nodeId, String deviceFamily) {

        final DOMMountPoint mountPoint = NetconfMountPoint.getNetconfNodeMountPoint(
                domMountPointService, nodeId);  /* TODO: handle possible error */
        final DOMDataBroker dataBroker = mountPoint.getService(DOMDataBroker.class).get();
        DOMDataReadOnlyTransaction rtx = dataBroker.newReadOnlyTransaction();

        String hostname;
        try {
            HostnameAdapter hostnameAdapter = makeHostnameAdaptor(deviceFamily);

            // The YANG Instance Identifier is relative to the mountpoint.
            // (note: When we set it to 'null', we read the whole device
            // configuration, but the netconf stack fails to analyze the
            // response: the special value YangInstanceIdentifier.EMPTY must be
            // used instead)

            Optional<NormalizedNode<?, ?>> opt = rtx.read(
                    LogicalDatastoreType.CONFIGURATION,
                    hostnameAdapter.getGetHostnameYIID()).checkedGet();

            if (opt.isPresent()) {
                final AnyXmlNode anyXmlData = (AnyXmlNode) opt.get();
                Node node = anyXmlData.getValue().getNode();
                XmlUtils.logNode(LOG, node);
                hostname = hostnameAdapter.lookForHostname(node);
                if (null == hostname) {
                    hostname = "[noname4]";
                }
            } else {
                hostname = "[noname1]";
            }

        } catch (ReadFailedException e) {
            LOG.warn("Failed to read operational datastore: {}", e);
            hostname = "[noname2]";
/*
        } catch (Exception e) {
            LOG.warn("Failed to read operational datastore: {}", e);
            hostname = "[noname3]";
*/
        } finally {
            rtx.close();
        }

        return hostname;
    }

    /** Set the hostname of a NETCONF device
     *
     * @param domMountPointService Mount point service
     * @param nodeId Name of the NETCONF mount point
     * @param deviceFamily Family of the NETCONF device. Can be "junos",
     *                     "nokia", "netconf-testtool" or null. If null,
     *                     defaults to "netconf-testtool".
     * @param hostname The new hostname
     */

    static Future<RpcResult<Void>> setHostname(
            final DOMMountPointService domMountPointService,
            String nodeId, String deviceFamily, String hostname) {

        HostnameAdapter hostnameAdapter = makeHostnameAdaptor(deviceFamily);

        Document hostnameDocument = hostnameAdapter.createSetHostnameDocument(hostname);
        LOG.trace("setHostname(): hand-made XML document:");
        XmlUtils.logNode(LOG, hostnameDocument);

        YangInstanceIdentifier yangInstanceIdentifier = hostnameAdapter.getSetHostnameYIID();
        AnyXmlNode anyXmlNode = Builders.anyXmlBuilder()
                .withNodeIdentifier(new YangInstanceIdentifier.NodeIdentifier(
                        QName.create(
                                hostnameAdapter.getSetHostnameNS(),
                                yangInstanceIdentifier.getLastPathArgument().getNodeType().getLocalName())))
                .withValue(new DOMSource(hostnameDocument.getDocumentElement()))
                .build();

        return editConfig(domMountPointService, nodeId, yangInstanceIdentifier, anyXmlNode);
    }

    static Future<RpcResult<Void>> editConfig(
            final DOMMountPointService domMountPointService, String nodeId,
            YangInstanceIdentifier yangInstanceIdentifier, AnyXmlNode anyXmlNode) {

        /* The following error string will be visible in the RPC reply */
        String userVisibleError = null;

        try {
            final DOMMountPoint mountPoint = NetconfMountPoint.getNetconfNodeMountPoint(
                    domMountPointService, nodeId);
            final DOMDataBroker dataBroker = mountPoint.getService(DOMDataBroker.class).get();
            DOMDataWriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();

            LOG.trace("editConfig(): XML document passed as a AnyXmlNode:");
            XmlUtils.logNode(LOG, anyXmlNode.getValue().getNode());

            // (actually) edit-config
            writeTransaction.merge(LogicalDatastoreType.CONFIGURATION, yangInstanceIdentifier, anyXmlNode);

            // commit (synchronously)
            final CheckedFuture<Void, TransactionCommitFailedException> submit = writeTransaction.submit();
            submit.checkedGet();
        }
        catch (IllegalArgumentException e) {
            /* Thrown by getNetconfNodeMountPoint() */
            LOG.warn("", e);
            userVisibleError = e.getMessage();
        }
        catch (IllegalStateException e) {
            /* Thrown by getService.get() */
            LOG.warn("", e);
            userVisibleError = "Failed to get data broker for mount point service";
        }
        catch (TransactionCommitFailedException e) {
            /* Thrown by submit.checkedGet() */
            LOG.warn("", e);
            userVisibleError = "NETCONF commit failed";
        }

        if (userVisibleError == null) {
            return RpcResultBuilder.<Void>success().buildFuture();
        }
        else {
            return RpcResultBuilder.<Void>failed()
                    .withRpcError(RpcResultBuilder.newError(
                            ErrorType.APPLICATION,
                            null,  // rem: tag=null => tag = "operation-failed"
                            userVisibleError))
                    .buildFuture();
        }
    }
}
