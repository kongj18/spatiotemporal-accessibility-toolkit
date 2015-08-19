Spatio-Temporal Accessibility ToolKit is a Java library that allows access to routing algorithms and the creation of potential path areas (also known as travel time isolines). This toolkit is built on top of the existing GeoTools and Sextante Libraries . Triangulation and contouring algorithms have come from the project found at http://bezdek2009dp.googlecode.com/svn.

This code allows you to input a Geotools FeatureClass representing a road network and build a graph from it. You can then create the potential path area from this graph which is returned in the form of either a contours FeatureClass (polylines) or as a regions  FeatureClass(polygons). An Example Main Java class is provided to demonstrate how the toolkit works. Click on the Wiki Tab above for more information and example screenshots.

<b>Figure below : Inverted space-time prism created using the spatiotemporal-accessibility-toolkit<b />

<img src='https://lh6.googleusercontent.com/-X25kmIYvGFk/Tl3e1oQiRvI/AAAAAAAAA7w/oS_cJqjamNQ/s576/spacetime-01.png' border='5' />