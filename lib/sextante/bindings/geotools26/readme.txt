This folder contains bindings for using SEXTANTE with GeoTools 2.6.0
It also contains a command-line version of SEXTANTE, based on GeoTools.

Here you can find information about using the standalone command-line version of SEXTANTE.

***INSTALLATION***
--------------------

    * put the sextante_cmd.jar file in a folder (for instance, c:\sextante_cmd)
    * put the core SEXTANTE files in that same folder
    * Download GeoTools 2.6.0
    * Unzip the content of the GeoTools file into the previous folder. It will create a new folder named geotools-2.6.0.

You should have something like the following:

   <your folder>:.
   |   bsh-2.0b4.jar
   |   jcommon-1.0.14.jar
   |   jep-2.4.0.jar
   |   jfreechart-1.0.11.jar
   |   jgraph.jar
   |   jts-1.9.jar
   |   kxml2.jar
   |   libDocEngines-0.15.jar
   |   libMath-0.5.jar
   |   sextante-0.5.jar
   |   sextante_cmd.jar
   |   sextante_gridAnalysis-0.5.jar
   |   .
   |   .
   |   .
   |   sextante_vectorTools-0.5.jar
   |   sextante_vegetationIndices-0.5.jar
   |   TableLayout?-bin-jdk1.5-2007-04-21.jar
   |
   +---geotools-2.5.0
        ant-optional-1.5.1.jar
        batik-awt-util-1.6.jar
        batik-bridge-1.6.jar
        .
        .
        .
        xml-apis-1.0.b2.jar
        xml-apis-xerces-2.7.1.jar
        xpp3-1.1.3.4.O.jar
        xsd-2.2.2.jar


***USAGE***
-------------------

To run a script, put the script in a text file (i.e myScript.txt). The syntax is the same as the one used in the built-in SEXTANTE command-line, but instead of using the name of a layer to refer to it, the filepath to the file containing that layer is needed. Suppported formats for both input and output files are TIFF(.tif) and ARC INFO ASCII (.asc) for raster layers, and shapefiles (.shp) for vector layers.

Check the SEXTANTE documentation for more information about running algorithms using SEXTANTE commands.

Once you have the script file, execute the sextante_cmd.jar file passing the path to the script file as argument:

>java -jar sextante_cmd.jar myScript.txt

A note for Windows users: backslashes in the script file must be escaped. A sentence like the following one

runalg("slope", "c:\input.tif", "#", "#", "c:\slope.tif");

is not correct. It should be replaced by the following one.

runalg("slope", "c:\\input.tif", "#", "#", "c:\\slope.tif");