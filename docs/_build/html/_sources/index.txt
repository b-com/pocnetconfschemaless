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

Table des matières:

.. toctree::
   :maxdepth: 2

   build
   start
   use_netconf_testtool
   use_junos_device


Indices and tables
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
