package net.sta.toolkit.potentialpatharea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;



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

private double getDistanceInMetresFromSpeed(){
	double distance=0.0;
	return distance;
}

private Geometry getCombinedBuffered(Double timeValue, FeatureCollection inputFC){
	Geometry outputFC = null;
	return outputFC;
}

}