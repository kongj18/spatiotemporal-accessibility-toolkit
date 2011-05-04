package net.sta.toolkit.testexample;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JToolBar;

import net.sta.toolkit.potentialpatharea.PotentialPathAreaAnalysis;
import net.sta.toolkit.routing.BuildGraphNetwork;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.collection.SubFeatureList;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.filter.SortBy;
import org.geotools.filter.SortByImpl;
import org.geotools.filter.SortOrder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultProjectedCRS;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.TypeName;
import org.geotools.validation.ValidationResults;
import org.geotools.validation.network.OrphanNodeValidation;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;

import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;


public class Main {

	private static FeatureSource inputFc;
	 private static StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
	    private static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
	    public static FeatureCollection<SimpleFeatureType, SimpleFeature> nodeCollection = FeatureCollections.newCollection();
	    static FeatureCollection<SimpleFeatureType, SimpleFeature> resultFC = FeatureCollections.newCollection();
	    private static Filter filter;
	    public static long startTime;
	    /*
	     * Convenient constants for the type of feature geometry in the shapefile
	     */
	    private enum GeomType { POINT, LINE, POLYGON };

	    /*
	     * Some default style variables
	     */
	    private static final Color LINE_COLOUR = Color.BLACK;
	    private static final Color FILL_COLOUR = Color.BLUE;
	    private static final Color SELECTED_COLOUR = Color.YELLOW;
	    private static final float OPACITY = 0.7f;
	    private static final float LINE_WIDTH = 2.0f;
	    private static final float POINT_SIZE = 10.0f;

	    private static JMapFrame mapFrame;
	    
	    
	    private static String geometryAttributeName;
	    private static GeomType geometryType;

	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, ParseException, SchemaException, CQLException, GeoAlgorithmExecutionException {
		
		        
        FeatureCollection networkFC = getGeneralisedNetworkData();
        
        GeometryFactory gf = new GeometryFactory();
		Point originPoint=gf.createPoint(new 
				Coordinate(681994,247610)); 
		
		ArrayList<Double> timeDiscretizations = new ArrayList<Double>();
		timeDiscretizations.add(10.0);
		
		
		//build the network using the point of origin and the network dataset
		BuildGraphNetwork routeCalculator = new BuildGraphNetwork();
		routeCalculator.BuildNetwork(networkFC, originPoint, "SpeedLimit");
		//build to source and put into the calulcationTODOTODOTOD
		nodeCollection = routeCalculator.getNodeFeatureCollection();
        FeatureSource nodeSource = DataUtilities.source(nodeCollection);
        writetoShape(nodeCollection.getSchema().getTypeName(),nodeCollection, nodeCollection.getSchema().getCoordinateReferenceSystem()); 
       
        PotentialPathAreaAnalysis ppa = new PotentialPathAreaAnalysis();
        		
        resultFC = ppa.DoProcessing(nodeSource, timeDiscretizations,
				PotentialPathAreaAnalysis.RangeorDiscrete.RANGED, PotentialPathAreaAnalysis.ContourorRegion.REGIONS,
				PotentialPathAreaAnalysis.RingorDisk.RING,
				nodeSource.getSchema().getCoordinateReferenceSystem(), 60);
		
        writetoShape(resultFC.getSchema().getTypeName(),resultFC, nodeSource.getSchema().getCoordinateReferenceSystem()); 
        
		
		
        
		
		MapContext map = new DefaultMapContext();
        map.setTitle("Feature selection tool example");
        
        Style styleNetwork = createRoadStyle(networkFC);
        map.addLayer( networkFC, styleNetwork);
        
        Style style = createAreaStyle(resultFC);
        //map.addLayer(collection, style);
        map.addLayer( resultFC, style);
        //map.addLayer(destinationSource, style);
        mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);

        
        JToolBar toolBar = mapFrame.getToolBar();
        JButton btn = new JButton("Select");
        toolBar.addSeparator();
        toolBar.add(btn);

        /**
         * Finally, we display the map frame. When it is closed
         * this application will exit.
         */
        mapFrame.setSize(600, 600);
        mapFrame.setVisible(true);
    }
		
private static Style createRoadStyle(FeatureCollection fc) {
    setGeometry(fc);
	Rule rule = createRule(Color.GRAY, FILL_COLOUR);

    FeatureTypeStyle fts = sf.createFeatureTypeStyle();
    fts.rules().add(rule);

    Style style = sf.createStyle();
    style.featureTypeStyles().add(fts);
    return style;
}

private static Style createAreaStyle(FeatureCollection fc) {
    setGeometry(fc);
	Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

    FeatureTypeStyle fts = sf.createFeatureTypeStyle();
    fts.rules().add(rule);

    Style style = sf.createStyle();
    style.featureTypeStyles().add(fts);
    return style;
}
	

