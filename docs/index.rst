.. pocnetconfschemaless documentation master file, created by
   sphinx-quickstart on Wed Apr  5 11:52:24 2017.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Documentation de pocnetconfschemaless
=====================================

pocnetconfschemaless est un PoC développé à b-com qui montre comment interagir avec un équipement NETCONF ne proposant
pas de modèle YANG depuis le code d'une application OpenDaylight.

Le poc permet de lire et d'écrire le hostname d'un device NETCONF. Les devices supportés sont
netconf-testtool (simulateur d'équipement NETCONF fourni avec ODL) et le routeur Juniper MX5 de b-com.

User Guide
----------

.. toctree::
   :maxdepth: 2

   user_guide/build
   user_guide/start
   user_guide/use_netconf_testtool
   user_guide/use_junos_device
   user_guide/use_nokia_sros_device

Developer guide
---------------

.. toctree::

   developer_guide/overview
   developer_guide/dependencies
   developer_guide/mount_point
   developer_guide/build_the_docs

OpenDaylight issues
-------------------

.. toctree::

   odl_issues/01_bad_transformer


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
