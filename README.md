tornit
=========

An XBRL taxonomy viewer made with the Play! framework.

Getting started
---------------

Prerequisites:
* A local maven repository with a version of TQA (Taxonomy Query API) compiled against scala 2.11.  The TQA dependency can be adjusted in build.sbt.
* The typesafe activator with the activator command available on the command line.

Setting up the taxoscope:
* Clone the project into a local directory.
* Place the taxonomy files you wish to view in the lib/taxonomy directory.
* Open a terminal in the root directory of the application and start the typesafe activator (with the *activator* command).
* Start the taxoscope by typing *run* in the activator console.
* Point your browser at localhost:9000 and start viewing!