private static Rule createRule(Color outlineColor, Color fillColor) {
    Symbolizer symbolizer = null;
    Fill fill = null;
    Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(LINE_WIDTH));

    switch (geometryType) {
        case POLYGON:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));
            symbolizer = sf.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
            break;

        case LINE:
            symbolizer = sf.createLineSymbolizer(stroke, geometryAttributeName);
            break;

        case POINT:
            fill = sf.createFill(ff.literal(fillColor), ff.literal(OPACITY));

            Mark mark = sf.getCircleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);

            Graphic graphic = sf.createDefaultGraphic();
            graphic.graphicalSymbols().clear();
            graphic.graphicalSymbols().add(mark);
            graphic.setSize(ff.literal(POINT_SIZE));

            symbolizer = sf.createPointSymbolizer(graphic, geometryAttributeName);
    }

    Rule rule = sf.createRule();
    rule.symbolizers().add(symbolizer);
    return rule;
}
//docs end create rule

//docs start set geometry
/**
 * Retrieve information about the feature geometry
 */
private static void setGeometry(FeatureCollection fc) {
	GeometryDescriptor geomDesc = fc.getSchema().getGeometryDescriptor();
    geometryAttributeName = geomDesc.getLocalName();

    Class<?> clazz = geomDesc.getType().getBinding();

    if (Polygon.class.isAssignableFrom(clazz) ||
            MultiPolygon.class.isAssignableFrom(clazz)) {
        geometryType = GeomType.POLYGON;

    } else if (LineString.class.isAssignableFrom(clazz) ||
            MultiLineString.class.isAssignableFrom(clazz)) {

        geometryType = GeomType.LINE;

    } else {
        geometryType = GeomType.POINT;
    }

}

private static void writetoShape(String tp,FeatureCollection<SimpleFeatureType, SimpleFeature> sfc, CoordinateReferenceSystem crs) throws SchemaException, IOException{
	File newFile = getNewShapeFile();

	    
	
	FeatureSource<SimpleFeatureType, SimpleFeature> outputFS = DataUtilities.source(sfc);
	/*Filter filter = Filter.INCLUDE;
	DefaultQuery   query = new DefaultQuery( tp, filter); 
	  org.opengis.filter.sort.SortBy[] sortby = new SortByImpl[1];
	  System.out.println(sfc.getSchema());
	  sortby[0] = ff.sort("time", SortOrder.ASCENDING); 
	  query.setSortBy(sortby);
	  FeatureCollection<SimpleFeatureType, SimpleFeature> newFC = outputFS.getFeatures(query);*/
	
	
	
    ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

    Map<String, Serializable> params = new HashMap<String, Serializable>();
    params.put("url", newFile.toURI().toURL());
    params.put("create spatial index", Boolean.TRUE);

    ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
    newDataStore.forceSchemaCRS(crs);
    final SimpleFeatureType TYPE = outputFS.getSchema();
    newDataStore.createSchema(TYPE);

    /*
     * You can comment out this line if you are using the createFeatureType
     * method (at end of class file) rather than DataUtilities.createType
     */
    
    
    Transaction transaction = new DefaultTransaction("create");

    String typeName = newDataStore.getTypeNames()[0];
    FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = newDataStore.getFeatureSource(typeName);
    
    if (featureSource instanceof FeatureStore) {
    	FeatureStore featureStore = (FeatureStore) featureSource;

        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(sfc);
            transaction.commit();

        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();

        } finally {
            transaction.close();
        }
        //System.exit(0); // success!
    } else {
        System.out.println(typeName + " does not support read/write access");
        //System.exit(1);
    }
}

private static File getNewShapeFile() {
    String path = "C:/";
    String newPath = "Output" + ".shp";

    JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
    chooser.setDialogTitle("Save shapefile");
    chooser.setSelectedFile(new File(newPath));

    int returnVal = chooser.showSaveDialog(null);

    if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
        // the user cancelled the dialog
        System.exit(0);
    }

    File newFile = chooser.getSelectedFile();
    

    return newFile;
}
	
private static FeatureCollection getNetworkData() throws IOException{
	File networkFile = new File("C:/Users/pcrease/Documents/ArcFiles/GISdata_Source/Roads_25_ZH.shp");
	FileDataStore networkStore = FileDataStoreFinder.getDataStore(networkFile);
	FeatureSource networkSource = networkStore.getFeatureSource();
    FeatureCollection networkFC = networkSource.getFeatures();
	return networkFC;
}

private static FeatureCollection getGeneralisedNetworkData() throws IOException{
	File networkFile = new File("C:/Users/pcrease/Documents/ArcFiles/GISdata_Source/AllRoads_VEC200.shp");
	FileDataStore networkStore = FileDataStoreFinder.getDataStore(networkFile);
	FeatureSource networkSource = networkStore.getFeatureSource();
    FeatureCollection networkFC = networkSource.getFeatures();
	return networkFC;
}



}
