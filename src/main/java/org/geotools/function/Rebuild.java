package org.geotools.function;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class Rebuild {
	public static Geometry rebuild(Geometry polygon, double minx, double miny, double maxx, double maxy) {
		GeometryFactory geomFactory = new GeometryFactory();
		Coordinate[][] corners = new Coordinate[2][2];
		
		corners[0][0] = new Coordinate(minx, miny);
		corners[0][1] = new Coordinate(minx, maxy);
		corners[1][0] = new Coordinate(maxx, miny);
		corners[1][1] = new Coordinate(maxx, maxy);

		Polygon square = geomFactory.createPolygon(
				geomFactory.createLinearRing(
						new Coordinate[] { corners[0][0], corners[1][0], corners[1][1], corners[0][1], corners[0][0] }),
				null);
//		Geometry new_polygon = polygon.intersection(square);
		Geometry new_polygon = square.intersection(polygon);
	//	System.out.println(polygon.intersection(square));
		return new_polygon;
		
	}
}
