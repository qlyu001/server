package org.geotools.function;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.Vector;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

// Get Coordinate array of pixels in one polygon.
public class GetPoints {

	public static Vector<Coordinate> get_vector_of_points(int[] rastersize, Geometry geom, Envelope env)
			throws IOException, CQLException {
		long startTime = System.currentTimeMillis();
		
		// All corresponding locations in raster data
		Vector<Coordinate> points_in_raster = new Vector<Coordinate>();
		/*GetCRSCodeAndOringinCoord getCRSCodeAndOringinCoord=new GetCRSCodeAndOringinCoord();
		double[] rastercoordinate=getCRSCodeAndOringinCoord.getCRSCodeAndOringinCoord(rasterPath);
		double proportionx=(rastercoordinate[2]-rastercoordinate[0])/raster.getWidth();
		double proportiony=(rastercoordinate[3]-rastercoordinate[1])/raster.getHeight();*/
		int x_left = Rounding.getfloor(RVConversion2.vectortoraster_x(env.getMinX()));
		int y_down = Rounding.getfloor(RVConversion2.vectortoraster_y(env.getMinY()));
		int x_right = Rounding.getcelling(RVConversion2.vectortoraster_x(env.getMaxX()));
		int y_up = Rounding.getcelling(RVConversion2.vectortoraster_y(env.getMaxY()));
	//	System.out.println("x1:"+x_left+"/"+x_right+"y1:"+y_down+"/"+y_up+"/"+env.getMaxX());
		
		/* 
		int x_left = Rounding.getfloor(RVConversion.vectortoraster_x(env.getMinX()));
		int y_down = Rounding.getcelling(RVConversion.vectortoraster_y(env.getMinY()));
		int x_right = Rounding.getcelling(RVConversion.vectortoraster_x(env.getMaxX()));
		int y_up = Rounding.getfloor(RVConversion.vectortoraster_y(env.getMaxY()));
*/
		// System.out.println("env.getMinX() " + env.getMinX() + " " +
		// env.getMinY() + " " + " " + env.getMaxX() + " " + env.getMaxY());
		// System.out.println("x_left " + x_left + " " + y_down + " " + " " +
		// x_right + " "+ y_up);
		// int numpoints = (x_right - x_left) * (y_down - y_up);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		if (geom.isRectangle()) {//attention up and down which one to use depends on raster file
			for (int j = y_down; j <= y_up; j++) {
				for (int i = x_left; i <= x_right; i++) {
					
					// Using a point in vector data to represent one pixel in
					// raster data
					// Create that point
					// System.out.println("point checked is " + point1);

					points_in_raster.addElement(new Coordinate(i, j));
				}
			}
		} else {
			for (int j = y_down; j <= y_up; j++) {
				
				int count = 0;
				for (int i = x_left; i <= x_right; i++) {
					
					double shpx = RVConversion2.rastertovector_x(i);
					double shpy = RVConversion2.rastertovector_y(j);
					
					// Create that point
					Coordinate coord1 = new Coordinate(shpx, shpy);
			
					Point point1 = geometryFactory.createPoint(coord1);

					// If point located in polygon
					if (geom.contains(point1)) {
						// Save its raster location
						points_in_raster.addElement(new Coordinate(i, j));
//						System.out.print(point1+ " ");
						count++;
					}
				}
//				System.out.println(" ");
//				if (j % 100 == 0 ) System.out.println("Points inside polygon at line " + j + " is " + count);
			}

		}
		// System.out.println("Pixels in this polygon is " +
		// points_in_raster.size());
		long endTime = System.currentTimeMillis();
	
		long duration = (endTime - startTime);
		// System.out.println(duration + " miliseconds");
		return points_in_raster;

	}

}
