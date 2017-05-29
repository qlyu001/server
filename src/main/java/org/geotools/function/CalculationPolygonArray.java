package org.geotools.function;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.geotools.filter.text.cql2.CQLException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

//Add Pixels in each polygon
public class CalculationPolygonArray {
	public static Vector<Coordinate> calculation_of_polygon_array(Raster raster, Vector<Geometry> allpolygons)
			throws IOException, CQLException {
		
		Vector<Coordinate> points = new Vector<Coordinate> ();
		int total = 0;
		for (int j = 0; j < allpolygons.size(); j++) {
		//	 System.out.println("Now is at polygon " + j);
		
			Envelope env = GetEnvolope.getenvelope(allpolygons.elementAt(j));
		
		//	 System.out.println("a " );
//			int x_left = Rounding.getfloor(RVConversion.vectortoraster_x(env.getMinX()));
//			int y_up = Rounding.getfloor(RVConversion.vectortoraster_y(env.getMaxY()));
//			int y_down = Rounding.getcelling(RVConversion.vectortoraster_y(env.getMinY()));
//			int x_right = Rounding.getcelling(RVConversion.vectortoraster_x(env.getMaxX()));
//			total += (Math.abs(y_up - y_down))*(Math.abs(x_left-x_right));
			
			//For each polygon, project its MBR to raster data. Iterate pixels in the range. 
			//By using a point in vector data to represent one pixel in raster data, we actually iterate some discrete points in vector data.
			//If point located in polygon, save its corresponding location in raster data in vector_of_points.
			//Use get_rgb to read information in each pixel.
			//Get average of specific polygon.
			//Get average of all polygons.
//			System.out.println(env);
//			System.out.println(allpolygons.elementAt(j).getEnvelope());
//			System.out.println(allpolygons.elementAt(j));
//			if (allpolygons.elementAt(j).isSimple()){
//				System.out.println(allpolygons.elementAt(j));
//			} else {
//				System.out.println("Not simple");
//			}
//			System.out.println(allpolygons.elementAt(j).getEnvelope().getArea());
//			 
//			System.out.println(allpolygons.elementAt(j).getArea());
//			Boolean isrectangle = allpolygons.elementAt(j).getEnvelope().equals(allpolygons.elementAt(j));
//			Boolean isrectangle = (allpolygons.elementAt(j).getArea() == allpolygons.elementAt(j).getEnvelope().getArea())? true : false;
//			System.out.println(isrectangle);
//			 System.out.println("Now is at polygon " + j);
		//	System.out.println(GetPoints.get_vector_of_points(GetRasterSize.rastersize, allpolygons.elementAt(j), env));
			points.addAll( GetPoints.get_vector_of_points(GetRasterSize.rastersize, allpolygons.elementAt(j), env) );
			//System.out.println("a " );
		}
//		Set<Coordinate> set = new HashSet<Coordinate>();
//		set.addAll(points);
//		points.clear();
//		points.addAll(set);
	//	System.out.println("Total checked points is" + total);
		return points;


	}
	
}
