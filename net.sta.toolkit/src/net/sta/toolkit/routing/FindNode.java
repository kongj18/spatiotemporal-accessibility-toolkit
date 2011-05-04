package net.sta.toolkit.routing;

import java.util.Collection;
import java.util.Iterator;

import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.structure.basic.BasicDirectedNode;
import org.geotools.graph.structure.basic.BasicNode;
import org.geotools.referencing.GeodeticCalculator;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;


public class FindNode {
	private static Node nearestNode = null;
	private static Double distanceFromGraphNode = 0.0;
	
	//compiles successfully when using geotools-2.7-RC2 - http://sourceforge.net/projects/geotools/files/GeoTools%202.7%20Releases/
	
	//function to get the closest node to the location of interest (specified using a Point geometry
	public static Node getNearestGraphNode(LineStringGraphGenerator lsgg, Graph g,Point pointy){	
        //initiliase the distance
	    double dist = 999999999;
	    
	    //loops through the nodes of the graph and finds the node closest to the point of interest
	    for (Object o:g.getNodes()) {
	    	Node n = (Node) o;
	    	Point p = ((Point)n.getObject());
	    	double newdist = CalculateEuclideanDistance(pointy.getX(), pointy.getY(), p.getCoordinate().x, p.getCoordinate().y);
	    	
    		if (newdist < dist) {
    			dist=newdist;
    			nearestNode = n;
	    	}
	    }
	    
	    
	    //returns the node closest to the locaiton of interest
		Node source = (BasicNode) lsgg.getNode(new Coordinate(((Point)nearestNode.getObject()).getCoordinate().x,
				((Point)nearestNode.getObject()).getCoordinate().y));
		
		//stores the distance between node and location of interest, can be accessed by calling the getDistanceFromGraphNode() function
		distanceFromGraphNode = dist;
		return nearestNode;
		
        
} 
	
	public static Double getDistanceFromGraphNode(){
		//returns the distance between node and location of interest in Metres(depends on the projection system of course!!)
		return distanceFromGraphNode;
	}
	
	private static double CalculateEuclideanDistance(double xOrig, double yOrig, double xDest, double yDest){
		//calculates traight lne distance
		double distance = Math.sqrt((xDest-xOrig)*(xDest-xOrig) + (yDest-yOrig)*(yDest-yOrig));      
		return distance;
		
	}

}

