package org.geotools.test;

import java.awt.image.Raster;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.function.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


//All about Scanline Method 
public class IntersectionMethodTest {
	
	public static void intersect_method(Raster raster,String countryname,String vectorPath,String placeKey) throws CQLException, IOException{
		//Change raster file
		Geometry geom =  Getgeom.getgeom(countryname,vectorPath,placeKey);
		long startTime1 = System.currentTimeMillis();
		Vector<Geometry> polygons = Decompose.get_decompose_array(geom,countryname,vectorPath,placeKey);
		long endTime1 = System.currentTimeMillis();
		System.out.println("Total number of polygons of "+ countryname+ " after decomposing is "+polygons.size());
		long duration1 =  endTime1 - startTime1;

		
		long startTime2 = System.currentTimeMillis();
		Vector<Coordinate> pionts_inside_geom = getpoints(polygons);
		long endTime2 = System.currentTimeMillis();

		
		long duration2 =  endTime2 - startTime2;
		
		System.out.println("Total number of Pixels is "+pionts_inside_geom.size());
		System.out.println("Intersection method number of pixels equals " + pionts_inside_geom.size());
		
		
		
		long startTime3 = System.currentTimeMillis();
		double result = AggregateOfPoints.aggregate(raster, pionts_inside_geom);
		long endTime3 = System.currentTimeMillis();
		
		long duration3 =  endTime3 - startTime3;
		System.out.println("GetPolygon time of Scanline method is " + duration1 + " ms");
		System.out.println("Get Pixels time of Scanline method is " + duration2 + " ms");
		System.out.println("Aggregate  time of Scanline method is " + duration3 + " ms");
	}

	public static Vector<Coordinate> getpoints(Vector<Geometry> polygons) {
		
		Vector<Coordinate> pionts_inside_geom = new Vector<Coordinate>();
		long duration1 = 0,duration2 = 0,duration3  = 0, duration4 = 0; 
		for (int index = 0; index < polygons.size(); index++){
			Envelope env = GetEnvolope.getenvelope(polygons.elementAt(index));
			Vector<Coordinate> pionts_inside_polygon = new Vector<Coordinate>();
			double minx_vector = env.getMinX();
			double maxx_vector = env.getMaxX();
			int y_up = Rounding.getcelling(RVConversion2.vectortoraster_y(env.getMaxY()));
			int y_down = Rounding.getfloor(RVConversion2.vectortoraster_y(env.getMinY()));
			
			//System.out.println(y_up+"---"+y_down);
						
			
//			for (int j = y_up; j < y_down; j++){
//				System.out.println(s.get(j));
//			}
			
//			System.out.println(y_up +" " + y_down);

			
			long startTime1 = System.currentTimeMillis();
			Vector<LineString> boundarys = get_boundary_segments(polygons.elementAt(index));
			long endTime1 = System.currentTimeMillis();
			
			long startTime2 = System.currentTimeMillis();
			Vector<Vector<LineString>> s = get_eachline_segments(y_up, y_down, boundarys);
			long endTime2 = System.currentTimeMillis();
		
			
			for (int j = y_down; j < y_up; j++){
//				System.out.println("Now is at line " + j);
				long startTime3 = System.currentTimeMillis();
				double [] all_x = get_cross_points(minx_vector, maxx_vector, s.get(j), j);
				long endTime3  = System.currentTimeMillis();
				
				long startTime4 = System.currentTimeMillis();
				Vector<Coordinate> inside_points = get_inside_points(j, minx_vector, maxx_vector, s.get(j), all_x);
				long endTime4 = System.currentTimeMillis();

//				System.out.println("Points inside polygon at line " + j + " is " + inside_points.size());
				
//				Vector<Coordinate> inside_points_vector = new Vector<Coordinate>();
//				for (int i = 0; i <  inside_points.size(); i++){
//					Coordinate point_in_vector = new  Coordinate();
//					point_in_vector.x = RVConversionAdaptToAll.rastertovector_x(inside_points.elementAt(i).x);
//					point_in_vector.y = RVConversionAdaptToAll.rastertovector_y(inside_points.elementAt(i).y);
//					inside_points_vector.add(point_in_vector);
//					System.out.print(point_in_vector);
//				}
				duration3 +=  (endTime3 - startTime3);
				duration4 +=  (endTime4 - startTime4);
				pionts_inside_polygon.addAll(inside_points);	
			}
			
			
			duration1 +=  (endTime1 - startTime1);
			duration2 +=  (endTime2 - startTime2);
			
			

			pionts_inside_geom.addAll(pionts_inside_polygon) ;
		}
		
		long total = duration1 + duration2 + duration3 + duration4;
//		System.out.println("Total time is " + total);
//		System.out.println("GetBoundary time of Scanline method is " + (double) duration1/total );
//		System.out.println("Get line segment of Scanline method is " + (double) duration2/total );
//		System.out.println("Get crosspoint   of Scanline method is " + (double) duration3/total );
//		System.out.println("Get allpoints    of Scanline method is " + (double) duration4/total );
		
		return pionts_inside_geom;
	
		
	}
	
