package net.sta.toolkit.potentialpatharea;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.ListIterator;




import net.sta.toolkit.sextante.polygonization.PolygonizeAlgorithm;
import net.sta.toolkit.testexample.Main;



import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;

import org.geotools.delaunay.DelaunayDataStore;
import org.geotools.delaunay.DelaunayDataStoreRAM;
import org.geotools.delaunay.IncrementalDT;

import org.geotools.delaunay.contourlines.LinearContourLines;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.visitor.CalcResult;
import org.geotools.feature.visitor.UniqueVisitor;

import org.geotools.filter.Expression;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.geometry.jts.WKTReader2;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.geotools.GTOutputFactory;
import es.unex.sextante.geotools.GTVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.vectorTools.delaunay.Triangulation;
import es.unex.sextante.vectorTools.delaunay.Triangulation.Triangle;


public class Triangulation_PotentialPathAreaAnalysis {

	public enum RangeorDiscrete {RANGED, DISCRETE};
	public enum ContourorRegion {CONTOURS, REGIONS};
	public enum RingorDisk {RING, DISK, BOUNDINGBOX};
	
	private static RangeorDiscrete RorD;
	private ContourorRegion CorR;
	private RingorDisk ringorDisk;
	
	private static FeatureSource graphNodes;
	
	private enum GeomType { POINT, LINE, POLYGON };
	private static String geometryAttributeName;
	private Coordinate[] m_Coords = null;
	
