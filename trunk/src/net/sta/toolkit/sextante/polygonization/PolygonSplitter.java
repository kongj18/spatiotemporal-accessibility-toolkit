package net.sta.toolkit.sextante.polygonization;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.operation.union.UnaryUnionOp;


public class PolygonSplitter
{
  GeometryFactory geomFact = null;

  static class LineStringExtractor extends ArrayList<LineString> 
  implements GeometryComponentFilter
  {
    GeometryFactory lineStringFactory = null;
    
    /** Construct instance that will extract LineStrings only 
     */
    public LineStringExtractor()
    {
    }
    /** Construct instance that will extract LineStrings and LinearRings
     *  and refactor LinerRings to LineString 
     */
    public LineStringExtractor( GeometryFactory lineStringFactory )
    {
      this.lineStringFactory = lineStringFactory;
    }
    /** Implementation of GeometryComponentFilter */
    public void filter( Geometry g )
    {
      if (g instanceof LineString)
      {
        LineString l = (LineString)g;
        if (lineStringFactory != null && l instanceof LinearRing) l = lineStringFactory.createLineString( l.getCoordinates() );
        add( l );
      }
    }
  };
  
  /** Sole constructor. The GeometryFactory is for creating LineStrings from LinearRings. */
  public PolygonSplitter( GeometryFactory gf )
  {
    this.geomFact = gf;
  }
  /** Split Polygon by a set of LineString and/or LinearRing Geometries */
  public List<Polygon> split( Polygon polyGeom, Geometry ... lineWork )
  {
    return split( polyGeom, lineWork );
  }
  /** Split MultiPolygon by a set of LineString and/or LinearRing Geometries */
  public List<Polygon> split( MultiPolygon polyGeom, Geometry ... lineWork )
  {
    return split( polyGeom, lineWork );
  }
  /** Split Polygon/MultiPolygon by a set of LineString and/or LinearRing Geometries */
  public List<Polygon> split( Geometry polyGeom, Geometry ... lineWork )
  {
    List<Polygon> resultPolyList = new ArrayList<Polygon>();
    if (!(polyGeom instanceof Polygon || polyGeom instanceof MultiPolygon)) return resultPolyList;
    
    // Extract and make LineStrings of all LinearRings in input Polygon/MultiPolygon
    LineStringExtractor lineExtractor = new LineStringExtractor( geomFact );
    polyGeom.apply( lineExtractor );
    if (lineExtractor.size() == 0) return resultPolyList;
    
    // Extract and make LineString of LinearRings and LineStrings in the input "lineWork"
    for (Geometry g : lineWork) g.apply( lineExtractor );
    
    // Perform union of all extracted LineStrings (the edge-noding process)
    UnaryUnionOp uOp = new UnaryUnionOp( lineExtractor );
    Geometry union = uOp.union();

    // Create polygons from unioned LineStrings
    Polygonizer polygonizer = new Polygonizer();
    polygonizer.add( union );
    
    @SuppressWarnings("unchecked")
    Collection<Polygon> polys = polygonizer.getPolygons();
    
    // Extract polygonized polygons that share an interior point with the
    // input Polygon/MultiPolygon, thus eliminating Polygons that resembles holes
    // in the original input Polygon/MultiPolygon
    for (Polygon p : polys)
    {
      if (polyGeom.contains( p.getInteriorPoint() ))
      {
        resultPolyList.add(p);
      }
    }
    // Return list of split-polygon
    return resultPolyList;
  }
}

