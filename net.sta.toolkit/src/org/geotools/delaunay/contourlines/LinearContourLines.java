/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *    
 *    @author      Josef Bezdek
 *	  @version     %I%, %G%
 *    @since JDK1.3 
 */



package org.geotools.delaunay.contourlines;


import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.delaunay.IncrementalDT;
import org.geotools.delaunay.PointDT;
import org.geotools.delaunay.TriangleDT;
import org.geotools.delaunay.DelaunayDataStore;
import org.geotools.delaunay.contourlines.Izolines;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

import java.util.Iterator;

public class LinearContourLines {
	
	
	/************************************************************************
	 * Private function which generetes izolines from triangle with defined elevated Step
	 * @param T - triangleDT
	 * @param elevatedStep - int elevated step
	 * @return linked list of extract izolines
	 * @throws ParseException 
	 * @throws SchemaException 
	 */
	public static FeatureCollection getRangedIzoLines(DelaunayDataStore triangles, ArrayList<Double> elevatedDifference, String EPSG, int rangeLimit) throws ParseException, SchemaException{
		
	    	WKTReader wktReader = new WKTReader();
	    	
	    	FeatureCollection trianglesFC = FeatureCollections.newCollection();
	    	
	    	
 	    	for (int i =0; i<triangles.getNumberOfTriangles(); i++){
	    		TriangleDT T = triangles.getTriangle(i);
	    		Double tt=T.A.z;
	    		
 	     		if (T!=null){
 	     			LinkedList contours = rangedIzoLines(T, elevatedDifference.get(0).intValue(), rangeLimit);
 	     			Iterator iter = contours.iterator();
 	     			while (iter.hasNext()){
 	     				Izolines L = (Izolines)iter.next();
 	     				LineString geometry = (LineString) wktReader.read("LINESTRING ("+(L.A.x)+" "+(L.A.y)+"," +(L.B.x)+" "+(L.B.y)+")");
 	     				SimpleFeature feature = BuildNewFeature(L,geometry);
 	     				trianglesFC.add(feature);
 	     				
 	     			}
 	     		}
	    	}
	    	
			return trianglesFC;
	   		}
	    	
		    
		   
			
	
