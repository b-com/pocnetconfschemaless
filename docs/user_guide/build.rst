Construire le logiciel pocnetconfschemaless
===========================================

Cette version de pocnetconfschemaless utilise la version Carbon d'OpenDaylight.
Elle nécessite de patcher la projet OpenDaylight netconf afin de corriger un bug dans le code netconf d'ODL et un bug
dans l'outil de test netconf-testtool présent dans le projet netconf.

Récupérer et construire une première fois le code de pocnetconfschemaless
-------------------------------------------------------------------------

On construit une première fois le logiciel pocnetconfschemaless en utilisant les dépendances maven officielles
d'OpenDaylight. Cela permet de tirer toutes les dépendances et de les mettres en cache dans ~/.m2/repository. Mais
on récupère le code netconf buggé::

   $ mkdir -p ~/code/roads
   $ git clone ssh://gitolite@forge.b-com.com/roads/SANDBOX/pocnetconfschemaless.git
   $ cd pocnetconfschemaless
   $ mvn clean install

.. _patch-netconf:

Récupérer, patcher et construire le projet netconf
--------------------------------------------------

On récupère le code du projet netconf depuis le mirroir GitHub d'ODL, et on
travaille sur le tag release/carbon::

   $ mkdir -p ~/code/opendaylight
   $ git clone https://github.com/opendaylight/netconf.git
   $ cd netconf
   $ git checkout release/carbon

Depuis le répertoire du projet netconf, appliquer les patchs::

   $ cd ~/code/opendaylight/netconf
   $ git am < ~/code/roads/pocnetconfschemaless/0001-netconf-testtool-accept-RSA-hostname-key.patch
   Application de  netconf-testtool: accept RSA hostname key
   $ git am < ~/code/roads/pocnetconfschemaless/0002-fix-for-schemaless-devices.patch
   Application de  fix for schemaless devices

.. note::

   The patch ``0001-netconf-testtool-accept-RSA-hostname-key.patch`` is not
   strictly necessary. It permits to connect to ``netconf-testtool`` with
   Ubuntu 16.04 ``ssh`` client, which is sometimes useful but not necessary to
   use the poc. The patch works with Boron and Carbon.

.. note::

   The patch ``0002-fix-for-schemaless-devices.patch`` fixes the issue
   :ref:`issue-bad-transformer`. It will be required until the problem is fixed
   in ODL.

Construire le projet netconf avec::

   $ cd ~/code/opendaylight/netconf
   $ mvn clean install -DskipTests

Reconstruire le logiciel pocnetconfschemaless
---------------------------------------------

Lors de la construction du projet netconf lors de l'étape précédente, les produits de la génération ont été copiés dans
le cache maven ~/.m2/repository. On va maintenant reconstruire pocnetconfschemaless en utilisant cette version de
netconf. Pour cela, on va utiliser l'option ``-nsu`` (no snapshot update) qui permet d'éviter la mise à jour du
cache maven avec une version plus récentes des artefacts netconf::

   $ cd ~/code/roads/pocnetconfschemaless
   $ mvn clean install -nsu

On dispose maintenant d'une distribution karaf avec un netconf patché dans
``~/code/roads/pocnetconfschemaless/karaf/target/assembly``.
