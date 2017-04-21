Utiliser pocnetconfschemaless avec netconf-testtool
===================================================

Pour démarrer le device, voir :ref:`start-netconf-testtool`.

Le poc s'utilise en passant des requêtes RESTConf au service de configuration netconf d'ODL (pour le montage
des équipements) et au service pocnetconfschemaless (pour lancer les requêtes de lecture/écriture du hostname).

On suppose que le client RESTConf (eg Firefox + plugin RESTClient) se trouve sur la même machine qu'ODL. La connexion
à ODL se fait avec le login ``admin`` et le mot de passe ``admin``.


Monter le device netconf-testtool
---------------------------------

::

    PUT http://localhost:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/netconf-testtool
    Content-Type: application/xml
    Accept: application/xml
    <node xmlns="urn:TBD:params:xml:ns:yang:network-topology">
      <node-id>netconf-testtool</node-id>
      <host xmlns="urn:opendaylight:netconf-node-topology">127.0.0.1</host>
      <port xmlns="urn:opendaylight:netconf-node-topology">17830</port>
      <username xmlns="urn:opendaylight:netconf-node-topology">admin</username>
      <password xmlns="urn:opendaylight:netconf-node-topology">admin</password>
      <tcp-only xmlns="urn:opendaylight:netconf-node-topology">false</tcp-only>
      <schemaless xmlns="urn:opendaylight:netconf-node-topology">true</schemaless>
    </node>

ODL répond avec le code HTTP ``201 Created``.

La commande karaf ``netconf:list-devices`` permet de confirmer que le device netconf-testtool est bien monté::

    opendaylight-user@root>netconf:list-devices
    NETCONF ID        | NETCONF IP | NETCONF Port | Status
    ---------------------------------------------------------
    netconf-testtool  | 127.0.0.1  | 17830        | connected
    controller-config | 127.0.0.1  | 1830         | connected

Lire le hostname
----------------

::

   POST http://localhost:8181/restconf/operations/pocnetconfschemaless:get-hostname
   Content-Type: application/yang.data+json
   Accept: application/json
   {
       "input": {
           "node-id": "netconf-testtool"
       }
   }

.. note::

   Dans tous les appels ``get-hostname`` et ``set-hostname``, on pourrait rajouter la famille de device NETCONF
   avec ``"device-family": "netconf-testtool"``.

   Mais ce n'est pas nécessaire car c'est la valeur par défaut.

pocnetconftesttool répond avec le contenu suivant::

    {
         "output": {
             "hostname": "[noname4]"
         }
    }

Cela signifie que le hostname n'est pas défini.

Ecrire le hostname
------------------

On va définir le hostname à la valeur ``ntt`` avec::

   POST http://localhost:8181/restconf/operations/pocnetconfschemaless:set-hostname
   Content-Type: application/yang.data+json
   Accept: application/json
   {
       "input": {
           "node-id": "netconf-testtool"
           "hostname": "ntt"
       }
   }

ODL répond avec le code HTTP ``200 OK``.

Dans les logs de netconf-testtool, on voit passer (entre autres) le message NETCONF suivant::

    <rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="m-4">
    <edit-config>
    <target>
    <candidate/>
    </target>
    <default-operation>none</default-operation>
    <config>
    <system xmlns="urn:opendaylight:hostname">
    <hostname xmlns:ns0="urn:ietf:params:xml:ns:netconf:base:1.0" ns0:operation="replace">ntt</hostname>
    </system>
    </config>
    </edit-config>
    </rpc>


Relire le hostname
------------------

Comme lors de la première lecture, on demande le hostname avec::

   POST http://localhost:8181/restconf/operations/pocnetconfschemaless:get-hostname
   Content-Type: application/yang.data+json
   Accept: application/json
   {
       "input": {
           "node-id": "netconf-testtool"
       }
   }

pocnetconftesttool répond cette fois avec le contenu suivant::

    {
         "output": {
             "hostname": "ntt"
         }
    }

Dans les logs netconf-testtool, on voit qu'ODL demande la configuration avec le message NETCONF::

    <rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="m-7">
    <get-config>
    <source>
    <running/>
    </source>
    </get-config>
    </rpc>

netconf-testtool répond avec::

    <rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="m-7">
    <data>
    <system xmlns="urn:opendaylight:hostname">
    <hostname xmlns:ns0="urn:ietf:params:xml:ns:netconf:base:1.0" ns0:operation="replace">ntt</hostname>
    </system>
    </data>
    </rpc-reply>


Démonter le device netconf-testtool
-----------------------------------

Une fois le travail sur l'équipement terminé, on peut le démonter avec la requête REST suivant::

   DELETE http://localhost:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/netconf-testtool

