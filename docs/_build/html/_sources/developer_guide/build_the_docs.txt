Générer la documentation
========================

La doc de pocnetconfschemaless est écrite au format reStructuredText, et la
version HTML est générée à l'aide de Sphinx.

Installer Sphinx
----------------

Installer Sphinx::

    pip install Sphinx

Editer la documentation
-----------------------

Pour modifier la documentation: éditer les fichiers .rst.

Générer la documentation au format HTML
---------------------------------------

Pour (re-)générer la documentation HTML::

    make html

La documentation est générée dans le répertoire ``_build/html``. Le point
d'entrée est ``_build/html/index.html``.

Générer la documentation au format PDF
--------------------------------------

La génération de la documentation PDF nécessite des dépendances
complémentaires. Sous Ubuntu 16.04::

    sudo apt install texlive texlive-latex-extra

.. note::

   Le paquet ``texlive-latex-extra`` contient le fichier ``titlesec.sty`` qui
   est nécessaire avec Sphinx.

   Le paquet ``texlive-lang-french`` est nécessaire pour générer des
   documents en français.

Pour générer la documentation PDF::

    make latexpdf

Le fichier PDF est généré vers ``_build/latex/pocnetconfschemaless.pdf``.
