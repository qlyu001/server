package org.geotools.function;
import java.io.IOException;
import java.util.Vector;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.vectorProjection.VectorProjection;
import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

//Decompose multipolygon to polygon array
public class Decompose2 {
	 public static Vector<Geometry> get_decompose_array(String countryname,String path,String rasterPath,String placeKey) throws IOException, CQLException {

		GetCRSCodeAndOringinCoord getCO=new GetCRSCodeAndOringinCoord();
		double[] rasterCoordinate=new double[5];
		rasterCoordinate=getCO.getCRSCodeAndOringinCoord(rasterPath);
		 
		Geometry geom =  Getgeom.getgeom(countryname,path,placeKey);
		
		int crscode=(int)rasterCoordinate[4];
		//System.out.println(crscode);
		
		VectorProjection vectorProjection=new VectorProjection();
		String before=geom.getEnvelope().toString();
		System.out.println("before:"+geom.getEnvelope());
		geom=vectorProjection.vectorProjection(geom, path,crscode);
		String after=geom.getEnvelope().toString();
		System.out.println("after1:"+geom.getEnvelope());
	
		if(crscode==4326){
		ExchangeXY exchangeXY=new ExchangeXY();
		geom=exchangeXY.exchangexy(geom);
		}
		Vector<Geometry> allpolygons = new Vector<Geometry>();
//		System.out.println("geom.getNumGeometries() "+geom.getNumGeometries());
		System.out.println("geom bounding is " + geom.getEnvelope());
		
//		System.out.println(rasterCoordinate[1]);
		Geometry intersect = Rebuild.rebuild(geom,rasterCoordinate[0]*1.001,rasterCoordinate[1]*1.001,rasterCoordinate[2]*0.999,rasterCoordinate[3]*0.999);
//		System.out.println("intersect.getNumGeometries() "+intersect.getNumGeometries());
//		System.out.println("Intersect bounding is " + intersect.getEnvelope());
		if(intersect.getNumGeometries()==1){
//			System.out.println("Upper Case");		
			allpolygons.add(intersect);
		}
		else{
			System.out.println("Down Case");
		for (int i = 0; i < intersect.getNumGeometries(); i++) {
			Geometry polygon =  intersect.getGeometryN(i);
			System.out.println(i + " th bounding is " + polygon.getEnvelope());

		//	System.out.println(polygon.getEnvelope());

//			System.out.println(polygon.getEnvelope());
		//	System.out.println("after"+polygon.getEnvelope());
			allpolygons.add(polygon);
			}
		}
		return allpolygons;
	}
}
