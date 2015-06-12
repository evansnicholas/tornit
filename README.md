tornit
=========

An XBRL taxonomy viewer built with the Play! framework. 

Getting started
---------------

Tornit can be run either as a standalone application deployed to a server, or as a more traditional desktop application.  The instructions below describe how to use tornit locally on your machine.

Prerequisites:
* A local maven repository with a version of TQA (Taxonomy Query API) compiled against Scala 2.11.  The TQA dependency can be adjusted in build.sbt.  Note that the TQA library is not open-source.  However, if you have your own taxonomy query engine, you can integrate it with tornit by implementing the plugin API defined in the model.TaxonomyApi trait, and then configuring tornit to use your plugin by adding an entry to conf/play.plugins.
* The typesafe activator with the activator command available on the command line.

Setting up tornit:
* Clone the project into a local directory.
* Place the taxonomy files you wish to view in the lib/taxonomy directory.
* Open a terminal in the root directory of the application and start the typesafe activator (with the *activator* command).
* Start the taxoscope by typing *run* in the activator console.
* Point your browser at localhost:9000 and start viewing!

Using Tornit
------------

Using Tornit you can browse the information available in the taxonomy.  

You can view presentation ELR's, and get information about the concepts in the presentation tree:

![Presentation ELR view](/screenshots/presentation_view.png "Presentation view.")

You can view a concept's dimensional tree:

![Dimensionsal view](/screenshots/dimension_view.png)

You can view a concept's labels:

![Labels view](/screenshots/labels_view.png)

You can view a concept's element declaration and get insight into its substitution group and type hierarchies:

![Schema view](/screenshots/schema_view.png)