	public static Vector<LineString> get_boundary_segments (Geometry polygon){
		Coordinate[] coords = polygon.getCoordinates();
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();

//		MultiLineString boundary =  (MultiLineString) polygon1.getBoundary();
		Vector<LineString> segments = new Vector<LineString>();

		for (int i = 0; i < coords.length - 1; i++){
			Coordinate[] terminal  = new Coordinate[] {coords[i], coords[i+1]};
		//	System.out.println(coords[i]+"/"+coords[i+1]);
			LineString line = geometryFactory.createLineString(terminal);
			segments.add(line);
		}
//		System.out.println("The polygon's number of boundary segments is " + segments.size());
		return segments;
	}
	
	public static Vector<Vector<LineString>> get_eachline_segments (int y_up,  int y_down, Vector<LineString> boundarys){
		Vector<Vector<LineString>> s = new Vector<Vector<LineString>>(y_up+1);
		for (int j = 0; j <= y_up+1; j++){
			s.add(j, new Vector<LineString>());
			
		}
		
		for (int i = 0; i < boundarys.size(); i++){
			LineString line = boundarys.elementAt(i);
			double ymax_vector = Math.max (line.getPointN(0).getY(), line.getPointN(1).getY());
			double ymin_vector = Math.min (line.getPointN(0).getY(), line.getPointN(1).getY());
			int ymax_raster = Rounding.getcelling(RVConversion2.vectortoraster_y(ymax_vector));
			int ymin_raster = Rounding.getfloor(RVConversion2.vectortoraster_y(ymin_vector));
			
//			System.out.println(y_down+" "+ ymin_raster + " " + ymax_raster);
			for (int j = ymin_raster; j <= ymax_raster; j++){	
				System.out.println(ymin_raster+"/"+ymax_raster);
				s.get(j).add(boundarys.elementAt(i));
				System.out.println(s.get(j));
			}
		}
		
		return s;
		
	}
	
	private static Vector<Coordinate> get_inside_points(int lineindex, double Minx, double Maxx, Vector<LineString> intersected_line, double [] all_x){		
//		int x_left = Rounding.getfloor(RVConversionAdaptToAll.vectortoraster_x(Minx));
//		int x_right = Rounding.getcelling(RVConversionAdaptToAll.vectortoraster_x(Maxx));
//		System.out.println(x_left + " " + x_right);
		Vector<Coordinate> points_inside = new Vector<Coordinate>();

		
//		System.out.println(" ");
		for (int i = 0; i < all_x.length - 1 ; i = i + 2){
			for (int j = Rounding.getcelling(all_x[i]); j <=  Rounding.getfloor(all_x[i+1]); j++){
				points_inside.add((new Coordinate(j,lineindex)));
			}
		}

		return points_inside;	
	}

	public static double[]   get_cross_points(double Minx, double Maxx, Vector<LineString> intersected_line,  int lineindex) {
		Vector<Coordinate> cross_points=  new Vector<Coordinate>();
		double y_in_vector = RVConversion2.rastertovector_y(lineindex);
		GeometryFactory geometryFactory = new GeometryFactory();
		LineString line = geometryFactory.createLineString(new Coordinate[] {new Coordinate(Minx, y_in_vector), new Coordinate(Maxx, y_in_vector)});

		for (int i = 0; i < intersected_line.size(); i++){
			Geometry intersection = intersected_line.elementAt(i).intersection(line);
			Coordinate[] coords = intersection.getCoordinates();
			if (coords.length == 1){
				Coordinate point_in_raster= new Coordinate(RVConversion2.vectortoraster_x(coords[0].x), RVConversion2.vectortoraster_y(coords[0].y));
//				System.out.println(point_in_raster);
				cross_points.add(point_in_raster);
				
			} else {
//				System.out.println(coords.length + " intersection between two lines");
			}
			
		}
		
		double [] all_x = new double [cross_points.size()];
//		double [] all_x_even = new double [cross_points.size()];
//		double [] all_x_odd = new double [cross_points.size()];
		for (int i = 0; i < all_x.length; i++){
			all_x[i] = cross_points.elementAt(i).x;
//			System.out.print(all_x[i]+ " ");
		}
		
		Arrays.sort(all_x);
		return all_x;
	}
}
