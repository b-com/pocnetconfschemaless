Démarrer le logiciel pocnetconfschemaless et le device NETCONF
==============================================================

pocnetconfschemaless peut travailler soit avec netconf-testtool, soit avec le routeur Juniper MX5.

Démarrer OpenDaylight
---------------------

::

   $ cd ~/code/roads/pocnetconfschemaless/karaf/target/assembly
   $ bin/karaf
   opendaylight-user@root>log:set TRACE org.opendaylight.netconf.sal
   opendaylight-user@root>log:set TRACE com.bcom
   opendaylight-user@root>feature:install odl-pocnetconfschemaless-ui
   opendaylight-user@root>feature:install odl-netconf-console
   opendaylight-user@root>log:tail

Attendre l'apparition du log suivant::

    2017-04-21 16:07:19,238 | INFO  | config-pusher    | ConfigPusherImpl                 | 130 - org.opendaylight.controller.config-persister-impl - 0.5.3.SNAPSHOT | Successfully pushed configuration snapshot 04-xsql.xml(odl-pocnetconfschemaless-ui,odl-pocnetconfschemaless-ui)

.. warning:: Les features du poc et de netconf ne doivent pas être chargées automatiquement lors du premier démarrage
   de karaf. C'est le comportement par défaut de pocnetconfschemaless. Sinon, à cause d'un bug, la connexion SSH ne
   s'établit pas avec netconf-testtool ou avec le routeur Juniper MX5.

.. note:: L'installation de la feature ``odl-netconf-console`` n'est pas nécessaire pour utiliser le poc. Elle
   est toutefois utile pour voir simplement l'état de la connexion d'ODL avec les différents devices NETCONF.

.. _start-netconf-testtool:

Démarrer netconf-testtool
-------------------------

A la place d'un vrai équipement réseau, le poc peut travailler avec le simulateur d'équipement NETCONF fourni dans le
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
   $ java -jar ~/code/opendaylight/netconf/netconf/tools/netconf-testtool/target/netconf-testtool-1.2.0-SNAPSHOT-executable.jar \
       --schemas-dir yang-hostname-v2/  --debug true

