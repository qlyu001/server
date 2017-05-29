package org.geotools.test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class exchangeTest {
	
		public static void main(String[]args) {
			GeometryFactory geomFactory = new GeometryFactory();
			Polygon polygon = geomFactory.createPolygon(
					geomFactory.createLinearRing(
							new Coordinate[] { new Coordinate(1, 3),  new Coordinate(2, 1),  new Coordinate(3, 2),  new Coordinate(3, 4), new Coordinate(1, 3) }),
					null);
			Coordinate[] coords = polygon.getCoordinates();
			System.out.println(polygon.getEnvelope());
			Polygon polygon2 = geomFactory.createPolygon(
					geomFactory.createLinearRing(
							new Coordinate[] { new Coordinate(coords[0].y, coords[0].x),  new Coordinate(coords[1].y, coords[1].x),  new Coordinate(coords[2].y, coords[2].x),  new Coordinate(coords[3].y, coords[3].x), new Coordinate(coords[0].y, coords[0].x) }),
					null);
			
			System.out.println(polygon2.getEnvelope());
			
			
			/*
			corners[0][0] = new Coordinate(minx, miny);
			corners[0][1] = new Coordinate(minx, maxy);
			corners[1][0] = new Coordinate(maxx, miny);
			corners[1][1] = new Coordinate(maxx, maxy);
			Polygon square = geomFactory.createPolygon(
					geomFactory.createLinearRing(
							new Coordinate[] { corners[0][0], corners[1][0], corners[1][1], corners[0][1], corners[0][0] }),
					null);
//			Geometry new_polygon = polygon.intersection(square);
			Geometry new_polygon = square.intersection(polygon);
			System.out.println(polygon.intersection(square));*/
			
	}
	
}