	private static Triangulation_PotentialPathAreaAnalysis.RingorDisk RingDisk;
	private static CoordinateReferenceSystem crs ;
	private static Double rangeStep;
	private static ArrayList<Double> possibleTimeDiscretizations = new ArrayList<Double>();
	private static ArrayList<Double> timeDiscretizations = new ArrayList<Double>();
	
public FeatureCollection DoProcessing(FeatureSource fs,ArrayList<Double> timeDiscretizations,
		RangeorDiscrete RorD, ContourorRegion CorR, 
		RingorDisk ringorDisk, CoordinateReferenceSystem cRS, 
		int rangeLimit ) throws ParseException, SchemaException, IOException, 
		CQLException, GeoAlgorithmExecutionException{
	
	double startTime = System.currentTimeMillis();
		this.graphNodes=fs;
		this.crs = cRS;
		this.RorD = RorD;
		this.CorR = CorR;
		this.ringorDisk = ringorDisk;
		this.timeDiscretizations = timeDiscretizations;
		
		FeatureCollection graphFC = graphNodes.getFeatures();
		RingDisk  = ringorDisk;
		DelaunayDataStore triangles = new DelaunayDataStoreRAM();
		IncrementalDT triangulation=new IncrementalDT(triangles);
		
		System.out.println("beginning triangulation...." );
		FeatureIterator<SimpleFeature> iterator = graphFC.features();
		int m = 0;
		
		while( iterator.hasNext() ){
			SimpleFeature feature = iterator.next();
			Point p = (Point) feature.getDefaultGeometry();
			String strTime = feature.getAttribute("time").toString();
			Double time = Double.parseDouble(strTime);
			
				if (RorD == RangeorDiscrete.DISCRETE&&timeDiscretizations.size()==1){
					
					if (time<timeDiscretizations.get(timeDiscretizations.size()-1)+10.0
						&&time>timeDiscretizations.get(timeDiscretizations.size()-1)-10.0)
					{
					Coordinate coordinate = new Coordinate(p.getX(), p.getY(), time);
					triangulation.insertPoint(coordinate);				
					}
				}
				else if (RorD == RangeorDiscrete.DISCRETE&&timeDiscretizations.size()>1){
						
						if (time<timeDiscretizations.get(timeDiscretizations.size()-1)+20.0)
						{
						Coordinate coordinate = new Coordinate(p.getX(), p.getY(), time);
						triangulation.insertPoint(coordinate);				
						}
									
				}
				else if (RorD == RangeorDiscrete.RANGED){
					
					if (time<=rangeLimit+10.0)
					{
					Coordinate coordinate = new Coordinate(p.getX(), p.getY(), time);
					triangulation.insertPoint(coordinate);				
					}
				}
					
			
		}
		iterator.close();
		triangles.closeInserting();
		//triangles.createShapefile("C:/Users/pcrease/Documents/ArcFiles", "triangles.shp", "21781");
		//System.out.println("number of triangles created = "+triangulation.number_Triangles );
		
		
		FeatureCollection fc1 = FeatureCollections.newCollection();
		
			if(RorD==RangeorDiscrete.RANGED){
				System.out.println("creating contour range..." );
				this.rangeStep=timeDiscretizations.get(0);
				fc1 = LinearContourLines.getRangedIzoLines(triangles, timeDiscretizations, "ESPG:21781", rangeLimit);
				System.out.println("created contour range after " +((System.currentTimeMillis()-startTime)/1000)+" seconds");
			}
			else{
				System.out.println("creating discrete contours..." );
			fc1 = LinearContourLines.getDiscreteIzoLines(triangles, timeDiscretizations, "ESPG:21781");
			System.out.println(fc1.size() + " contour lines created");
			System.out.println("created discrete contours after " +((System.currentTimeMillis()-startTime)/1000)+" seconds");
			}
		
		System.out.println("combining contour lines..." );
		FeatureCollection combinedFC = CombineIsoLineGeometry(fc1, "time");
		System.out.println("combined contour lines after " +((System.currentTimeMillis()-startTime)/1000)+" seconds");
		
			if (CorR== ContourorRegion.CONTOURS&&ringorDisk==RingorDisk.BOUNDINGBOX){
				FeatureCollection bboxFC=CreateBoundingBox(combinedFC);
				System.out.println("finished accessibility analysis after "+((System.currentTimeMillis()-startTime)/1000)+" seconds");
			return bboxFC;
			}
			if (CorR== ContourorRegion.CONTOURS&&ringorDisk!=RingorDisk.BOUNDINGBOX){
			System.out.println("finished accessibility analysis after "+((System.currentTimeMillis()-startTime)/1000)+" seconds");
			return combinedFC;
			}
		
			
		triangulation.closeDataStore();
		FeatureIterator<SimpleFeature> polyFeatureIt = combinedFC.features();
		
		FeatureCollection outputFC = FeatureCollections.newCollection();
		if(ringorDisk == RingorDisk.DISK){
			System.out.println("polygonizing contours into disks..." );
		while (polyFeatureIt.hasNext()){//polygonizes unlinked lines
			SimpleFeature sf = polyFeatureIt.next();
			SimpleFeature polygonizedFeature = PolygonizeFeature((Double)sf.getAttribute("time"),sf);;
				if(polygonizedFeature!=null){
				outputFC.add(polygonizedFeature);
				}
				else{
					System.out.println("empty");
				}
		}
		}
		else{
			System.out.println("polygonizing contours into rings..." );
			outputFC = PolygonizeRings(combinedFC);
			System.out.println("created ringed regions after " +((System.currentTimeMillis()-startTime)/1000)+" seconds");
		}
		polyFeatureIt.close();
		System.out.println("finished accessibility analysis after "+((System.currentTimeMillis()-startTime)/1000)+" seconds");
		
		return outputFC;
		
	}
	
	
private FeatureCollection CombineIsoLineGeometry(FeatureCollection fc, String attributeName) throws IOException, CQLException, SchemaException{
	FeatureCollection combinedFC = FeatureCollections.newCollection();
	
	FeatureSource isolineSource = DataUtilities.source(fc);
	FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
	Expression expression = (Expression) filterFactory.property(attributeName);
	
	UniqueVisitor visitor = new UniqueVisitor(filterFactory.property(attributeName));
	fc.accepts(visitor, null);
	CalcResult result = visitor.getResult();
	List list = result.toList();
	Iterator listIterator = list.iterator();
	
	while( listIterator.hasNext() ){
		Double uniquevalue = (Double) listIterator.next();
		Filter filter = CQL.toFilter(attributeName+"="+uniquevalue);
		FeatureCollection uniqueFeatures = isolineSource.getFeatures( filter );
		SimpleFeature combinedFeature = CombineGeometry(uniqueFeatures); 
		
		combinedFC.add(combinedFeature);
		
	}
	
	
	return combinedFC;
	}


private SimpleFeature CombineGeometry(FeatureCollection features) throws SchemaException{
	Geometry[] geomArray = new Geometry[features.size()];
	Double time = 0.0;
FeatureIterator<SimpleFeature> iterator = features.features();
int i = 0;

	
	     while( iterator.hasNext() ){
	    	 SimpleFeature feature = iterator.next();
	    	 time = (Double)feature.getAttribute("time");
	    	 Geometry geom = (Geometry) feature.getDefaultGeometry();
	   	  geomArray[i] = geom;
	   	  i = i+1;
	     }
	     
	
	
	Geometry all = null;
	  for(int j=0;j<geomArray.length;j++){
		  Geometry geomtoAddtemp = geomArray[j];
		  
		  if( all == null ){
    			all = geomtoAddtemp;
    		}
    		else {
    			all = all.union( geomtoAddtemp );
    		}
		
		  
	  
	  }
	  
	  SimpleFeature resultFeature = BuildNewFeature(time, all);
	  iterator.close();
	return resultFeature;
	
}


private static SimpleFeature BuildNewFeature(Double time,  Geometry LineStrings) throws SchemaException{
	
	final SimpleFeatureType TYPE = DataUtilities.createType("Location",
            "geom:MultiLineString:srid=21781," + // <- the geometry attribute: Polyline type
                    
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


private static SimpleFeature BuildNewPolygonisedRingFeature(Double toBreak,Double fromBreak,  Geometry LineStrings) throws SchemaException{
	
	final SimpleFeatureType TYPE = DataUtilities.createType("Location",
            "geom:MultiPolygon:srid=21781," + // <- the geometry attribute: Polyline type
            "toBreak:Double,"+
            "fromBreak:Double"// <- a String attribute
                    ); 
	
	//populates the new feature object, builds it and returns it
	SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
	
	featureBuilder.add(LineStrings);
	featureBuilder.add(toBreak);
	featureBuilder.add(fromBreak);
	
	//Cost is calculates here using the cost (Distance) of the route plus the straight line distances between the closest graph nodes
	//and the origin and destination points
	
	
    SimpleFeature feature = featureBuilder.buildFeature(null);
	return feature;
	
}

private static SimpleFeature BuildNewPolygonisedFeature(Double time,  Geometry LineStrings) throws SchemaException{
	////////////////////////////////////////////////////CHANGE THIS TO REMOVE 21781
	final SimpleFeatureType TYPE = DataUtilities.createType("Location",
            "geom:MultiPolygon:srid=21781," + // <- the geometry attribute: Polyline type
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


private static SimpleFeature PolygonizeFeature(Double Elevation, SimpleFeature feat) throws IOException, GeoAlgorithmExecutionException, SchemaException{
	
		
	FeatureCollection newFC = FeatureCollections.newCollection();
	newFC.add(feat);
	FeatureSource fs = DataUtilities.source(newFC);
	
	Query query1 = Query.ALL;
	
	GTVectorLayer layer = new GTVectorLayer();
	
	layer.create(fs, query1);
	layer.setName(fs.getName().getLocalPart());
	
	PolygonizeAlgorithm polygonizer = new PolygonizeAlgorithm();
	
	ParametersSet clusterParams = polygonizer.getParameters();
	clusterParams.getParameter(PolygonizeAlgorithm.LAYER).setParameterValue(layer);
	
	
	OutputObjectsSet outputs = polygonizer.getOutputObjects();
	Output out = outputs.getOutput(PolygonizeAlgorithm.RESULT);
	//out.setOutputChannel(new FileOutputChannel("C:/accessibleareas.shp"));
	
	OutputFactory outputFactory = new GTOutputFactory();
	
	polygonizer.execute(null, outputFactory);

	/*
	* Now the result can be taken from the output container
	*/
	IVectorLayer result = (IVectorLayer) out.getOutputObject();
	FeatureStore resultFeatureStore = (FeatureStore) result.getBaseDataObject();
	FeatureCollection inputFC = resultFeatureStore.getFeatures();
	SimpleFeature newPolygonFeature = null;
	FeatureIterator<SimpleFeature> iterator = inputFC.features();
	
	Geometry outputPolygon = null;
	 
	//need to now add the elevation data back to the geometry
	int i = 0;
	Polygon polygon = null ;
	
	
	
		while( iterator.hasNext() ){
		       	 SimpleFeature feature = iterator.next();
		    	 Geometry geom = (Geometry) feature.getDefaultGeometry();
		   	     MultiPolygon inputPolygon = (MultiPolygon) geom;
		   	     GeometryFactory gf = new GeometryFactory();
		   	     
		   	    
		   	    	
		   	    	 for (int t = 0; t < inputPolygon.getNumGeometries(); t++) {
		   		          Polygon p;
		   		          p = (Polygon) inputPolygon.getGeometryN(t);
		   		          
		   		         
		   		         LineString polyring = p.getExteriorRing();
		   		      
		   		         CoordinateList list = new CoordinateList( polyring.getCoordinates() );
		   		         list.closeRing();
		   		         LinearRing ring = gf.createLinearRing( list.toCoordinateArray() );
		   		         polygon = gf.createPolygon( ring, null );
		   		         
		   		          
		   		
					   		if (outputPolygon == null){
					   			outputPolygon = polygon;
					   		}
					   		else{
					   			outputPolygon = outputPolygon.union(polygon);
					   		}   			   		
		   		       }
		   	     
		   	     
		   	     
		   	     
		     }
		iterator.close();
	 
		
	 if (outputPolygon != null){
		newPolygonFeature = BuildNewPolygonisedFeature((Double)feat.getAttribute("time"),outputPolygon);	
	 }
	 else{
		 System.out.println("null");
	 }
	return newPolygonFeature;
	
	
	
}


private static FeatureCollection PolygonizeRings(FeatureCollection fc) throws IOException, GeoAlgorithmExecutionException, SchemaException{
	
	
	FeatureSource fs = DataUtilities.source(fc);
	
	Query query1 = Query.ALL;
	
	GTVectorLayer layer = new GTVectorLayer();
	
	layer.create(fs, query1);
	layer.setName(fs.getName().getLocalPart());
	
	PolygonizeAlgorithm polygonizer = new PolygonizeAlgorithm();
	
	ParametersSet clusterParams = polygonizer.getParameters();
	clusterParams.getParameter(PolygonizeAlgorithm.LAYER).setParameterValue(layer);
	
	
	OutputObjectsSet outputs = polygonizer.getOutputObjects();
	Output out = outputs.getOutput(PolygonizeAlgorithm.RESULT);
	
	//out.setOutputChannel(new FileOutputChannel("C:/accessibleareas.shp"));
	
	OutputFactory outputFactory = new GTOutputFactory();
	
	polygonizer.execute(null, outputFactory);

	/*
	* Now the result can be taken from the output container
	*/
	IVectorLayer result = (IVectorLayer) out.getOutputObject();
	FeatureStore resultFeatureStore = (FeatureStore) result.getBaseDataObject();
	FeatureCollection inputFC = resultFeatureStore.getFeatures();
	SimpleFeature newPolygonFeature = null;
	FeatureIterator<SimpleFeature> iterator = inputFC.features();
	FeatureCollection outputFC = FeatureCollections.newCollection();
	Double i = 0.0;
	if (RorD == RangeorDiscrete.RANGED ){
	for (int t = 0; t < inputFC.size(); t++) {
		i = i + rangeStep;
		possibleTimeDiscretizations.add(i);
	}}
	
	
	SimpleFeature tempFeature;
		while( iterator.hasNext() ){
		       	 SimpleFeature feature = iterator.next();
		       	tempFeature = CreateAttributesforRings(feature);
		       	if (tempFeature != null){
		       	outputFC.add(tempFeature);
		       	}
		     }
		iterator.close();
	 
		
	return outputFC;
	
	
	
}


private static SimpleFeature CreateAttributesforRings(SimpleFeature unattributedFeature) throws IOException, SchemaException{
	
	Geometry geom = (Geometry) unattributedFeature.getDefaultGeometry();
	ReferencedEnvelope bounds = new ReferencedEnvelope( unattributedFeature.getBounds().getMinX(),
			unattributedFeature.getBounds().getMaxX(),
			unattributedFeature.getBounds().getMinY(),
			unattributedFeature.getBounds().getMaxY(),
			crs);
	BoundingBox bb = unattributedFeature.getBounds();
	
	
	MultiPolygon inputPolygon = (MultiPolygon) geom;
	String geomName = graphNodes.getSchema().getGeometryDescriptor().getLocalName();
	FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2( GeoTools.getDefaultHints() );
	Filter boundsCheck = ff.bbox( ff.property(geomName), bounds );
	Filter polygonCheck = ff.not( ff.disjoint( ff.property(geomName), ff.literal( inputPolygon) ));
	Filter filter = ff.and( boundsCheck, polygonCheck );
	
	
	FeatureCollection attributedNodes = graphNodes.getFeatures( filter );
	if (attributedNodes.size()==0){
		return null;
	}
	
	FeatureIterator<SimpleFeature> tempIt = attributedNodes.features();
	SimpleFeature tempFeature = tempIt.next();
	
	ListIterator<Double> listIt;
	
	if (RorD == RangeorDiscrete.RANGED ){
	listIt = possibleTimeDiscretizations.listIterator();
	}
	else
	{
	listIt = timeDiscretizations.listIterator();
	}
	Double currenttemp=0.0, pasttemp=0.0, timeValue = 0.0,toValue=0.0, fromValue=0.0, contourValue=0.0;
	while (listIt.hasNext()){
		currenttemp = listIt.next();
		timeValue = (Double) tempFeature.getAttribute("time");
		
		if (timeValue<currenttemp&timeValue>pasttemp){
			contourValue = currenttemp;
		}
		pasttemp = currenttemp;
		
	}
	
	if (contourValue>0.0){ //else create rings for areas where the nodes are greater than range value
	SimpleFeature attributedFeature = BuildNewPolygonisedFeature(contourValue, inputPolygon) ;
	return attributedFeature;
	}
	
	return null;
	
	
}
	

private static FeatureCollection CreateBoundingBox(FeatureCollection contourData) throws SchemaException{
	FeatureCollection outputBboxes = FeatureCollections.newCollection();
	
	FeatureIterator<SimpleFeature> tempIt = contourData.features();
	
	while (tempIt.hasNext()){
		SimpleFeature tmpFeature = tempIt.next();
		Double timeValue = (Double) tmpFeature.getAttribute("time");
		BoundingBox bbox = tmpFeature.getBounds();
		SimpleFeature outputFeature = createGeometryfromBoundingBox(bbox, timeValue);
		outputBboxes.add(outputFeature);
		
	}
	System.out.println("created "+ outputBboxes.size()+" BoundingBoxes");
	return outputBboxes;
}

private static SimpleFeature createGeometryfromBoundingBox(BoundingBox bbox, Double time) throws SchemaException{
	
	
	final SimpleFeatureType TYPE = DataUtilities.createType("Location",
            "geom:Polygon:srid=21781," + // <- the geometry attribute: Polyline type
                    
            "time:Double"// <- a String attribute
                    ); 
	Geometry bBoxPolygon = JTS.toGeometry(bbox);
	//populates the new feature object, builds it and returns it
	SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(TYPE);
	featureBuilder.add(bBoxPolygon);
	featureBuilder.add(time);
	//Cost is calculates here using the cost (Distance) of the route plus the straight line distances between the closest graph nodes
	//and the origin and destination points
	
	
    SimpleFeature feature = featureBuilder.buildFeature(null);
	return feature;
}


}