	public static LinkedList rangedIzoLines(TriangleDT T, int elevatedStep, int rangeLimit){
		Double minZ = new Double(0);
		Double maxZ = new Double(0);
		PointDT startIZO = null;
		PointDT stopIZO = null;
		LinkedList contours = new LinkedList();
		double elev = -1000;
				minZ = T.A.z;
				maxZ = T.A.z;
				if (minZ > T.B.z)
					minZ = T.B.z;
				if (minZ > T.C.z)
					minZ = T.C.z;
				if (maxZ < T.B.z)
					maxZ = T.B.z;
				if (maxZ < T.C.z)
					maxZ = T.C.z;
				elev = ((int)(minZ/elevatedStep+1))*elevatedStep;
				
				elev = elev+0.0001;
				
				while (elev <= maxZ){
					
						
					
					if (((T.A.z<=elev)&(T.B.z>=elev))||((T.A.z>=elev)&(T.B.z<=elev))){
						startIZO = solveLinearInterpolation(T.A, T.B, elev);
					}
					if (((T.A.z<=elev)&(T.C.z>=elev))||((T.A.z>=elev)&(T.C.z<=elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(T.A, T.C, elev);
						else
							stopIZO = solveLinearInterpolation(T.A, T.C, elev);
					}
					if (((T.B.z<=elev)&(T.C.z>=elev))||((T.B.z>=elev)&(T.C.z<=elev))){
						if (startIZO == null)
							startIZO = solveLinearInterpolation(T.B, T.C, elev);
						if (stopIZO == null)
							stopIZO = solveLinearInterpolation(T.B, T.C, elev);
					}
					
					contours.add(new Izolines(startIZO,stopIZO,(int)elev));	//}
					startIZO = null;
					stopIZO = null;
					elev = elev + elevatedStep;
					if(rangeLimit<elev){
						return contours;
					}
					
					}
				return contours;
			}
	

	/********************************************************************************
	 * Private function which computes  point on line, (Linear interpolation)
	 * @param A - start point of line
	 * @param B - end point of line
	 * @param elev - defined elevation
	 * @return - coordinate of point with definied elevation
	 */
	
	public static FeatureCollection getDiscreteIzoLines(DelaunayDataStore triangles, ArrayList<Double> elevationValues, String EPSG) throws ParseException, SchemaException{
		
    	WKTReader wktReader = new WKTReader();
    	
    	FeatureCollection trianglesFC = FeatureCollections.newCollection();
    	
    	
	    	for (int i =0; i<triangles.getNumberOfTriangles(); i++){
    		TriangleDT T = triangles.getTriangle(i);
    		
    		
	     		if (T!=null){
	     			LinkedList contours = discreteIzoLines(T, elevationValues);
	     			Iterator iter = contours.iterator();
	     			while (iter.hasNext()){
	     				Izolines L = (Izolines)iter.next();
	     				LineString geometry = (LineString) wktReader.read("LINESTRING ("+(L.A.x)+" "+(L.A.y)+"," +(L.B.x)+" "+(L.B.y)+")");
	     				SimpleFeature feature = BuildNewFeature(L,geometry);
	     				trianglesFC.add(feature);
	     				
	     			}
	     		}
    	}
    	
		return trianglesFC;
   		}
    	
	
	public static LinkedList discreteIzoLines(TriangleDT T, ArrayList<Double> elevationValues){
		Double minZ = new Double(0);
		Double maxZ = new Double(0);
		PointDT startIZO = null;
		PointDT stopIZO = null;
		LinkedList contours = new LinkedList();
		double elev;
				minZ = T.A.z;
				maxZ = T.A.z;
				if (minZ > T.B.z)
					minZ = T.B.z;
				if (minZ > T.C.z)
					minZ = T.C.z;
				if (maxZ < T.B.z)
					maxZ = T.B.z;
				if (maxZ < T.C.z)
					maxZ = T.C.z;
				
				ListIterator elevStep = elevationValues.listIterator();
				int i = 0;
				while (elevStep.hasNext()){
				
					elev = elevationValues.get(i);
					
						if (((T.A.z<=elev)&(T.B.z>=elev))||((T.A.z>=elev)&(T.B.z<=elev))){
							startIZO = solveLinearInterpolation(T.A, T.B, elev);
						}
						if (((T.A.z<=elev)&(T.C.z>=elev))||((T.A.z>=elev)&(T.C.z<=elev))){
							if (startIZO == null)
								startIZO = solveLinearInterpolation(T.A, T.C, elev);
							else
								stopIZO = solveLinearInterpolation(T.A, T.C, elev);
						}
						if (((T.B.z<=elev)&(T.C.z>=elev))||((T.B.z>=elev)&(T.C.z<=elev))){
							if (startIZO == null)
								startIZO = solveLinearInterpolation(T.B, T.C, elev);
							if (stopIZO == null)
								stopIZO = solveLinearInterpolation(T.B, T.C, elev);
						}
						
						if(stopIZO != null&startIZO != null){
						contours.add(new Izolines(startIZO,stopIZO,(int)elev));	}
						startIZO = null;
						stopIZO = null;
						elevStep.next();
					//	}
					i = i+1;
					
				}
				
				return contours;
			}
	

	/********************************************************************************
	 * Private function which computes  point on line, (Linear interpolation)
	 * @param A - start point of line
	 * @param B - end point of line
	 * @param elev - defined elevation
	 * @return - coordinate of point with definied elevation
	 */
	
	private static  PointDT solveLinearInterpolation(PointDT A, PointDT B, double elev){
		//double distance = Math.sqrt(Math.pow((A.x-B.x), 2)+Math.pow((A.y-B.y), 2));
		double koef;
		double rate;
		
		if (B.z>A.z){
			rate = (elev - A.z) / (B.z-A.z);
			return  new PointDT((A.x+(B.x-A.x)*rate),(A.y+(B.y-A.y)*rate),elev);
		}
		else{
			rate = (elev - B.z) / (A.z-B.z);
			return  new PointDT((B.x+(A.x-B.x)*rate),(B.y+(A.y-B.y)*rate),elev);
		}
	}
	
	private static SimpleFeature BuildNewFeature(Izolines L, LineString geometry) throws SchemaException{
		
		final SimpleFeatureType TYPE = DataUtilities.createType("Location",
	            "geom:MultiLineString:srid=21781," + // <- the geometry attribute: Polyline type
	                    "time:Double"// <- a String attribute
	                    ); 
		
		//populates the new feature object, builds it and returns it
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
		featureBuilder.add(geometry);
		featureBuilder.add(L.elevation);
		//Cost is calculates here using the cost (Distance) of the route plus the straight line distances between the closest graph nodes
		//and the origin and destination points
		
		
	    SimpleFeature feature = featureBuilder.buildFeature(null);
		return feature;
		
	}
	

	
	
	
}
