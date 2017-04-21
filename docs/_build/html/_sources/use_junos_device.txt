Utiliser pocnetconfschemaless avec un équipement Juniper sous JunOS
===================================================================

Dans cet exemple, on travaille avec le routeur Juniper MX5 (JunOS 14.2R1.9) utilisé sur la maquette FSOL du projet
ROADS. Le hostname du routeur est pjunren1.

Le poc s'utilise en passant des requêtes RESTConf au service de configuration netconf d'ODL (pour le montage
des équipements) et au service pocnetconfschemaless (pour lancer les requêtes de lecture/écriture du hostname).

On suppose que le client RESTConf (eg Firefox + plugin RESTClient) se trouve sur la même machine qu'ODL. La connexion
à ODL se fait avec le login ``admin`` et le mot de passe ``admin``.

Monter le device pjunren1
-------------------------

::

    PUT http://localhost:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/pjunren1
    Content-Type: application/xml
    Accept: application/xml
    <node xmlns="urn:TBD:params:xml:ns:yang:network-topology">
      <node-id>pjunren1</node-id>
      <host xmlns="urn:opendaylight:netconf-node-topology">10.51.0.102</host>
      <port xmlns="urn:opendaylight:netconf-node-topology">830</port>
      <username xmlns="urn:opendaylight:netconf-node-topology">admin</username>
      <password xmlns="urn:opendaylight:netconf-node-topology">juniper1</password>
      <tcp-only xmlns="urn:opendaylight:netconf-node-topology">false</tcp-only>
      <schemaless xmlns="urn:opendaylight:netconf-node-topology">true</schemaless>
    </node>

ODL répond avec le code HTTP ``201 Created``.

La commande karaf ``netconf:list-devices`` permet de confirmer que le device pjunren1 est bien monté::

    opendaylight-user@root>netconf:list-devices
    NETCONF ID        | NETCONF IP  | NETCONF Port | Status
    ----------------------------------------------------------
    controller-config | 127.0.0.1   | 1830         | connected
    pjunren1          | 10.51.0.102 | 830          | connected

Lire le hostname
----------------

::

   POST http://localhost:8181/restconf/operations/pocnetconfschemaless:get-hostname
   Content-Type: application/yang.data+json
   Accept: application/json
   {
       "input": {
           "node-id": "pjunren1",
           "device-family": "junos"
       }
   }

pocnetconftesttool répond avec le contenu suivant::

    {
         "output": {
             "hostname": "pjunren1"
         }
    }


Ecrire (modifier) le hostname
-----------------------------

On va définir le hostname à la valeur ``pjunren1new`` avec::

   POST http://localhost:8181/restconf/operations/pocnetconfschemaless:set-hostname
   Content-Type: application/yang.data+json
   Accept: application/json
   {
       "input": {
           "node-id": "pjunren1",
           "device-family": "junos",
           "hostname": "pjunren1new"
       }
   }

ODL répond avec le code HTTP ``200 OK``.

Dans les logs d'ODL, on voit passer (entre autres) le message NETCONF suivant::

    <rpc message-id="m-29" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
    <edit-config>
    <target>
    <candidate/>
    </target>
    <config>
    <configuration xmlns="http://xml.juniper.net/xnm/1.1/xnm">
    <system>
    <host-name>pjunren1new</host-name>
    </system>
    </configuration>
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
           "node-id": "pjunren1",
           "device-family": "junos"
       }
   }

pocnetconftesttool répond cette fois avec le contenu suivant::

    {
         "output": {
             "hostname": "pjunren1new"
         }
    }

Démonter le device pjunren1
---------------------------

Une fois le travail sur l'équipement terminé, on peut le démonter avec la requête REST suivant::

   DELETE http://localhost:8181/restconf/config/network-topology:network-topology/topology/topology-netconf/node/pjunren1
