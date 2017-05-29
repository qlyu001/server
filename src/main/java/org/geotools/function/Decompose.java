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
public class Decompose {
	 public static Vector<Geometry> get_decompose_array(String countryname,String path,String placeKey) throws IOException, CQLException {

		 Geometry geom =Getgeom.getgeom(countryname, path, placeKey);
			Vector<Geometry> allpolygons = new Vector<Geometry>();
			
			if(geom.getNumGeometries()==1){
			//	geom=Rebuild.rebuild(geom,rasterCoordinate[0]*1.001,rasterCoordinate[1]*1.001,rasterCoordinate[2]*0.999,rasterCoordinate[3]*0.999);
				//geom=Rebuild.rebuild(geom,rasterCoordinate[0],rasterCoordinate[1],rasterCoordinate[2],rasterCoordinate[3]);
				allpolygons.add(geom);
			}
			else{
				
				for (int i=0;i<geom.getNumGeometries();i++){
				Geometry polygon=geom.getGeometryN(i);
			
				// polygon = Rebuild.rebuild(polygon,rasterCoordinate[0],rasterCoordinate[1],rasterCoordinate[2],rasterCoordinate[3]);
				allpolygons.add(polygon);
				}
				}
		return allpolygons;
		}


	
}
