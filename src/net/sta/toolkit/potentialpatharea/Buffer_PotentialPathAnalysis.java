package net.sta.toolkit.potentialpatharea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;



public class Buffer_PotentialPathAnalysis {

public enum VectorOrRaster {VECTOR, RASTER};
private VectorOrRaster vR;
private int imageWidth=256;
private int imageHeight=256;
private static CoordinateReferenceSystem crs;
private static Double rangeStep;
private static ArrayList<Double> possibleTimeDiscretizations = new ArrayList<Double>();
private static ArrayList<Double> timeDiscretizations = new ArrayList<Double>();
private static FeatureSource graphNodes;
private Double speedKM;
private GeometryFactory gf=new GeometryFactory();

public void doProcessing(FeatureSource fs, VectorOrRaster vOrR, CoordinateReferenceSystem cRS,ArrayList<Double> timeDiscretizations,Double speedKM) throws IOException, CQLException{
	this.graphNodes=fs;
	this.crs = cRS;
	this.vR = vOrR;
	this.timeDiscretizations = timeDiscretizations;
	this.speedKM=speedKM;
	
	//remove nodes outside the largest time discretization
	FeatureCollection graphFC=fs.getFeatures();	
	
	
	Iterator<Double> listIT= timeDiscretizations.iterator();
	while (listIT.hasNext()){		
		Double timeValue=listIT.next();
		Filter filter=CQL.toFilter("time<"+timeValue);
		FeatureCollection filteredFC=graphFC.subCollection(filter);
		Geometry convexHulls=getCombinedBuffered(timeValue, graphFC);		
	}
	
	System.out.println("beginning procesing...." );
	
}

private FeatureCollection getVectorOutput(){
	FeatureCollection outputFC=FeatureCollections.newCollection();
	
	return outputFC;
}


private Double getHighestValue(ArrayList<Double> list){
	Double maxValue=0.0;
	Iterator<Double> listIt=list.iterator();
	
	while(listIt.hasNext()){
		Double value=listIt.next();
		if(value>maxValue){
			maxValue=value;
		}
	}
	
	return maxValue;
}

private double getDistanceInMetresFromSpeed(Double timeToNode){
	double distance=timeToNode/(speedKM*1000);
	return distance;
}

private Geometry getCombinedBuffered(Double timeValue, FeatureCollection inputFC){
	Geometry outputGeom = null;
	Geometry[] arrayGeom=new Geometry[inputFC.size()];
	FeatureIterator featIT=inputFC.features();
	int i = 0;
	
	while(featIT.hasNext()){
		SimpleFeature feature=(SimpleFeature) featIT.next();
		Double timeToNode=(Double) feature.getAttribute("time");
		Point point=(Point) feature.getDefaultGeometry();
		Double distanceToBuffer=getDistanceInMetresFromSpeed(timeToNode);
		arrayGeom[i]=point.buffer(distanceToBuffer);
		i++;
	}
	
	
	
	
	return outputGeom;
}

}