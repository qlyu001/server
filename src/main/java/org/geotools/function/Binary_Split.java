package org.geotools.function;

import java.awt.image.Raster;

import java.io.IOException;
import java.util.Stack;
import java.util.Vector;
import javax.imageio.ImageIO;

import org.geotools.filter.text.cql2.CQLException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

//All about Binary_Split
public class Binary_Split {

	public static Vector<Geometry>  binary_split(String countryname,String vectorPath,String placeKey) throws CQLException, IOException {
	
		Vector<Geometry> allpolygons = get_binary_split_array(countryname,vectorPath,placeKey);
		// System.out.println(getrastersize(raster)[0]);
		// System.out.println("Total number of polygons of "+ countryname+ "
		// after spit is "+allpolygons.size());
	//	CalculationPolygonArray.calculation_of_polygon_array(raster, allpolygons,rasterPath);

	//	System.out.println("Processing time is " + duration + " miliseconds.");
		return allpolygons;

	}

	public static Vector<Geometry> get_binary_split_array(String countryname,String vectorPath,String placeKey) throws IOException, CQLException {
		Vector<Geometry> allpolygons = new Vector<Geometry>();

		// Decompose multipolygon
		Vector<Geometry> polygons = Decompose.get_decompose_array(countryname,vectorPath,placeKey);
	//	System.out.println(polygons.size());
		// System.out.println("Country " + countryname + " contains " +
		// polygons.length + " polygons.");
	
		// Recursively split each polygon
		for (int i = 0; i < polygons.size(); i++) {
			Vector<Geometry> aftersplit = binary_recursive(polygons.elementAt(i));
			allpolygons.addAll(aftersplit);
		}
		// System.out.print(duration + " ");
		// System.out.println("Binary_Split processing time of " + countryname +
		// " is " + duration + " miliseconds");
		return allpolygons;
	}

	private static Vector<Geometry> binary_recursive(Geometry geom) throws IOException {

		final int DefaultThreshold = 16;
		Vector<Geometry> output = new Vector<Geometry>();
		Stack<Geometry> toSplit = new Stack<Geometry>();
		toSplit.push(geom);
		while (!toSplit.isEmpty()) {

			Geometry geomtoSplit = toSplit.pop();
			// System.out.println("Points is " + geomtoSplit.getNumPoints());
			if (geomtoSplit.getNumPoints() <= DefaultThreshold) {
				output.add(geomtoSplit);
			} else {
				// split by median of x coordinates, then split by median of y
				Geometry[] splitbyx = binary_split_x(geomtoSplit);
				for (int i = 0; i < splitbyx.length; i++) {
					Geometry[] splitbyy = binary_split_y(splitbyx[i]);
					for (int j = 0; j < splitbyy.length; j++) {
						if (!splitbyy[j].isEmpty())
							toSplit.push(splitbyy[j]);
					}
				}

			}
		}
		return output;
	}

	// Split method. Return two children cut from up to down

	public static Geometry[] binary_split_x(Geometry geom) {

		GeometryFactory geomFactory = new GeometryFactory();

		Geometry[] parts = new Geometry[2];
		// Store all Coordinates
		Coordinate[] allpoints = new Coordinate[geom.getNumPoints()];
		allpoints = geom.getCoordinates();
		double[] allx = new double[geom.getNumPoints()];
		int index = 0;
		// Get all x Coordinates
		for (Coordinate i : allpoints) {
			allx[index++] = i.x;
		}

		// for (int i = 0; i < allx.length; i++){
		// System.out.println(allx[i]);
		// }

		double median;
		// Can use median or average
		median = GetAverage.averageofmedian(allx);
		// median = averageofall(allx);
		// System.out.println(median);
		Geometry envelope = geom.getEnvelope();
		Coordinate[] coords = envelope.getCoordinates();
		Coordinate[][] corners = new Coordinate[2][3];
		double x1 = Math.min(coords[0].x, coords[2].x);
		double x2 = Math.max(coords[0].x, coords[2].x);
		double y1 = Math.min(coords[0].y, coords[2].y);
		double y2 = Math.max(coords[0].y, coords[2].y);
		// System.out.println(x1+" " +x2+" "+y1 +" " +y2);

		corners[0][0] = new Coordinate(x1, y1);
		corners[0][1] = new Coordinate(median, y1);
		corners[0][2] = new Coordinate(x2, y1);
		corners[1][0] = new Coordinate(x1, y2);
		corners[1][1] = new Coordinate(median, y2);
		corners[1][2] = new Coordinate(x2, y2);

		// Create MBR of spited polygon
		Polygon q0 = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[0][0], corners[1][0], corners[1][1], corners[0][1], corners[0][0] }),
				null);
		// Intersection of MBR and original polygon.
		parts[0] = q0.intersection(geom);
		Polygon q1 = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[0][1], corners[1][1], corners[1][2], corners[0][2], corners[0][1] }),
				null);
		parts[1] = q1.intersection(geom);

		return parts;
	}

	// Split method. Return two children cut from left to right
	// Difference between split_x is how we build the MBR of children

	public static Geometry[] binary_split_y(Geometry geom) {

		
		 GeometryFactory geomFactory = new GeometryFactory();
		Geometry[] parts = new Geometry[2];
		Coordinate[] allpoints = new Coordinate[geom.getNumPoints()];
		allpoints = geom.getCoordinates();
		double[] ally = new double[geom.getNumPoints()];
		int index = 0;
		for (Coordinate i : allpoints) {
			ally[index++] = i.y;
		}

		double median;
		median = GetAverage.averageofmedian(ally);
		// median = averageofall(ally);

		Geometry envelope = geom.getEnvelope();
		Coordinate[] coords = envelope.getCoordinates();
		Coordinate[][] corners = new Coordinate[2][3];
		double x1 = Math.min(coords[0].x, coords[2].x);
		double x2 = Math.max(coords[0].x, coords[2].x);
		double y1 = Math.min(coords[0].y, coords[2].y);
		double y2 = Math.max(coords[0].y, coords[2].y);

		corners[0][0] = new Coordinate(x1, y1);
		corners[0][1] = new Coordinate(x1, median);
		corners[0][2] = new Coordinate(x1, y2);
		corners[1][0] = new Coordinate(x2, y1);
		corners[1][1] = new Coordinate(x2, median);
		corners[1][2] = new Coordinate(x2, y2);

		Polygon q0 = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[0][0], corners[1][0], corners[1][1], corners[0][1], corners[0][0] }),
				null);
		parts[0] = q0.intersection(geom);
		Polygon q1 = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[0][1], corners[1][1], corners[1][2], corners[0][2], corners[0][1] }),
				null);
		parts[1] = q1.intersection(geom);

		return parts;
	}

}
