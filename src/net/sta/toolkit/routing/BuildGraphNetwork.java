package net.sta.toolkit.routing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.FactoryFinder;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.traverse.standard.DijkstraIterator.EdgeWeighter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicEdge;

public class BuildGraphNetwork {

	private FeatureSource<SimpleFeatureType, SimpleFeature> routeSource;
	private Graph networkGraph;
	private LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
	private  FeatureCollection<SimpleFeatureType,SimpleFeature> routeFeatures  = FeatureCollections.newCollection();
	private static  FeatureCollection<SimpleFeatureType,SimpleFeature> nodeFeatures  = FeatureCollections.newCollection();
	private DijkstraShortestPathFinder dspf;
	private ArrayList<Double> Costs = new ArrayList<Double>();
	private Double distanceOrigintoGraph = 0.0;
	
	//Compiles successfully when using geotools-2.7-RC2 - http://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/
	
	
	public void BuildNetwork(FeatureCollection networkFC,Point originPoint, final String speedFieldkmh) throws SchemaException{
		System.out.println("Building Network....");
		//get the object to generate the graph from line string objects
		LineStringGraphGenerator lineStringGen = new LineStringGraphGenerator();
		//wrap it in a feature graph generator
		FeatureGraphGenerator featureGen = new FeatureGraphGenerator( lineStringGen );
		//throw all the features into the graph generator
		FeatureIterator iter = networkFC.features();
		try {
		  while(iter.hasNext()){
		     Feature feature = iter.next();
		     featureGen.add( feature );
		  }
		} finally {
		  iter.close();
		}
		//build the graph
		networkGraph = featureGen.getGraph();
		
		//find the node of the graph closest to the origin point and returns the node
		Node source = FindNode.getNearestGraphNode(lineStringGen, networkGraph,originPoint); 
		//distance between the origin location and the nearest graph node
		distanceOrigintoGraph = FindNode.getDistanceFromGraphNode();
		
		//weight the edges of the graph using the distance of each linestring		
		EdgeWeighter weighter = new EdgeWeighter() {
			public double getWeight(org.geotools.graph.structure.Edge e) {
				SimpleFeature aLineString = (SimpleFeature) e.getObject(); 
				 Geometry geom = (Geometry) aLineString.getDefaultGeometry();
				 
				 Double speedLimit = (Double) aLineString.getAttribute(speedFieldkmh);
				 
				 Double stDistance = geom.getLength()/((speedLimit*1000)/60);
                 return stDistance;	}
					};
			
					//calculate the cost(distance) of each graph node to the node closest to the origin
					dspf = new DijkstraShortestPathFinder( networkGraph, source, weighter );
					dspf.calculate();
					nodeFeatures=ExportNodes(networkGraph, dspf);
					
		
	}
	

public static FeatureCollection ExportNodes(Graph g, DijkstraShortestPathFinder dspf) throws SchemaException{
	
	FeatureCollection networkFC = FeatureCollections.newCollection();
	
	for (Object o:g.getNodes()) {
    	Node n = (Node) o;
    	Point p = ((Point)n.getObject());
    	//
    	Double networkCost = dspf.getCost( n );
    	if (networkCost<10000){//prevents dangles being included, these produce very high cost values
    	SimpleFeature feature = BuildNewFeature(networkCost,p);
    	networkFC.add(feature);
    	}
    	
		
    }
	System.out.println("Exported " + networkFC.size()+" Graph Nodes....");
	return networkFC;
}
	
private static SimpleFeature BuildNewFeature(Double time, Geometry LineStrings) throws SchemaException{
	
	final SimpleFeatureType TYPE = DataUtilities.createType("Location",
            "geom:Point:srid=21781," + // <- the geometry attribute: Polyline type
                    "time:Double"// <- a String attribute
                    ); 
	
	//populates the new feature object, builds it and returns it
	SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
	featureBuilder.add(LineStrings);
	featureBuilder.add(time);
	//Cost is calculates here using the cost (Distance) of the route plus the straight line distances between the closest graph nodes
	//and the origin and destination points
	
	
    SimpleFeature feature = featureBuilder.buildFeature(null);
	return feature;
	
}

public static FeatureCollection getNodeFeatureCollection(){
	
	return nodeFeatures;
}

	
}
