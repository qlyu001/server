package org.geotools.function;
import java.io.IOException;
import java.util.Stack;
import java.util.Vector;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.vectorProjection.VectorProjection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;


//All about QuadraticSplit method
public class QuadraticSplit {
	public static Vector<Geometry> get_cross_split_array(String countryname,String path,String placeKey) throws IOException, CQLException {
		Vector<Geometry> allpolygons = new Vector<Geometry>();
		System.out.println("t");
		Vector<Geometry> polygons = Decompose.get_decompose_array(countryname ,path,placeKey);

		long startTime = System.currentTimeMillis();
		for (int i = 0; i < polygons.size(); i++) {
			//System.out.println("polygons.size()"+polygons.size());
			Vector<Geometry> aftersplit = cross_recursive(polygons.elementAt(i));
			//System.out.println("t"+aftersplit.);
			allpolygons.addAll(aftersplit);
		}

		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);
		// System.out.println(duration);
		System.out.println("Totally splitted to "+ allpolygons.size()+" polygons.");
		return allpolygons;
	}



	public static Geometry[] cross_split_process(Geometry geom) {

		GeometryFactory geomFactory = new GeometryFactory();
		Geometry[] parts = new Geometry[4];
		Geometry envelope = geom.getEnvelope();
		Coordinate[] coords = envelope.getCoordinates();
		Coordinate[][] corners = new Coordinate[3][3];
		double x1 = Math.min(coords[0].x, coords[2].x);
		double x2 = Math.max(coords[0].x, coords[2].x);
		double y1 = Math.min(coords[0].y, coords[2].y);
		double y2 = Math.max(coords[0].y, coords[2].y);
		double cx = (x1 + x2) / 2;
		double cy = (y1 + y2) / 2;
		corners[0][0] = new Coordinate(x1, y1);
		corners[0][1] = new Coordinate(x1, cy);
		corners[0][2] = new Coordinate(x1, y2);
		corners[1][0] = new Coordinate(cx, y1);
		corners[1][1] = new Coordinate(cx, cy);
		corners[1][2] = new Coordinate(cx, y2);
		corners[2][0] = new Coordinate(x2, y1);
		corners[2][1] = new Coordinate(x2, cy);
		corners[2][2] = new Coordinate(x2, y2);

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
		Polygon q2 = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[1][0], corners[2][0], corners[2][1], corners[1][1], corners[1][0] }),
				null);
		parts[2] = q2.intersection(geom);
		Polygon q3 = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[1][1], corners[2][1], corners[2][2], corners[1][2], corners[1][1] }),
				null);
		parts[3] = q3.intersection(geom);

		return parts;
	}

	public static Vector<Geometry> cross_recursive(Geometry geom) throws IOException {

		final int DefaultThreshold =16;
		// int threshold = b.size() == 11? DefaultThreshold : (Integer)b.get(1);
		// if (threshold < 4)
		// throw new Exception("Size threshold must be at least 4");
		Vector<Geometry> output = new Vector<Geometry>();
		Stack<Geometry> toSplit = new Stack<Geometry>();

		
		//project the polygon by the crs code
	
		toSplit.push(geom);
		while (!toSplit.isEmpty()) {
			Geometry geomtoSplit = toSplit.pop();
			// System.out.println("Points is " + geomtoSplit.getNumPoints());
			if (geomtoSplit.getNumPoints() <= DefaultThreshold) {
				output.add(geomtoSplit);
			} else {
				// Large geometry. Split into four
				Geometry[] parts = cross_split_process(geomtoSplit);
				for (Geometry part : parts){
					if (part.getNumGeometries() > 1){
						for (int i = 0 ; i < part.getNumGeometries(); i++ ){
							toSplit.push(part.getGeometryN(i));
						}
					} else 
					{
					if (!part.isEmpty())
						toSplit.push(part);
					}
				}
			}
		}
		// for (Geometry i : output){
		// System.out.println("Points is " + i.getNumPoints());
		// }
		return output;
	}
	


}
