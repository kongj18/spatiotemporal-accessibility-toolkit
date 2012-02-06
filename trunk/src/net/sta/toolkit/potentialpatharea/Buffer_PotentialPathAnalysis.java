package net.sta.toolkit.potentialpatharea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
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
private FeatureCollection outputVectorFC=FeatureCollections.newCollection();

public void doProcessing(FeatureSource fs, VectorOrRaster vOrR, CoordinateReferenceSystem cRS,ArrayList<Double> timeDiscretizations,Double speedKM) throws IOException, CQLException, SchemaException{
	this.graphNodes=fs;
	this.crs = cRS;
	this.vR = vOrR;
	this.timeDiscretizations = timeDiscretizations;
	this.speedKM=speedKM;
	
	//remove nodes outside the largest time discretization
	FeatureCollection graphFC=fs.getFeatures();	
	//got to build the geomteries
	
	if(vR==VectorOrRaster.VECTOR){
	Iterator<Double> listIT= timeDiscretizations.iterator();
	while (listIT.hasNext()){		
		Double timeValue=listIT.next();
		Filter filter=CQL.toFilter("time<"+timeValue);
		FeatureCollection filteredFC=graphFC.subCollection(filter);
		Geometry convexHulls=getCombinedBuffered(timeValue, graphFC);	
		SimpleFeature outputFeature =BuildNewPolygonisedFeature(timeValue,convexHulls);
		outputVectorFC.add(outputFeature);
	}
	}
	
	if(vR==VectorOrRaster.RASTER){
		
	}
	
	System.out.println("beginning procesing...." );
	
}

private FeatureCollection getVectorOutput(){
	return 	outputVectorFC;
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
	
	GeometryCollection geometryCollection =  gf.createGeometryCollection(arrayGeom);
	outputGeom=geometryCollection.union();	
	
	return outputGeom;
}

private static SimpleFeature BuildNewPolygonisedFeature(Double time,  Geometry polyGeom) throws SchemaException{
	
	final SimpleFeatureType TYPE = DataUtilities.createType("Location",
            "geom:MultiPolygon:srid=21781," + // <- the geometry attribute: Polyline type
            "time:Double"// <- a String attribute
                    ); 
	
	SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
	
	featureBuilder.add(polyGeom);
	featureBuilder.add(time);
	
    SimpleFeature feature = featureBuilder.buildFeature(null);
	return feature;
	
}

public void setImageWidth(int imageWidth) {
	this.imageWidth = imageWidth;
}

public int getImageWidth() {
	return imageWidth;
}

public void setImageHeight(int imageHeight) {
	this.imageHeight = imageHeight;
}

public int getImageHeight() {
	return imageHeight;
}

}