Démarrer le logiciel pocnetconfschemaless et le device NETCONF
==============================================================

Démarrer OpenDaylight
---------------------

::

   $ cd ~/code/roads/pocnetconfschemaless/karaf/target/assembly
   $ bin/karaf
   opendaylight-user@root>log:set TRACE org.opendaylight.netconf.sal
   opendaylight-user@root>log:set TRACE com.bcom
   opendaylight-user@root>feature:install odl-netconf-console
   opendaylight-user@root>log:tail

Attendre l'apparition du log suivant::

   2017-04-05 13:50:53,582 | INFO  | sing-executor-11 | NetconfDevice                    | 304 - org.opendaylight.netconf.sal-netconf-connector - 1.4.3.SNAPSHOT | RemoteDevice{controller-config}: Netconf connector initialized successfully

Démarrer netconf-testtool
-------------------------

A la place d'un vrai équipement réseau, le poc travaille avec un simulateur d'équipement NETCONF fourni dans le
projet netconf d'ODL.

Pour le poc, la structure de la configuration utilisée par netconf-testtool est donnée dans un fichier YANG construit
pour les besoins du poc::

    $ cat ~/code/roads/pocnetconfschemaless/netconf-testtool-config/yang-hostname-v2/hostname@2017-03-31.yang
    module hostname {
        yang-version 1;
        namespace "urn:opendaylight:hostname";
        prefix "hn";

        revision "2017-03-31";

        container system {
            leaf hostname {
                type string;
            }
            leaf location {
                type string;
            }
        }
    }

Pour démarrer netconf-testtool::

   $ cd ~/code/roads/pocnetconfschemaless/netconf-testtool-config
   $ java -jar ~/code/opendaylight/netconf/netconf/tools/netconf-testtool/target/netconf-testtool-1.1.3-SNAPSHOT-executable.jar \
       --schemas-dir yang-hostname-v2/  --debug true

